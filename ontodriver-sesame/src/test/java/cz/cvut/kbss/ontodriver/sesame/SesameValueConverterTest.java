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
package cz.cvut.kbss.ontodriver.sesame;

import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.NamedResource;
import cz.cvut.kbss.ontodriver.sesame.exceptions.SesameDriverException;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SesameValueConverterTest {

    private static final String LANG = "en";
    private static final URI PROPERTY = URI.create("http://krizik.felk.cvut.cz/ontologies/jopa#property");

    private SesameValueConverter converter;

    @Before
    public void setUp() {
        this.converter = new SesameValueConverter(SimpleValueFactory.getInstance(), LANG);
    }

    @Test
    public void convertsObjectPropertyNamedResourceToSesameUri() throws Exception {
        final Value res = converter.toSesameValue(assertion(Assertion.AssertionType.OBJECT_PROPERTY),
                value(NamedResource.create("http://krizik.felk.cvut.cz/ontologies/jopa#individual")));
        assertTrue(res instanceof org.eclipse.rdf4j.model.IRI);
    }

    private Assertion assertion(Assertion.AssertionType type) {
        switch (type) {
            case CLASS:
                return Assertion.createClassAssertion(false);
            case PROPERTY:
                return Assertion.createPropertyAssertion(PROPERTY, false);
            case OBJECT_PROPERTY:
                return Assertion.createObjectPropertyAssertion(PROPERTY, false);
            case DATA_PROPERTY:
                return Assertion.createDataPropertyAssertion(PROPERTY, false);
            case ANNOTATION_PROPERTY:
                return Assertion.createAnnotationPropertyAssertion(PROPERTY, false);
            default:
                throw new IllegalArgumentException("Unknown assertion type: " + type);
        }
    }

    private <T> cz.cvut.kbss.ontodriver.model.Value<T> value(T val) {
        return new cz.cvut.kbss.ontodriver.model.Value<>(val);
    }

    @Test
    public void convertsDataPropertyValueToSesameLiteral() throws Exception {
        final int val = 117;
        final Value res = converter.toSesameValue(assertion(Assertion.AssertionType.DATA_PROPERTY), value(val));
        assertTrue(res instanceof Literal);
        assertEquals(val, ((Literal) res).intValue());
    }

    @Test
    public void convertsAnnotationPropertyLiteralValueToSesameLiteral() throws Exception {
        final String val = "AnnotationValue";
        final Value res = converter.toSesameValue(assertion(Assertion.AssertionType.ANNOTATION_PROPERTY), value(val));
        assertTrue(res instanceof Literal);
        assertEquals(val, ((Literal) res).getLabel());
        assertEquals(LANG, ((Literal) res).getLanguage().orElse(""));
    }

    @Test
    public void convertsAnnotationPropertyNamedResourceToSesameUri() throws Exception {
        final NamedResource val = NamedResource
                .create("http://krizik.felk.cvut.cz/ontologies/jopa#individualAnnotation");
        final Value res = converter.toSesameValue(assertion(Assertion.AssertionType.ANNOTATION_PROPERTY), value(val));
        assertTrue(res instanceof org.eclipse.rdf4j.model.IRI);
        assertEquals(val.toString(), res.toString());
    }

    @Test(expected = SesameDriverException.class)
    public void conversionThrowsExceptionWhenObjectPropertyValueIsNotUri() throws Exception {
        converter.toSesameValue(assertion(Assertion.AssertionType.OBJECT_PROPERTY), value(117));
    }
}