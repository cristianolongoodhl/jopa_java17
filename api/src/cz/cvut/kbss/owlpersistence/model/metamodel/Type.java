/**
 * Copyright (C) 2011 Czech Technical University in Prague
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

package cz.cvut.kbss.owlpersistence.model.metamodel;

import cz.cvut.kbss.owlpersistence.UnusedJPA;

/**
 * Instances of the type Type represent persistent object or attribute types.
 * 
 * @param <X>
 *            The type of the represented object or attribute
 */
public interface Type<X> {
	public static enum PersistenceType {
		ENTITY,

		@Deprecated
		@UnusedJPA
		/**
		 * Embeddable classes are logical groupings of state in Java that is "flattened" in RDB. Such mapping seems useless, or at lest of low priority, in ontologies. 
		 */
		EMBEDDABLE,

		@Deprecated
		@UnusedJPA
		/**
		 * Mapped superclasses help building entities with common state definition. They are not entities. For ontologies this requirement seems useless as creating hierarchies is a natural operation.
		 */
		MAPPED_SUPERCLASS,

		BASIC,
	}

	/**
	 * Return the persistence type.
	 * 
	 * @return persistence type
	 */
	PersistenceType getPersistenceType();

	/**
	 * Return the represented Java type.
	 * 
	 * @return Java type
	 */
	Class<X> getJavaType();
}
