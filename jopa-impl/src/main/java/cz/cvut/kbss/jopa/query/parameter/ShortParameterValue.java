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
package cz.cvut.kbss.jopa.query.parameter;

import cz.cvut.kbss.jopa.vocabulary.XSD;

import java.util.Objects;

class ShortParameterValue extends AbstractParameterValue {

    private final short value;

    ShortParameterValue(Short value) {
        this.value = Objects.requireNonNull(value);
    }

    @Override
    public Short getValue() {
        return value;
    }

    @Override
    public String getQueryString() {
        return "\"" + value + "\"^^<" + XSD.SHORT + ">";
    }
}
