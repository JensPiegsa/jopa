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
package cz.cvut.kbss.jopa.model;

import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.loaders.PersistenceUnitClassFinder;
import cz.cvut.kbss.jopa.model.metamodel.*;
import cz.cvut.kbss.jopa.query.NamedQueryManager;
import cz.cvut.kbss.jopa.query.ResultSetMappingManager;
import cz.cvut.kbss.jopa.sessions.MetamodelProvider;
import cz.cvut.kbss.jopa.utils.Configuration;
import cz.cvut.kbss.ontodriver.config.OntoDriverProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MetamodelImpl implements Metamodel, MetamodelProvider {

    private static final Logger LOG = LoggerFactory.getLogger(Metamodel.class);

    private static final String ASPECTJ_CLASS = "org.aspectj.weaver.loadtime.Agent";

    private Map<Class<?>, ManagedType<?>> typeMap;
    private Map<Class<?>, EntityType<?>> entities;
    private Set<Class<?>> inferredClasses;
    private TypeReferenceMap typeReferenceMap;

    private NamedQueryManager namedQueryManager;
    private ResultSetMappingManager resultSetMappingManager;

    private final Configuration configuration;

    private Set<URI> moduleExtractionSignature;

    protected MetamodelImpl() {
        // Protected constructor for easier mocking
        this.configuration = null;
    }

    public MetamodelImpl(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    /**
     * Builds the metamodel for classes (entity classes, attribute converters etc.) discovered by the specified {@link
     * PersistenceUnitClassFinder}.
     *
     * @param classFinder Finder of PU classes
     */
    public void build(PersistenceUnitClassFinder classFinder) {
        Objects.requireNonNull(classFinder);
        LOG.debug("Building metamodel...");
        checkForWeaver();
        classFinder.scanClasspath(configuration);

        final MetamodelBuilder metamodelBuilder = new MetamodelBuilder(configuration);
        metamodelBuilder.buildMetamodel(classFinder);

        this.typeMap = metamodelBuilder.getTypeMap();
        this.entities = metamodelBuilder.getEntities();
        this.inferredClasses = metamodelBuilder.getInferredClasses();
        this.namedQueryManager = metamodelBuilder.getNamedQueryManager();
        this.resultSetMappingManager = metamodelBuilder.getResultSetMappingManager();
        this.typeReferenceMap = metamodelBuilder.getTypeReferenceMap();
        new StaticMetamodelInitializer(this).initializeStaticMetamodel();
    }

    /**
     * Check the class path for aspectj weaver, which is vital for using lazy loading.
     */
    private static void checkForWeaver() {
        try {
            MetamodelImpl.class.getClassLoader().loadClass(ASPECTJ_CLASS);
        } catch (ClassNotFoundException e) {
            LOG.error("AspectJ not found on classpath. Cannot run without AspectJ.");
            throw new OWLPersistenceException(e);
        }
    }

    /**
     * Builds a reduced metamodel for the specified set of entity classes.
     * <p>
     * The result of calling this method is not equivalent to {@link #build(PersistenceUnitClassFinder)} as it does not
     * process result set mappings, attribute converters, named queries etc. It only builds metamodel for the specified
     * entity classes.
     * <p>
     * Also, no additional processing (like initializing static metamodel) is done.
     *
     * @param entityClasses Entity classes to initialize the metamodel with
     */
    public void build(Set<Class<?>> entityClasses) {
        Objects.requireNonNull(entityClasses);
        LOG.debug("Building reduced metamodel from entity classes {}...", entityClasses);
        final MetamodelBuilder metamodelBuilder = new MetamodelBuilder(configuration);
        metamodelBuilder.buildMetamodel(entityClasses);

        this.typeMap = metamodelBuilder.getTypeMap();
        this.entities = metamodelBuilder.getEntities();
        this.inferredClasses = metamodelBuilder.getInferredClasses();
        this.typeReferenceMap = metamodelBuilder.getTypeReferenceMap();
    }

    @Override
    public <X> IdentifiableEntityType<X> entity(Class<X> cls) {
        if (!isEntityType(cls)) {
            throw new IllegalArgumentException(cls.getName() + " is not a known entity in this persistence unit.");
        }
        return (IdentifiableEntityType<X>) entities.get(cls);
    }

    @Override
    public Set<EntityType<?>> getMappedEntities(String classIri) {
        Objects.requireNonNull(classIri);
        return entities.values().stream().filter(et -> Objects.equals(et.getIRI().toString(), classIri))
                       .collect(Collectors.toSet());
    }

    @Override
    public Set<EntityType<?>> getEntities() {
        return new HashSet<>(entities.values());
    }

    @Override
    public Set<ManagedType<?>> getManagedTypes() {
        return new HashSet<>(typeMap.values());
    }

    @Override
    public Set<Class<?>> getInferredClasses() {
        return Collections.unmodifiableSet(inferredClasses);
    }

    public NamedQueryManager getNamedQueryManager() {
        return namedQueryManager;
    }

    public ResultSetMappingManager getResultSetMappingManager() {
        return resultSetMappingManager;
    }

    @Override
    public Set<URI> getModuleExtractionExtraSignature() {
        return Collections.unmodifiableSet(getSignatureInternal());
    }

    @Override
    public void addUriToModuleExtractionSignature(URI uri) {
        Objects.requireNonNull(uri);
        synchronized (this) {
            getSignatureInternal().add(uri);
        }
    }

    private synchronized Set<URI> getSignatureInternal() {
        // This can be lazily loaded since we don't know if we'll need it
        if (moduleExtractionSignature == null) {
            initModuleExtractionSignature();
        }
        return moduleExtractionSignature;
    }

    private void initModuleExtractionSignature() {
        assert configuration != null;
        final String sig = configuration.get(OntoDriverProperties.MODULE_EXTRACTION_SIGNATURE, "");
        if (sig.isEmpty()) {
            this.moduleExtractionSignature = new HashSet<>();
        } else {
            final String[] signature = sig.split(Pattern.quote(OntoDriverProperties.SIGNATURE_DELIMITER));
            this.moduleExtractionSignature = new HashSet<>(signature.length);
            try {
                for (String uri : signature) {
                    moduleExtractionSignature.add(URI.create(uri));
                }
            } catch (IllegalArgumentException e) {
                throw new OWLPersistenceException("Invalid URI encountered in module extraction signature.", e);
            }
        }
    }

    @Override
    public Metamodel getMetamodel() {
        return this;
    }

    @Override
    public boolean isEntityType(Class<?> cls) {
        Objects.requireNonNull(cls);
        return entities.containsKey(cls);
    }

    /**
     * Gets types which contain an attribute of the specified type.
     *
     * @param cls Type referred to
     * @return Set of referring types, possibly empty
     */
    public Set<Class<?>> getReferringTypes(Class<?> cls) {
        return typeReferenceMap.getReferringTypes(cls);
    }
}
