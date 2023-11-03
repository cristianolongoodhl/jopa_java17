/*
 * JOPA
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package cz.cvut.kbss.jopa.model;

public final class JOPAPersistenceProperties extends PersistenceProperties {

    /**
     * Logical URI of the underlying ontology.
     */
    public static final String ONTOLOGY_URI_KEY = "cz.cvut.jopa.ontology.logicalUri";

    /**
     * Physical location of the underlying ontology storage.
     */
    public static final String ONTOLOGY_PHYSICAL_URI_KEY = "cz.cvut.jopa.ontology.physicalURI";

    /**
     * Class name of the OntoDriver implementation.
     */
    public static final String DATA_SOURCE_CLASS = "cz.cvut.jopa.dataSource.class";

    /**
     * Ontology language.
     */
    public static final String LANG = "cz.cvut.jopa.lang";

    /**
     * Whether a second level cache should be used.
     */
    public static final String CACHE_ENABLED = "cz.cvut.jopa.cache.enable";

    /**
     * Where the entity classes are located.
     */
    public static final String SCAN_PACKAGE = "cz.cvut.jopa.scanPackage";

    /**
     * Cached entity time to live. In seconds.
     */
    public static final String CACHE_TTL = "cz.cvut.jopa.cache.ttl";

    /**
     * How often should the cache be swept for dead entities. In seconds.
     */
    public static final String CACHE_SWEEP_RATE = "cz.cvut.jopa.cache.sweepRate";

    /**
     * Type of the second level cache. Currently supported are {@literal ttl} and {@literal lru}.
     */
    public static final String CACHE_TYPE = "cz.cvut.jopa.cache.type";

    /**
     * Capacity of the LRU second level cache.
     */
    public static final String LRU_CACHE_CAPACITY = "cz.cvut.jopa.cache.lru.capacity";

    /**
     * Disable integrity constraints validation on entity/field load.
     */
    public static final String DISABLE_IC_VALIDATION_ON_LOAD = "cz.cvut.jopa.ic.validation.disableOnLoad";

    /**
     * Indicates whether JOPA should prefer {@link MultilingualString} over {@link String} when target type is unclear
     * from context.
     * <p>
     * For example, when a field is of type {@link Object} and the value is language-tagged string, this setting will
     * influence the actual type set on the field.
     */
    public static final String PREFER_MULTILINGUAL_STRING = "cz.cvut.jopa.preferMultilingualString";

    /**
     * Name of a custom {@link cz.cvut.kbss.jopa.loaders.ClasspathScanner} implementation used to discover entity
     * classes.
     * <p>
     * The specified class must have a public no-arg constructor.
     */
    public static final String CLASSPATH_SCANNER_CLASS = "cz.cvut.jopa.classpathScanner";

    /**
     * Ignores removal of inferred values when entity state is merged into the persistence context.
     * <p>
     * Normally, attempts to remove inferred values lead to an {@link cz.cvut.kbss.jopa.exceptions.InferredAttributeModifiedException}.
     * But when an entity is merged into the persistence context, it may be merged without inferred attribute values
     * because they were not accessible to the client application. Instead of requiring the application to reconstruct
     * them, this configuration allows to ignore such changes completely.
     */
    public static final String IGNORE_INFERRED_VALUE_REMOVAL_ON_MERGE = "cz.cvut.kbss.jopa.ignoreInferredValueRemovalOnMerge";

    private JOPAPersistenceProperties() {
        throw new AssertionError();
    }
}
