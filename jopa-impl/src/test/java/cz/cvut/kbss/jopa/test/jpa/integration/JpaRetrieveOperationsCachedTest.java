package cz.cvut.kbss.jopa.test.jpa.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.test.OWLClassA;
import cz.cvut.kbss.jopa.test.OWLClassD;
import cz.cvut.kbss.jopa.test.OWLClassG;
import cz.cvut.kbss.jopa.test.OWLClassH;
import cz.cvut.kbss.jopa.test.OWLClassI;
import cz.cvut.kbss.jopa.test.TestEnvironment;
import cz.cvut.kbss.jopa.test.utils.JenaStorageConfig;
import cz.cvut.kbss.jopa.test.utils.OwlapiStorageConfig;
import cz.cvut.kbss.jopa.test.utils.OwldbStorageConfig;
import cz.cvut.kbss.jopa.test.utils.StorageConfig;
import cz.cvut.kbss.ontodriver.Context;

public class JpaRetrieveOperationsCachedTest {

	private static final Logger LOG = Logger.getLogger(JpaRetrieveOperationsCachedTest.class
			.getName());

	private static final List<StorageConfig> storages = initStorages();

	private static OWLClassA entityA;
	private static OWLClassD entityD;
	private static OWLClassG entityG;
	private static OWLClassH entityH;
	private static OWLClassI entityI;

	private static EntityManager em;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		entityA = new OWLClassA();
		entityA.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityA"));
		entityA.setStringAttribute("entityAStringAttribute");
		final Set<String> types = new HashSet<String>();
		types.add("OWLClassA");
		entityA.setTypes(types);
		entityD = new OWLClassD();
		entityD.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityD"));
		entityD.setOwlClassA(entityA);
		entityI = new OWLClassI();
		entityI.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityI"));
		entityI.setOwlClassA(entityA);
		entityH = new OWLClassH();
		entityH.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityH"));
		entityH.setOwlClassA(entityA);
		entityG = new OWLClassG();
		entityG.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/tests/entityG"));
		entityG.setOwlClassH(entityH);
	}

	@Before
	public void setUp() throws Exception {
		TestEnvironment.clearDatabase();
		TestEnvironment.resetOwldbHibernateProvider();
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
	}

	@Test
	public void testRetrieveFirstAvailable() {
		LOG.config("Test: retrieve first available entity with the specified primary key.");
		em = TestEnvironment
				.getPersistenceConnector("RetrieveFirstAvailableCached", storages, true);
		final List<Context> contexts = em.getAvailableContexts();
		assertFalse(contexts.isEmpty());
		final Context ctx = contexts.get(contexts.size() - 1);
		em.getTransaction().begin();
		em.persist(entityA, ctx.getUri());
		assertTrue(em.contains(entityA));
		em.getTransaction().commit();
		em.clear();

		final OWLClassA res = em.find(OWLClassA.class, entityA.getUri());
		assertNotNull(res);
		assertEquals(entityA.getUri(), res.getUri());
		assertEquals(entityA.getStringAttribute(), res.getStringAttribute());
		assertEquals(entityA.getTypes().size(), res.getTypes().size());
		assertTrue(entityA.getTypes().containsAll(res.getTypes()));
	}

	@Test
	public void testRetrieveFromAll() {
		LOG.config("Test: persist entity into all contexts and retrieve from all.");
		em = TestEnvironment.getPersistenceConnector("RetrieveFromAllCached", storages, true);
		final List<Context> contexts = em.getAvailableContexts();
		em.getTransaction().begin();
		for (Context ctx : contexts) {
			em.persist(entityA, ctx.getUri());
			em.persist(entityD, ctx.getUri());
		}
		em.getTransaction().commit();
		em.clear();

		final List<OWLClassD> results = new ArrayList<OWLClassD>(contexts.size());
		for (Context ctx : contexts) {
			final OWLClassD resD = em.find(OWLClassD.class, entityD.getUri(), ctx.getUri());
			assertNotNull(resD);
			assertNotNull(resD.getOwlClassA());
			for (OWLClassD d : results) {
				assertFalse(d == resD);
			}
			results.add(resD);
		}
	}

	public void testRetreiveRelationship() {
		LOG.config("Test: retrieve three entities connected by two relationships.");
		em = TestEnvironment.getPersistenceConnector("RetrieveRelationshipCached", storages, true);
		final Context ctx = em.getAvailableContexts().get(0);
		em.getTransaction().begin();
		em.persist(entityG, ctx.getUri());
		assertTrue(em.contains(entityH));
		assertTrue(em.contains(entityA));
		em.getTransaction().commit();
		em.clear();

		final OWLClassG resG = em.find(OWLClassG.class, entityG.getUri(), ctx.getUri());
		assertNotNull(resG);
		assertNotNull(resG.getOwlClassH());
		assertNotNull(resG.getOwlClassH().getOwlClassA());
	}

	@Test
	public void testRetrieveNotFound() {
		LOG.config("Test: retrieve entity which is not in the specified context.");
		em = TestEnvironment.getPersistenceConnector("RetrieveNotFoundCached", storages, true);
		final List<Context> contexts = em.getAvailableContexts();
		assertFalse(contexts.isEmpty());
		final Context empty = contexts.get(0);
		final Context target = contexts.get(contexts.size() - 1);
		assertFalse(empty.equals(target));
		em.getTransaction().begin();
		em.persist(entityI, target.getUri());
		em.getTransaction().commit();
		em.clear();

		final OWLClassI resI = em.find(OWLClassI.class, entityI.getUri(), empty.getUri());
		assertNull(resI);
	}

	private static List<StorageConfig> initStorages() {
		final List<StorageConfig> lst = new ArrayList<>(3);
		lst.add(new OwlapiStorageConfig());
		lst.add(new OwldbStorageConfig());
		lst.add(new JenaStorageConfig());
		return lst;
	}

}