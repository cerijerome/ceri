package ceri.common.util;

import java.util.Comparator;
import ceri.common.function.Excepts.Predicate;
import ceri.common.function.Predicates;
import ceri.common.reflect.Reflect;
import ceri.common.text.StringUtil;

/**
 * Implemented by classes that provide a name.
 */
public interface Named {
	static Comparator<Named> COMPARATOR = Comparator.comparing(Named::name);

	/**
	 * Provides the instance name. By default this is the simple class name and system hash.
	 */
	default String name() {
		return name(this, null);
	}

	/**
	 * Provides a default name if the given name is null.
	 */
	static String name(Object obj, String name) {
		if (name != null) return name;
		return Reflect.nameHash(obj);
	}

	/**
	 * Provides name or null string.
	 */
	static String nameOf(Named named) {
		return named != null ? named.name() : StringUtil.NULL;
	}

	/**
	 * Predicate by name.
	 */
	static <E extends Exception, T extends Named> Predicate<E, T> by(Predicate<E, String> name) {
		return Predicates.testing(Named::name, name);
	}
}
