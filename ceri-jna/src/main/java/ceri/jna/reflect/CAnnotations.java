package ceri.jna.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.except.Exceptions;
import ceri.common.reflect.Annotations;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Streams;
import ceri.jna.util.JnaOs;

/**
 * Annotations declaring c code features.
 */
public class CAnnotations {

	private CAnnotations() {}

	/**
	 * Settings for c code symbol generation.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	public @interface CGen {
		/** The OS array used to generate c code; empty for all. */
		JnaOs[] os() default {};

		/** The classes used to generate c code. */
		Class<?>[] target();

		/** Additional classes to reload for generation. */
		Class<?>[] reload() default {};

		/** The location of generated code. End with / for a directory. */
		String location() default "";

		/**
		 * For manual creation of settings.
		 */
		record Value(JnaOs[] os, Class<?>[] target, Class<?>[] reload, String location) {

			/** No targets to process. */
			public static final Value NONE =
				new Value(JnaOs.NONE, ArrayUtil.Empty.classes, ArrayUtil.Empty.classes, "");

			/**
			 * Create from annotation.
			 */
			public static Value from(CGen cgen) {
				if (cgen == null) return NONE;
				return builder(cgen.target()).os(cgen.os()).reload(cgen.reload())
					.location(cgen.location()).value();
			}

			/**
			 * Builder for c code generation settings.
			 */
			public static class Builder {
				private JnaOs[] os = {};
				private final Class<?>[] target;
				private Class<?>[] reload = {};
				private String location = "";

				private Builder(Class<?>... target) {
					this.target = target;
				}

				/**
				 * Set OS.
				 */
				public Builder os(JnaOs... os) {
					this.os = os;
					return this;
				}

				/**
				 * Set support classes.
				 */
				public Builder reload(Class<?>... reload) {
					this.reload = reload;
					return this;
				}

				public Builder location(String location) {
					this.location = location;
					return this;
				}

				/**
				 * Build the settings.
				 */
				public Value value() {
					return new Value(knownOs(os), target, reload, location);
				}
			}

			/**
			 * Create a build with given targets.
			 */
			public static Builder builder(Class<?>... targets) {
				return new Builder(targets);
			}

			/**
			 * Returns the set of combined target and support classes.
			 */
			public Set<Class<?>> classes() {
				return Streams.of(target, reload).flatMap(Streams::of).toSet();
			}

