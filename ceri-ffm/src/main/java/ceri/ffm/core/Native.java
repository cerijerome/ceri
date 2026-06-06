package ceri.ffm.core;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import ceri.common.collect.Maps;
import ceri.common.concurrent.Lazy;
import ceri.common.except.Exceptions;
import ceri.common.io.Buffers;
import ceri.common.reflect.Generics;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;
import ceri.ffm.type.IntType;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.RawPointer;
import ceri.ffm.type.Struct;
import ceri.ffm.type.Union;

public class Native {
	private static final Pattern INNER_REGEX = Pattern.compile("\\(.*\\)");
	private static final Lazy.ForClass<Kind> KINDS = Lazy.forClass(c -> kind(c));
	private static final Map<Class<?>, Class<?>> PROMOTIONS = promotions();
	/** The native linker. */
	public static final Linker LINKER = Linker.nativeLinker();
	/** The default symbol lookup. */
	public static final SymbolLookup SYMBOLS = LINKER.defaultLookup();

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
		struct,
		union,
		pointer,
		primitivePointer,
		pointerType,
		functionPointer,
		string,
		buffer;
	}

	/**
	 * Type analysis result providing generic type, array breakdown, and native type category.
	 */
	public static class Spec {
		public static final Spec NULL = new Spec(null, Generics.Typed.NULL, null);
		public static final Spec VOID = new Spec(null, Generics.Typed.VOID, null);
		private final Kind kind;
		private final Generics.Typed typed;
		private final Generics.Array array;

		/**
		 * Extracts the generic type with native category, throws an exception if not valid.
		 */
		public static Spec valid(Generics.Typed typed) {
			var spec = of(typed);
			if (spec.kind() != null) return spec;
			throw Exceptions.illegalArg("Unsupported type: " + typed);
		}

		/**
		 * Extracts the generic type with native category, returns null if not valid.
		 */
		public static Spec of(Generics.Typed typed) {
			if (Generics.Typed.isNull(typed)) return Spec.NULL;
			if (Generics.Typed.VOID.equals(typed)) return Spec.VOID;
			var array = typed.array();
			var kind = kindFromComponent(array.component());
			return new Spec(kind, typed, array);
		}

		private Spec(Kind kind, Generics.Typed typed, Generics.Array array) {
			this.kind = kind;
			this.typed = typed;
			this.array = array != null ? array : typed.array();
		}

		/**
		 * Returns the native type category.
		 */
		public Kind kind() {
			return kind;
		}

		/**
		 * Returns the generic type.
		 */
		public Generics.Typed typed() {
			return typed;
		}

		/**
		 * Returns true if kind is valid.
		 */
		public boolean isValid() {
			return kind() != null;
		}

		/**
		 * Returns true if the type is a non-array primitive.
		 */
		public boolean isPrimitive() {
			return kind() == Kind.primitive && !isArray();
		}

		/**
		 * Returns true if the type is an array.
		 */
		public boolean isArray() {
			return dimensions() > 0;
		}

		/**
		 * Returns the type broken down as component and dimensions.
		 */
		public Generics.Array array() {
			return array;
		}

		/**
		 * Returns the component class type, or class type if not an array.
		 */
		public <T> Class<T> component() {
			return Reflect.unchecked(array.cls());
		}

		/**
		 * Returns the (multi-)array dimension count or zero if not an array.
		 */
		public int dimensions() {
			return array.dimensions();
		}

		@Override
		public int hashCode() {
			return typed().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Spec s) && Objects.equals(typed(), s.typed());
		}

		@Override
		public String toString() {
			return kind + ":" + typed;
		}
	}

	/**
	 * Returns true if the type can be treated as void.
	 */
	public static boolean isVoid(Generics.Typed typed) {
		if (typed == null) return false;
		return typed.isNull() || typed.isVoid() || typed.isUnbounded();
	}

	/**
	 * Find native call by name from the default lookup.
	 */
	public static MemorySegment find(String method) {
		return SYMBOLS.find(method).orElseThrow();
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

	/**
	 * Wraps the type string if its non-wrapped parts are separated by spaces.
	 */
	public static String wrap(String type) {
		if (Strings.isEmpty(type)) return type;
		var outer = INNER_REGEX.matcher(type).replaceFirst("");
		if (outer.contains(" ")) return '(' + type + ')';
		return type;
	}
	
	// support

	private static Kind kindFromComponent(Generics.Typed component) {
		if (component == null) return null;
		Class<?> cls = component.cls();
		var kind = KINDS.get(cls);
		if (kind != null) return kind;
		if (cls == Pointer.class) {
			var t = component.type(0);
			if (isVoid(t)) return Kind.pointer;
			return kindFromComponent(t.components()) != null ? Kind.pointer : null;
		}
		// pointer type TBD
		// function pointer TBD
		return null;
	}

	private static Kind kind(Class<?> cls) {
		if (Reflect.PRIMITIVES.contains(cls)) return Kind.primitive;
		if (Reflect.BOXED.contains(cls)) return Kind.boxed;
		if (IntType.class.isAssignableFrom(cls)) return Kind.intType;
		if (Struct.class.isAssignableFrom(cls)) return Kind.struct;
		if (Union.class.isAssignableFrom(cls)) return Kind.union;
		if (cls == Pointer.class) return null; // needs type checks
		if (RawPointer.class.isAssignableFrom(cls)) return Kind.pointer;
		if (cls == String.class) return Kind.string;
		if (Buffers.BASE_TYPES.contains(cls)) return Kind.buffer;
		return null;
	}

	private static Map<Class<?>, Class<?>> promotions() {
		return Maps.Builder.<Class<?>, Class<?>>of()
			.putKeys(int.class, boolean.class, byte.class, short.class)
			.putKeys(double.class, float.class)
			.putKeys(Integer.class, Boolean.class, Byte.class, Short.class)
			.putKeys(Double.class, Float.class).wrap();
	}
}
