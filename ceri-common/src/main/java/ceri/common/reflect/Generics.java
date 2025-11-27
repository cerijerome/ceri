package ceri.common.reflect;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Objects;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.text.Joiner;

/**
 * Support for accessing generic types.
 */
public class Generics {
	private static final Joiner GENERIC = Joiner.of("<", ", ", ">");
	private static final Joiner AND = Joiner.of(" & ");
	private static final Typed OBJ = new Typed(Object.class);
	private static final List<Typed> UPPER = Immutable.listOf(OBJ);
	private static final List<Typed> NO_TYPES = Immutable.list();

	private Generics() {}

	/**
	 * Represents a generic array type with component and number of dimensions.
	 */
	public record Array(Typed component, int dimensions) {
		@Override
		public final String toString() {
			return component() + "[]".repeat(dimensions());
		}
	}

	/**
	 * Provides safe access to type classes and bounds.
	 */
	public static class Typed {
		public static final Typed NULL = new Typed(null);
		private final Type type;
		private final Class<?> cls;
		private volatile List<Typed> types = null;
		private volatile List<Typed> upper = null;
		private volatile List<Typed> lower = null;

		/**
		 * Returns an instance for the type.
		 */
		public static Typed of(Type type) {
			if (type == null) return Typed.NULL;
			if (Object.class.equals(type)) return OBJ;
			return new Typed(type);
		}

		private Typed(Type type) {
			this.type = type;
			this.cls = Generics.classFrom(type);
		}

		/**
		 * Provides access to the underlying type.
		 */
		public Type raw() {
			return type;
		}

		/**
		 * Returns true if this represents no type.
		 */
		public boolean isNull() {
			return type == null;
		}

		/**
		 * Returns the class if this type is a class or class-based parameterized type, or null.
		 */
		public <T> Class<T> cls() {
			return Reflect.unchecked(cls);
		}

		/**
		 * Returns true if this type has no bounds.
		 */
		public boolean isUnbounded() {
			if (type == null || cls() != null) return false;
			return lower().isEmpty() && upper().equals(UPPER);
		}

		/**
		 * Returns the name if a type variable, otherwise empty string.
		 */
		public String varName() {
			return type instanceof TypeVariable v ? v.getName() : "";
		}

		/**
		 * Returns true if this is a generic array or array class type.
		 */
		public boolean isArray() {
			if (type instanceof GenericArrayType) return true;
			return cls != null && cls.getComponentType() != null;
		}

		/**
		 * Returns the component type if this is a generic array or array class type, otherwise null
		 * instance.
		 */
		public Typed component() {
			if (type instanceof GenericArrayType a) return of(a.getGenericComponentType());
			return cls == null ? NULL : of(cls.getComponentType());
		}

		/**
		 * Returns the core array component type and number of dimensions for this type. If not an
		 * array, the number of dimensions is 0.
		 */
		public Array array() {
			int dims = 0;
			var type = this;
			while (true) {
				var component = type.component();
				if (component.isNull()) return new Array(type, dims);
				type = component;
				dims++;
			}
		}

		/**
		 * Returns the parameterized type at given index, or null instance.
		 */
		public Typed type(int index) {
			return Lists.at(types(), index, NULL);
		}

		/**
		 * Returns the parameterized types, or empty list.
		 */
		public List<Typed> types() {
			var types = this.types;
			if (types == null) {
				types = type instanceof ParameterizedType p ? listOf(p.getActualTypeArguments()) :
					NO_TYPES;
				this.types = types;
			}
			return types;
		}

		/**
		 * Returns the upper bound at given index, or null instance.
		 */
		public Typed upper(int index) {
			return Lists.at(upper(), index, NULL);
		}

		/**
		 * Returns the upper bounds, or empty list if null.
		 */
		public List<Typed> upper() {
			var upper = this.upper;
			if (upper == null) {
				upper = resolveUpper(type);
				this.upper = upper;
			}
			return upper;
		}

		/**
		 * Returns the lower bound at given index, or null instance.
		 */
		public Typed lower(int index) {
			return Lists.at(lower(), index, NULL);
		}

		/**
		 * Returns the lower bounds, or empty list if null.
		 */
		public List<Typed> lower() {
			var lower = this.lower;
			if (lower == null) {
				lower = resolveLower(type);
				this.lower = lower;
			}
			return lower;
		}

		/**
		 * Returns true if the underlying type equals the given type.
		 */
		public boolean isType(Type type) {
			return Objects.equals(raw(), type);
		}

