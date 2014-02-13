package cz.cvut.kbss.ontodriver;

import cz.cvut.kbss.ontodriver.exceptions.OntoDriverException;

/**
 * Represents an ontology data source. </p>
 * 
 * This could be either a single ontology or multiple ontologies, named graphs
 * or ontology modules.
 * 
 * The way {@code DataSource} instances will be initialized is
 * implementation-dependent.
 * 
 * @author kidney
 * 
 */
public interface DataSource extends Closeable {

	/**
	 * Requests a connection to the underlying data source. </p>
	 * 
	 * @return A {@code Connection} to the data source
	 * @throws OntoDriverException
	 *             If an ontology access error occurs
	 */
	public Connection getConnection() throws OntoDriverException;

	/**
	 * Requests a connection to the underlying data source. </p>
	 * 
	 * @param persistenceProvider
	 *            Facade representing accessible persistence provider methods
	 * @return a {@code Connection} to the data source
	 * @throws OntoDriverException
	 *             If an ontology access error occurs
	 */
	public Connection getConnection(PersistenceProviderFacade persistenceProvider)
			throws OntoDriverException;
}