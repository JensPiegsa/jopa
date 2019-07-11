/**
 * Copyright (C) 2019 Czech Technical University in Prague
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
package cz.cvut.kbss.jopa.model.metamodel;

import cz.cvut.kbss.jopa.model.IRI;
import cz.cvut.kbss.jopa.model.annotations.OWLDataProperty;

import java.lang.reflect.Field;

class DataPropertyAttributes extends PropertyAttributes {

    DataPropertyAttributes(FieldMappingValidator validator) {
        super(validator);
    }

    @Override
    void resolve(Field field, MetamodelBuilder metamodelBuilder, Class<?> fieldValueCls) {
        super.resolve(field, metamodelBuilder, fieldValueCls);
        final OWLDataProperty odp = field.getAnnotation(OWLDataProperty.class);
        assert odp != null;

        this.persistentAttributeType = Attribute.PersistentAttributeType.DATA;
        this.iri = IRI.create(typeBuilderContext.resolveNamespace(odp.iri()));
        this.fetchType = odp.fetch();
        this.type = BasicTypeImpl.get(fieldValueCls);
        this.lexicalForm = odp.lexicalForm();
        this.simpleLiteral = odp.simpleLiteral();
        validator.validateDataPropertyField(field, odp);
    }
}
