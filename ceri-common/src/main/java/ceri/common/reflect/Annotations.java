package ceri.common.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.util.Basics;

/**
 * Utility methods to support annotations.
 */
public class Annotations {
	/** An empty annotation array. */
	public static final Annotation[] NONE = new Annotation[0];
	/** An empty annotation element. */
	public static final AnnotatedType NULL_TYPE = nullType();

	private Annotations() {}

	/**
	 * Creates an annotation element path from generic type traversal.
	 */
	public static class Path {
		private AnnotatedElement element;
		private Generics.Typed typed;

		private Path(AnnotatedElement element, Generics.Typed typed) {
			this.element = element;
			this.typed = typed;
		}

		/**
		 * Adds an annotation element without modifying the type.
		 */
		public Path orphan(AnnotatedElement element) {
			return sub(node(element, null));
		}

		/**
		 * Adds an annotation element without modifying the type.
		 */
		public Path sub(AnnotatedElement element) {
			if (element == null) return this;
			if (this.element == null) this.element = element;
			else this.element = node(element, this.element);
			return this;
		}

		/**
		 * Sets the type, adding the type annotation element if available.
		 */
		public Path sub(Generics.Typed typed) {
			if (typed == null) return this;
			this.typed = typed;
			return sub(Annotations.element(typed));
		}

		/**
		 * Modifies the type, adding the type annotation element if available.
		 */
		public Path sub(Functions.Operator<Generics.Typed> operator) {
			if (operator == null || typed == null) return this;
			return sub(operator.apply(typed));
		}

		/**
		 * Adds the generic sub-type of given index.
		 */
		public Path type(int index) {
			return sub(t -> t.type(index));
		}

		/**
		 * Adds the generic upper bound of given index.
		 */
		public Path upper(int index) {
			return sub(t -> t.upper(index));
		}

		/**
		 * Adds the generic lower bound of given index.
		 */
		public Path lower(int index) {
			return sub(t -> t.lower(index));
		}

		/**
		 * Returns the current type.
		 */
		public Generics.Typed typed() {
			return typed;
		}

		/**
		 * Returns the current element.
		 */
		public AnnotatedElement get() {
			return element;
		}
	}

	/**
	 * A path node that encapsulates an element and its parent. Useful for resolving annotations.
	 */
	public record Node(AnnotatedElement element, AnnotatedElement parent)
		implements AnnotatedElement {
		/** An empty instance. */
		public static final Node NULL = new Node(null, null);

		/**
		 * Returns a new node with this node as the parent.
		 */
		public Node sub(AnnotatedElement element) {
			if (element == null) return this;
			return new Node(element, this);
		}

		/**
		 * Returns a new node with this node as the parent.
		 */
		public Node sub(Generics.Typed typed) {
			return sub(Annotations.element(typed));
		}

		@Override
		public AnnotatedElement element() {
			return Basics.def(element, NULL_TYPE);
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return element().getAnnotation(annotationClass);
		}

		@Override
		public Annotation[] getAnnotations() {
			return element().getAnnotations();
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			return element().getDeclaredAnnotations();
		}
	}

	/**
	 * Starts the path as empty.
	 */
	public static Path path() {
		return path((AnnotatedElement) null);
	}

	/**
	 * Starts the path with the given parent and no type.
	 */
	public static Path path(AnnotatedElement parent) {
		return path(parent, null);
	}

	/**
	 * Starts the path with the given parent and type, without adding the type.
	 */
	public static Path path(AnnotatedElement parent, Generics.Typed typed) {
		return new Path(parent, typed);
	}

	/**
	 * Starts the path with the field type.
	 */
	public static Path path(Field field) {
		return path(field, Generics.typed(field));
	}

	/**
	 * Starts the path with the parameter type.
	 */
	public static Path path(Parameter parameter) {
		return path(parameter, Generics.typed(parameter));
	}

	/**
	 * Starts the path with the method/constructor return type.
	 */
	public static Path pathReturn(Executable executable) {
		return path(executable, Generics.typedReturn(executable));
	}

	/**
	 * Returns a node instance without a parent. Can be used to prevent default parent resolution.
	 */
	public static Node node(AnnotatedElement element) {
		return node(element, null);
	}

