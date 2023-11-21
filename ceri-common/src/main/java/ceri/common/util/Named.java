package ceri.common.util;

import ceri.common.reflect.ReflectUtil;

public interface Named {

	/**
	 * Provides the instance name. By default this is the simple class name and system hash.
	 */
	default String name() {
		return name(this, null);
	}

	static String name(Object obj, String name) {
		if (name != null) return name;
		return ReflectUtil.className(obj) + ReflectUtil.hashId(obj);
	}

}
