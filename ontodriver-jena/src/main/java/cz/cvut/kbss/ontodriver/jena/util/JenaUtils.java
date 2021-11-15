/**
 * Copyright (C) 2020 Czech Technical University in Prague
 * <p>
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.ontodriver.jena.util;

import cz.cvut.kbss.ontodriver.model.Assertion;
import cz.cvut.kbss.ontodriver.model.LangString;
import cz.cvut.kbss.ontodriver.model.Value;
import cz.cvut.kbss.ontodriver.util.IdentifierUtils;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Utility methods for working with Jena API.
 */
public class JenaUtils {

    private JenaUtils() {
        throw new AssertionError();
    }

    /**
     * Transforms the specified value to an {@link RDFNode}, be it a resource or a literal.
     *
     * @param assertion Assertion representing the asserted property
     * @param value     Value to transform
     * @return Jena RDFNode
     */
    public static <T> RDFNode valueToRdfNode(Assertion assertion, Value<T> value) {
        final T val = value.getValue();
        if (IdentifierUtils.isResourceIdentifier(val)) {
            return ResourceFactory.createResource(value.stringValue());
        } else if (val instanceof LangString) {
            final LangString langString = (LangString) val;
            return langString.getLanguage().map(lang -> ResourceFactory.createLangLiteral(langString.getValue(), lang))
                    .orElseGet(() -> ResourceFactory.createTypedLiteral(langString.getValue()));
        } else if (val instanceof String) {
            return assertion.hasLanguage() ? ResourceFactory.createLangLiteral((String) val, assertion.getLanguage()) : ResourceFactory.createTypedLiteral(val);
        } else if (val instanceof cz.cvut.kbss.ontodriver.model.Literal) {
            return ResourceFactory.createTypedLiteral(((cz.cvut.kbss.ontodriver.model.Literal) val).getLexicalForm(),
                    TypeMapper.getInstance().getTypeByName(((cz.cvut.kbss.ontodriver.model.Literal) val).getDatatype()));
        } else if (val instanceof Date) {
            // Jena does not like Java Date
            final GregorianCalendar cal = new GregorianCalendar();
            cal.setTime((Date) val);
            return ResourceFactory.createTypedLiteral(cal);
        } else {
            return ResourceFactory.createTypedLiteral(val);
        }
    }

    public static Object literalToValue(Literal literal) {
        if (literal.getDatatype().equals(RDFLangString.rdfLangString)) {
            return new LangString(literal.getString(), literal.getLanguage());
        }
        if (literal.getDatatype().equals(XSDDatatype.XSDlong)) {
            // This is because Jena returns XSD:long values as Integers, when they fit. But we don't want this.
            return literal.getLong();
        } else if (literal.getDatatype().equals(XSDDatatype.XSDdateTime)) {
            // Jena does not like Java Date
            return ((XSDDateTime) literal.getValue()).asCalendar().getTime();
        }
        final Object result = literal.getValue();
        if (result.getClass().getName().startsWith("org.apache.jena")) {
            // If the result is a Jena type, it means the datatype does not have a JDK-based counterpart in Jena
            return new cz.cvut.kbss.ontodriver.model.Literal(literal.getLexicalForm(), literal.getDatatypeURI());
        }
        return result;
    }
}