	/**
	 * Returns a node instance without a parent. Can be used to prevent default parent resolution.
	 */
	public static Node node(Generics.Typed typed) {
		return node(Annotations.element(typed));
	}

	/**
	 * Returns a node instance.
	 */
	public static Node node(AnnotatedElement element, AnnotatedElement parent) {
		if (element == null && parent == null) return Node.NULL;
		return new Node(element, parent);
	}

	/**
	 * Returns true if the element has the annotation type.
	 */
	public static boolean has(AnnotatedElement element, Class<? extends Annotation> annotationCls) {
		return element != null && annotationCls != null
			&& element.getAnnotation(annotationCls) != null;
	}

	/**
	 * Get annotation from element, or return null.
	 */
	public static <T extends Annotation> T annotation(AnnotatedElement element,
		Class<T> annotationCls) {
		return element == null ? null : element.getAnnotation(annotationCls);
	}

	/**
	 * Get field annotation from enum, or return null.
	 */
	public static <T extends Annotation> T annotation(Enum<?> en, Class<T> annotationCls) {
		return annotation(Reflect.enumToField(en), annotationCls);
	}

	/**
	 * Get repeat annotations from element as a list.
	 */
	public static <T extends Annotation> List<T> annotations(AnnotatedElement element,
		Class<T> annotationCls) {
		return element == null ? List.of() :
			Immutable.wrapListOf(element.getAnnotationsByType(annotationCls));
	}

	/**
	 * Get repeat field annotations from enum as a list.
	 */
	public static <T extends Annotation> List<T> annotations(Enum<?> en, Class<T> annotationCls) {
		return annotations(Reflect.enumToField(en), annotationCls);
	}

	/**
	 * Get TYPE annotation from supplied class, or return null. Useful for static initialization of
	 * default class annotations:
	 * 
	 * <pre>
	 * // Anno.DEFAULT should be accessed before getAnnotation(Anno.class)
	 * // in order to successfully initialize the class
	 * 
	 * &#64;Retention(RetentionPolicy.RUNTIME)
	 * &#64;Target({ ElementType.TYPE })
	 * public static @interface Anno {
	 * 	public static final Anno DEFAULT = annotationFromClass(() -> {
	 * 		&#64;Anno
	 * 		class Default {}
	 * 		return Default.class;
	 * 	}, Anno.class);
	 * 
	 * 	String s() default "";
	 * 
	 * 	int i() default 0;
	 * }
	 * </pre>
	 */
	public static <T extends Annotation> T annotationFromClass(
		Functions.Supplier<? extends AnnotatedElement> elementSupplier, Class<T> annotationCls) {
		return elementSupplier == null ? null : annotation(elementSupplier.get(), annotationCls);
	}

	/**
	 * Get FIELD annotation from supplied enum, or return null. Useful for static initialization of
	 * default enum annotations:
	 * 
	 * <pre>
	 * // Anno.DEFAULT should be accessed before getAnnotation(Anno.class)
	 * // in order to successfully initialize the class
	 * 
	 * &#64;Retention(RetentionPolicy.RUNTIME)
	 * &#64;Target({ ElementType.FIELD })
	 * public static @interface Anno {
	 * 	public static final Anno DEFAULT = annotationFromEnum(() -> {
	 * 		enum Default {
	 * 			&#64;Anno
	 * 			DEFAULT
	 * 		}
	 * 		return Default.DEFAULT;
	 * 	}, Anno.class);
	 * 
	 * 	public String s() default "yyy";
	 * 
	 * 	public int i() default 7;
	 * }
	 * </pre>
	 */
	public static <T extends Annotation> T
		annotationFromEnum(Functions.Supplier<Enum<?>> enumSupplier, Class<T> annotationCls) {
		return enumSupplier == null ? null : annotation(enumSupplier.get(), annotationCls);
	}

	/**
	 * Apply accessor to annotation, return null if not found.
	 */
	public static <T extends Annotation, R> R value(AnnotatedElement element,
		Class<T> annotationCls, Functions.Function<T, R> valueAccessor) {
		return value(element, annotationCls, valueAccessor, null);
	}

	/**
	 * Apply accessor to annotation, return default if not found.
	 */
	public static <T extends Annotation, R> R value(AnnotatedElement element,
		Class<T> annotationCls, Functions.Function<T, R> valueAccessor, R def) {
		T annotation = annotation(element, annotationCls);
		return annotation == null ? def : valueAccessor.apply(annotation);
	}

