package cz.cvut.kbss.ontodriver.impl.sesame;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;

import cz.cvut.kbss.jopa.exceptions.OWLEntityExistsException;
import cz.cvut.kbss.jopa.model.EntityDescriptor;
import cz.cvut.kbss.jopa.model.IRI;
import cz.cvut.kbss.jopa.model.RepositoryID;
import cz.cvut.kbss.jopa.model.annotations.FetchType;
import cz.cvut.kbss.jopa.model.metamodel.Attribute;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.PropertiesSpecification;
import cz.cvut.kbss.jopa.model.metamodel.TypesSpecification;
import cz.cvut.kbss.ontodriver.ResultSet;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;
import cz.cvut.kbss.ontodriver.exceptions.OntoDriverInternalException;
import cz.cvut.kbss.ontodriver.exceptions.PrimaryKeyNotSetException;
import cz.cvut.kbss.ontodriver.impl.ModuleInternal;
import cz.cvut.kbss.ontodriver.impl.owlapi.OwlModuleException;
import cz.cvut.kbss.ontodriver.impl.utils.ICValidationUtils;

/**
 * This class uses assertions for checking arguments of public methods. This is
 * because the class itself is package private and the arguments are expected to
 * be already verified by the caller.
 * 
 * @author ledvima1
 * 
 */
class SesameModuleInternal implements ModuleInternal<SesameChange, SesameStatement> {

	static final Logger LOG = Logger.getLogger(SesameModuleInternal.class.getName());

	final SesameStorageModule module;
	private StorageProxy storage;
	private PropertiesHandler propertiesHandler;
	private TypesHandler typesHandler;
	// The ValueFactory can be final since it is singleton anyway
	private final ValueFactory valueFactory;
	private final String lang;
	private List<SesameChange> changes = new LinkedList<>();
	private Set<URI> temporaryIndividuals = new HashSet<>();

	static enum ObjectType {
		LITERAL, OBJECT
	};

	SesameModuleInternal(SesameOntologyDataHolder data, SesameStorageModule storageModule) {
		assert data != null : "argument data is null";
		assert storageModule != null : "argument storageModule is null";
		this.module = storageModule;
		this.storage = data.getStorage();
		this.valueFactory = data.getValueFactory();
		this.lang = data.getLanguage();
		this.propertiesHandler = new PropertiesHandler(this);
		this.typesHandler = new TypesHandler(this);
	}

	@Override
	public boolean containsEntity(Object primaryKey, RepositoryID contexts)
			throws OntoDriverException {
		assert primaryKey != null : "argument primaryKey is null";
		final URI uri = getAddressAsSesameUri(primaryKey);
		return isInOntologySignature(uri, asSesameUris(contexts.getContexts()));
	}

	@Override
	public <T> T findEntity(Class<T> cls, Object primaryKey, EntityDescriptor descriptor)
			throws OntoDriverException {
		assert cls != null : "argument cls is null";
		assert primaryKey != null : "argument primaryKey is null";

		final URI uri = getAddressAsSesameUri(primaryKey);
		final URI ctx = getAddressAsSesameUri(descriptor.getEntityContext());
		if (!isInOntologySignature(uri, Collections.singleton(ctx))) {
			return null;
		}
		final T entity = loadEntity(cls, uri, descriptor);

		assert entity != null;
		return entity;
	}

