package cz.cvut.kbss.ontodriver.owlapi;

import cz.cvut.kbss.ontodriver.Closeable;
import cz.cvut.kbss.ontodriver.OntologyStorageProperties;
import cz.cvut.kbss.ontodriver.exception.OntoDriverException;
import cz.cvut.kbss.ontodriver.owlapi.connector.ConnectorFactory;
import cz.cvut.kbss.ontodriver.owlapi.exception.OwlapiDriverException;
import cz.cvut.kbss.ontodriver.owlapi.list.OwlapiLists;
import cz.cvut.kbss.ontodriver.Connection;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class OwlapiDriver implements Closeable, ConnectionListener {

    private final OntologyStorageProperties storageProperties;
    private final Map<String, String> properties;
    private boolean open = true;

    private final Set<OwlapiConnection> openConnections = new HashSet<>();

    OwlapiDriver(OntologyStorageProperties storageProperties, Map<String, String> properties) {
        this.storageProperties = storageProperties;
        this.properties = properties;
    }

    @Override
    public void close() throws OntoDriverException {
        if (!open) {
            return;
        }
        for (OwlapiConnection c : openConnections) {
            try {
                c.removeListener(this);
                c.close();
            } catch (Exception e) {
                if (e instanceof OntoDriverException) {
                    throw (OntoDriverException) e;
                } else {
                    throw new OwlapiDriverException(e);
                }
            }
        }
        ConnectorFactory.getInstance().close();
        this.open = false;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    Connection acquireConnection() throws OntoDriverException {
        assert open;
        final OwlapiAdapter adapter = new OwlapiAdapter(
                ConnectorFactory.getInstance().getConnector(storageProperties, properties), properties);
        final OwlapiConnection c = new OwlapiConnection(adapter);
        c.setTypes(new OwlapiTypes(c, adapter));
        c.setProperties(new OwlapiProperties(c, adapter));
        c.setLists(new OwlapiLists(c, adapter));
        openConnections.add(c);
        c.addListener(this);
        return c;
    }

    @Override
    public void connectionClosed(Connection connection) {
        openConnections.remove(connection);
    }
}
