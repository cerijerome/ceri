package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;

/**
 * Native basics.
 */
public class Native {
	private static final Map<Class<?>, Class<?>> PROMOTIONS = promotions();
	/** The native linker. */
	public static final Linker LINKER = Linker.nativeLinker();
	/** The default symbol lookup. */
	public static final SymbolLookup LOOKUP = LINKER.defaultLookup();

	/**
	 * Supported types.
	 */
	public enum Kind {
		NONE,
		PRIMITIVE,
		BOXED,
		INT_TYPE,
		STRUCT,
		UNION,
		POINTER,
		PRIMITIVE_POINTER,
		POINTER_TYPE,
		CALLBACK,
		STRING,
		BUFFER;
	}

	/**
	 * Represents an adapted value, with option to resolve the original value after changes.
	 */
	public interface Adapted<T> {
		/**
		 * Provides the adapted value.
		 */
		T value();

		/**
		 * Updates the original value from the adapted value, if changed.
		 */
		default void resolve() {}

		/**
		 * Returns an instance for an immutable value.
		 */
		static <T> Adapted<T> of(T value) {
			return () -> value;
		}

		/**
		 * Returns an instance for a value and resolving consumer.
		 */
		static <T> Adapted<T> of(T value, Functions.Consumer<T> resolver) {
			if (resolver == null) return of(value);
			return new Native.Adapted<>() {
				@Override
				public T value() {
					return value;
				}

				@Override
				public void resolve() {
					resolver.accept(value());
				}
			};
		}
	}

	/**
	 * Encapsulates an adapter between a local type (T) and a native type (R).
	 */
	public record Adapter<T, R>(Generics.Typed localType, Class<? extends R> nativeCls, R nativeDef,
		MemoryLayout layout, Functions.BiFunction<SegmentAllocator, T, Native.Adapted<R>> toNative,
		Functions.Function<R, T> toLocal) {
		public static final Adapter<?, ?> VOID = new Adapter<>(Generics.Typed.VOID, void.class,
			null, Layouts.EMPTY, (_, _) -> null, _ -> null);

		/**
		 * Returns the local class type.
		 */
		public Class<T> localCls() {
			return localType().cls();
		}

		/**
		 * Adapts the local value to its native value, with option to resolve the original value
		 * after changes.
		 */
		public Native.Adapted<R> toNative(SegmentAllocator allocator, T localValue) {
			return toNative().apply(allocator, localValue);
		}

		/**
		 * Adapts the native value to its local value.
		 */
		public T toLocal(R nativeValue) {
			return toLocal().apply(nativeValue);
		}

		@Override
		public final String toString() {
			return localType().toString();
		}
	}

	/**
	 * Known canonical layout names.
	 */
	public enum Canonical {
		BOOL("bool"),
		CHAR("char"),
		SHORT("short"),
		INT("int"),
		LONG("long"),
		LONG_LONG("long long"),
		FLOAT("float"),
		DOUBLE("double"),
		SIZE_T("size_t"),
		WCHAR_T("wchar_t"),
		VOID_P("void*");

		public final String name;

		private Canonical(String name) {
			this.name = name;
		}
	}

	/**
	 * Registry for native type sizes in bytes.
	 */
	public static class Size {
		private static final Map<String, Integer> sizes = Maps.concurrent();
		public static final int UNSPECIFIED = -1;

		static {
			LINKER.canonicalLayouts()
				.forEach((name, layout) -> register(name, (int) layout.byteSize()));
		}

		private Size() {}

		/**
		 * Looks up a registered size by canonical name. Fails if not found.
		 */
		public static int lookup(Canonical canonical) {
			return lookup(canonical.name);
		}

		/**
		 * Looks up a registered size by name. Fails if not found.
		 */
		public static int lookup(String name) {
			return Maps.getOrThrow(sizes, name);
		}

		public static int canonical(String name) {
			var layout = LINKER.canonicalLayouts().get(name);
			if (layout == null) return UNSPECIFIED;
			return register(name, (int) layout.byteSize());
		}

		/**
		 * Registers a size by name. Fails if the size is already registered.
		 */
		public static int register(String name, int size) {
			Maps.put(Maps.Put.unique, sizes, name, size);
			return size;
		}
	}

	/**
	 * Find native call by name from the default lookup.
	 */
	public static MemorySegment find(String method) {
		return LOOKUP.find(method).orElseThrow();
	}

	/**
	 * Creates a method handle from the default lookup.
	 */
	public static MethodHandle method(String name, FunctionDescriptor fdesc,
		Linker.Option... options) {
		return LINKER.downcallHandle(find(name), fdesc, options);
	}

	/**
	 * Modifies type to mirror c type promotion, such as with variadic args.
	 */
	public static Class<?> promote(Class<?> cls) {
		return PROMOTIONS.getOrDefault(cls, cls);
	}

	/**
	 * Looks up a canonical layout by name. Returns null if not found.
	 */
	public static <L extends MemoryLayout> L layout(String name) {
		return Reflect.unchecked(LINKER.canonicalLayouts().get(name));
	}

	// support

	private static Map<Class<?>, Class<?>> promotions() {
		return Maps.Builder.<Class<?>, Class<?>>of()
			.putKeys(int.class, boolean.class, byte.class, short.class)
			.putKeys(double.class, float.class)
			.putKeys(Integer.class, Boolean.class, Byte.class, Short.class)
			.putKeys(Double.class, Float.class).wrap();
	}
}
