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
package cz.cvut.kbss.jopa.model.metamodel;

import cz.cvut.kbss.jopa.exception.MetamodelInitializationException;
import cz.cvut.kbss.jopa.exceptions.OWLPersistenceException;
import cz.cvut.kbss.jopa.model.BeanListenerAspect;
import cz.cvut.kbss.jopa.model.IRI;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.*;
import cz.cvut.kbss.jopa.oom.converter.ConverterWrapper;
import cz.cvut.kbss.jopa.utils.EntityPropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

class ClassFieldMetamodelProcessor<X> {

    private static final Logger LOG = LoggerFactory.getLogger(ClassFieldMetamodelProcessor.class);

    private final FieldMappingValidator mappingValidator = new FieldMappingValidator();

    private final Class<X> cls;
    private final AbstractIdentifiableType<X> et;
    private final TypeBuilderContext<X> context;
    private final MetamodelBuilder metamodelBuilder;

    ClassFieldMetamodelProcessor(TypeBuilderContext<X> context, MetamodelBuilder metamodelBuilder) {
        this.cls = context.getType().getJavaType();
        this.context = context;
        this.et = context.getType();
        this.metamodelBuilder = metamodelBuilder;
    }

    void processField(Field field) {
        LOG.trace("processing field: {}", field);
        if (EntityPropertiesUtils.isFieldTransient(field)) {
            // Do not log static fields
            if (!EntityPropertiesUtils.isFieldStatic(field)) {
                LOG.trace("Skipping transient field {}", field);
            }
            return;
        }
        if (field.getType().isPrimitive()) {
            throw new MetamodelInitializationException("Primitive types cannot be used for entity fields. Field " + field + " in class " + cls);
        }

        final Class<?> fieldValueCls = getFieldValueType(field);
        field.setAccessible(true);
        final InferenceInfo inference = processInferenceInfo(field);

        if (isTypesField(field)) {
            processTypesField(field, fieldValueCls, inference);
            return;
        }
        if (isPropertiesField(field)) {
            processPropertiesField(field, fieldValueCls, inference);
            return;
        }

        if (isQueryAttribute(field)) {
            createQueryAttribute(field, fieldValueCls);
            return;
        }

        PropertyInfo propertyInfo = PropertyInfo.from(field);

        final PropertyAttributes propertyAtt = PropertyAttributes.create(propertyInfo, mappingValidator, context);
        propertyAtt.resolve(propertyInfo, metamodelBuilder, fieldValueCls);

        if (propertyAtt.isKnownOwlProperty()) {
            final AbstractAttribute<X, ?> a = createAttribute(propertyInfo, inference, propertyAtt);
            registerTypeReference(a);
            return;
        }

        if (isAspectIntegrationField(field)) {
            return;
        }

        if (tryProcessIdentifierField(field)) {
            return;
        }

        AnnotatedAccessor fieldAccessor = findAnnotatedMethodBelongingToField(field);

        if (fieldAccessor != null) {
            createAndRegisterAttribute(field, inference, fieldValueCls, fieldAccessor);
            return;
        }

        throw new MetamodelInitializationException("Unable to process field " + field + ". It is not transient but has no mapping information.");
    }

