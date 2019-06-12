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

import cz.cvut.kbss.jopa.model.annotations.LexicalForm;
import cz.cvut.kbss.jopa.oom.converter.ConverterWrapper;
import cz.cvut.kbss.jopa.oom.converter.EnumConverter;
import cz.cvut.kbss.jopa.oom.converter.ToLexicalFormConverter;

import java.lang.reflect.Field;
import java.util.Optional;

/**
 * Determines potential converters which may be used on a field.
 * <p>
 * Currently, only built-in converters for data and annotation property attributes are supported, but in the future,
 * custom converters should be also supported.
 */
class ConverterResolver {

    private final Converters converters;

    ConverterResolver(Converters converters) {
        this.converters = converters;
    }

    /**
     * Determines converter which should be used for transformation of values to and from the specified field.
     * <p>
     * Besides custom converters, the system supports a number of built-in converters, which ensure that e.g. widening conversion
     * or mapping to Java 8 Date/Time API is supported.
     *
     * @param field  The field for which converter should be determined
     * @param config Mapping configuration extracted during metamodel building
     * @return Possible converter instance to be used for transformation of values of the specified field.
     * Returns empty {@code Optional} if no suitable converter is found (or needed)
     */
    public Optional<ConverterWrapper<?, ?>> resolveConverter(Field field, PropertyAttributes config) {
        if (config.getPersistentAttributeType() == Attribute.PersistentAttributeType.OBJECT) {
            return Optional.empty();
        }
        final Class<?> attValueType = config.getType().getJavaType();
        if (attValueType.isEnum()) {
            return Optional.of(new EnumConverter(attValueType));
        }
        if (isLexicalFormField(field, config)) {
            return Optional.of(new ToLexicalFormConverter());
        }
        return converters.getConverter(attValueType);
    }

    private boolean isLexicalFormField(Field field, PropertyAttributes config) {
        return (config.getPersistentAttributeType() == Attribute.PersistentAttributeType.DATA ||
                config.getPersistentAttributeType() == Attribute.PersistentAttributeType.ANNOTATION) &&
                field.getAnnotation(
                        LexicalForm.class) != null;
    }
}
