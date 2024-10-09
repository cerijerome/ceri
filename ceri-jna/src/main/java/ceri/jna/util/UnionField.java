package ceri.jna.util;

import static ceri.common.math.MathUtil.uint;
import com.sun.jna.Union;
import ceri.common.data.Field;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionObjIntConsumer;
import ceri.common.function.ExceptionObjLongConsumer;
import ceri.common.function.ExceptionToIntFunction;
import ceri.common.function.ExceptionToLongFunction;

/**
 * Methods to provide access to union fields, automatically setting type for get and set.
 */
public class UnionField {

	private UnionField() {}

	/**
	 * Accessor for a union field. Sets field type, and auto-reads field on get.
	 */
	public static <E extends Exception, U extends Union, T> Field<E, U, T> of(String name,
		ExceptionFunction<E, U, T> getter, ExceptionBiConsumer<E, U, T> setter) {
		return Field.of(getter == null ? null : u -> {
			u.setType(name);
			if (u.getAutoRead()) u.readField(name);
			return getter.apply(u);
		}, setter == null ? null : (u, t) -> {
			u.setType(name);
			setter.accept(u, t);
		});
	}

	/**
	 * Create an instance with getter and setter for an unsigned int field.
	 */
	public static <E extends Exception, U extends Union> Field.Long<E, U> ofUint(String name,
		ExceptionToIntFunction<E, U> getter, ExceptionObjIntConsumer<E, U> setter) {
		return ofLong(name, getter == null ? null : t -> uint(getter.applyAsInt(t)),
			setter == null ? null : (t, v) -> setter.accept(t, (int) v));
	}

	/**
	 * Accessor for a union long field. Sets field type as needed.
	 */
	public static <E extends Exception, U extends Union> Field.Long<E, U> ofLong(String name,
		ExceptionToLongFunction<E, U> getter, ExceptionObjLongConsumer<E, U> setter) {
		return Field.ofLong(getter, setter == null ? null : (u, t) -> {
			u.setType(name); // only required for set
			setter.accept(u, t);
		});
	}

}
