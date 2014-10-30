package cz.cvut.kbss.ontodriver.sesame;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import cz.cvut.kbss.ontodriver.OntoDriverProperties;
import cz.cvut.kbss.ontodriver.OntologyStorageProperties;
import cz.cvut.kbss.ontodriver_new.Connection;

public class SesameDataSourceTest {

	@Mock
	private SesameDriver driverMock;

	private SesameDataSource dataSource;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.dataSource = new SesameDataSource(mock(OntologyStorageProperties.class));
		TestUtils.setMock("driver", dataSource, driverMock);
	}

	@Test(expected = NullPointerException.class)
	public void testSesameDataSourceNullStorageProperties() {
		final SesameDataSource ds = new SesameDataSource(null);
		fail("This line should not have been reached.");
		assert ds == null;
	}

	@Test(expected = NullPointerException.class)
	public void testSesameDataSourceTwoArgConstructorNullStorageProperties() {
		final SesameDataSource ds = new SesameDataSource(null,
				Collections.<String, String> emptyMap());
		fail("This line should not have been reached.");
		assert ds == null;
	}

	@Test(expected = NullPointerException.class)
	public void testSesameDataSourceTwoArgConstructorNullProperties() {
		final SesameDataSource ds = new SesameDataSource(mock(OntologyStorageProperties.class),
				null);
		fail("This line should not have been reached.");
		assert ds == null;
	}

	@Test
	public void testSesameDataSourceWithProperties() throws Exception {
		final Map<String, String> properties = Collections.singletonMap(
				OntoDriverProperties.CONNECTION_AUTO_COMMIT, "false");
		final SesameDataSource ds = new SesameDataSource(mock(OntologyStorageProperties.class),
				properties);
		final Field driverField = SesameDataSource.class.getDeclaredField("driver");
		driverField.setAccessible(true);
		final SesameDriver driver = (SesameDriver) driverField.get(ds);
		final Field propsField = SesameDriver.class.getDeclaredField("properties");
		propsField.setAccessible(true);
		final Map<String, String> res = (Map<String, String>) propsField.get(driver);
		assertEquals(properties, res);
	}

	@Test
	public void testClose() throws Exception {
		assertTrue(dataSource.isOpen());
		dataSource.close();
		assertFalse(dataSource.isOpen());
		dataSource.close();
		assertFalse(dataSource.isOpen());
	}

	@Test
	public void testGetConnection() throws Exception {
		final Connection res = dataSource.getConnection();
		verify(driverMock).acquireConnection();
		assertNull(res);
	}

	@Test(expected = IllegalStateException.class)
	public void testGetConnectionOnClosed() throws Exception {
		dataSource.close();
		assertFalse(dataSource.isOpen());
		try {
			dataSource.getConnection();
		} finally {
			verify(driverMock, never()).acquireConnection();
		}
	}
}
