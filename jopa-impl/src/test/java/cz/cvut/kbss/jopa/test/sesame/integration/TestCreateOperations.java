package cz.cvut.kbss.jopa.test.sesame.integration;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import cz.cvut.kbss.jopa.exceptions.OWLEntityExistsException;
import cz.cvut.kbss.jopa.exceptions.RollbackException;
import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.owlapi.OWLAPIPersistenceProperties;
import cz.cvut.kbss.jopa.test.TestEnvironment;
import cz.cvut.kbss.jopa.test.integration.runners.CreateOperationsRunner;
import cz.cvut.kbss.jopa.test.utils.SesameMemoryStorageConfig;
import cz.cvut.kbss.jopa.test.utils.SesameNativeStorageConfig;
import cz.cvut.kbss.jopa.test.utils.StorageConfig;
import cz.cvut.kbss.ontodriver.OntoDriverProperties;

public class TestCreateOperations {

	private static final Logger LOG = Logger.getLogger(TestCreateOperations.class.getName());

	private static final List<StorageConfig> storages = initStorages();
	private static final Map<String, String> properties = initProperties();

	private static CreateOperationsRunner runner;

	private EntityManager em;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		runner = new CreateOperationsRunner();
	}

	@After
	public void tearDown() throws Exception {
		if (em.isOpen()) {
			if (em.getTransaction().isActive()) {
				em.getTransaction().rollback();
			}
			em.close();
			em.getEntityManagerFactory().close();
		}
		runner.initBeforeTest();
	}

	@Test
	public void testPersistWithGenerated() {
		LOG.config("Test: persist into all contexts, also with generated id.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistWithGenerated", storages, false,
				properties);
		runner.persistWithGenerated(em, context(1));
	}

	@Test
	public void testPersistCascade() {
		LOG.config("Test: persist with cascade over two relationships.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistWithCascade", storages, false,
				properties);
		runner.persistCascade(em, context(0));
	}

	@Test(expected = OWLEntityExistsException.class)
	public void testPersistTwiceInOne() {
		LOG.config("Test: persist twice into one context.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistTwice", storages, false,
				properties);
		runner.persistTwice(em, context(0));
	}

	@Test(expected = RollbackException.class)
	public void testPersistWithoutCascade() {
		LOG.config("Test: try persisting relationship not marked as cascade.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistWithoutCascade", storages,
				false, properties);
		runner.persistWithoutCascade(em, context(0));
	}

	@Test
	public void testPersistSimpleList() {
		LOG.config("Test: persist entity with simple list.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistSimpleList", storages, false,
				properties);
		runner.persistSimpleList(em, context(1));
	}

	@Test(expected = RollbackException.class)
	public void testPersistSimpleListNoCascade() {
		LOG.config("Test: persist entity with simple list, but don't persist the referenced entities.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistSimpleListNoCascade", storages,
				false, properties);
		runner.persistSimpleListNoCascade(em, context(1));
	}

	@Test
	public void testPersistReferencedList() {
		LOG.config("Test: persist entity with referenced list.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistReferencedList", storages,
				false, properties);
		runner.persistReferencedList(em, context(0));
	}

	@Test(expected = RollbackException.class)
	public void testPersistReferencedListNoCascade() {
		LOG.config("Test: persist entity with referenced list. Don't persist the referenced entities.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistReferencedListNoCascade",
				storages, false, properties);
		runner.persistReferencedListNoCascade(em, context(0));
	}

	@Test
	public void testPersistSimpleAndReferencedList() {
		LOG.config("Test: persist entity with both simple and referenced list.");
		em = TestEnvironment.getPersistenceConnector("SesamePersistSimpleAndReferencedList",
				storages, false, properties);
		runner.persistSimpleAndReferencedList(em, context(0));
	}

	@Test
	public void testPersistProperties() {
		LOG.config("Test: persist entity with properties.");
		em = TestEnvironment.getPersistenceConnector("JpaIntegration-PersistWithProperties",
				storages, false, properties);
		runner.persistProperties(em, context(1));
	}

	@Test
	public void testPersistPropertiesEmpty() {
		LOG.config("Test: persist entity with properties. The properties will be an empty map.");
		em = TestEnvironment.getPersistenceConnector("JpaIntegration-PersistWithPropertiesEmpty",
				storages, false, properties);
		runner.persistPropertiesEmpty(em, context(0));
	}

	private URI context(int index) {
		return em.getAvailableContexts().get(index).getUri();
	}

	private static List<StorageConfig> initStorages() {
		final List<StorageConfig> lst = new ArrayList<>(2);
		lst.add(new SesameNativeStorageConfig());
		lst.add(new SesameMemoryStorageConfig());
		return lst;
	}

	private static Map<String, String> initProperties() {
		final Map<String, String> map = new HashMap<>();
		map.put(OntoDriverProperties.USE_TRANSACTIONAL_ONTOLOGY, Boolean.TRUE.toString());
		map.put(OntoDriverProperties.SESAME_USE_VOLATILE_STORAGE, Boolean.TRUE.toString());
		map.put(OntoDriverProperties.SESAME_USE_INFERENCE, Boolean.FALSE.toString());
		map.put(OWLAPIPersistenceProperties.LANG, "en");
		return map;
	}
}