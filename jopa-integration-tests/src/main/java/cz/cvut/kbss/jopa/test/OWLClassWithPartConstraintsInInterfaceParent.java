/**
 * Copyright (C) 2022 Czech Technical University in Prague
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
package cz.cvut.kbss.jopa.test;

import cz.cvut.kbss.jopa.model.annotations.Id;
import cz.cvut.kbss.jopa.model.annotations.OWLClass;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.Set;

@OWLClass(iri = Vocabulary.C_OWL_CLASS_PART_CONSTR_IN_PARENT)
public class OWLClassWithPartConstraintsInInterfaceParent implements OWLInterfaceE {

    protected OWLClassWithUnProperties data;
    protected Set<OWLClassWithUnProperties> dataList;
    protected ZoneOffset withConverter;

    public OWLClassWithPartConstraintsInInterfaceParent(URI uri) {
        this.uri = uri;
    }

    public OWLClassWithPartConstraintsInInterfaceParent() {
    }

    @Id
    private URI uri;

    public URI getUri() {
        return uri;
    }

    public void setUri(URI uri) {
        this.uri = uri;
    }

    @Override
    public OWLClassWithUnProperties getData() {
        return data;
    }

    public void setData(OWLClassWithUnProperties data) {
        this.data = data;
    }

    @Override
    public Set<OWLClassWithUnProperties> getDataList() {
        return dataList;
    }

    public void setDataList(Set<OWLClassWithUnProperties> dataList) {
        this.dataList = dataList;
    }

    @Override
    public ZoneOffset getWithConverter() {
        return withConverter;
    }

    public void setWithConverter(ZoneOffset withConverter) {
        this.withConverter = withConverter;
    }
}
