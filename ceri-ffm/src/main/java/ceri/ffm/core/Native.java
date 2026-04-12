package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.Buffer;
import java.util.Map;
import ceri.common.collect.Maps;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Struct;

public class Native {
	private static final Map<Class<?>, Class<?>> PROMOTIONS = promotions();
	/** The native linker. */
	public static final Linker LINKER = Linker.nativeLinker();
	/** The default symbol lookup. */
	public static final SymbolLookup LOOKUP = LINKER.defaultLookup();

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
	 * Alignment support.
	 */
	public static class Align {
		/** Indicates byte alignment is unspecified. */
		public static final long UNSPECIFIED = -1;
		/** Indicates natural byte alignment. */
		public static final long NATURAL = 0;
		/** Indicates no byte alignment. */
		public static final long NONE = 1;

		private Align() {}
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
	 * Supported types.
	 */
	public enum Kind {
		primitive,
		boxed,
		intType,
		string,
		buffer,
		struct,
		union,
		pointer, // typed (native types)
		pointerType,
		funcPointer; // typed (any)

		private static final Map<Class<?>, Kind> MAP = map();

		/**
		 * Type analysis result including original type and array breakdown.
		 */
		public record Spec(Kind kind, Generics.Typed typed, Generics.Array array) {
			public static final Spec NULL =
				new Spec(null, Generics.Typed.NULL, Generics.Array.NULL);
			public static final Spec VOID =
				new Spec(null, Generics.Typed.VOID, Generics.Array.NULL);

			/**
			 * Returns true if the type is primitive and not an array.
			 */
			public boolean isPrimitive() {
				return kind() == Kind.primitive && !array().isArray();
			}
			
			/**
			 * Returns the component class type, or class type if not an array.
			 */
			public <T> Class<T> component() {
				return Reflect.unchecked(array().cls());
			}
		}

		public static Spec validSpec(Generics.Typed typed) {
			var spec = spec(typed);
			if (spec.kind() != null) return spec;
			throw Exceptions.illegalArg("Unsupported type: " + typed);
		}

		public static Spec validSpec(Class<?> cls) {
			return validSpec(Generics.Typed.of(cls));
		}

		public static Spec spec(Generics.Typed typed) {
			if (typed == null) return Spec.NULL;
			var array = typed.array();
			var kind = fromComponent(array.component());
			return new Spec(kind, typed, array);
		}

		public static Spec spec(Class<?> cls) {
			return spec(Generics.Typed.of(cls));
		}

		private static Kind fromComponent(Generics.Typed component) {
			if (component == null) return null;
			var cls = component.cls();
			var kind = MAP.get(cls); // primitive, boxed, string
			if (kind != null) return kind;
			if (IntType.class.isAssignableFrom(cls)) return intType;
			if (Buffer.class.isAssignableFrom(cls)) return buffer;
			if (Struct.class.isAssignableFrom(cls)) return struct;
			// union TBD
			if (Pointer.class.isAssignableFrom(cls)) {
				var t = component.type(0);
				return t.isNull() || Generics.Typed.VOID.equals(t) || t.isUnbounded()
					|| fromComponent(t.array().component()) != null ? pointer : null;
			}
			// pointer type TBD
			// func pointer TBD
			return null;
		}

		private static Map<Class<?>, Kind> map() {
			var b = Maps.Builder.<Class<?>, Kind>of();
			b.putKeys(primitive, Reflect.PRIMITIVES);
			b.putKeys(boxed, Reflect.BOXED);
			b.putKeys(string, String.class);
			return b.wrap();
		}
	}

	public static final Pointer<?> p0 = null;
	public static final Pointer<int[]> p1 = null;
	public static final Pointer<Pointer<?>> p2 = null;

	public static void main(String[] args) {
		var t0 = Generics.typed(Reflect.publicField(Native.class, "p0"));
		var t1 = Generics.typed(Reflect.publicField(Native.class, "p1"));
		var t2 = Generics.typed(Reflect.publicField(Native.class, "p2"));
		System.out.println(t0 + " => " + Kind.spec(t0));
		System.out.println(t1 + " => " + Kind.spec(t1));
		System.out.println(t2 + " => " + Kind.spec(t2));
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
		return Maps.Builder.of(Maps.<Class<?>, Class<?>>of())
			.putKeys(int.class, boolean.class, byte.class, short.class)
			.putKeys(double.class, float.class)
			.putKeys(Integer.class, Boolean.class, Byte.class, Short.class)
			.putKeys(Double.class, Float.class).wrap();
	}
}
