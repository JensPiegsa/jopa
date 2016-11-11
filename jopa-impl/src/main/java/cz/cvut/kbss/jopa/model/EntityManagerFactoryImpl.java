/**
 * Copyright (C) 2016 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.model;

import cz.cvut.kbss.jopa.loaders.EntityLoader;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.Metamodel;
import cz.cvut.kbss.jopa.model.query.Query;
import cz.cvut.kbss.jopa.sessions.Cache;
import cz.cvut.kbss.jopa.sessions.ServerSession;
import cz.cvut.kbss.jopa.utils.Configuration;
import cz.cvut.kbss.jopa.utils.EntityPropertiesUtils;
import cz.cvut.kbss.ontodriver.OntologyStorageProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManagerFactoryImpl implements EntityManagerFactory, PersistenceUnitUtil {

    private volatile boolean open = true;

    private final Set<AbstractEntityManager> em;
    private final Configuration configuration;
    private final OntologyStorageProperties storageProperties;

    private volatile ServerSession serverSession;

    private volatile MetamodelImpl metamodel;

    public EntityManagerFactoryImpl(final Map<String, String> properties) {
        this.em = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.configuration = new Configuration(properties != null ? properties : Collections.emptyMap());
        this.storageProperties = initStorageProperties();
        initMetamodel();
    }

    private OntologyStorageProperties initStorageProperties() {
        return OntologyStorageProperties.driver(configuration.get(JOPAPersistenceProperties.DATA_SOURCE_CLASS))
                                        .ontologyUri(configuration.get(JOPAPersistenceProperties.ONTOLOGY_URI_KEY))
                                        .physicalUri(configuration
                                                .get(JOPAPersistenceProperties.ONTOLOGY_PHYSICAL_URI_KEY))
                                        .username(configuration.get(JOPAPersistenceProperties.DATA_SOURCE_USERNAME))
                                        .password(configuration.get(JOPAPersistenceProperties.DATA_SOURCE_PASSWORD))
                                        .build();
    }

    private void initMetamodel() {
        this.metamodel = new MetamodelImpl(configuration);
        metamodel.build(new EntityLoader());
    }

    @Override
    public void close() {
        verifyOpen();
        synchronized (this) {
            if (!open) {
                return;
            }
            open = false;

            em.stream().filter(EntityManager::isOpen).forEach(EntityManager::close);
            em.clear();
            if (serverSession != null) {
                serverSession.close();
                this.serverSession = null;
            }
        }
    }

    @Override
    public EntityManager createEntityManager() {
        return this.createEntityManager(Collections.emptyMap());
    }

    @Override
    public EntityManager createEntityManager(Map<String, String> map) {
        verifyOpen();

        final Map<String, String> newMap = new HashMap<>(map);

        newMap.putAll(configuration.getProperties());

        initServerSession();

        final AbstractEntityManager c = new EntityManagerImpl(this, new Configuration(newMap), this.serverSession);

        em.add(c);
        return c;
    }

    private void verifyOpen() {
        if (!open) {
            throw new IllegalStateException("The entity manager factory is closed.");
        }
    }

    /**
     * Initializes the server session if necessary.
     */
    private synchronized void initServerSession() {
        if (serverSession == null) {
            this.serverSession = new ServerSession(storageProperties, configuration.getProperties(), metamodel);
        }
    }

    /**
     * The server session should by initialized by now, but to make sure, there is default initialization with an empty
     * properties map.
     *
     * @return The ServerSession for this factory.
     */
    public ServerSession getServerSession() {
        return serverSession;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public Map<String, String> getProperties() {
        verifyOpen();
        return configuration.getProperties();
    }

    @Override
    public Metamodel getMetamodel() {
        verifyOpen();
        return metamodel;
    }

    @Override
    public PersistenceUnitUtil getPersistenceUnitUtil() {
        verifyOpen();
        return this;
    }

    @Override
    public void addNamedQuery(String name, Query query) {
        verifyOpen();
        throw new NotYetImplementedException();
    }

    @Override
    public Object getIdentifier(Object entity) {
        final EntityType<?> et = getMetamodel().entity(entity.getClass());
        return EntityPropertiesUtils.getFieldValue(et.getIdentifier().getJavaField(), entity);
    }

    @Override
    public boolean isLoaded(Object entity, String attributeName) {
        for (final AbstractEntityManager emi : em) {
            if (emi.contains(entity)) {
                return attributeName == null || emi.isLoaded(entity, attributeName);

            }
        }

        return false;
    }

    @Override
    public boolean isLoaded(Object entity) {
        // TODO
        return false;
        // return isLoaded(entity);
    }

    @Override
    public Cache getCache() {
        verifyOpen();
        initServerSession();
        return serverSession.getLiveObjectCache();
    }

    void entityManagerClosed(AbstractEntityManager manager) {
        assert manager != null;
        em.remove(manager);
    }
}
