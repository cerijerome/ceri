package ceri.common.reflect;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Test;
import ceri.common.util.Align;

public class AnnotationUtilTest {

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	@Repeatable(As.class)
	private @interface A {
		String s() default "s";

		int i() default -1;

		boolean b() default true;
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target({ ElementType.FIELD, ElementType.TYPE })
	private @interface As {
		A[] value() default {};
	}

	@A(i = 123)
	private static enum E {
		a,
		@A(s = "b")
		b,
		@A(i = 3)
		@A(s = "c")
		c;
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(AnnotationUtil.class);
	}

	@Test
	public void testClassAnnotation() {
		assertNull(AnnotationUtil.annotation((Class<?>) null, A.class));
		assertNull(AnnotationUtil.annotation(getClass(), A.class));
		assertA(AnnotationUtil.annotation(E.class, A.class), "s", 123);
	}

	@Test
	public void testEnumAnnotation() {
		assertNull(AnnotationUtil.annotation((Enum<?>) null, A.class));
		assertNull(AnnotationUtil.annotation(Align.H.left, A.class));
		assertNull(AnnotationUtil.annotation(E.a, A.class));
		assertA(AnnotationUtil.annotation(E.b, A.class), "b", -1);
	}

	@Test
	public void testEnumAnnotations() {
		assertOrdered(AnnotationUtil.annotations((Enum<?>) null, A.class));
		assertOrdered(AnnotationUtil.annotations(Align.H.left, A.class));
		assertOrdered(AnnotationUtil.annotations(E.a, A.class));
		var annos = AnnotationUtil.annotations(E.b, A.class);
		assertEquals(annos.size(), 1);
		assertA(annos.get(0), "b", -1);
		annos = AnnotationUtil.annotations(E.c, A.class);
		assertEquals(annos.size(), 2);
		assertA(annos.get(0), "s", 3);
		assertA(annos.get(1), "c", -1);
	}

	@Test
	public void testAnnotationFromClass() {
		assertNull(AnnotationUtil.annotationFromClass(null, A.class));
		assertNull(AnnotationUtil.annotationFromClass(() -> {
			class C0 {}
			return C0.class;
		}, A.class));
		assertA(AnnotationUtil.annotationFromClass(() -> {
			@A(s = "C")
			class C0 {}
			return C0.class;
		}, A.class), "C", -1);
	}

	@Test
	public void testAnnotationFromEnum() {
		assertNull(AnnotationUtil.annotationFromEnum(null, A.class));
		assertNull(AnnotationUtil.annotationFromEnum(() -> {
			enum E0 {
				a
			}
			return E0.a;
		}, A.class));
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