			/**
			 * Return the location, or default if empty.
			 */
			public String location(String def) {
				return location().isEmpty() ? def : location();
			}
		}
	}

	/**
	 * Return annotated c code generation settings.
	 */
	public static CGen.Value cgen(Class<?> cls) {
		return CGen.Value.from(Annotations.annotation(cls, CGen.class));
	}

	/**
	 * Specifies c header files implemented by the class.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE })
	@Repeatable(CIncludes.class)
	public @interface CInclude {
		/** For OS-specific includes; unknown for all OS types. */
		JnaOs[] os() default {};

		/** Array of include paths. */
		String[] value();

		/**
		 * Container for includes by OS.
		 */
		record Value(Map<JnaOs, Set<String>> map) {
			public static final Value NONE = new Value(Map.of());

			/**
			 * Create from annotations.
			 */
			public static Value from(CInclude[] includes) {
				if (includes.length == 0) return NONE;
				var b = builder();
				for (var include : includes) {
					var os = include.os();
					if (os.length == 0) b.add(include.value());
					else for (var o : os)
						b.add(o, include.value());
				}
				return b.value();
			}

			/**
			 * Create with the same includes for each OS.
			 */
			public static Value of(String... includes) {
				return builder().add(includes).value();
			}

			/**
			 * Builder for includes by OS.
			 */
			public static class Builder {
				private final Map<JnaOs, Set<String>> map = Maps.tree();

				private Builder() {}

				/**
				 * Add includes for each OS.
				 */
				public Builder add(String... includes) {
					return add(Arrays.asList(includes));
				}

				/**
				 * Add includes for each OS.
				 */
				public Builder add(Collection<String> includes) {
					for (var os : JnaOs.KNOWN)
						add(os, includes);
					return this;
				}

				/**
				 * Add includes for the OS.
				 */
				public Builder add(JnaOs os, String... includes) {
					return add(os, Arrays.asList(includes));
				}

				/**
				 * Add includes for the OS.
				 */
				public Builder add(JnaOs os, Collection<String> includes) {
					if (!includes.isEmpty())
						map.computeIfAbsent(os, _ -> Sets.link()).addAll(includes);
					return this;
				}

				/**
				 * Create the container.
				 */
				public Value value() {
					return map.isEmpty() ? NONE : new Value(Immutable.mapOfSets(map));
				}
			}

			public static Builder builder() {
				return new Builder();
			}

			/**
			 * Provides the includes for the OS.
			 */
			public Set<String> includes(JnaOs os) {
				return map.getOrDefault(os, Set.of());
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	private @interface CIncludes {
		CInclude[] value();
	}

	/**
	 * Return a map of c includes by OS.
	 */
	public static CInclude.Value cincludes(Class<?> cls) {
		return CInclude.Value.from(cls.getAnnotationsByType(CInclude.class));
	}

	/**
	 * Not defined in c code. Cannot be used with c type annotation.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.FIELD })
	public @interface CUndefined {}

	/**
	 * C type settings by OS. Cannot be used with undefined annotation.
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.FIELD })
	@Repeatable(CTypes.class)
	public @interface CType {
		/** The OS that defines this type; empty for every OS. */
		JnaOs[] os() default {};

		/** The type name in c code. Empty string to use type name. */
		String name() default "";

		/** The enum value field name. Empty string to use default. */
		String valueField() default "";

		/** Type attributes. */
		Attr[] attrs() default {};

		enum Attr {
			/** Type is declared with typedef in c code. */
			typedef,
			/** Type is declared as an enum in c code. */
			cenum,
			/** Type value is signed. */
			signed;
		}

		/**
		 * For manual creation of a c type.
		 */
		record Value(JnaOs[] os, String name, String valueField, Attr... attrs) {

			private static final String VALUE_FIELD = "value";
			/** Equivalent to the undefined annotation. */
			public static final Value UNDEFINED = new Value(null, "", "");
			public static final Value DEFAULT = new Value(JnaOs.NONE, "", "");

			/**
			 * Creates a value from the annotation.
			 */
			public static Value from(CType ctype) {
				return new Value(ctype.os(), ctype.name(), ctype.valueField(), ctype.attrs());
			}

			/**
			 * Create new c type settings.
			 */
			public static CType.Value of(CType.Attr... attrs) {
				return of("", attrs);
			}

			/**
			 * Create new c type settings.
			 */
			public static CType.Value of(String name, CType.Attr... attrs) {
				return of(name, "", attrs);
			}

			/**
			 * Create new c type settings.
			 */
			public static CType.Value of(String name, String valueField, CType.Attr... attrs) {
				if (name.isEmpty() && valueField.isEmpty() && attrs.length == 0) return DEFAULT;
				return new CType.Value(JnaOs.NONE, name, valueField, attrs);
			}

			/**
			 * Create new c type settings.
			 */
			public static CType.Value of(JnaOs os, CType.Attr... attrs) {
				return of(os, "", attrs);
			}

			/**
			 * Create new c type settings.
			 */
			public static CType.Value of(JnaOs os, String name, CType.Attr... attrs) {
				return of(os, name, "", attrs);
			}

			/**
			 * Create new c type settings.
			 */
			public static CType.Value of(JnaOs os, String name, String valueField,
				CType.Attr... attrs) {
				return new CType.Value(new JnaOs[] { os }, name, valueField, attrs);
			}

			/**
			 * Returns true if the type is undefined in c for every OS.
			 */
			public boolean undefined() {
				return os() == null;
			}

			/**
			 * Returns the type name, or default.
			 */
			public String name(String def) {
				return name().isEmpty() ? def : name();
			}

			/**
			 * Returns the enum value field name, or default.
			 */
			@Override
			public String valueField() {
				return valueField.isEmpty() ? VALUE_FIELD : valueField;
			}

			/**
			 * Returns true if the type is defined by typedef in c.
			 */
			public boolean typedef() {
				return ArrayUtil.has(attrs(), Attr.typedef);
			}

			/**
			 * Returns true if the type is defined as an enum in c.
			 */
			public boolean cenum() {
				return ArrayUtil.has(attrs(), Attr.cenum);
			}

			/**
			 * Returns true if the type value is defined as signed in c.
			 */
			public boolean signed() {
				return ArrayUtil.has(attrs(), Attr.signed);
			}
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.TYPE, ElementType.FIELD })
	private @interface CTypes {
		CType[] value();
	}

	/**
	 * Find c type settings by annotation for the OS.
	 */
	public static CType.Value ctype(Enum<?> en, JnaOs os) {
		return ctype(Reflect.enumToField(en), os);
	}

	/**
	 * Find c type settings by annotation for the OS.
	 */
	public static CType.Value ctype(AnnotatedElement element, JnaOs os) {
		if (element == null) return CType.Value.UNDEFINED;
		var ctypes = element.getAnnotationsByType(CType.class);
		var undefined = element.isAnnotationPresent(CUndefined.class);
		// No type annotation and undefined annotation => undefined
		// No type annotation and no undefined annotation => default
		// Type annotation and undefined annotation => not permitted
		if (ctypes.length == 0) return undefined ? CType.Value.UNDEFINED : CType.Value.DEFAULT;
		if (undefined) throw Exceptions.illegalArg("Not permitted: %s and %s",
			CUndefined.class.getSimpleName(), CType.class.getSimpleName());
		return ctypeForOs(ctypes, os);
	}

	private static CType.Value ctypeForOs(CType[] ctypes, JnaOs os) {
		// Type annotation for OS => type
		// Type annotation without OS => type
		// Otherwise undefined
		for (var ctype : ctypes)
			if (ArrayUtil.has(ctype.os(), os)) return CType.Value.from(ctype);
		for (var ctype : ctypes)
			if (ctype.os().length == 0) return CType.Value.from(ctype);
		return CType.Value.UNDEFINED;
	}

	private static JnaOs[] knownOs(JnaOs[] os) {
		if (os.length > 0) return os;
		return JnaOs.KNOWN.toArray(JnaOs[]::new);
	}
}
