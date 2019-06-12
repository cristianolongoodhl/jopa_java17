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

import cz.cvut.kbss.jopa.exception.InvalidFieldMappingException;
import cz.cvut.kbss.jopa.model.annotations.LexicalForm;
import cz.cvut.kbss.jopa.model.annotations.Properties;
import cz.cvut.kbss.jopa.model.annotations.Types;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;

class FieldMappingValidatorTest {

    private FieldMappingValidator validator = new FieldMappingValidator();

    @Test
    void nonMapPropertiesFieldThrowsException() {
        assertThrows(InvalidFieldMappingException.class, () -> validator.validatePropertiesField(getField("values")));
    }

    private Field getField(String name) throws Exception {
        return InvalidClass.class.getDeclaredField(name);
    }

    @Test
    void rawPropertiesMapThrowsException() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validatePropertiesField(getField("rawProperties")));
    }

    @Test
    void propertiesFieldRequiresValueTypeToBeSet() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validatePropertiesField(getField("propertiesWithIntegerValue")));
    }

    @Test
    void propertiesFieldWithInvalidKeyTypeThrowsException() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validatePropertiesField(getField("propertiesWithInvalidKey")));
    }

    @Test
    void validPropertiesFieldPassesValidation() throws Exception {
        validator.validatePropertiesField(getField("validProperties"));
    }

    @Test
    void nonSetTypesFieldThrowsException() {
        assertThrows(InvalidFieldMappingException.class, () -> validator.validateTypesField(getField("typesList")));
    }

    @Test
    void rawTypesSetThrowsException() {
        assertThrows(InvalidFieldMappingException.class, () -> validator.validateTypesField(getField("rawTypes")));
    }

    @Test
    void invalidTypesValueTypeThrowsException() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validateTypesField(getField("invalidValueTypes")));
    }

    @Test
    void setOfUrisIsValidTypesField() throws Exception {
        validator.validateTypesField(getField("validTypes"));
    }

    @Test
    void uriIsValidIdentifierField() throws Exception {
        validator.validateIdentifierType(getField("validUriIdentifier").getType());
    }

    @Test
    void urlIsValidIdentifierField() throws Exception {
        validator.validateIdentifierType(getField("validUrlIdentifier").getType());
    }

    @Test
    void stringIsValidIdentifierField() throws Exception {
        validator.validateIdentifierType(getField("validStringIdentifier").getType());
    }

    @Test
    void invalidIdentifierTypeThrowsException() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validateIdentifierType(getField("invalidIdentifier").getType()));
    }

    @Test
    void lexicalFormAnnotationIsValidOnStringField() throws Exception {
        validator.validateLexicalFormField(getField("validLexicalForm"));
    }

    @Test
    void lexicalFormAnnotationIsInvalidOnNonStringField() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validateLexicalFormField(getField("invalidLexicalForm")));
    }

    @Test
    void validateDataPropertyFieldInvokesLexicalFormValidation() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validateDataPropertyField(getField("invalidLexicalForm")));
    }

    @Test
    void validateAnnotationPropertyFieldInvokesLexicalFormValidation() {
        assertThrows(InvalidFieldMappingException.class,
                () -> validator.validateAnnotationPropertyField(getField("invalidLexicalForm")));
    }

    @SuppressWarnings("unused")
    private static final class InvalidClass {

        // Properties tests

        @Properties
        private Set<String> values;

        @Properties
        private Map rawProperties;

        @Properties
        private Map<String, Integer> propertiesWithIntegerValue;

        @Properties
        private Map<Long, Set<String>> propertiesWithInvalidKey;

        @Properties
        private Map<URI, Set<Object>> validProperties;

        // Types tests

        @Types
        private List<String> typesList;

        @Types
        private Set rawTypes;

        @Types
        private Set<Integer> invalidValueTypes;

        @Types
        private Set<URI> validTypes;

        URI validUriIdentifier;

        URL validUrlIdentifier;

        String validStringIdentifier;

        Integer invalidIdentifier;

        @LexicalForm
        private String validLexicalForm;

        @LexicalForm
        private Integer invalidLexicalForm;
    }
}