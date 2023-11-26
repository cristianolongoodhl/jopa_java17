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
package cz.cvut.kbss.ontodriver.jena.list;

import cz.cvut.kbss.ontodriver.descriptor.ReferencedListDescriptor;
import cz.cvut.kbss.ontodriver.descriptor.ReferencedListValueDescriptor;
import cz.cvut.kbss.ontodriver.jena.connector.StorageConnector;
import cz.cvut.kbss.ontodriver.jena.util.JenaUtils;
import cz.cvut.kbss.ontodriver.model.Axiom;
import cz.cvut.kbss.ontodriver.model.NamedResource;
import cz.cvut.kbss.ontodriver.model.Value;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.apache.jena.rdf.model.ResourceFactory.*;

public class ReferencedListHandler {

    private final StorageConnector connector;

    public ReferencedListHandler(StorageConnector connector) {
        this.connector = connector;
    }

    List<Axiom<NamedResource>> loadList(ReferencedListDescriptor descriptor) {
        final List<Axiom<NamedResource>> result = new ArrayList<>();
        final AbstractListIterator it = new ReferencedListIterator(descriptor, connector);
        while (it.hasNext()) {
            result.add(it.nextAxiom());
        }
        return result;
    }

    <V> void persistList(ReferencedListValueDescriptor<V> descriptor) {
        final List<V> values = descriptor.getValues();
        if (values.isEmpty()) {
            return;
        }
        Resource owner = createResource(descriptor.getListOwner().getIdentifier().toString());
        appendNewNodes(descriptor, 0, owner);
    }

    <V> void appendNewNodes(ReferencedListValueDescriptor<V> descriptor, int i, Resource lastNode) {
        assert lastNode != null;
        final List<Statement> toAdd = new ArrayList<>((descriptor.getValues().size() - i) * 2);
        final Property hasList = createProperty(descriptor.getListProperty().getIdentifier().toString());
        final Property hasNext = createProperty(descriptor.getNextNode().getIdentifier().toString());
        final Property hasContent = createProperty(descriptor.getNodeContent().getIdentifier().toString());
        final String context = descriptor.getContext() != null ? descriptor.getContext().toString() : null;
        for (; i < descriptor.getValues().size(); i++) {
            lastNode =
                    appendNode(lastNode, descriptor.getValues().get(i), i == 0 ? hasList : hasNext, hasContent, context,
                            toAdd, descriptor);
        }
        connector.add(toAdd, context);
    }

    private <V> Resource appendNode(Resource previousNode, V value, Property link, Property hasContent,
                                String context, List<Statement> statements, ReferencedListValueDescriptor<V> descriptor) {
        final Resource node = generateNewListNode(descriptor.getListOwner().getIdentifier(), context);
        statements.add(createStatement(previousNode, link, node));
        statements.add(createStatement(node, hasContent, JenaUtils.valueToRdfNode(descriptor.getNodeContent(), new Value<>(value))));
        return node;
    }

    private Resource generateNewListNode(URI baseUri, String context) {
        Resource node;
        int index = 0;
        Collection<Statement> statements;
        do {
            node = createResource(baseUri.toString() + "-SEQ_" + index++);
            statements = connector
                    .find(node, null, null, context != null ? Collections.singleton(context) : Collections.emptySet());
        } while (!statements.isEmpty());
        return node;
    }

    <V> void updateList(ReferencedListValueDescriptor<V> descriptor) {
        final AbstractListIterator it = new ReferencedListIterator(descriptor, connector);
        int i = 0;
        while (it.hasNext() && i < descriptor.getValues().size()) {
            final V update = descriptor.getValues().get(i);
            final NamedResource existing = it.nextValue();
            if (!existing.equals(update)) {
                // TODO Replace with generic handling of an object, we do not know that it is a NamedResource
                it.replace(createResource(update.toString()));
            }
            i++;
        }
        removeObsoleteNodes(it);
        if (i < descriptor.getValues().size()) {
            appendNewNodes(descriptor, i, it.getCurrentNode());
        }
    }

    private static void removeObsoleteNodes(AbstractListIterator it) {
        while (it.hasNext()) {
            it.nextValue();
            it.removeWithoutReconnect();
        }
    }
}
