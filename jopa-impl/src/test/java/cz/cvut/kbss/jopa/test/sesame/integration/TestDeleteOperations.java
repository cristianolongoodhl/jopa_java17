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

import cz.cvut.kbss.jopa.model.EntityManager;
import cz.cvut.kbss.jopa.owlapi.OWLAPIPersistenceProperties;
import cz.cvut.kbss.jopa.test.TestEnvironment;
import cz.cvut.kbss.jopa.test.integration.runners.DeleteOperationsRunner;
import cz.cvut.kbss.jopa.test.utils.SesameMemoryStorageConfig;
import cz.cvut.kbss.jopa.test.utils.SesameNativeStorageConfig;
import cz.cvut.kbss.jopa.test.utils.StorageConfig;
import cz.cvut.kbss.ontodriver.OntoDriverProperties;

public class TestDeleteOperations {

	private static final Logger LOG = Logger.getLogger(TestDeleteOperations.class.getName());

	private static final List<StorageConfig> storages = initStorages();
	private static final Map<String, String> properties = initProperties();

	private static DeleteOperationsRunner runner;

	private EntityManager em;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		runner = new DeleteOperationsRunner();
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
	public void testRemoveReference() {
		LOG.config("Test: remove entity referenced by another entity.");
		em = TestEnvironment.getPersistenceConnector("SesameRemoveReference", storages, true,
				properties);
		runner.removeReference(em, context(0));
	}

	@Test
	public void testRemoveCascade() {
		LOG.config("Test: remove cascade.");
		em = TestEnvironment.getPersistenceConnector("SesameRemoveCascade", storages, true,
				properties);
		runner.removeCascade(em, context(1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testRemoveDetached() {
		LOG.config("Test: try removing detached entity.");
		em = TestEnvironment.getPersistenceConnector("SesameRemoveDetached", storages, true,
				properties);
		runner.removeDetached(em, context(1));
	}

	@Test
	public void testRemoveFromSimpleList() {
		LOG.config("Test: remove entity from simple list.");
		em = TestEnvironment.getPersistenceConnector("SesameRemoveFromSimpleList", storages, true,
				properties);
		runner.removeFromSimpleList(em, context(0));
	}

	@Test
	public void testRemoveFromReferencedList() {
		LOG.config("Test: remove entity from referenced list.");
		em = TestEnvironment.getPersistenceConnector("SesameRemoveFromReferencedList", storages,
				true, properties);
		runner.removeFromReferencedList(em, context(1));
	}

	@Test
	public void testRemoveListOwner() {
		LOG.config("Test: remove owner of simple and referenced list.");
		em = TestEnvironment.getPersistenceConnector("SesameRemoveListOwner", storages, true,
				properties);
		runner.removeListOwner(em, context(0));
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