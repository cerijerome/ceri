package ceri.jna.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import com.sun.jna.Union;
import ceri.common.data.ValueField;
import ceri.common.function.Accessor;

/**
 * Methods to provide access to union fields, automatically setting type for get and set.
 */
public class UnionField {

	private UnionField() {}

	/**
	 * Accessor for a typed field. Calls autoRead() on get, as typed fields are not loaded before
	 * setType().
	 */
	public static <U extends Union, T> Accessor.Typed<U, T> of(String name, Function<U, T> getFn,
		BiConsumer<U, T> setFn) {
		return of(name, Accessor.typed(getFn, setFn));
	}

	/**
	 * Accessor for a typed field. Calls autoRead() on get, as typed fields are not loaded before
	 * setType().
	 */
	public static <U extends Union, T> Accessor.Typed<U, T> of(Class<?> cls, Function<U, T> getFn,
		BiConsumer<U, T> setFn) {
		return of(cls, Accessor.typed(getFn, setFn));
	}

	/**
	 * Accessor for a typed field. Calls autoRead() on get, as typed fields are not loaded before
	 * setType().
	 */
	public static <U extends Union, T> Accessor.Typed<U, T> of(String name,
		Accessor.Typed<U, T> accessor) {
		return Accessor.typed(u -> {
			u.setType(name);
			u.autoRead();
			return accessor.get(u);
		}, (u, t) -> {
			u.setType(name);
			accessor.set(u, t);
		});
	}

	/**
	 * Accessor for a typed field. Calls autoRead() on get, as typed fields are not loaded before
	 * setType().
	 */
	public static <U extends Union, T> Accessor.Typed<U, T> of(Class<?> cls,
		Accessor.Typed<U, T> accessor) {
		return Accessor.typed(u -> {
			u.setType(cls);
			u.autoRead();
			return accessor.get(u);
		}, (u, t) -> {
			u.setType(cls);
			accessor.set(u, t);
		});
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> ValueField.Typed<RuntimeException, U> of(String name,
		ValueField.Typed<RuntimeException, U> accessor) {
		return ValueField.Typed.of(accessor.getFn, (u, i) -> {
			u.setType(name); // only needed for setter
			accessor.set(u, i);
		});
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> ValueField.Typed<RuntimeException, U> of(Class<?> cls,
		ValueField.Typed<RuntimeException, U> accessor) {
		return ValueField.Typed.of(accessor.getFn, (u, i) -> {
			u.setType(cls); // only needed for setter
			accessor.set(u, i);
		});
	}
}
