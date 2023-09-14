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
package cz.cvut.kbss.jopa.environment;

import cz.cvut.kbss.jopa.model.annotations.*;

import java.net.URI;
import java.util.Set;

@OWLClass(iri = Vocabulary.c_Person)
public class Person {

    @Id
    private URI uri;

    @OWLDataProperty(iri = Vocabulary.p_p_username)
    private String username;

    @OWLDataProperty(iri = Vocabulary.p_p_gender)
    private String gender;

    @OWLDataProperty(iri = Vocabulary.p_p_age)
    private Integer age;

    @OWLObjectProperty(iri = Vocabulary.p_p_hasPhone)
    private Phone phone;

    @Types
    private Set<String> types;
}