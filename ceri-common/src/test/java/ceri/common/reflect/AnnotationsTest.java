package ceri.common.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.function.Functions;
import ceri.common.log.Level;
import ceri.common.test.Assert;
import ceri.common.test.Captor;
import ceri.common.test.Testing;
import ceri.common.test.Testing.I;

public class AnnotationsTest {
	private static final AnnotatedType NULL = null;
	private static final Functions.Function<AnnotatedElement, Integer> NULL_FN = null;

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

	@I(-2)
	public static class B {
		@I(-1)
		public static class C {
			private static final Field F = Reflect.publicField(C.class, "f");
			private static final Method M = Reflect.publicMethod(C.class, "m");
			private static final Parameter MP = M.getParameters()[0];
			private static final RecordComponent RP = R.class.getRecordComponents()[0];
			private static final Field A = Reflect.publicField(C.class, "a");
			private static final Generics.Typed FT = Generics.typed(F);
			private static final Generics.Typed MPT = Generics.typed(MP);
			private static final Generics.Typed RPT = Generics.typed(RP);
			private static final Generics.Typed AT = Generics.typed(A);

			// List<Map<Integer, ? extends C>> f;
			public static final @I(1) List<@I(2) Map<@I(3) Integer, @I(4) ? extends @I(5) C>> f =
				null;

			// List<Map<Integer, ? extends C>> m(List<Map<Integer, ? extends C>> p);
			public @I(11) List<@I(12) Map<@I(13) Integer, @I(14) ? extends @I(15) C>>
				m(@I(21) List<@I(22) Map<@I(23) Integer, @I(24) ? extends @I(25) C>> p) {
				return p;
			}

			// R(int a)
			@I(31)
			public record R(@I(32) int a) {}

