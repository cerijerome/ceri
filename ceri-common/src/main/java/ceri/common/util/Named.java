package ceri.common.util;

import ceri.common.reflect.ReflectUtil;

public interface Named {

	/**
	 * Provides the instance name. By default this is the simple class name and system hash.
	 */
	default String name() {
		return ReflectUtil.className(this) + ReflectUtil.hashId(this);
	}

}