	@Override
	public boolean isConsistent(RepositoryID contexts) throws OntoDriverException {
		assert contexts != null;
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> void persistEntity(Object primaryKey, T entity, EntityDescriptor descriptor)
			throws OntoDriverException {
		assert entity != null : "argument entity is null";

		final EntityType<T> entityType = getEntityType((Class<T>) entity.getClass());
		URI uri = primaryKey != null ? getAddressAsSesameUri(primaryKey) : null;
		if (uri == null) {
			uri = resolveIdentifier(entity, entityType);
		} else {
			module.incrementPrimaryKeyCounter();
		}
		final URI ctx = getAddressAsSesameUri(descriptor.getEntityContext());
		if (isInOntologySignature(uri, Collections.singleton(ctx))) {
			throw new OWLEntityExistsException("Entity with primary key " + uri
					+ " already exists in one of contexts " + descriptor);
		}
		addInstanceToOntology(uri, entityType, ctx);
		saveEntityAttributes(entity, uri, entityType, descriptor);

		temporaryIndividuals.remove(uri);
	}

	@Override
	public <T> void mergeEntity(T entity, Field mergedField, EntityDescriptor descriptor)
			throws OntoDriverException {
		assert entity != null : "argument entity is null";

		final URI uri = getIdentifier(entity);
		final URI entityContext = getAddressAsSesameUri(descriptor.getEntityContext());
		if (!isInOntologySignature(uri, Collections.singleton(entityContext))) {
			throw new OntoDriverException(new IllegalArgumentException("The entity " + entity
					+ " is not persistent within this context."));
		}
		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Saving value of field " + mergedField + " for entity with id = " + uri);
		}
		final EntityType<T> et = getEntityType((Class<T>) entity.getClass());
		if (!mergedField.isAccessible()) {
			mergedField.setAccessible(true);
		}
		final TypesSpecification<?, ?> ts = et.getTypes();
		final PropertiesSpecification<?, ?> ps = et.getProperties();
		try {
			// Get context URI
			final URI ctx = getAddressAsSesameUri(descriptor.getFieldContext(mergedField.getName()));

			if (ts != null && ts.getJavaField().equals(mergedField)) {
				typesHandler.save(entity, uri, ts, et, ctx, true);
			} else if (ps != null && ps.getJavaField().equals(mergedField)) {
				propertiesHandler.save(entity, uri, ps, et, ctx, true);
			} else {
				final Attribute<?, ?> att = et.getAttribute(mergedField.getName());
				final SubjectModels<T> m = new SubjectModels<T>(storage, uri, entity, valueFactory,
						descriptor);
				saveReference(att, et, true, m);
			}
		} catch (RuntimeException | IllegalAccessException e) {
			throw new OntoDriverInternalException(e);
		}
	}

	@Override
	public void removeEntity(Object primaryKey, EntityDescriptor descriptor)
			throws OntoDriverException {
		assert primaryKey != null : "argument primaryKey is null";

		final URI uri = getAddressAsSesameUri(primaryKey);
		removeEntityFromOntology(uri, descriptor);
	}

