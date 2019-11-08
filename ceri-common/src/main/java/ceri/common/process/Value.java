package ceri.common.process;

import ceri.common.util.BasicUtil;

public interface Value {

	default <T extends Named> T asNamed(Class<T> cls) {
		return BasicUtil.castOrNull(cls, this);
	}
	
	public static interface Named extends Value {
		String name();
	}

	static Named asNamed(Value value) {
		return asNamed(Named.class, value);
	}

	static <T extends Named> T asNamed(Class<T> cls, Value value) {
		return value == null ? null : value.asNamed(cls);
	}
}
