package ceri.common.reflect;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import ceri.common.collect.Enums;
import ceri.common.collect.Immutable;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.text.Strings;
import ceri.common.util.Basics;

/**
 * Utility methods to support annotations.
 */
public class Annotations {
	/** An empty annotation array. */
	public static final Annotation[] NONE = new Annotation[0];
	/** An empty annotated type/element. */
	public static final AnnotatedType NULL = ofNull();

	private Annotations() {}

	/**
	 * An annotated element wrapper that provides a parent for annotation resolution.
	 */
	public static class Resolvable implements AnnotatedElement {
		public static final Resolvable NULL = new Resolvable(null, Annotations.NULL);
		private AnnotatedElement parent;
		private AnnotatedElement element;

		/**
		 * Removes all resolvable wrappers from an element.
		 */
		public static AnnotatedElement unwrap(AnnotatedElement element) {
			while (element instanceof Resolvable r)
				element = r.element();
			return element;
		}

		/**
		 * Returns a normalized instance for the element and its parent.
		 */
		public static Resolvable of(AnnotatedElement parent, AnnotatedElement element) {
			if (isNull(element)) return isNull(parent) ? NULL : of(null, parent);
			if (parent == element) parent = null;
			return new Resolvable(parent, element);
		}

		private Resolvable(AnnotatedElement parent, AnnotatedElement element) {
			this.parent = parent;
			this.element = element;
		}

		/**
		 * Returns the wrapped element.
		 */
		public AnnotatedElement element() {
			return element;
		}

		/**
		 * Returns the parent element, which may be null.
		 */
		public AnnotatedElement parent() {
			return parent;
		}

		@Override
		public Annotation[] getDeclaredAnnotations() {
			return element().getDeclaredAnnotations();
		}

		@Override
		public Annotation[] getAnnotations() {
			return element().getAnnotations();
		}

		@Override
		public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
			return element().getAnnotation(annotationClass);
		}

