package cz.cvut.kbss.ontodriver_new;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import cz.cvut.kbss.jopa.utils.ErrorUtils;
import cz.cvut.kbss.ontodriver_new.model.Assertion;
import cz.cvut.kbss.ontodriver_new.model.NamedResource;
import cz.cvut.kbss.ontodriver_new.model.Value;

public class MutationAxiomDescriptor extends AxiomDescriptor {

	private final Map<Assertion, List<Value<?>>> values;

	public MutationAxiomDescriptor(NamedResource subject) {
		super(subject);
		this.values = new HashMap<>();
	}

	/**
	 * Adds a new value for the specified assertion. </p>
	 * 
	 * @param assertion
	 *            The assertion
	 * @param value
	 *            The value to add
	 * @return This descriptor
	 * @throws NullPointerException
	 *             if either of the arguments is {@code null}
	 */
	public MutationAxiomDescriptor addAssertionValue(Assertion assertion, Value<?> value) {
		Objects.requireNonNull(assertion, ErrorUtils.constructNPXMessage("assertion"));
		Objects.requireNonNull(value, ErrorUtils.constructNPXMessage("value"));

		final List<Value<?>> assertionValues = getAssertionList(assertion);
		assertionValues.add(value);
		return this;
	}

	private List<Value<?>> getAssertionList(Assertion assertion) {
		assert assertion != null;
		if (!values.containsKey(assertion)) {
			values.put(assertion, new ArrayList<Value<?>>());
		}
		return values.get(assertion);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + values.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		MutationAxiomDescriptor other = (MutationAxiomDescriptor) obj;
		if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(super.toString());
		sb.append("property values: ").append(values);
		return sb.toString();
	}
}
