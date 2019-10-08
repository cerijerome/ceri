package ceri.log.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Parameters {
	private final List<String> list = new ArrayList<>();
	private final List<String> readOnly = Collections.unmodifiableList(list);

	public static Parameters of(String... parameters) {
		return new Parameters().add(parameters);
	}

	public static Parameters of(Collection<String> parameters) {
		return new Parameters().add(parameters);
	}

	public static Parameters of(Parameters parameters) {
		return new Parameters().add(parameters);
	}

	public Parameters add(String... parameters) {
		Collections.addAll(list, parameters);
		return this;
	}

	public Parameters add(Number number) {
		list.add(String.valueOf(number));
		return this;
	}

	public Parameters add(Collection<String> parameters) {
		list.addAll(parameters);
		return this;
	}

	public Parameters add(Parameters parameters) {
		list.addAll(parameters.list);
		return this;
	}

	public List<String> list() {
		return readOnly;
	}

}
