package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import ceri.common.collect.Maps;

public class Native {
	private static final Map<Class<?>, Class<?>> PROMOTIONS = promotions();
	/** The native linker. */
	public static final Linker LINKER = Linker.nativeLinker();
	/** The default symbol lookup. */
	public static final SymbolLookup LOOKUP = LINKER.defaultLookup();
	/** Native pointer size. */
	public static final int POINTER_BYTES = (int) ValueLayout.ADDRESS.byteSize();
	/** Indicates byte alignment is unspecified. */
	public static final long ALIGN_UNSPECIFIED = -1;
	/** Indicates natural byte alignment. */
	public static final long ALIGN_NATURAL = 0;

	/**
	 * Registry for native type sizes in bytes.
	 */
	// TODO: have system property overrides?
	public static class Size {
		private static final Map<String, Integer> sizes = Maps.concurrent();
		public static final int UNSPECIFIED = -1;
		public static final int BOOL = canonical("bool");
		public static final int CHAR = canonical("char");
		public static final int SHORT = canonical("short");
		public static final int INT = canonical("int");
		public static final int LONG = canonical("long");
		public static final int LONG_LONG = canonical("long long");
		public static final int FLOAT = canonical("float");
		public static final int DOUBLE = canonical("double");
		public static final int SIZE_T = canonical("size_t");
		public static final int WCHAR_T = canonical("wchar_t");
		public static final int VOID_P = canonical("void*");
		
		private Size() {}

		/**
		 * Looks up a registered size by name. Fails if not found.
		 */
		public static int lookup(String name) {
			return Maps.getOrThrow(sizes, name);
		}

		/**
		 * Registers a size by name. Fails if the size is already registered.
		 */
		public static int register(String name, int size) {
			Maps.put(Maps.Put.unique, sizes, name, size);
			return size;
		}

		/**
		 * Looks up and registers a canonical layout size. Returns unspecified if not found.
		 */
		public static int canonical(String name) {
			var layout = LINKER.canonicalLayouts().get(name);
			if (layout == null) return UNSPECIFIED;
			return register(name, (int) layout.byteSize());
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

	// support

	private static Map<Class<?>, Class<?>> promotions() {
		return Maps.Builder.of(Maps.<Class<?>, Class<?>>of())
			.putKeys(int.class, boolean.class, byte.class, short.class)
			.putKeys(double.class, float.class)
			.putKeys(Integer.class, Boolean.class, Byte.class, Short.class)
			.putKeys(Double.class, Float.class).wrap();
	}
}