    /**
     * Do a bottom top search of hierarchy in order to find annotated accessor belonging to given field.
     * This is used when a property annotation ( {@link OWLDataProperty,OWLObjectProperty,OWLAnnotationProperty)
     * is declared on method, not on field.
     */
    private AnnotatedAccessor findAnnotatedMethodBelongingToField(Field field) {
        LOG.debug("finding property definition to field {} ", field);

        Deque<IdentifiableType<?>> toVisit = new ArrayDeque<>();
        toVisit.add(et);

        boolean found = false;
        AnnotatedAccessor foundAcessor = null;

        while (!toVisit.isEmpty()) {
            IdentifiableType<?> top = toVisit.peek();
            Collection<AnnotatedAccessor> accessorsInParent = metamodelBuilder.getAnnotatedAccessorsForClass(top);

            for (AnnotatedAccessor annotatedAccessor : accessorsInParent) {

                if (!propertyBelongsToMethod(field, annotatedAccessor)) {
                    continue;
                }

                LOG.debug("Found annotated method belonging to field - {} - {}", annotatedAccessor.getMethod().getName(), field.getName());
                if (!found) {  // first belonging method
                    found = true;
                    foundAcessor = annotatedAccessor;
                } else if (methodsAnnotationsEqual(annotatedAccessor.getMethod(), foundAcessor.getMethod())) {
                    LOG.debug("Methods are equal, skipping");
                } else { /// Two non-equal methods that could belong to the field - ambiguous
                    throw new MetamodelInitializationException("Ambiguous hierarchy - fields can inherit only from multiple methods if their property mapping annotations equal. However for field " + field + " two non-compatible methods were found - " + foundAcessor.getMethod() + " and " + annotatedAccessor.getMethod());
                }

            }
            /// remove top
            toVisit.pop();
            /// add all parents
            toVisit.addAll(top.getSupertypes());
        }

        return foundAcessor;
    }

    private void createAndRegisterAttribute(Field field, InferenceInfo inference, Class<?> fieldValueCls, AnnotatedAccessor annotatedAccessor) {
        final PropertyInfo info = PropertyInfo.from(annotatedAccessor.getMethod(), field);

        final PropertyAttributes propertyAtt = PropertyAttributes.create(info, mappingValidator, context);
        propertyAtt.resolve(info, metamodelBuilder, fieldValueCls);

        final AbstractAttribute<X, ?> a = createAttribute(info, inference, propertyAtt);
        registerTypeReference(a);
    }

    private boolean methodsAnnotationsEqual(Method newMethod, Method foundMethod) {

        return Objects.equals(newMethod.getAnnotation(OWLObjectProperty.class), foundMethod.getAnnotation(OWLObjectProperty.class)) &&
                Objects.equals(newMethod.getAnnotation(OWLDataProperty.class), foundMethod.getAnnotation(OWLDataProperty.class)) &&
                Objects.equals(newMethod.getAnnotation(OWLAnnotationProperty.class), foundMethod.getAnnotation(OWLAnnotationProperty.class)) &&
                Objects.equals(newMethod.getAnnotation(Convert.class), foundMethod.getAnnotation(Convert.class)) &&
                Objects.equals(newMethod.getAnnotation(Sequence.class), foundMethod.getAnnotation(Sequence.class)) &&
                Objects.equals(newMethod.getAnnotation(Enumerated.class), foundMethod.getAnnotation(Enumerated.class)) &&
                Objects.equals(newMethod.getAnnotation(ParticipationConstraints.class), foundMethod.getAnnotation(ParticipationConstraints.class));

    }


    private boolean propertyBelongsToMethod(Field property, AnnotatedAccessor accessor) {
        if (!property.getName().equals(accessor.getPropertyName())) {
            return false;
        }
        if (!property.getType().isAssignableFrom(accessor.getPropertyType())) {
            throw new MetamodelInitializationException("Non-compatible types between method " + accessor + " and field " + property.getName() + ". Method is accessor to type " + accessor.getPropertyType() + ", field type is "+property.getType());
        }

        return true;
    }

