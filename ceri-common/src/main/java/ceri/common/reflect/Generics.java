package ceri.common.reflect;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedParameterizedType;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.AnnotatedTypeVariable;
import java.lang.reflect.AnnotatedWildcardType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Objects;
import ceri.common.array.RawArray;
import ceri.common.collect.Immutable;
import ceri.common.collect.Lists;
import ceri.common.text.Joiner;

/**
 * Support for accessing generic types.
 */
public class Generics {
	private static final Joiner GENERIC = Joiner.of("<", ", ", ">");
	private static final Joiner AND = Joiner.of(" & ");

	private Generics() {}

	/**
	 * A token to pass a structured generic type at runtime.
	 */
	public static abstract class Token<T> {
		public static final Token<?> NULL = new Token<>(Annotations.Node.NULL) {};
		private final Annotations.Node node;

		protected Token() {
			var superClass = (AnnotatedParameterizedType) getClass().getAnnotatedSuperclass();
			node = Annotations.node(superClass.getAnnotatedActualTypeArguments()[0]);
		}

		private Token(Annotations.Node node) {
			this.node = node;
		}

		/**
		 * Provides the annotated node wrapper.
		 */
		public Annotations.Node node() {
			return node;
		}

		/**
		 * Provides the generic type wrapper.
		 */
		public Typed typed() {
			return node().typed();
		}

		/**
		 * Provides the type.
		 */
		public Type type() {
			return typed().raw();
		}

		/**
		 * Provides the class, or null if the type if not a class or parameterized type.
		 */
		public Class<T> cls() {
			return typed().cls();
		}

		@Override
		public int hashCode() {
			return typed().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Token<?> t) && Objects.equals(typed(), t.typed());
		}