			// List<int[][]>[] a;
			public static final @I(41) List<@I(42) int @I(43) [] @I(44) []> @I(45) [] a = null;
		}
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Annotations.class);
	}

	@Test
	public void testNullType() {
		Assert.equal(Annotations.NULL.getType(), null);
		Assert.array(Annotations.NULL.getDeclaredAnnotations());
		Assert.array(Annotations.NULL.getAnnotations());
		Assert.equal(Annotations.NULL.getAnnotation(I.class), null);
	}

	@Test
	public void testResolvableOf() {
		assertResolvable(Annotations.Resolvable.of(null, null), null, Annotations.NULL);
		assertResolvable(Annotations.Resolvable.of(B.C.A, B.C.A), null, B.C.A);
		assertResolvable(Annotations.Resolvable.of(B.class, B.C.A), B.class, B.C.A);
	}

	@Test
	public void testResolvableUnwrap() {
		Assert.same(Annotations.Resolvable.unwrap(null), null);
		Assert.same(Annotations.Resolvable.unwrap(B.C.F), B.C.F);
		var nested = Annotations.resolvable(B.class, B.C.A, B.C.F);
		Assert.same(Annotations.Resolvable.unwrap(nested), B.C.F);
	}

	@Test
	public void testResolvableResolution() {
		assertI(Annotations.resolvable(B.class, B.C.A, B.C.F), 1, 41, -2, null);
		assertI(Annotations.resolvable(B.C.F, B.C.A, B.class), -2, 41, 1, -1, -2, null);
	}

	@Test
	public void testResolvableElement() {
		var n = Annotations.Resolvable.of(B.C.F, null);
		Assert.array(n.getAnnotations(), Annotations.annotation(B.C.F, I.class));
		Assert.array(n.getDeclaredAnnotations(), Annotations.annotation(B.C.F, I.class));
	}

	@Test
	public void testResolvableToString() {
		Assert.string(Annotations.resolvable(NULL, null), "null");
		Assert.string(Annotations.resolvable(NULL, B.C.A), B.C.A.toString());
		Assert.string(Annotations.resolvable(B.C.A, B.C.F), "(%s)", B.C.F);
	}

	@Test
	public void testResolvable() {
		assertI(Annotations.resolvable(), (Integer) null);
		assertI(Annotations.resolvable(B.C.class), -1, -2, null);
		assertI(Annotations.resolvable(B.class, B.C.MP), 21, -2, null);
	}

	@Test
	public void testNodeOf() {
		assertNode(Annotations.Node.of(null, null), Generics.Typed.NULL, Annotations.NULL);
		assertNode(Annotations.Node.of(B.C.FT, null), B.C.FT, Annotations.NULL);
		assertNode(Annotations.Node.of(null, B.C.F), Generics.Typed.NULL, B.C.F);
	}

	@Test
	public void testNodeSub() {
		var node = Annotations.Node.of(B.C.FT, B.C.class);
		assertI(node, -1, -2, null);
		assertI(node.sub(), 1, -1, -2, null);
		assertI(node.sub((Generics.Typed) null), -1, -2, null);
		assertI(node.sub(node), -1, -2, null);
		assertI(node.sub(Annotations.node(B.C.FT)), 1, -1, -2, null);
		assertI(node.sub(Annotations.node(B.C.A)), 41, -1, -2, null);
		assertI(node.sub(B.C.A), 41, -1, -2, null);
		assertI(node.sub((Functions.Operator<Generics.Typed>) null), -1, -2, null);
		assertI(node.sub(t -> t.type(0)), 2, -1, -2, null);
		assertI(Annotations.node(B.C.FT).sub(B.C.FT.type(0).type(1)), 4, 1);
		assertI(Annotations.node(B.C.FT).sub(B.C.FT.type(0).type(2)), 1);
	}

	@Test
	public void testNodeTraversal() {
		var node = Annotations.node(B.C.F);
		assertI(node, 1, -1, -2, null);
		assertI(node.type(0).type(1).upper(0), 5, 4, 2, 1, -1, -2, null);
		assertI(node.type(0).type(1).lower(0), 4, 2, 1, -1, -2, null);
		node = Annotations.node(B.C.A);
		assertI(node, 41, -1, -2, null);
		assertI(node.component().type(0).components(), 42, 43, 41, 41, -1, -2, null);
	}

	@Test
	public void testNode() {
		assertI(Annotations.node(B.C.class), -1, -2, null);
		assertI(Annotations.nodeReturn(B.C.M), 11, -1, -2, null);
		assertI(Annotations.node(B.C.MP), 21, 11, -1, -2, null);
		assertI(Annotations.node(B.C.MPT), 21);
		assertI(Annotations.node(B.C.MP.getAnnotatedType()), 21);
	}

	@Test
	public void testTargets() {
		Assert.unordered(Annotations.targets(null));
		Assert.unordered(Annotations.targets(A.class), ElementType.FIELD, ElementType.TYPE);
	}

	@Test
	public void testHasAnnotation() {
		Assert.equal(Annotations.has((Class<?>) null, A.class), false);
		Assert.equal(Annotations.has(E.class, null), false);
		Assert.equal(Annotations.has(E.class, A.class), true);
		Assert.equal(Annotations.has(getClass(), A.class), false);
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

	@Test
	public void testListValue() {
		var c = Reflect.enumToField(E.c);
		Assert.equal(Annotations.reduceValue(c, A.class, List::size), 2);
	}

	@Test
	public void testResolve() {
		Assert.equal(Annotations.resolve(B.C.F, NULL_FN), null);
		Assert.equal(Annotations.resolve(B.C.F, I.class, null), null);
		Assert.equal(Annotations.resolve(B.C.F, I.class, I::value), 1);
		Assert.equal(Annotations.resolve(B.C.F, I.class, I::value, -1), 1);
		Assert.equal(Annotations.resolve(String.class, I.class, I::value, -1), -1);
		Assert.equal(Annotations.resolve(null, (e, _) -> Testing.i(e), -3), -3);
		Assert.equal(Annotations.resolve(B.C.F, null, -3), -3);
		Assert.equal(Annotations.resolve(B.C.F, (e, _) -> Testing.i(e), -3), 1);
		Assert.equal(Annotations.resolve(String.class, (e, _) -> Testing.i(e), -1), -1);
	}

	@Test
	public void testParent() {
		Assert.equal(Annotations.parent(null), null);
		Assert.equal(Annotations.parent(B.C.RP), B.C.R.class);
	}

	@Test
	public void testElement() {
		Assert.equal(Annotations.element(null), null);
		Assert.equal(Annotations.element(B.C.RPT), B.C.RP.getAnnotatedType());
	}

	@Test
	public void testComponent() {
		Assert.equal(Annotations.component(null), null);
		assertI(B.C.A, 41, -1, -2, null);
		assertI(Annotations.component(B.C.A), 41, -1, -2, null); // no change
		assertI(B.C.AT.annotated(), 45);
		assertI(Annotations.component(B.C.AT.annotated()), 41);
		assertI(B.C.AT.component().type(0).annotated(), 43);
		assertI(Annotations.component(B.C.AT.component().type(0).annotated()), 42);
	}

	private static void assertA(A anno, String s, int i) {
		Assert.equal(anno.s(), s);
		Assert.equal(anno.i(), i);
	}

	private static void assertI(Annotations.Node node, Integer... values) {
		assertI(node.element(), values);
	}

	private static void assertI(AnnotatedElement element, Integer... values) {
		var captor = Captor.<Integer>of();
		Annotations.resolve(element, e -> captor.accept(Testing.i(e), null));
		captor.verify(values);
	}

	private static void assertResolvable(Annotations.Resolvable resolvable, AnnotatedElement parent,
		AnnotatedElement element) {
		Assert.equal(resolvable.parent(), parent);
		Assert.equal(resolvable.element(), element);
	}

	private static void assertNode(Annotations.Node node, Generics.Typed typed,
		AnnotatedElement element) {
		Assert.equal(node.typed(), typed);
		Assert.equal(node.element(), element);
	}
}
