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
package cz.cvut.kbss.jopa.oom;

import cz.cvut.kbss.jopa.environment.OWLClassA;
import cz.cvut.kbss.jopa.environment.OWLClassC;
import cz.cvut.kbss.jopa.environment.utils.MetamodelMocks;
import cz.cvut.kbss.jopa.model.annotations.OWLObjectProperty;
import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.descriptors.EntityDescriptor;
import cz.cvut.kbss.jopa.model.metamodel.ListAttribute;
import cz.cvut.kbss.jopa.query.sparql.SparqlQueryFactory;
import cz.cvut.kbss.jopa.sessions.UnitOfWorkImpl;
import cz.cvut.kbss.jopa.utils.Configuration;
import cz.cvut.kbss.ontodriver.descriptor.SimpleListDescriptor;
import cz.cvut.kbss.ontodriver.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EntityConstructorPluralAttributesTest {

    private static final URI ID = URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/entityC");
    private static final NamedResource SUBJECT = NamedResource.create(ID);

    private static final Map<URI, OWLClassA> LIST_CONTENT = initListContent();
    private static URI firstListElem;

    @Mock
    private ObjectOntologyMapperImpl mapperMock;

    private MetamodelMocks metamodelMocks;

    private ListAttribute<OWLClassC, OWLClassA> simpleListMock;

    private Assertion hasSimpleListAssertion;

    private Descriptor descriptor;

    private EntityConstructor constructor;

    @BeforeEach
    public void setUp() throws Exception {
        final UnitOfWorkImpl uowMock = mock(UnitOfWorkImpl.class);
        when(mapperMock.getUow()).thenReturn(uowMock);
        when(uowMock.getQueryFactory()).thenReturn(mock(SparqlQueryFactory.class));
        when(mapperMock.getConfiguration()).thenReturn(new Configuration(Collections.emptyMap()));
        this.metamodelMocks = new MetamodelMocks();
        this.simpleListMock = metamodelMocks.forOwlClassC().simpleListAtt();
        this.descriptor = new EntityDescriptor();
        final URI simpleListProperty = URI.create(OWLClassC.getSimpleListField()
                                                           .getAnnotation(OWLObjectProperty.class).iri());
        this.hasSimpleListAssertion = Assertion
                .createObjectPropertyAssertion(simpleListProperty, simpleListMock.isInferred());

        this.constructor = new EntityConstructor(mapperMock);
    }

    @Test
    public void reconstructsEntityWithSimpleList() {
        final Collection<Axiom<?>> axioms = initAxiomsForC();
        prepareMapperMockForSimpleListLoad();

        final OWLClassC res = constructor
                .reconstructEntity(ID, metamodelMocks.forOwlClassC().entityType(), descriptor, axioms);

        assertNotNull(res);
        assertNotNull(res.getSimpleList());
        assertEquals(LIST_CONTENT.size(), res.getSimpleList().size());
        assertTrue(res.getSimpleList().containsAll(LIST_CONTENT.values()));
        verify(mapperMock).loadSimpleList(any(SimpleListDescriptor.class));
    }

    private void prepareMapperMockForSimpleListLoad() {
        for (Entry<URI, OWLClassA> e : LIST_CONTENT.entrySet()) {
            when(mapperMock.getEntityFromCacheOrOntology(OWLClassA.class, e.getKey(),
                                                            descriptor.getAttributeDescriptor(
                                                                    simpleListMock))).thenReturn(
                    e.getValue());
        }
        final Collection<Axiom<NamedResource>> listAxioms = initSimpleListAxioms();
        when(mapperMock.loadSimpleList(any(SimpleListDescriptor.class))).thenReturn(listAxioms);
    }

    private Collection<Axiom<?>> initAxiomsForC() {
        final Collection<Axiom<?>> axioms = new HashSet<>();
        axioms.add(new AxiomImpl<>(SUBJECT, Assertion.createClassAssertion(false), new Value<>(
                URI.create(OWLClassC.getClassIri()))));
        axioms.add(new AxiomImpl<>(SUBJECT, hasSimpleListAssertion, new Value<>(firstListElem)));
        return axioms;
    }

    private Collection<Axiom<NamedResource>> initSimpleListAxioms() {
        final URI nextElemProperty = simpleListMock.getOWLObjectPropertyHasNextIRI().toURI();
        final Collection<Axiom<NamedResource>> axioms = new ArrayList<>(LIST_CONTENT.size());
        boolean first = true;
        URI previous = null;
        for (URI key : LIST_CONTENT.keySet()) {
            final Axiom<NamedResource> ax;
            if (first) {
                ax = new AxiomImpl<>(NamedResource.create(ID), hasSimpleListAssertion,
                                     new Value<>(NamedResource.create(key)));
                first = false;
            } else {
                ax = new AxiomImpl<>(NamedResource.create(previous),
                                     Assertion.createObjectPropertyAssertion(nextElemProperty,
                                                                             simpleListMock.isInferred()),
                                     new Value<>(NamedResource.create(key)));
            }
            previous = key;
            axioms.add(ax);
        }
        return axioms;
    }

    @Test
    public void setsSimpleListLazilyLoadedFieldValue() {
        final Collection<Axiom<?>> axioms = Collections.singleton(new AxiomImpl<>(
                SUBJECT, hasSimpleListAssertion, new Value<>(firstListElem)));
        prepareMapperMockForSimpleListLoad();

        final OWLClassC c = new OWLClassC();
        c.setUri(ID);
        assertNull(c.getSimpleList());
        constructor.setFieldValue(c, metamodelMocks.forOwlClassC().simpleListAtt(), axioms,
                                  metamodelMocks.forOwlClassC().entityType(), descriptor);
        assertNotNull(c.getSimpleList());
        assertEquals(LIST_CONTENT.size(), c.getSimpleList().size());
        assertTrue(c.getSimpleList().containsAll(LIST_CONTENT.values()));
        verify(mapperMock).loadSimpleList(any(SimpleListDescriptor.class));
    }

    private static Map<URI, OWLClassA> initListContent() {
        final Map<URI, OWLClassA> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            final OWLClassA a = new OWLClassA();
            a.setUri(URI.create("http://krizik.felk.cvut.cz/ontologies/jopa/entityA_" + i));
            a.setStringAttribute("stringAtt" + i);
            map.put(a.getUri(), a);
            if (i == 0) {
                firstListElem = a.getUri();
            }
        }
        return map;
    }
}
