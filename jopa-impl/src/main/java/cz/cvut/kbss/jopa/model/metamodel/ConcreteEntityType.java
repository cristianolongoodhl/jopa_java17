/*
 * Copyright (C) 2023 Czech Technical University in Prague
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details. You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cz.cvut.kbss.jopa.model.metamodel;

import cz.cvut.kbss.jopa.model.IRI;

/**
 * Instances of the type ConcreteEntityType represent entity
 * types which are can be directly instantiated while loading from storage.
 *
 *
 * @param <X> Entity type being represented by this instance
 */
public class ConcreteEntityType<X> extends IdentifiableEntityType<X> {

    public ConcreteEntityType(String name, Class<X> javaType, IRI iri) {
        super(name, javaType, iri);
    }

    @Override
    public boolean isAbstract() {
        return false;
    }
}
