package ceri.common.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.Test;
import ceri.common.log.Level;
import ceri.common.test.Assert;

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
		Assert.privateConstructor(Annotations.class);
	}

	@Test
	public void testClassAnnotation() {
		Assert.isNull(Annotations.annotation((Class<?>) null, A.class));
		Assert.isNull(Annotations.annotation(getClass(), A.class));
		assertA(Annotations.annotation(E.class, A.class), "s", 123);
	}

	@Test
	public void testEnumAnnotation() {
		Assert.isNull(Annotations.annotation((Enum<?>) null, A.class));
		Assert.isNull(Annotations.annotation(Level.WARN, A.class));
		Assert.isNull(Annotations.annotation(E.a, A.class));
		assertA(Annotations.annotation(E.b, A.class), "b", -1);
	}

	@Test
	public void testEnumAnnotations() {
		Assert.ordered(Annotations.annotations((Enum<?>) null, A.class));
		Assert.ordered(Annotations.annotations(Level.ALL, A.class));
		Assert.ordered(Annotations.annotations(E.a, A.class));
		var annos = Annotations.annotations(E.b, A.class);
		Assert.equal(annos.size(), 1);
		assertA(annos.get(0), "b", -1);
		annos = Annotations.annotations(E.c, A.class);
		Assert.equal(annos.size(), 2);
		assertA(annos.get(0), "s", 3);
		assertA(annos.get(1), "c", -1);
	}

	@Test
	public void testAnnotationFromClass() {
		Assert.isNull(Annotations.annotationFromClass(null, A.class));
		Assert.isNull(Annotations.annotationFromClass(() -> {
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
		Assert.isNull(Annotations.annotationFromEnum(null, A.class));
		Assert.isNull(Annotations.annotationFromEnum(() -> {
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
		Assert.equal(Annotations.value((Class<?>) null, A.class, A::s), null);
		Assert.equal(Annotations.value((Class<?>) null, A.class, A::s, "x"), "x");
		Assert.equal(Annotations.value((Class<?>) null, A.class, A::i, 1), 1);
		Assert.equal(Annotations.value((Class<?>) null, A.class, A::b, false), false);
		Assert.equal(Annotations.value(E.class, A.class, A::s), "s");
		Assert.equal(Annotations.value(E.class, A.class, A::s, "x"), "s");
		Assert.equal(Annotations.value(E.class, A.class, A::i, 1), 123);
		Assert.equal(Annotations.value(E.class, A.class, A::b, false), true);
	}

	@Test
	public void testEnumValue() {
		Assert.equal(Annotations.value((Enum<?>) null, A.class, A::s), null);
		Assert.equal(Annotations.value((Enum<?>) null, A.class, A::s, "x"), "x");
		Assert.equal(Annotations.value((Enum<?>) null, A.class, A::i, 1), 1);
		Assert.equal(Annotations.value((Enum<?>) null, A.class, A::b, false), false);
		Assert.equal(Annotations.value(E.a, A.class, A::s), null);
		Assert.equal(Annotations.value(E.a, A.class, A::s, "x"), "x");
		Assert.equal(Annotations.value(E.a, A.class, A::i, 1), 1);
		Assert.equal(Annotations.value(E.a, A.class, A::b, false), false);
		Assert.equal(Annotations.value(E.b, A.class, A::s), "b");
		Assert.equal(Annotations.value(E.b, A.class, A::s, "x"), "b");
		Assert.equal(Annotations.value(E.b, A.class, A::i, 1), -1);
		Assert.equal(Annotations.value(E.b, A.class, A::b, false), true);
	}

	private static void assertA(A anno, String s, int i) {
		Assert.equal(anno.s(), s);
		Assert.equal(anno.i(), i);
	}
}