		@Override
		public int hashCode() {
			return Objects.hash(element(), parent());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Resolvable r) && Objects.equals(element(), r.element())
				&& Objects.equals(parent(), r.parent());
		}

		@Override
		public String toString() {
			return "(" + element() + ")";
		}
	}

	/**
	 * Creates an annotation element path from generic type traversal.
	 */
	public static class Node {
		public static final Node NULL = new Node(Generics.Typed.NULL, Annotations.NULL);
		private Generics.Typed typed;
		private AnnotatedElement element;

		/**
		 * Returns true if the node is null or the null node instance.
		 */
		public static boolean isNull(Node node) {
			return node == null || node == NULL;
		}

		/**
		 * Returns a normalized instance for the type and element.
		 */
		public static Node of(Generics.Typed typed, AnnotatedElement element) {
			if (Generics.Typed.isNull(typed))
				return Annotations.isNull(element) ? NULL : new Node(Generics.Typed.NULL, element);
			if (Annotations.isNull(element)) return new Node(typed, Annotations.NULL);
			return new Node(typed, element);
		}

		private Node(Generics.Typed typed, AnnotatedElement element) {
			this.typed = typed;
			this.element = element;
		}

		/**
		 * Returns the generic type.
		 */
		public Generics.Typed typed() {
			return typed;
		}

		/**
		 * Returns the annotation element.
		 */
		public AnnotatedElement element() {
			return element;
		}

		/**
		 * Adds the annotation element for the current type if not already present.
		 */
		public Node sub() {
			return sub(typed());
		}

		/**
		 * Adds this node's element as a parent to the given node.
		 */
		public Node sub(Node node) {
			return sub(node.typed(), node.element());
		}

		/**
		 * Adds an annotation element without modifying the type.
		 */
		public Node sub(AnnotatedElement element) {
			return sub(typed(), element);
		}

		/**
		 * Adds a type and its annotation element, if available.
		 */
		public Node sub(Generics.Typed typed) {
			return sub(typed, Annotations.element(typed));
		}

		/**
		 * Modifies the type, adding the type annotation element if available.
		 */
		public Node sub(Functions.Operator<Generics.Typed> operator) {
			if (operator == null) return this;
			return sub(operator.apply(typed()));
		}

		/**
		 * Returns the number of types available.
		 */
		public int types() {
			return typed().types().size();
		}

		/**
		 * Traverses to and adds the generic sub-type of given index, if available.
		 */
		public Node type(int index) {
			return sub(t -> t.type(index));
		}

		/**
		 * Returns true if the type is a generic array or array class.
		 */
		public boolean isArray() {
			return typed().isArray();
		}

		/**
		 * Traverses to and adds the next generic array component, if available.
		 */
		public Node component() {
			return sub(Generics.Typed::component);
		}

		/**
		 * Traverses to and adds the multi-dimensional array component, if available.
		 */
		public Node components() {
			return sub(Generics.Typed::components);
		}

		/**
		 * Returns the number of upper bounds available.
		 */
		public int upper() {
			return typed().upper().size();
		}

		/**
		 * Traverses to and adds the generic upper bound of given index, if available.
		 */
		public Node upper(int index) {
			return sub(t -> t.upper(index));
		}

		/**
		 * Returns the number of lower bounds available.
		 */
		public int lower() {
			return typed().lower().size();
		}

		/**
		 * Traverses to and adds the generic lower bound of given index, if available.
		 */
		public Node lower(int index) {
			return sub(t -> t.lower(index));
		}

		private Node sub(Generics.Typed typed, AnnotatedElement element) {
			if (Generics.Typed.isNull(typed)) typed = this.typed;
			element = Annotations.resolvableElement(element(), element);
			if (typed == typed() && element == element()) return this;
			return of(typed, element);
		}

		@Override
		public int hashCode() {
			return Objects.hash(typed(), element());
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Node n) && Objects.equals(typed(), n.typed())
				&& Objects.equals(element(), n.element());
		}

		@Override
		public String toString() {
			var name = shortNameOf(element());
			var typed = typed().toString();
			if (Strings.isEmpty(name) || name.equals(typed)) return typed;
			return typed + ":" + name;
		}
	}

	/**
	 * Returns true if the element is null or the null instance.
	 */
	public static boolean isNull(AnnotatedElement element) {
		return element == null || element == NULL;
	}

	/**
	 * Returns the element, or the null instance if null.
	 */
	public static AnnotatedElement safe(AnnotatedElement element) {
		return Basics.def(element, NULL);
	}

	/**
	 * Returns a normalized resolvable element for the given type and parent.
	 */
	public static AnnotatedElement resolvable(AnnotatedElement parent, Generics.Typed typed) {
		return resolvableElement(parent, element(typed));
	}

	/**
	 * Returns a normalized resolvable element for the given elements.
	 */
	public static AnnotatedElement resolvable(AnnotatedElement... elements) {
		if (ceri.common.array.Array.isEmpty(elements)) return NULL;
		var element = safe(elements[0]);
		for (int i = 1; i < elements.length; i++)
			element = resolvableElement(element, elements[i]);
		return element;
	}

	/**
	 * Returns a node for the type and its annotated element if available.
	 */
	public static Node node(Generics.Typed typed) {
		return Node.of(typed, element(typed));
	}

	/**
	 * Returns a node for the type and its annotated element if available.
	 */
	public static Node node(Type type) {
		return node(Generics.typed(type));
	}

	/**
	 * Returns a node for the field type and annotated element.
	 */
	public static Node node(Field field) {
		return Node.of(Generics.typed(field), field);
	}

	/**
	 * Returns a node for the parameter type and annotated element.
	 */
	public static Node node(Parameter param) {
		return Node.of(Generics.typed(param), param);
	}

	/**
	 * Returns a node for the method/constructor return type and annotated element.
	 */
	public static Node nodeReturn(Executable exec) {
		return Node.of(Generics.typedReturn(exec), exec);
	}

	/**
	 * Returns a node for the record component type and annotated element.
	 */
	public static Node node(RecordComponent comp) {
		return Node.of(Generics.typed(comp), comp);
	}

	/**
	 * Returns a node for the annotated type.
	 */
	public static Node node(AnnotatedType type) {
		return node(Generics.typed(type));
	}

	/**
	 * Returns the targets for an annotation type.
	 */
	public static <T extends Annotation> Set<ElementType> targets(Class<T> annotationCls) {
		return Enums.set(value(annotationCls, Target.class, Target::value));
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
	 * Attempts to find an annotation on an element or its ancestors, and apply a function. Returns
	 * default if annotation not found.
	 */
	public static <E extends Exception, T extends Annotation, R> R resolve(AnnotatedElement element,
		Class<T> annotationCls, Excepts.Function<E, T, R> function, R def) throws E {
		return Basics.def(resolve(element, annotationCls, function), def);
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
	 * Attempts to resolve a function against an element and its ancestors until the result is
	 * non-null. Returns default if no value found. Allows a function with default to be used with
	 * direct and resolved elements.
	 */
	public static <E extends Exception, T> T resolve(AnnotatedElement element,
		Excepts.BiFunction<E, AnnotatedElement, T, T> function, T def) throws E {
		if (element == null || function == null) return def;
		return Basics.def(resolve(element, e -> function.apply(e, null)), def);
	}

	/**
	 * Attempts to find an element's parent from its type; returns null if not found. Useful for
	 * traversing upwards to resolve annotations.
	 */
	public static AnnotatedElement parent(AnnotatedElement element) {
		return switch (element) {
			case Resolvable r -> r.parent();
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

	/**
	 * Returns the component element of a multi-dimensional array. Useful to access the same
	 * left-positioned annotations as non-generic annotated array elements.
	 */
	public static AnnotatedElement component(AnnotatedElement element) {
		while (true) {
			element = Resolvable.unwrap(element);
			if (element instanceof AnnotatedArrayType a)
				element = a.getAnnotatedGenericComponentType();
			else break;
		}
		return element;
	}

	// support

	private static AnnotatedElement resolvableElement(AnnotatedElement parent,
		AnnotatedElement element) {
		if (isNull(element)) return isNull(parent) ? NULL : parent;
		if (isNull(parent) || element == parent) return element;
		return Resolvable.of(parent, element);
	}

	private static String shortNameOf(Object obj) {
		return switch (obj) {
			case Class<?> c -> c.getSimpleName();
			case Member m -> m.getName();
			case Parameter e -> e.getName(); // argN unless compiled with -parameters
			case RecordComponent r -> r.getName();
			case AnnotatedType a -> shortNameOf(a.getType());
			case null, default -> "";
		};
	}

	private static AnnotatedType ofNull() {
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

			@Override
			public String toString() {
				return Strings.NULL;
			}
		};
	}
}
