package ceri.serial.jna;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;
import com.sun.jna.Union;
import ceri.common.data.IntAccessor;
import ceri.common.function.Accessor;
import ceri.common.function.ObjByteConsumer;
import ceri.common.function.ObjShortConsumer;
import ceri.common.function.ToByteFunction;
import ceri.common.function.ToShortFunction;

/**
 * Methods to provide access to union fields, automatically setting type for get and set.
 */
public class UnionAccessor {

	private UnionAccessor() {}

	public static <U extends Union, T> Accessor.Typed<U, T> of(String name, Function<U, T> getFn,
		BiConsumer<U, T> setFn) {
		return of(name, Accessor.typed(getFn, setFn));
	}

	public static <U extends Union, T> Accessor.Typed<U, T> of(Class<?> cls, Function<U, T> getFn,
		BiConsumer<U, T> setFn) {
		return of(cls, Accessor.typed(getFn, setFn));
	}

	public static <U extends Union, T> Accessor.Typed<U, T> of(String name,
		Accessor.Typed<U, T> accessor) {
		return adapt(u -> u.setType(name), accessor);
	}

	public static <U extends Union, T> Accessor.Typed<U, T> of(Class<?> cls,
		Accessor.Typed<U, T> accessor) {
		return adapt(u -> u.setType(cls), accessor);
	}

	public static <U extends Union> IntAccessor.Typed<U> ofUbyte(String name,
		ToByteFunction<U> getFn, ObjByteConsumer<U> setFn) {
		return ofInt(name, IntAccessor.typedUbyte(getFn, setFn));
	}

	public static <U extends Union> IntAccessor.Typed<U> ofUbyte(Class<?> cls,
		ToByteFunction<U> getFn, ObjByteConsumer<U> setFn) {
		return ofInt(cls, IntAccessor.typedUbyte(getFn, setFn));
	}

	public static <U extends Union> IntAccessor.Typed<U> ofUshort(String name,
		ToShortFunction<U> getFn, ObjShortConsumer<U> setFn) {
		return ofInt(name, IntAccessor.typedUshort(getFn, setFn));
	}

	public static <U extends Union> IntAccessor.Typed<U> ofUshort(Class<?> cls,
		ToShortFunction<U> getFn, ObjShortConsumer<U> setFn) {
		return ofInt(cls, IntAccessor.typedUshort(getFn, setFn));
	}

	public static <U extends Union> IntAccessor.Typed<U> ofInt(String name, ToIntFunction<U> getFn,
		ObjIntConsumer<U> setFn) {
		return ofInt(name, IntAccessor.typed(getFn, setFn));
	}

	public static <U extends Union> IntAccessor.Typed<U> ofInt(Class<?> cls, ToIntFunction<U> getFn,
		ObjIntConsumer<U> setFn) {
		return ofInt(cls, IntAccessor.typed(getFn, setFn));
	}

	public static <U extends Union> IntAccessor.Typed<U> ofInt(String name,
		IntAccessor.Typed<U> accessor) {
		return adaptInt(u -> u.setType(name), accessor);
	}

	public static <U extends Union> IntAccessor.Typed<U> ofInt(Class<?> cls,
		IntAccessor.Typed<U> accessor) {
		return adaptInt(u -> u.setType(cls), accessor);
	}

	private static <U extends Union, T> Accessor.Typed<U, T> adapt(Consumer<U> setTypeFn,
		Accessor.Typed<U, T> accessor) {
		return Accessor.typed(u -> {
			setTypeFn.accept(u);
			return accessor.get(u);
		}, (u, t) -> {
			setTypeFn.accept(u);
			accessor.set(u, t);
		});
	}

	private static <U extends Union> IntAccessor.Typed<U> adaptInt(Consumer<U> setTypeFn,
		IntAccessor.Typed<U> accessor) {
		return IntAccessor.typed(accessor.getFn, (u, i) -> {
			setTypeFn.accept(u); // only needed for setter
			accessor.set(u, i);
		});
	}

}
