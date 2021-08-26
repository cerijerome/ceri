package ceri.common.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import ceri.common.function.ToBooleanFunction;

/**
 * Utility methods to support annotations.
 */
public class AnnotationUtil {

	private AnnotationUtil() {}

	/**
	 * Get TYPE annotation from class, or return null.
	 */
	public static <T extends Annotation> T annotation(Class<?> cls, Class<T> annotationCls) {
		return cls == null ? null : cls.getAnnotation(annotationCls);
	}

	/**
	 * Get FIELD annotation from enum, or return null.
	 */
	public static <T extends Annotation> T annotation(Enum<?> en, Class<T> annotationCls) {
		Field field = ReflectUtil.enumField(en);
		return field == null ? null : field.getAnnotation(annotationCls);
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
	public static <T extends Annotation> T annotationFromClass(Supplier<Class<?>> clsSupplier,
		Class<T> annotationCls) {
		return clsSupplier == null ? null : annotation(clsSupplier.get(), annotationCls);
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
	 * Apply function to TYPE annotation, return null if not found.
	 */
	public static <T extends Annotation, R> R value(Class<?> cls, Class<T> annotationCls,
		Function<T, R> fn) {
		return value(cls, annotationCls, fn, null);
	}

	/**
	 * Apply function to TYPE annotation, return default if not found.
	 */
	public static <T extends Annotation, R> R value(Class<?> cls, Class<T> annotationCls,
		Function<T, R> fn, R def) {
		T annotation = annotation(cls, annotationCls);
		return annotation == null ? def : fn.apply(annotation);
	}

	/**
	 * Apply function to TYPE annotation, return default if not found.
	 */
	public static <T extends Annotation> boolean value(Class<?> cls, Class<T> annotationCls,
		ToBooleanFunction<T> fn, boolean def) {
		T annotation = annotation(cls, annotationCls);
		return annotation == null ? def : fn.applyAsBoolean(annotation);
	}

	/**
	 * Apply function to TYPE annotation, return default if not found.
	 */
	public static <T extends Annotation> int value(Class<?> cls, Class<T> annotationCls,
		ToIntFunction<T> fn, int def) {
		T annotation = annotation(cls, annotationCls);
		return annotation == null ? def : fn.applyAsInt(annotation);
	}

	/**
	 * Apply function to FIELD annotation, return null if not found.
	 */
	public static <T extends Annotation, R> R value(Enum<?> en, Class<T> annotationCls,
		Function<T, R> fn) {
		return value(en, annotationCls, fn, null);
	}

	/**
	 * Apply function to FIELD annotation, return default if not found.
	 */
	public static <T extends Annotation, R> R value(Enum<?> en, Class<T> annotationCls,
		Function<T, R> fn, R def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : fn.apply(annotation);
	}

	/**
	 * Apply function to FIELD annotation, return default if not found.
	 */
	public static <T extends Annotation> boolean value(Enum<?> en, Class<T> annotationCls,
		ToBooleanFunction<T> fn, boolean def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : fn.applyAsBoolean(annotation);
	}

	/**
	 * Apply function to FIELD annotation, return default if not found.
	 */
	public static <T extends Annotation> int value(Enum<?> en, Class<T> annotationCls,
		ToIntFunction<T> fn, int def) {
		T annotation = annotation(en, annotationCls);
		return annotation == null ? def : fn.applyAsInt(annotation);
	}

}
