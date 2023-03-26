package ceri.serial.jna;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import com.sun.jna.Union;
import ceri.common.data.IntField;
import ceri.common.function.Accessor;
import ceri.common.function.ObjByteConsumer;
import ceri.common.function.ObjShortConsumer;
import ceri.common.function.ToByteFunction;
import ceri.common.function.ToShortFunction;

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
		return adapt(u -> u.setType(name), accessor);
	}

	/**
	 * Accessor for a typed field. Calls autoRead() on get, as typed fields are not loaded before
	 * setType().
	 */
	public static <U extends Union, T> Accessor.Typed<U, T> of(Class<?> cls,
		Accessor.Typed<U, T> accessor) {
		return adapt(u -> u.setType(cls), accessor);
	}

	/**
	 * Accessor for an unsigned byte field.
	 */
	public static <U extends Union> IntField.Typed<U> ofUbyte(ToByteFunction<U> getFn,
		ObjByteConsumer<U> setFn) {
		return ofInt(byte.class, IntField.typedUbyte(getFn, setFn));
	}

	/**
	 * Accessor for an unsigned short field.
	 */
	public static <U extends Union> IntField.Typed<U> ofUshort(
		ToShortFunction<U> getFn, ObjShortConsumer<U> setFn) {
		return ofInt(short.class, IntField.typedUshort(getFn, setFn));
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> IntField.Typed<U> ofInt(ToIntFunction<U> getFn,
		ObjIntConsumer<U> setFn) {
		return ofInt(int.class, getFn, setFn);
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> IntField.Typed<U> ofInt(String name, ToIntFunction<U> getFn,
		ObjIntConsumer<U> setFn) {
		return ofInt(name, IntField.typed(getFn, setFn));
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> IntField.Typed<U> ofInt(Class<?> cls, ToIntFunction<U> getFn,
		ObjIntConsumer<U> setFn) {
		return ofInt(cls, IntField.typed(getFn, setFn));
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> IntField.Typed<U> ofInt(String name,
		IntField.Typed<U> accessor) {
		return adaptInt(u -> u.setType(name), accessor);
	}

	/**
	 * Accessor for an int field.
	 */
	public static <U extends Union> IntField.Typed<U> ofInt(Class<?> cls,
		IntField.Typed<U> accessor) {
		return adaptInt(u -> u.setType(cls), accessor);
	}

	private static <U extends Union, T> Accessor.Typed<U, T> adapt(Consumer<U> setTypeFn,
		Accessor.Typed<U, T> accessor) {
		return Accessor.typed(u -> {
			setTypeFn.accept(u);
			u.autoRead();
			return accessor.get(u);
		}, (u, t) -> {
			setTypeFn.accept(u);
			accessor.set(u, t);
		});
	}

	private static <U extends Union> IntField.Typed<U> adaptInt(Consumer<U> setTypeFn,
		IntField.Typed<U> accessor) {
		return IntField.typed(accessor.getFn, (u, i) -> {
			setTypeFn.accept(u); // only needed for setter
			accessor.set(u, i);
		});
	}

}
