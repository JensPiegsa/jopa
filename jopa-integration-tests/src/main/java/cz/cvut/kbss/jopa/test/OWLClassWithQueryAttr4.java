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
package cz.cvut.kbss.jopa.test;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;
import cz.cvut.kbss.jopa.model.annotations.Sparql;

import java.net.URI;

@OWLClass(iri = Vocabulary.C_OwlClassWithQueryAttr4)
public class OWLClassWithQueryAttr4 implements HasUri {

    private static final String QUERY = "ASK" +
                                        "WHERE {?this <http://krizik.felk.cvut.cz/ontologies/jopa/attributes#B-stringAttribute> ?stringAttribute}";

    @Id
    private URI uri;

    @OWLDataProperty(iri = "http://krizik.felk.cvut.cz/ontologies/jopa/attributes#B-stringAttribute", simpleLiteral = true)
    private String stringAttribute;

    @Sparql(query=QUERY)
    private Boolean askQueryAttribute;

    public OWLClassWithQueryAttr4() {
    }

    public OWLClassWithQueryAttr4(URI uri) {
        this.uri = uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    public String getStringAttribute() {
        return stringAttribute;
    }

    public void setStringAttribute(String stringAttribute) {
        this.stringAttribute = stringAttribute;
    }

    public Boolean getAskQueryAttribute() {
        return askQueryAttribute;
    }

    public void setAskQueryAttribute(Boolean askQueryAttribute) {
        this.askQueryAttribute = askQueryAttribute;
    }

    public static String getSparqlQuery() {
        return QUERY;
    }

    @Override
    public String toString() {
        String out = "OWLClassWithQueryAttr: uri = " + uri;
        out += ", stringAttribute = " + stringAttribute;
        return out;
    }
}