    private static Class<?> getFieldValueType(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            return getSetOrListErasureType((ParameterizedType) field.getGenericType());
        } else if (field.getType().isArray()) {
            throw new MetamodelInitializationException("Array persistent attributes are not supported.");
        } else {
            return field.getType();
        }
    }

    private static Class<?> getSetOrListErasureType(final ParameterizedType cls) {
        final Type[] t = cls.getActualTypeArguments();

        if (t.length != 1) {
            throw new OWLPersistenceException("Only collections with a single generic parameter are supported.");
        }
        Type type = t[0];
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            final Type rawType = ((ParameterizedType) type).getRawType();
            return (Class<?>) rawType;
        }
        throw new OWLPersistenceException("Unsupported collection element type " + type);
    }

    private InferenceInfo processInferenceInfo(Field field) {
        final Inferred inferred = field.getAnnotation(Inferred.class);

        final InferenceInfo inference = new InferenceInfo(inferred);
        if (inference.inferred) {
            metamodelBuilder.addInferredClass(cls);
        }
        return inference;
    }

    private static boolean isTypesField(Field field) {
        return field.getAnnotation(Types.class) != null;
    }

    private void processTypesField(Field field, Class<?> fieldValueCls, InferenceInfo inference) {
        Types tt = field.getAnnotation(Types.class);
        mappingValidator.validateTypesField(field);
        final FetchType fetchType = inference.inferred ? FetchType.EAGER : tt.fetchType();
        et.addDirectTypes(new TypesSpecificationImpl<>(et, fetchType, field, fieldValueCls, inference.inferred));
    }

    private static boolean isPropertiesField(Field field) {
        return field.getAnnotation(Properties.class) != null;
    }

    private void processPropertiesField(Field field, Class<?> fieldValueCls, InferenceInfo inference) {
        Properties properties = field.getAnnotation(Properties.class);
        mappingValidator.validatePropertiesField(field);
        final PropertiesParametersResolver paramsResolver = new PropertiesParametersResolver(field);
        final FetchType fetchType = inference.inferred ? FetchType.EAGER : properties.fetchType();
        et.addOtherProperties(
                PropertiesSpecificationImpl.declaringType(et).fetchType(fetchType).javaField(field)
                        .javaType(fieldValueCls).inferred(inference.inferred)
                        .propertyIdType(paramsResolver.getPropertyIdentifierType())
                        .propertyValueType(paramsResolver.getPropertyValueType()).build());
    }

    private static boolean isQueryAttribute(Field field) {
        return field.getAnnotation(Sparql.class) != null;
    }

    private void createQueryAttribute(Field field, Class<?> fieldValueCls) {
        final Sparql sparqlAnnotation = field.getAnnotation(Sparql.class);
        final String query = sparqlAnnotation.query();
        final FetchType fetchType = sparqlAnnotation.fetchType();

        ParticipationConstraint[] participationConstraints = field.getAnnotationsByType(ParticipationConstraint.class);

        final AbstractQueryAttribute<X, ?> a;

        cz.cvut.kbss.jopa.model.metamodel.Type<?> type;

        if (ManagedClassProcessor.isManagedType(fieldValueCls)) {
            type = metamodelBuilder.getEntityClass(fieldValueCls);
        } else {
            type = BasicTypeImpl.get(fieldValueCls);
        }

        Optional<ConverterWrapper<?, ?>> optionalConverterWrapper = context.getConverterResolver().resolveConverter(type);
        ConverterWrapper<?, ?> converterWrapper = null;

        if (optionalConverterWrapper.isPresent()) {
            converterWrapper = optionalConverterWrapper.get();
        }

        if (Collection.class.isAssignableFrom(field.getType())) {
            a = new PluralQueryAttributeImpl<>(query, sparqlAnnotation.enableReferencingAttributes(), field, et, fetchType, participationConstraints, type, field.getType(), converterWrapper);
        } else if (Map.class.isAssignableFrom(field.getType())) {
            throw new IllegalArgumentException("NOT YET SUPPORTED");
        } else {
            a = new SingularQueryAttributeImpl<>(query, sparqlAnnotation.enableReferencingAttributes(), field, et, fetchType, type, participationConstraints, converterWrapper);
        }

        et.addDeclaredQueryAttribute(field.getName(), a);
    }

    private AbstractAttribute<X, ?> createAttribute(PropertyInfo property, InferenceInfo
            inference, PropertyAttributes propertyAttributes) {
        final AbstractAttribute<X, ?> a;
        if (property.getType().isAssignableFrom(Collection.class)) {
            final AbstractPluralAttribute.PluralAttributeBuilder builder = CollectionAttributeImpl.builder(propertyAttributes).declaringType(et).propertyInfo(property).inferred(inference.inferred).includeExplicit(inference.includeExplicit);
            context.getConverterResolver().resolveConverter(property, propertyAttributes).ifPresent(builder::converter);
            a = (AbstractAttribute<X, ?>) builder.build();
        } else if (property.getType().isAssignableFrom(List.class)) {
            a = createListAttribute(property, inference, propertyAttributes);
        } else if (property.getType().isAssignableFrom(Set.class)) {
            final AbstractPluralAttribute.PluralAttributeBuilder builder = SetAttributeImpl.builder(propertyAttributes).declaringType(et).propertyInfo(property).inferred(inference.inferred).includeExplicit(inference.includeExplicit);
            context.getConverterResolver().resolveConverter(property, propertyAttributes).ifPresent(builder::converter);
            a = (AbstractAttribute<X, ?>) builder.build();
        } else if (property.getType().isAssignableFrom(Map.class)) {
            throw new IllegalArgumentException("NOT YET SUPPORTED");
        } else {
            final SingularAttributeImpl.SingularAttributeBuilder builder = SingularAttributeImpl.builder(propertyAttributes).declaringType(et).propertyInfo(property).inferred(inference.inferred).includeExplicit(inference.includeExplicit);
            context.getConverterResolver().resolveConverter(property, propertyAttributes).ifPresent(builder::converter);
            a = (AbstractAttribute<X, ?>) builder.build();
        }
        et.addDeclaredAttribute(property.getName(), a);
        return a;
    }

    private AbstractAttribute<X, ?> createListAttribute(PropertyInfo property, InferenceInfo
            inference, PropertyAttributes propertyAttributes) {
        final Sequence os = property.getAnnotation(Sequence.class);
        if (os == null) {
            throw new MetamodelInitializationException("Expected Sequence annotation.");
        }
        final ListAttributeImpl.ListAttributeBuilder builder = ListAttributeImpl.builder(propertyAttributes).declaringType(et).propertyInfo(property).inferred(inference.inferred).includeExplicit(inference.includeExplicit).owlListClass(IRI.create(os.ClassOWLListIRI())).hasNextProperty(IRI.create(os.ObjectPropertyHasNextIRI())).hasContentsProperty(IRI.create(os.ObjectPropertyHasContentsIRI())).sequenceType(os.type());
        context.getConverterResolver().resolveConverter(property, propertyAttributes).ifPresent(builder::converter);
        return builder.build();
    }

    private void registerTypeReference(Attribute<X, ?> attribute) {
        final Class<?> type = attribute.isCollection() ? ((PluralAttribute<X, ?, ?>) attribute).getBindableJavaType() : attribute.getJavaType();
        if (metamodelBuilder.hasManagedType(type)) {
            metamodelBuilder.registerTypeReference(type, et.getJavaType());
        }
    }

    private boolean tryProcessIdentifierField(Field field) {
        final Id id = field.getAnnotation(Id.class);
        if (id == null) {
            return false;
        }
        mappingValidator.validateIdentifierType(field.getType());
        et.setIdentifier(new IRIIdentifierImpl<>(et, field, id.generated()));
        return true;
    }

    private static boolean isAspectIntegrationField(Field field) {
        // AspectJ integration fields cannot be declared transitive (they're generated by the AJC), so we have to
        // skip them manually
        return field.getType().equals(BeanListenerAspect.Manageable.class);
    }

    private static class InferenceInfo {
        private final boolean inferred;
        private final boolean includeExplicit;

        InferenceInfo(Inferred inferredAnnotation) {
            this.inferred = inferredAnnotation != null;
            this.includeExplicit = inferredAnnotation == null || inferredAnnotation.includeExplicit();
        }
    }
}
