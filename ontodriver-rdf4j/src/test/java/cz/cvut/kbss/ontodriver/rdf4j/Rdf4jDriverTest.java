/**
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.rdf4j;

import cz.cvut.kbss.ontodriver.Connection;
import cz.cvut.kbss.ontodriver.OntologyStorageProperties;
import cz.cvut.kbss.ontodriver.config.DriverConfiguration;
import cz.cvut.kbss.ontodriver.rdf4j.config.Rdf4jOntoDriverProperties;
import cz.cvut.kbss.ontodriver.rdf4j.connector.Connector;
import cz.cvut.kbss.ontodriver.rdf4j.connector.ConnectorFactory;
import cz.cvut.kbss.ontodriver.rdf4j.connector.init.FactoryOfFactories;
import cz.cvut.kbss.ontodriver.rdf4j.loader.StatementLoaderFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class Rdf4jDriverTest {

    private static OntologyStorageProperties storageProperties;

    private Rdf4jDriver driver;

    @Mock
    private StatementLoaderFactory statementLoaderFactory;

    @Mock
    private ConnectorFactory connectorFactory;

    @BeforeAll
    public static void setUpBeforeClass() {
        storageProperties = OntologyStorageProperties.physicalUri(URI.create("mem:test"))
                                                     .driver(Rdf4jDataSource.class.getCanonicalName())
                                                     .build();
    }

    @BeforeEach
    public void setUp() throws Exception {
        final Map<String, String> properties = new HashMap<>();
        properties.put(Rdf4jOntoDriverProperties.USE_VOLATILE_STORAGE, Boolean.toString(true));
        FactoryOfFactories factoryOfFactories = mock(FactoryOfFactories.class);
        when(factoryOfFactories.createConnectorFactory()).thenReturn(connectorFactory);
        when(factoryOfFactories.createStatementLoaderFactory()).thenReturn(statementLoaderFactory);
        this.driver = new Rdf4jDriver(storageProperties, properties) {
            @Override
            protected FactoryOfFactories getFactoryOfFactories(DriverConfiguration config) {
                return factoryOfFactories;
            }
        };
    }

    @Test
    public void testClose() throws Exception {
        assertTrue(driver.isOpen());
        driver.close();
        assertFalse(driver.isOpen());
        verify(connectorFactory).close();
    }

    @Test
    public void acquiresConnection() {
        when(connectorFactory.createStorageConnector()).thenReturn(mock(Connector.class));
        final Connection res = driver.acquireConnection();
        assertNotNull(res);
        assertNotNull(res.lists());
    }

    @Test
    public void removesClosedConnectionFromActiveConnections() throws Exception {
        when(connectorFactory.createStorageConnector()).thenReturn(mock(Connector.class));
        final Connection conn = driver.acquireConnection();
        assertNotNull(conn);
        final Field connectionsField = Rdf4jDriver.class.getDeclaredField("openConnections");
        connectionsField.setAccessible(true);
        final Set<Connection> openedConnections = (Set<Connection>) connectionsField.get(driver);
        assertTrue(openedConnections.contains(conn));
        conn.close();
        assertFalse(openedConnections.contains(conn));
    }
}
