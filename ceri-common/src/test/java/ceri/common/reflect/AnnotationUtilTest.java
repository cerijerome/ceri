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
		assertPrivateConstructor(Annotations.class);
	}

	@Test
	public void testClassAnnotation() {
		assertNull(Annotations.annotation((Class<?>) null, A.class));
		assertNull(Annotations.annotation(getClass(), A.class));
		assertA(Annotations.annotation(E.class, A.class), "s", 123);
	}

	@Test
	public void testEnumAnnotation() {
		assertNull(Annotations.annotation((Enum<?>) null, A.class));
		assertNull(Annotations.annotation(Align.H.left, A.class));
		assertNull(Annotations.annotation(E.a, A.class));
		assertA(Annotations.annotation(E.b, A.class), "b", -1);
	}

	@Test
	public void testEnumAnnotations() {
		assertOrdered(Annotations.annotations((Enum<?>) null, A.class));
		assertOrdered(Annotations.annotations(Align.H.left, A.class));
		assertOrdered(Annotations.annotations(E.a, A.class));
		var annos = Annotations.annotations(E.b, A.class);
		assertEquals(annos.size(), 1);
		assertA(annos.get(0), "b", -1);
		annos = Annotations.annotations(E.c, A.class);
		assertEquals(annos.size(), 2);
		assertA(annos.get(0), "s", 3);
		assertA(annos.get(1), "c", -1);
	}

	@Test
	public void testAnnotationFromClass() {
		assertNull(Annotations.annotationFromClass(null, A.class));
		assertNull(Annotations.annotationFromClass(() -> {
			class C0 {}
			return C0.class;
		}, A.class));
		assertA(Annotations.annotationFromClass(() -> {
			@A(s = "C")
			class C0 {}
			return C0.class;
		}, A.class), "C", -1);
	}

	@Test
	public void testAnnotationFromEnum() {
		assertNull(Annotations.annotationFromEnum(null, A.class));
		assertNull(Annotations.annotationFromEnum(() -> {
			enum E0 {
				a
			}
			return E0.a;
		}, A.class));
		assertA(Annotations.annotationFromEnum(() -> {
			enum E0 {
				@A(s = "a")
				a
			}
			return E0.a;
		}, A.class), "a", -1);
	}

	@Test
	public void testClassValue() {
		assertEquals(Annotations.value((Class<?>) null, A.class, A::s), null);
		assertEquals(Annotations.value((Class<?>) null, A.class, A::s, "x"), "x");
		assertEquals(Annotations.value((Class<?>) null, A.class, A::i, 1), 1);
		assertEquals(Annotations.value((Class<?>) null, A.class, A::b, false), false);
		assertEquals(Annotations.value(E.class, A.class, A::s), "s");
		assertEquals(Annotations.value(E.class, A.class, A::s, "x"), "s");
		assertEquals(Annotations.value(E.class, A.class, A::i, 1), 123);
		assertEquals(Annotations.value(E.class, A.class, A::b, false), true);
	}

	@Test
	public void testEnumValue() {
		assertEquals(Annotations.value((Enum<?>) null, A.class, A::s), null);
		assertEquals(Annotations.value((Enum<?>) null, A.class, A::s, "x"), "x");
		assertEquals(Annotations.value((Enum<?>) null, A.class, A::i, 1), 1);
		assertEquals(Annotations.value((Enum<?>) null, A.class, A::b, false), false);
		assertEquals(Annotations.value(E.a, A.class, A::s), null);
		assertEquals(Annotations.value(E.a, A.class, A::s, "x"), "x");
		assertEquals(Annotations.value(E.a, A.class, A::i, 1), 1);
		assertEquals(Annotations.value(E.a, A.class, A::b, false), false);
		assertEquals(Annotations.value(E.b, A.class, A::s), "b");
		assertEquals(Annotations.value(E.b, A.class, A::s, "x"), "b");
		assertEquals(Annotations.value(E.b, A.class, A::i, 1), -1);
		assertEquals(Annotations.value(E.b, A.class, A::b, false), true);
	}

	private static void assertA(A anno, String s, int i) {
		assertEquals(anno.s(), s);
		assertEquals(anno.i(), i);
	}
}
