package ceri.common.util;

public class NameValue<T> extends KeyValue<String, T> {

	public static <T> NameValue<T> of(String name, T value) {
		return new NameValue<>(name, value);
	}

	protected NameValue(String name, T value) {
		super(name, value);
	}

}
