package ceri.common.reflect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Test;
import ceri.common.util.Align;

public class AnnotationUtilTest {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	private static @interface A {
		String s() default "s";

		int i() default -1;

		boolean b() default true;
	}

	@A(i = 123)
	private static enum E {
		a,
		@A(s = "b")
		b,
		c;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(AnnotationUtil.class);
	}

	@Test
	public void testClassAnnotation() {
		assertEquals(AnnotationUtil.annotation((Class<?>) null, A.class), null);
		assertEquals(AnnotationUtil.annotation(getClass(), A.class), null);
		assertA(AnnotationUtil.annotation(E.class, A.class), "s", 123);
	}

	@Test
	public void testEnumAnnotation() {
		assertEquals(AnnotationUtil.annotation((Enum<?>) null, A.class), null);
		assertEquals(AnnotationUtil.annotation(Align.H.left, A.class), null);
		assertEquals(AnnotationUtil.annotation(E.a, A.class), null);
		assertA(AnnotationUtil.annotation(E.b, A.class), "b", -1);
	}

	@Test
	public void testAnnotationFromClass() {
		assertEquals(AnnotationUtil.annotationFromClass(null, A.class), null);
		assertEquals(AnnotationUtil.annotationFromClass(() -> {
			class C0 {}
			return C0.class;
		}, A.class), null);
		assertA(AnnotationUtil.annotationFromClass(() -> {
			@A(s = "C")
			class C0 {}
			return C0.class;
		}, A.class), "C", -1);
	}

	@Test
	public void testAnnotationFromEnum() {
		assertEquals(AnnotationUtil.annotationFromEnum(null, A.class), null);
		assertEquals(AnnotationUtil.annotationFromEnum(() -> {
			enum E0 {
				a
			}
			return E0.a;
		}, A.class), null);
		assertA(AnnotationUtil.annotationFromEnum(() -> {
			enum E0 {
				@A(s = "a")
				a
			}
			return E0.a;
		}, A.class), "a", -1);
	}

	@Test
	public void testClassValue() {
		assertEquals(AnnotationUtil.value((Class<?>) null, A.class, A::s), null);
		assertEquals(AnnotationUtil.value((Class<?>) null, A.class, A::s, "x"), "x");
		assertEquals(AnnotationUtil.value((Class<?>) null, A.class, A::i, 1), 1);
		assertEquals(AnnotationUtil.value((Class<?>) null, A.class, A::b, false), false);
		assertEquals(AnnotationUtil.value(E.class, A.class, A::s), "s");
		assertEquals(AnnotationUtil.value(E.class, A.class, A::s, "x"), "s");
		assertEquals(AnnotationUtil.value(E.class, A.class, A::i, 1), 123);
		assertEquals(AnnotationUtil.value(E.class, A.class, A::b, false), true);
	}

	@Test
	public void testEnumValue() {
		assertEquals(AnnotationUtil.value((Enum<?>) null, A.class, A::s), null);
		assertEquals(AnnotationUtil.value((Enum<?>) null, A.class, A::s, "x"), "x");
		assertEquals(AnnotationUtil.value((Enum<?>) null, A.class, A::i, 1), 1);
		assertEquals(AnnotationUtil.value((Enum<?>) null, A.class, A::b, false), false);
		assertEquals(AnnotationUtil.value(E.a, A.class, A::s), null);
		assertEquals(AnnotationUtil.value(E.a, A.class, A::s, "x"), "x");
		assertEquals(AnnotationUtil.value(E.a, A.class, A::i, 1), 1);
		assertEquals(AnnotationUtil.value(E.a, A.class, A::b, false), false);
		assertEquals(AnnotationUtil.value(E.b, A.class, A::s), "b");
		assertEquals(AnnotationUtil.value(E.b, A.class, A::s, "x"), "b");
		assertEquals(AnnotationUtil.value(E.b, A.class, A::i, 1), -1);
		assertEquals(AnnotationUtil.value(E.b, A.class, A::b, false), true);
	}

	private static void assertA(A anno, String s, int i) {
		assertEquals(anno.s(), s);
		assertEquals(anno.i(), i);
	}
}
