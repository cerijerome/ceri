package ceri.common.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import ceri.common.collection.ImmutableUtil;
import ceri.common.function.Functions.ToBoolFunction;

/**
 * Utility methods to support annotations.
 */
public class AnnotationUtil {

	private AnnotationUtil() {}

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
		return annotation(ReflectUtil.enumToField(en), annotationCls);
	}

	/**
	 * Get repeat annotations from element as a list.
	 */
	public static <T extends Annotation> List<T> annotations(AnnotatedElement element,
		Class<T> annotationCls) {
		return element == null ? List.of() :
			ImmutableUtil.wrapAsList(element.getAnnotationsByType(annotationCls));
	}

	/**
	 * Get repeat field annotations from enum as a list.
	 */
	public static <T extends Annotation> List<T> annotations(Enum<?> en, Class<T> annotationCls) {
		return annotations(ReflectUtil.enumToField(en), annotationCls);
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
		Supplier<? extends AnnotatedElement> elementSupplier, Class<T> annotationCls) {
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
	public static <T extends Annotation> T annotationFromEnum(Supplier<Enum<?>> enumSupplier,
		Class<T> annotationCls) {
		return enumSupplier == null ? null : annotation(enumSupplier.get(), annotationCls);
	}

	/**
	 * Apply accessor to annotation, return null if not found.
	 */
	public static <T extends Annotation, R> R value(AnnotatedElement element,
		Class<T> annotationCls, Function<T, R> valueAccessor) {
		return value(element, annotationCls, valueAccessor, null);
	}

	/**
	 * Apply accessor to annotation, return default if not found.
	 */
	public static <T extends Annotation, R> R value(AnnotatedElement element,
		Class<T> annotationCls, Function<T, R> valueAccessor, R def) {
		T annotation = annotation(element, annotationCls);
		return annotation == null ? def : valueAccessor.apply(annotation);
	}

	/**
	 * Apply accessor to annotation, return default if not found.
	 */
	public static <T extends Annotation> boolean value(AnnotatedElement element,
		Class<T> annotationCls, ToBoolFunction<T> valueAccessor, boolean def) {
		T annotation = annotation(element, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsBool(annotation);
	}

	/**
	 * Apply accessor to annotation, return default if not found.
	 */
	public static <T extends Annotation> int value(AnnotatedElement element, Class<T> annotationCls,
		ToIntFunction<T> valueAccessor, int def) {
		T annotation = annotation(element, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsInt(annotation);
	}

	/**
	 * Apply accessor to field annotation, return null if not found.
	 */
	public static <T extends Annotation, R> R value(Enum<?> en, Class<T> annotationCls,
		Function<T, R> valueAccessor) {
		return value(en, annotationCls, valueAccessor, null);
	}

	/**
	 * Apply accessor to field annotation, return default if not found.
	 */
	public static <T extends Annotation, R> R value(Enum<?> en, Class<T> annotationCls,
		Function<T, R> valueAccessor, R def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : valueAccessor.apply(annotation);
	}

	/**
	 * Apply accessor to field annotation, return default if not found.
	 */
	public static <T extends Annotation> boolean value(Enum<?> en, Class<T> annotationCls,
		ToBoolFunction<T> valueAccessor, boolean def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsBool(annotation);
	}

	/**
	 * Apply accessor to field annotation, return default if not found.
	 */
	public static <T extends Annotation> int value(Enum<?> en, Class<T> annotationCls,
		ToIntFunction<T> valueAccessor, int def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : valueAccessor.applyAsInt(annotation);
	}
}
