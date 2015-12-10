package cz.cvut.kbss.jopa.oom;

import cz.cvut.kbss.jopa.model.descriptors.Descriptor;
import cz.cvut.kbss.jopa.model.metamodel.EntityType;
import cz.cvut.kbss.jopa.model.metamodel.ListAttribute;
import cz.cvut.kbss.ontodriver.descriptor.ListDescriptor;
import cz.cvut.kbss.ontodriver.descriptor.ListValueDescriptor;
import cz.cvut.kbss.ontodriver.model.Axiom;

import java.util.List;

abstract class ListPropertyStrategy<L extends ListDescriptor, V extends ListValueDescriptor, X>
        extends PluralObjectPropertyStrategy<X> {

    protected final ListAttribute<? super X, ?> listAttribute;

    public ListPropertyStrategy(EntityType<X> et, ListAttribute<? super X, ?> att, Descriptor descriptor,
                                EntityMappingHelper mapper) {
        super(et, att, descriptor, mapper);
        this.listAttribute = att;
    }

    @Override
    protected void buildAxiomValuesFromInstance(X instance, AxiomValueGatherer valueBuilder)
            throws IllegalAccessException {
        final Object value = extractFieldValueFromInstance(instance);
        assert (value instanceof List || value == null);
        extractListValues((List<?>) value, instance, valueBuilder);
    }

    abstract L createListDescriptor(Axiom<?> ax);

    abstract V createListValueDescriptor(X instance);

    abstract <K> void extractListValues(List<K> list, X instance,
                                        AxiomValueGatherer valueBuilder);
}