	@Override
	public <T> void loadFieldValue(T entity, Field field, EntityDescriptor descriptor)
			throws OntoDriverException {
		assert entity != null : "argument entity is null";
		assert field != null : "argument field is null";
		final Class<T> cls = (Class<T>) entity.getClass();
		final EntityType<T> et = getEntityType(cls);
		final URI primaryKey = getIdentifier(entity);
		final URI ctx = getAddressAsSesameUri(descriptor.getFieldContext(field.getName()));
		try {
			if (et.getTypes() != null && et.getTypes().getJavaField().equals(field)) {
				typesHandler.load(entity, primaryKey, et.getTypes(), et, ctx);
			} else if (et.getProperties() != null
					&& et.getProperties().getJavaField().equals(field)) {
				propertiesHandler.load(entity, primaryKey, et.getProperties(), et, ctx);
			} else {
				final SubjectModels<T> m = new SubjectModels<T>(storage, primaryKey, entity,
						valueFactory, descriptor);
				loadReference(et.getAttribute(field.getName()), m, true);
			}
		} catch (Exception e) {
			LOG.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public void rollback() {
		clear();
	}

	@Override
	public void reset() throws OntoDriverException {
		clear();
		final SesameOntologyDataHolder data = module.getOntologyData();
		this.storage = data.getStorage();
		this.propertiesHandler = new PropertiesHandler(this);
		this.typesHandler = new TypesHandler(this);
	}

	@Override
	public ResultSet executeStatement(SesameStatement statement) {
		statement.setStorage(storage);
		return statement.executeStatement();
	}

	@Override
	public List<SesameChange> commitAndRetrieveChanges() {
		if (!temporaryIndividuals.isEmpty()) {
			throw new IllegalStateException(
					"There are some uncommitted and unpersisted entities in the ontology.");
		}
		final List<SesameChange> toReturn = changes;
		clear();
		return toReturn;
	}

	private void clear() {
		this.changes = new LinkedList<>();
		this.temporaryIndividuals = new HashSet<>();
		if (storage.isOpen()) {
			try {
				storage.close();
			} catch (OntoDriverException e) {
				LOG.severe("Exception caught when closing Sesame storage proxy: " + e);
			}
		}
	}

	void addStatement(Statement stmt, URI context) {
		storage.addStatement(stmt, context);
		changes.add(new SesameAddChange(stmt, context));
	}

	void addStatements(Collection<Statement> stmts, URI context) {
		storage.addStatements(stmts, context);
		for (Statement stmt : stmts) {
			changes.add(new SesameAddChange(stmt, context));
		}
	}

	void removeStatements(Collection<Statement> stmts, URI context) {
		// First create the changes because the statements may be backed by the
		// model in which case they are removed from the collection as well
		for (Statement stmt : stmts) {
			changes.add(new SesameRemoveChange(stmt, context));
		}
		storage.removeStatements(stmts, context);
	}

	void addIndividualsForReferencedEntities(Collection<?> ents, URI context)
			throws OntoDriverException {
		assert ents != null;
		assert !ents.isEmpty();

		for (Object ob : ents) {
			final EntityType<?> et = getEntityType(ob.getClass());
			URI uri = getIdentifier(ob);
			if (uri == null) {
				uri = resolveIdentifier(ob, et);
			}
			if (!isInOntologySignature(uri, Collections.singleton(context))
					&& !temporaryIndividuals.contains(uri)) {
				if (LOG.isLoggable(Level.FINEST)) {
					LOG.finest("Adding class assertion axiom for a not yet persisted entity " + uri);
				}
				addInstanceToOntology(uri, et, context);
				temporaryIndividuals.add(uri);
			}
		}
	}

	void removeTemporaryIndividual(URI individual) {
		assert individual != null;
		temporaryIndividuals.remove(individual);
	}

	private void addInstanceToOntology(URI primaryKey, EntityType<?> et, URI context) {
		assert primaryKey != null;
		assert et != null;
		assert context != null;

		final URI typeUri = valueFactory.createURI(et.getIRI().toString());
		final Statement stmt = valueFactory.createStatement(primaryKey, RDF.TYPE, typeUri);
		addStatement(stmt, context);
	}

	/**
	 * Generates new primary key. </p>
	 * 
	 * The primary key consists of the URI of this ontology context, name of the
	 * RDF class and an integer, which represents counter of existing
	 * statements.
	 * 
	 * @param typeName
	 * @return
	 */
	URI generatePrimaryKey(String typeName) {
		assert typeName != null;

		URI uri = null;
		int i;
		final String base = module.getRepository().getPhysicalUri().toString() + "#" + typeName
				+ "_";
		do {
			i = module.getNewPrimaryKey();
			uri = valueFactory.createURI(base + i);
		} while (isInOntologySignature(uri, Collections.<URI> emptySet()));
		return uri;
	}

	/**
	 * Returns the specified address as Sesame's URI.
	 * 
	 * Currently supported address types are : java.net.URI, java.net.URL and
	 * cz.cvut.kbss.jopa.model.IRI
	 * 
	 * @param primaryKey
	 *            Entity primary key
	 * @return Sesame URI
	 */
	URI getAddressAsSesameUri(Object primaryKey) {
		if (primaryKey == null) {
			return null;
		}
		if (primaryKey instanceof java.net.URI || primaryKey instanceof IRI
				|| primaryKey instanceof org.semanticweb.owlapi.model.IRI
				|| primaryKey instanceof URL) {
			return valueFactory.createURI(primaryKey.toString());
		} else {
			throw new IllegalArgumentException("Unsupported type of primary key "
					+ primaryKey.getClass());
		}
	}

	/**
	 * Gets entity type for the specified class.
	 * 
	 * @param cls
	 *            entity class
	 * @return Entity type
	 */
	<T> EntityType<T> getEntityType(Class<T> cls) {
		assert cls != null;
		return module.getMetamodel().entity(cls);
	}

	URI getIdentifier(Object entity) {
		assert entity != null;

		final EntityType<?> type = getEntityType(entity.getClass());
		try {
			Object idValue = type.getIdentifier().getJavaField().get(entity);
			if (idValue == null) {
				return null;
			}
			if (idValue instanceof URI) {
				return (URI) idValue;
			} else if (idValue instanceof String) {
				return valueFactory.createURI((String) idValue);
			} else if (idValue instanceof java.net.URI) {
				return valueFactory.createURI(((java.net.URI) idValue).toString());
			} else {
				throw new OwlModuleException("Unknown identifier type: " + idValue.getClass());
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new OntoDriverInternalException(e);
		}
	}

	String getLang() {
		return lang;
	}

	StorageProxy getStorage() {
		return storage;
	}

	ValueFactory getValueFactory() {
		return valueFactory;
	}

	/**
	 * Returns true if the specified URI is a subject or object in the current
	 * ontology signature.
	 * 
	 * @param uri
	 * @return
	 */
	private boolean isInOntologySignature(URI uri, Set<URI> contexts) {
		assert uri != null : "argument uri is null";
		final boolean inModel = storage.contains(uri, contexts);
		return (inModel && !temporaryIndividuals.contains(uri));
	}

	/**
	 * Loads an instance of the specified class with the specified primary key.
	 * 
	 * @param cls
	 *            Entity class
	 * @param uri
	 *            Primary key
	 * @return The loaded entity
	 * @throws OntoDriverException
	 *             If an error occurs during load
	 */
	<T> T loadEntity(Class<T> cls, URI uri, EntityDescriptor descriptor) throws OntoDriverException {
		assert cls != null;
		assert uri != null;

		if (LOG.isLoggable(Level.FINEST)) {
			LOG.finest("Loading entity of with pk " + uri);
		}
		final EntityType<T> type = getEntityType(cls);
		if (type == null) {
			throw new IllegalArgumentException("Class " + cls + " is not a registered entity type.");
		}

		T instance = null;
		try {
			instance = cls.newInstance();
		} catch (IllegalAccessException | InstantiationException e) {
			throw new OntoDriverException("Unable to instantiate class " + cls, e);
		}
		SesameUtils.setEntityIdentifier(type, instance, uri);
		loadEntityFromModel(instance, uri, type, descriptor);
		return instance;
	}

	/**
	 * Loads attributes of the specified instance, as defined by the metamodel.
	 * 
	 * @param instance
	 *            Entity instance
	 * @param uri
	 *            Primary key
	 * @param entityType
	 *            Entity type resolved from the metamodel
	 * @throws OntoDriverException
	 *             If an error occurs during load
	 */
	private <T> void loadEntityFromModel(T instance, URI uri, EntityType<T> entityType,
			EntityDescriptor descriptor) throws OntoDriverException {
		try {
			final SubjectModels<T> sm = new LoadingSubjectModels<T>(storage, uri, instance,
					valueFactory, descriptor);
			final TypesSpecification<?, ?> types = entityType.getTypes();
			if (types != null && types.getFetchType() != FetchType.LAZY) {
				typesHandler.load(instance, types, entityType, sm);
			}

			final PropertiesSpecification<?, ?> properties = entityType.getProperties();
			if (properties != null && properties.getFetchType() != FetchType.LAZY) {
				propertiesHandler.load(instance, properties, entityType, sm);
			}
			for (Attribute<?, ?> att : entityType.getAttributes()) {
				loadReference(att, sm, false);
			}
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new OntoDriverException("Exception caught when loading attributes of entity "
					+ uri, e);
		}
	}

	/**
	 * Loads standard attribute of the specified entity, i. e. either reference
	 * to other entities, data value or annotation property value.
	 * 
	 * @param entity
	 *            entity
	 * @param uri
	 *            primary key
	 * @param attribute
	 *            the attribute to load
	 * @param alwaysLoad
	 *            whether the attribute should be loaded even if it is marked as
	 *            lazily loaded
	 * @throws OntoDriverException
	 */
	private <T> void loadReference(Attribute<?, ?> attribute, SubjectModels<T> models,
			boolean alwaysLoad) throws OntoDriverException {
		try {
			final AttributeStrategy strategy = AttributeStrategyFactory.createStrategy(attribute,
					this, models);
			strategy.load(attribute, alwaysLoad);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new OntoDriverException(e);
		}
		ICValidationUtils.validateIntegrityConstraints(models.entity, models.primaryKey, attribute);
	}

	/**
	 * Removes all occurrences of the specified resource from the ontology. </p>
	 * 
	 * The resource is assumed not to be a property URI.
	 * 
	 * @param primaryKey
	 *            Resource URI
	 */
	private void removeEntityFromOntology(URI primaryKey, EntityDescriptor descriptor) {
		final Set<URI> contexts = new HashSet<>(descriptor.getFieldContexts().size() + 1);
		for (java.net.URI u : descriptor.getFieldContexts().values()) {
			contexts.add(getAddressAsSesameUri(u));
		}
		contexts.add(getAddressAsSesameUri(descriptor.getEntityContext()));
		for (Statement stmt : storage.filter(primaryKey, null, null, false, contexts)) {
			changes.add(new SesameRemoveChange(stmt, (URI) stmt.getContext()));
			storage.removeStatement(stmt, (URI) stmt.getContext());
		}
		for (Statement stmt : storage.filter(null, null, primaryKey, false, contexts)) {
			changes.add(new SesameRemoveChange(stmt, (URI) stmt.getContext()));
			storage.removeStatement(stmt, (URI) stmt.getContext());
		}
	}

	private URI resolveIdentifier(Object entity, EntityType<?> et) throws OntoDriverException {
		if (!et.getIdentifier().isGenerated()) {
			throw new PrimaryKeyNotSetException(
					"The entity has neither primary key set nor is its id field annotated as auto generated. Entity = "
							+ entity);
		}
		final URI uri = generatePrimaryKey(et.getName());
		setIdentifier(entity, uri, et);
		return uri;
	}

	/**
	 * Saves all entity attributes' values.
	 * 
	 * @param entity
	 *            Entity
	 * @param primaryKey
	 *            Entity primary key
	 * @param entityType
	 *            Entity type resolved from the metamodel
	 * @param descriptor
	 *            Context to which the attribute values will be saved. If there
	 *            are multiple, only the first one is used
	 * @throws OntoDriverException
	 */
	private <T> void saveEntityAttributes(T entity, URI primaryKey, EntityType<T> entityType,
			EntityDescriptor descriptor) throws OntoDriverException {
		try {
			final SubjectModels<T> m = new SubjectModels<T>(storage, primaryKey, entity,
					valueFactory, descriptor);
			final TypesSpecification<?, ?> types = entityType.getTypes();
			if (types != null) {
				final URI ctx = m.getFieldContext(types.getJavaField().getName());
				typesHandler.save(entity, primaryKey, types, entityType, ctx, false);
			}
			final PropertiesSpecification<?, ?> properties = entityType.getProperties();
			if (properties != null) {
				final URI ctx = m.getFieldContext(properties.getJavaField().getName());
				propertiesHandler.save(entity, primaryKey, properties, entityType, ctx, false);
			}
			for (Attribute<?, ?> att : entityType.getAttributes()) {
				saveReference(att, entityType, false, m);
			}
		} catch (RuntimeException | IllegalAccessException e) {
			throw new OntoDriverInternalException(e);
		}
	}

	/**
	 * Saves values of all attributes, except types and properties, of the
	 * specified entity into the ontology. </p>
	 * 
	 * This includes removing old values of the properties.
	 * 
	 * @param entity
	 *            The entity
	 * @param primaryKey
	 *            Entity primary key
	 * @param att
	 *            The attribute to save
	 * @param entityType
	 *            Entity type as resolved from the metamodel
	 * @throws OntoDriverException
	 *             If the attribute is inferred
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private <T> void saveReference(Attribute<?, ?> att, EntityType<T> entityType,
			boolean removeOld, SubjectModels<T> models) throws OntoDriverException,
			IllegalArgumentException, IllegalAccessException {
		if (att.isInferred()) {
			throw new OntoDriverException("Inferred fields must not be set externally.");
		}
		ICValidationUtils.validateIntegrityConstraints(models.entity, models.primaryKey, att);

		final Object oValue = att.getJavaField().get(models.entity);
		final AttributeStrategy strategy = AttributeStrategyFactory.createStrategy(att, this,
				models);
		strategy.save(att, oValue, removeOld);
	}

	private void setIdentifier(Object entity, URI uri, EntityType<?> et) throws OntoDriverException {
		final Field idField = et.getIdentifier().getJavaField();
		final String strUri = uri != null ? uri.toString() : null;
		try {
			if (strUri == null) {
				idField.set(entity, null);
			} else if (String.class == idField.getType()) {
				idField.set(entity, strUri);
			} else if (java.net.URI.class == idField.getType()) {
				idField.set(entity, java.net.URI.create(strUri));
			} else if (IRI.class == idField.getType()) {
				idField.set(entity, IRI.create(strUri));
			} else if (java.net.URL.class == idField.getType()) {
				idField.set(entity, new URL(strUri));
			} else {
				throw new IllegalArgumentException("Unknown identifier type: " + idField.getType());
			}
		} catch (IllegalArgumentException | IllegalAccessException | MalformedURLException e) {
			throw new OntoDriverException("Unable to set entity identifier.", e);
		}
	}

	/**
	 * Returns true if the specified value is an instance of URI or null.
	 * 
	 * @param value
	 *            the value
	 * @return boolean
	 */
	boolean isUri(Value value) {
		return (value == null || (value instanceof URI));
	}

	private Set<URI> asSesameUris(Set<java.net.URI> javaUris) {
		assert javaUris != null;

		final Set<URI> toReturn = new HashSet<>(javaUris.size());
		for (java.net.URI u : javaUris) {
			toReturn.add(valueFactory.createURI(u.toString()));
		}
		return toReturn;
	}
}
