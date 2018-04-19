package cz.cvut.kbss.ontodriver.jena.connector;

import cz.cvut.kbss.ontodriver.config.Configuration;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ReadCommittedConnectorFactoryTest extends ConnectorFactoryTestBase {

    @Test
    public void createConnectorCreatesNewChangeTrackingConnectorWrappingCentralConnector() throws Exception {
        final Configuration configuration = StorageTestUtil.createConfiguration("test:uri");
        final ConnectorFactory factory = connectorFactory(configuration);
        final StorageConnector connector = factory.createConnector();
        assertTrue(connector instanceof ChangeTrackingStorageConnector);
        assertNotNull(getCentralConnector(factory));
        assertTrue(getCentralConnector(factory).isOpen());
    }

    @Override
    ConnectorFactory connectorFactory(Configuration configuration) {
        return new ReadCommittedConnectorFactory(configuration);
    }

    @Override
    SharedStorageConnector getCentralConnector(ConnectorFactory factory) throws Exception {
        final Field connectorField = ReadCommittedConnectorFactory.class.getDeclaredField("centralConnector");
        connectorField.setAccessible(true);
        return (SharedStorageConnector) connectorField.get(factory);
    }
}