package ceri.log.process.parse;

import ceri.common.util.BasicUtil;

public interface Named extends Value {

	String name();
	
	static Named asNamed(Value value) {
		return asNamed(Named.class, value);
	}
	
	static <T extends Named> T asNamed(Class<T> cls, Value value) {
		return BasicUtil.castOrNull(cls, value);
	}
	
}
