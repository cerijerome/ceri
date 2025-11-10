package ceri.common.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import ceri.common.collect.Immutable;
import ceri.common.function.Functions;

/**
 * Utility methods to support annotations.
 */
public class Annotations {

	private Annotations() {}

	/**
	 * Returns true if the element has the annotation type.
	 */
	public static boolean has(AnnotatedElement element,
		Class<? extends Annotation> annotationCls) {
		return element != null && element.getAnnotation(annotationCls) != null;
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
	public static <T extends Annotation, R> R listValue(AnnotatedElement element,
		Class<T> annotationCls, Functions.Function<List<T>, R> valueAccessor) {
		var annotations = annotations(element, annotationCls);
		return valueAccessor.apply(annotations);
	}
}