	/**
	 * Apply accessor to annotation, return default if not found.
	 */
	public static <T extends Annotation> boolean value(AnnotatedElement element,
		Class<T> annotationCls, Functions.ToBoolFunction<T> valueAccessor, boolean def) {
		T annotation = annotation(element, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsBool(annotation);
	}

	/**
	 * Apply accessor to annotation, return default if not found.
	 */
	public static <T extends Annotation> int value(AnnotatedElement element, Class<T> annotationCls,
		Functions.ToIntFunction<T> valueAccessor, int def) {
		T annotation = annotation(element, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsInt(annotation);
	}

	/**
	 * Apply accessor to field annotation, return null if not found.
	 */
	public static <T extends Annotation, R> R value(Enum<?> en, Class<T> annotationCls,
		Functions.Function<T, R> valueAccessor) {
		return value(en, annotationCls, valueAccessor, null);
	}

	/**
	 * Apply accessor to field annotation, return default if not found.
	 */
	public static <T extends Annotation, R> R value(Enum<?> en, Class<T> annotationCls,
		Functions.Function<T, R> valueAccessor, R def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : valueAccessor.apply(annotation);
	}

	/**
	 * Apply accessor to field annotation, return default if not found.
	 */
	public static <T extends Annotation> boolean value(Enum<?> en, Class<T> annotationCls,
		Functions.ToBoolFunction<T> valueAccessor, boolean def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsBool(annotation);
	}

	/**
	 * Apply accessor to field annotation, return default if not found.
	 */
	public static <T extends Annotation> int value(Enum<?> en, Class<T> annotationCls,
		Functions.ToIntFunction<T> valueAccessor, int def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsInt(annotation);
	}

	/**
	 * Apply accessor to annotation list.
	 */
	public static <T extends Annotation, R> R reduceValue(AnnotatedElement element,
		Class<T> annotationCls, Functions.Function<List<T>, R> valueAccessor) {
		var annotations = annotations(element, annotationCls);
		return valueAccessor.apply(annotations);
	}

	/**
	 * Attempts to find an annotation on an element or its ancestors.
	 */
	public static <T extends Annotation> T resolve(AnnotatedElement element,
		Class<T> annotationCls) {
		return resolve(element, e -> e.getAnnotation(annotationCls));
	}

	/**
	 * Attempts to find an annotation on an element or its ancestors, and apply a function.
	 */
	public static <E extends Exception, T extends Annotation, R> R resolve(AnnotatedElement element,
		Class<T> annotationCls, Excepts.Function<E, T, R> function) throws E {
		if (function == null) return null;
		var anno = resolve(element, annotationCls);
		return anno == null ? null : function.apply(anno);
	}

	/**
	 * Attempts to resolve a function against an element and its ancestors until the result is
	 * non-null.
	 */
	public static <E extends Exception, T> T resolve(AnnotatedElement element,
		Excepts.Function<E, AnnotatedElement, T> function) throws E {
		if (element == null || function == null) return null;
		var result = function.apply(element);
		if (result != null) return result;
		return resolve(parent(element), function);
	}

	/**
	 * Attempts to find an element's parent from its type; returns null if not found. Useful for
	 * traversing upwards to resolve annotations.
	 */
	public static AnnotatedElement parent(AnnotatedElement element) {
		return switch (element) {
			case Node n -> n.parent();
			case Class<?> c -> c.getDeclaringClass();
			case Member m -> m.getDeclaringClass();
			case Parameter p -> p.getDeclaringExecutable();
			case RecordComponent r -> r.getDeclaringRecord();
			case null, default -> null;
		};
	}

	/**
	 * Returns the typed annotation element or null.
	 */
	public static AnnotatedElement element(Generics.Typed typed) {
		return typed == null ? null : typed.annotated();
	}

	// support

	private static AnnotatedType nullType() {
		return new AnnotatedType() {
			@Override
			public Type getType() {
				return null;
			}

			@Override
			public Annotation[] getDeclaredAnnotations() {
				return NONE;
			}

			@Override
			public Annotation[] getAnnotations() {
				return NONE;
			}

			@Override
			public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
				return null;
			}
		};
	}
}
