/**
 * Copyright (C) 2019 Czech Technical University in Prague
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
package cz.cvut.kbss.jopa.utils;

import cz.cvut.kbss.jopa.vocabulary.RDF;
import cz.cvut.kbss.jopa.vocabulary.RDFS;
import cz.cvut.kbss.jopa.vocabulary.XSD;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Holds mapping of prefixes to namespaces and allows resolution of prefixed IRIs.
 * <p>
 * Prefixes for RDF (rdf:), RDFS (rdfs:) and XSD (xsd:) are pre-registered.
 */
public class NamespaceResolver {

    private final Map<String, String> namespaces = new HashMap<>();

    public NamespaceResolver() {
        registerDefaultPrefixes();
    }

    private void registerDefaultPrefixes() {
        registerNamespace(RDF.PREFIX, RDF.NAMESPACE);
        registerNamespace(RDFS.PREFIX, RDFS.NAMESPACE);
        registerNamespace(XSD.PREFIX, XSD.NAMESPACE);
    }

    /**
     * Registers the specified namespace with the specified prefix.
     *
     * @param prefix    Prefix representing the namespace
     * @param namespace Namespace to represent
     */
    public void registerNamespace(String prefix, String namespace) {
        Objects.requireNonNull(prefix);
        Objects.requireNonNull(namespace);
        namespaces.put(prefix, namespace);
    }

    /**
     * Replaces prefix in the specified IRI with a full namespace IRI, if the IRI contains a prefix and it is registered
     * in this resolver.
     *
     * @param iri The IRI to resolve
     * @return Full IRI, if this resolver was able to resolve it, or the original IRI
     */
    public String resolveFullIri(String iri) {
        Objects.requireNonNull(iri);
        final int colonIndex = iri.indexOf(':');
        if (colonIndex == -1 || colonIndex > iri.length()) {
            return iri;
        }
        final String prefix = iri.substring(0, colonIndex);
        if (!namespaces.containsKey(prefix)) {
            return iri;
        }
        final String localName = iri.substring(colonIndex + 1);
        return namespaces.get(prefix) + localName;
    }
}