		@Override
		public String toString() {
			return node.toString();
		}
	}

	/**
	 * Represents a generic array type with component and number of dimensions, or non-array type if
	 * the number of dimensions is zero.
	 */
	public record Array(Typed component, int dimensions) {
		public static final Array NULL = new Array(Typed.NULL, 0);

		/**
		 * Returns true if the array or component is null.
		 */
		public static boolean isNull(Array array) {
			return array == null || Typed.isNull(array.component());
		}

		/**
		 * Returns an instance
		 */
		public static Array of(Class<?> cls, int dimensions) {
			return new Array(typed(cls), dimensions);
		}

		/**
		 * Returns true if this represents an array (dimensions > 0).
		 */
		public boolean isArray() {
			return dimensions() > 0;
		}

		/**
		 * Returns the class if the component type is a class or class-based parameterized type, or
		 * null.
		 */
		public Class<?> cls() {
			return component() == null ? null : component().cls();
		}

		@Override
		public String toString() {
			return component() + "[]".repeat(dimensions());
		}
	}

	/**
	 * Provides safe access to type classes and bounds.
	 */
	public static abstract class Typed {
		public static final Typed NULL = new Direct(null);
		public static final Typed OBJECT = new Direct(Object.class);
		public static final Typed VOID = new Direct(void.class);
		private static final List<Typed> UPPER = List.of(OBJECT);
		private static final List<Typed> NO_TYPES = List.of();
		private final Class<?> cls;
		private volatile List<Typed> types = null;
		private volatile List<Typed> upper = null;
		private volatile List<Typed> lower = null;

		/**
		 * Returns true if null or null type.
		 */
		public static boolean isNull(Typed typed) {
			return typed == null || typed.isNull();
		}

		/**
		 * Returns true if the primitive void type.
		 */
		public static boolean isVoid(Typed typed) {
			return typed != null && typed.isVoid();
		}

		private Typed(Type type) {
			this.cls = Generics.classFrom(type);
		}

		/**
		 * Provides access to the underlying type.
		 */
		public abstract Type raw();

		/**
		 * Returns true if this represents no type.
		 */
		public boolean isNull() {
			return raw() == null;
		}

		/**
		 * Returns true if this represents the primitive void type.
		 */
		public boolean isVoid() {
			return VOID.equals(this);
		}

		/**
		 * Returns the class if this type is a class or class-based parameterized type, or null.
		 */
		public <T> Class<T> cls() {
			return Reflect.unchecked(cls);
		}

		/**
		 * Provides an annotated type if supported.
		 */
		public abstract AnnotatedElement annotated();

		/**
		 * Returns the owner type if available.
		 */
		public abstract Typed owner();

		/**
		 * Returns true if this type has no bounds.
		 */
		public boolean isUnbounded() {
			if (isNull() || cls() != null) return false;
			return lower().isEmpty() && upper().equals(UPPER);
		}

		/**
		 * Returns the name if a type variable, otherwise empty string.
		 */
		public String varName() {
			return raw() instanceof TypeVariable v ? v.getName() : "";
		}

		/**
		 * Returns true if this is a generic array or array class type.
		 */
		public boolean isArray() {
			if (raw() instanceof GenericArrayType) return true;
			return cls != null && cls.getComponentType() != null;
		}

		/**
		 * Returns the component type if this is a generic array or array class type, otherwise null
		 * instance.
		 */
		public Typed component() {
			var typed = resolveComponent();
			if (typed != null) return typed;
			return cls == null ? NULL : typed(cls.getComponentType());
		}

		/**
		 * Returns the type resolved to a multi-dimensional array component. Returns this type if
		 * not an array.
		 */
		public Typed components() {
			if (!isArray()) return this;
			return component().components();
		}

		/**
		 * Returns the core array component type and number of dimensions for this type. If not an
		 * array, the number of dimensions is 0.
		 */
		public Array array() {
			return Generics.array(this);
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
				types = resolveTypes();
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
				upper = resolveUpper();
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
				lower = resolveLower();
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
			return Objects.hash(raw());
		}

		@Override
		public boolean equals(Object obj) {
			return obj == this || ((obj instanceof Typed b) && isType(b.raw()));
		}

		/**
		 * Returns a string descriptor with class package names.
		 */
		public String fullString() {
			return string(false, this);
		}

		@Override
		public String toString() {
			return string(true, this);
		}

		// shared

		abstract Typed resolveComponent();

		abstract List<Typed> resolveTypes();

		abstract List<Typed> resolveUpper();

		abstract List<Typed> resolveLower();
	}

	/**
	 * Provides access to types without annotations.
	 */
	private static class Direct extends Typed {
		private final Type type;

		private Direct(Type type) {
			super(type);
			this.type = type;
		}

		@Override
		public Type raw() {
			return type;
		}

		@Override
		public AnnotatedElement annotated() {
			return Reflect.castOrNull(AnnotatedElement.class, type);
		}

		@Override
		public Typed owner() {
			return switch (type) {
				case ParameterizedType p -> typed(p.getOwnerType());
				case null, default -> NULL;
			};
		}

		@Override
		Typed resolveComponent() {
			return (type instanceof GenericArrayType a) ? typed(a.getGenericComponentType()) : null;
		}

		@Override
		List<Typed> resolveTypes() {
			return (type instanceof ParameterizedType p) ? listOf(p.getActualTypeArguments()) :
				Typed.NO_TYPES;
		}

		@Override
		List<Typed> resolveUpper() {
			return switch (type) {
				case Class<?> _ -> List.of(this); // cases split for code coverage
				case GenericArrayType _ -> List.of(this);
				case ParameterizedType _ -> List.of(this);
				case WildcardType w -> listOf(w.getUpperBounds());
				case TypeVariable<?> v -> listOf(v.getBounds());
				case null -> Typed.NO_TYPES;
				default -> Typed.UPPER;
			};
		}

		@Override
		List<Typed> resolveLower() {
			return switch (type) {
				case Class<?> _ -> List.of(this); // cases split for code coverage
				case GenericArrayType _ -> List.of(this);
				case ParameterizedType _ -> List.of(this);
				case WildcardType w -> listOf(w.getLowerBounds());
				case null, default -> Typed.NO_TYPES;
			};
		}
	}

	/**
	 * Provides access to types with annotations.
	 */
	private static class Annotated extends Typed {
		/** Usually a wrapper for a parameterized type, should not be null. */
		private final AnnotatedType type;

		private Annotated(AnnotatedType type) {
			super(type.getType());
			this.type = type;
		}

		@Override
		public Type raw() {
			return type.getType();
		}

		@Override
		public AnnotatedType annotated() {
			return type;
		}

		@Override
		public Typed owner() {
			return typed(type.getAnnotatedOwnerType());
		}

		@Override
		Typed resolveComponent() {
			return (type instanceof AnnotatedArrayType a) ?
				typed(a.getAnnotatedGenericComponentType()) : null;
		}

		@Override
		List<Typed> resolveTypes() {
			return (type instanceof AnnotatedParameterizedType p) ?
				listOf(p.getAnnotatedActualTypeArguments()) : Typed.NO_TYPES;
		}

		@Override
		List<Typed> resolveUpper() {
			if (raw() instanceof Class<?>) return List.of(this);
			return switch (type) {
				case AnnotatedArrayType _ -> List.of(this);
				case AnnotatedParameterizedType _ -> List.of(this);
				case AnnotatedWildcardType w -> listOf(w.getAnnotatedUpperBounds());
				case AnnotatedTypeVariable v -> listOf(v.getAnnotatedBounds());
				default -> Typed.UPPER;
			};
		}

		@Override
		List<Typed> resolveLower() {
			if (raw() instanceof Class<?>) return List.of(this);
			return switch (type) {
				case AnnotatedArrayType _ -> List.of(this);
				case AnnotatedParameterizedType _ -> List.of(this);
				case AnnotatedWildcardType w -> listOf(w.getAnnotatedLowerBounds());
				default -> Typed.NO_TYPES;
			};
		}
	}

	/**
	 * Provides generic type traversal.
	 */
	public static Typed typed(Type type) {
		if (type == null) return Typed.NULL;
		if (Objects.equals(type, Typed.OBJECT.raw())) return Typed.OBJECT;
		if (Objects.equals(type, Typed.VOID.raw())) return Typed.VOID;
		return new Direct(type);
	}

	/**
	 * Provides generic type traversal with annotations.
	 */
	public static Typed typed(AnnotatedType type) {
		return type == null ? Typed.NULL : new Annotated(type);
	}

	/**
	 * Provides generic field type traversal with annotations.
	 */
	public static Typed typed(Field field) {
		return field == null ? Typed.NULL : typed(field.getAnnotatedType());
	}

	/**
	 * Provides generic parameter type traversal with annotations.
	 */
	public static Typed typed(Parameter param) {
		return param == null ? Typed.NULL : typed(param.getAnnotatedType());
	}

	/**
	 * Provides generic parameter type traversal with annotations.
	 */
	public static Typed typed(RecordComponent comp) {
		return comp == null ? Typed.NULL : typed(comp.getAnnotatedType());
	}

	/**
	 * Provides generic method/constructor return type traversal with annotations.
	 */
	public static Typed typedReturn(Executable exec) {
		return exec == null ? Typed.NULL : typed(exec.getAnnotatedReturnType());
	}

	/**
	 * Returns an instance for the object's type.
	 */
	public static Typed typedClass(Object obj) {
		return typed(Reflect.getClass(obj));
	}

	/**
	 * Returns an instance for the object's type.
	 */
	public static Typed typedFrom(Object obj) {
		return switch (obj) {
			case Type t -> typed(t);
			case AnnotatedType a -> typed(a);
			case Field f -> typed(f);
			case Parameter p -> typed(p);
			case null, default -> typedClass(obj);
		};
	}

	/**
	 * Returns the class from the type if a class or class-based parameterized type, or null.
	 */
	public static Class<?> classFrom(Type type) {
		if (type == null) return null;
		if (type instanceof Class<?> c) return c;
		if (type instanceof ParameterizedType p) return classFrom(p.getRawType());
		if (type instanceof GenericArrayType a)
			return RawArray.arrayType(classFrom(a.getGenericComponentType()));
		return null;
	}

	/**
	 * Returns the class from the type if a class or class-based parameterized type, or null.
	 */
	public static Class<?> classFrom(AnnotatedType type) {
		return type == null ? null : classFrom(type.getType());
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

	private static Array array(Typed typed) {
		int dims = 0;
		while (true) {
			var component = typed.component();
			if (component.isNull()) return new Array(typed, dims);
			typed = component;
			dims++;
		}
	}

	private static String string(boolean compact, Typed typed) {
		var raw = typed.raw();
		return switch (raw) {
			case Class<?> c -> name(c, compact);
			case GenericArrayType _ -> string(compact, typed.component()) + "[]";
			case ParameterizedType _ -> asParamString(compact, typed.cls(), typed.types());
			case TypeVariable<?> v -> v.getName();
			case WildcardType _ -> wildcardString(compact, typed.upper(), typed.lower());
			case null, default -> String.valueOf(raw);
		};
	}

	private static String typeVarString(TypeVariable<?> variable) {
		var bounds = variable.getBounds();
		if (bounds.length == 1 && bounds[0] == Object.class) return variable.getTypeName();
		var b = new StringBuilder(variable.getName()).append(" extends ");
		AND.appendAll(b, Type::getTypeName, bounds);
		return b.toString();
	}

	private static String asParamString(boolean compact, Class<?> cls, List<Typed> types) {
		return name(cls, compact) + GENERIC.join(t -> string(compact, t), types);
	}

	private static String wildcardString(boolean compact, List<Typed> upper, List<Typed> lower) {
		var b = new StringBuilder("?");
		if (hasUpper(upper)) AND.append(b.append(" extends "), t -> string(compact, t), upper);
		if (!lower.isEmpty()) AND.append(b.append(" super "), t -> string(compact, t), lower);
		return b.toString();
	}

	private static String name(Class<?> cls, boolean compact) {
		return compact ? Reflect.simple(cls) : cls.getTypeName();
	}

	private static boolean hasUpper(List<Typed> upper) {
		return !upper.isEmpty() && !upper.equals(Typed.UPPER);
	}

	private static List<Typed> listOf(Type... types) {
		if (RawArray.isEmpty(types)) return Typed.NO_TYPES;
		if (types.length == 1 && Object.class.equals(types[0])) return Typed.UPPER;
		return Immutable.adaptListOf(Generics::typed, types);
	}

	private static List<Typed> listOf(AnnotatedType... types) {
		if (RawArray.isEmpty(types)) return Typed.NO_TYPES;
		if (types.length == 1 && Object.class.equals(types[0].getType())) return Typed.UPPER;
		return Immutable.adaptListOf(Generics::typed, types);
	}
}
