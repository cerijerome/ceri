package ceri.common.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import ceri.common.collect.Immutable;

/**
 * Convenience class for collecting command string parameters.
 */
public class Parameters {
	public static Parameters NULL = new Parameters(List.of());
	private final List<String> list;
	private final List<String> readOnly;

	public static Parameters of(Object value, Object... values) {
		return of().add(value).addAll(values);
	}

	public static Parameters ofAll(Object[] values) {
		return of().addAll(values);
	}

	public static Parameters ofAll(Collection<? extends Object> values) {
		return of().addAll(values);
	}

	public static Parameters ofAll(Parameters parameters) {
		return of().addAll(parameters);
	}

	public static Parameters of() {
		return new Parameters(new ArrayList<>());
	}

	private Parameters(List<String> list) {
		this.list = list;
		readOnly = Immutable.wrap(list);
	}

	/**
	 * Add objects, which are then converted by String.valueOf if not null.
	 */
	public Parameters add(Object value, Object... values) {
		return addValue(value).addAll(values);
	}

	/**
	 * Add an array of objects.
	 */
	public Parameters addAll(Object[] values) {
		for (Object value : values)
			addValue(value);
		return this;
	}

	/**
	 * Add a collection of objects.
	 */
	public Parameters addAll(Collection<? extends Object> values) {
		values.forEach(this::addValue);
		return this;
	}

	/**
	 * Add parameters.
	 */
	public Parameters addAll(Parameters parameters) {
		return addAll(parameters.list);
	}

	/**
	 * Read-only view of current parameters.
	 */
	public List<String> list() {
		return readOnly;
	}

	private Parameters addValue(Object value) {
		list.add(value == null ? null : String.valueOf(value));
		return this;
	}

	@Override
	public int hashCode() {
		return Objects.hash(list);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		return (obj instanceof Parameters other) && Objects.equals(list, other.list);
	}

	@Override
	public String toString() {
		return Processes.toString(list);
	}
}
