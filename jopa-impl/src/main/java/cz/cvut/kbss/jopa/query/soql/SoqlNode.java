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
package cz.cvut.kbss.jopa.query.soql;

abstract class SoqlNode implements FilterableExpression {

    SoqlNode parent;
    SoqlNode child;

    SoqlNode() {
    }

    SoqlNode(SoqlNode parent) {
        this.parent = parent;
    }

    public boolean hasChild() {
        return child != null;
    }

    public SoqlNode getChild() {
        return child;
    }

    public SoqlNode getParent() {
        return parent;
    }

    public void setChild(SoqlNode child) {
        this.child = child;
    }

    public void setParent(SoqlNode parent) {
        this.parent = parent;
    }

    public abstract String getValue();

    public abstract void setValue(String value);

    public abstract String getCapitalizedValue();

    public abstract String getIri();

    public abstract void setIri(String iri);
}
