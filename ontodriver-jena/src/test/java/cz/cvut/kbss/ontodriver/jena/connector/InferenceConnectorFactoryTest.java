package cz.cvut.kbss.ontodriver.jena.connector;

import cz.cvut.kbss.ontodriver.config.Configuration;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertTrue;

public class InferenceConnectorFactoryTest extends ConnectorFactoryTestBase {

    @Override
    ConnectorFactory connectorFactory(Configuration configuration) {
        return new InferenceConnectorFactory(configuration);
    }

    @Override
    SharedStorageConnector getCentralConnector(ConnectorFactory factory) throws Exception {
        final Field connectorField = InferenceConnectorFactory.class.getDeclaredField("centralConnector");
        connectorField.setAccessible(true);
        return (SharedStorageConnector) connectorField.get(factory);
    }

    @Test
    public void createInferredConnectorReturnsCorrectConnector() throws Exception {
        final Configuration configuration = StorageTestUtil.createConfiguration("test:uri");
        final ConnectorFactory factory = connectorFactory(configuration);
        final StorageConnector connector = factory.createConnector();
        final InferredStorageConnector result = factory.createInferredConnector(connector);
        assertTrue(result instanceof SnapshotStorageConnectorWithInference);
    }
}