		@Override
		public int hashCode() {
			return Objects.hash(type);
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this || ((obj instanceof Typed b) && isType(b.type));
		}

		/**
		 * Returns a string descriptor with class package names.
		 */
		public String fullString() {
			return asString(false);
		}

		@Override
		public String toString() {
			return asString(true);
		}

		private String asString(boolean compact) {
			return switch (type) {
				case Class<?> _ -> name(cls, compact);
				case GenericArrayType _ -> component().asString(compact) + "[]";
				case ParameterizedType _ -> asParamString(compact);
				case TypeVariable<?> v -> v.getName();
				case WildcardType _ -> asWildcardString(compact);
				case null, default -> String.valueOf(type);
			};
		}

		private String asParamString(boolean compact) {
			return name(cls, compact) + GENERIC.join(t -> t.asString(compact), types());
		}

		private String asWildcardString(boolean compact) {
			var b = new StringBuilder("?");
			if (hasUpper()) AND.append(b.append(" extends "), t -> t.asString(compact), upper());
			if (!lower().isEmpty())
				AND.append(b.append(" super "), t -> t.asString(compact), lower());
			return b.toString();
		}

		private boolean hasUpper() {
			return !upper().isEmpty() && !upper().equals(UPPER);
		}
	}

	/**
	 * A token to pass a structured generic type at runtime.
	 */
	public static abstract class Token<T> {
		public final Typed typed;

		protected Token() {
			var superClass = (ParameterizedType) getClass().getGenericSuperclass();
			typed = Typed.of(superClass.getActualTypeArguments()[0]);
		}

		/**
		 * Provides the class, or null if the type if not a class or parameterized type.
		 */
		public Class<T> cls() {
			return typed.cls();
		}

		@Override
		public String toString() {
			return typed.toString();
		}
	}

	/**
	 * Provides class and generic types from the parameter.
	 */
	public static Typed typed(Parameter param) {
		if (param == null) return Typed.NULL;
		return Typed.of(param.getParameterizedType());
	}

	/**
	 * Provides class and generic types from the field.
	 */
	public static Typed typed(Field field) {
		if (field == null) return Typed.NULL;
		return Typed.of(field.getGenericType());
	}

	/**
	 * Returns the class from the type if a class or class-based parameterized type, or null.
	 */
	public static Class<?> classFrom(Type type) {
		if (type instanceof Class<?> cls) return cls;
		if (type instanceof ParameterizedType param) return classFrom(param.getRawType());
		return null;
	}

	/**
	 * Provides a generics-friendly, long-form string representation of a type, or standard string
	 * representation.
	 */
	public static String fullString(Object decl) {
		return switch (decl) {
			case Class<?> c -> c.toGenericString();
			case Executable e -> e.toGenericString();
			case Field f -> f.toGenericString();
			case TypeVariable<?> v -> typeVarString(v);
			case Type t -> t.getTypeName();
			case null, default -> String.valueOf(decl);
		};
	}

	// support

	private static String typeVarString(TypeVariable<?> variable) {
		var bounds = variable.getBounds();
		if (bounds.length == 1 && bounds[0] == Object.class) return variable.getTypeName();
		var b = new StringBuilder(variable.getName()).append(" extends ");
		AND.appendAll(b, Type::getTypeName, bounds);
		return b.toString();
	}

	private static String name(Class<?> cls, boolean compact) {
		return compact ? Reflect.simple(cls) : cls.getTypeName();
	}

	private static List<Typed> resolveUpper(Type type) {
		return switch (type) {
			case Class<?> _ -> listOf(type);
			case GenericArrayType _ -> listOf(type); // code coverage only if split
			case ParameterizedType p -> listOf(p.getRawType());
			case WildcardType w -> listOf(w.getUpperBounds());
			case TypeVariable<?> v -> listOf(v.getBounds());
			case null -> NO_TYPES;
			default -> UPPER;
		};
	}

	private static List<Typed> resolveLower(Type type) {
		return switch (type) {
			case Class<?> _ -> listOf(type);
			case GenericArrayType _ -> listOf(type); // code coverage only if split
			case ParameterizedType p -> listOf(p.getRawType());
			case WildcardType w -> listOf(w.getLowerBounds());
			case null, default -> NO_TYPES;
		};
	}

	private static List<Typed> listOf(Type... types) {
		if (ceri.common.array.Array.isEmpty(types)) return NO_TYPES;
		if (types.length == 1 && Object.class.equals(types[0])) return UPPER;
		return Immutable.adaptListOf(Typed::of, types);
	}
}
