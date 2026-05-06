package ceri.common.reflect;

import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.array.Dimensions;
import ceri.common.reflect.Generics.Typed;
import ceri.common.test.Assert;
import ceri.common.test.Testing;
import ceri.common.test.Testing.I;

public class GenericsTest {
	private static final Type TYPE = new Type() {};
	private static final Type NULL = null;
	private final Field listN2d = Reflect.publicField(Types.class, "listN2d");
	private final Field listT = Reflect.publicField(Types.class, "listT");
	private final Field listT2 = Reflect.publicField(Types.class, "listT2");
	private final Field listT3 = Reflect.publicField(Types.class, "listT3");
	private final Field list = Reflect.publicField(Types.class, "list");
	private final Field listS = Reflect.publicField(Types.class, "listS");
	private final Field mapListListT = Reflect.publicField(Types.class, "mapListListT");
	private final Field inner = Reflect.publicField(Types.class, "inner");
	private final Field nested = Reflect.publicField(Types.class, "nested");
	private final Method method = Reflect.publicMethod(Types.class, "method");
	private final TypeVariable<Method> u = method.getTypeParameters()[0];
	private final Parameter t2d = method.getParameters()[0];
	private final RecordComponent rp = Types.R.class.getRecordComponents()[0];
	private final Generics.Token<List<?>> t_list = new Generics.Token<>() {};
	private final Generics.Token<List<?>[]> t_listd = new Generics.Token<>() {};
	private final Generics.Token<List<? super String>> t_listS = new Generics.Token<>() {};
	private final Generics.Token<List<? extends Number>> t_listN = new Generics.Token<>() {};
	private final Generics.Token<List<? extends Number>[][]> t_listN2d = new Generics.Token<>() {};
	private final Types<?> types = new Types<>();

	@I(-1)
	static class Types<T> {
		public @I(1) List<?> list;
		public List<T> listT;
		public List<? extends T> listT2;
		public List<? super T> listT3;
		public List<? super String> listS;
		public List<? extends Number>[][] listN2d;
		public Map<List<?>, List<T>> mapListListT;
		public Inner<String> inner = new Inner<>();
		public Nested<String> nested = new Nested<>();
		private Generics.Token<List<T>> t_listT = new Generics.Token<>() {};
		private Generics.Token<T[][]> t_t2d = new Generics.Token<>() {};

		@SuppressWarnings({ "unused" })
		public class Inner<U> {}

		@SuppressWarnings({ "unused" })
		public static class Nested<U> {}

		@SuppressWarnings("unused")
		public <U extends Number & Comparable<U>> List<T> method(T[][] t2d, U u) {
			return null;
		}

		public record R(List<?> p) {}

		public Generics.Token<Types<T>> token() {
			return new Generics.Token<>() {};
		}
	}

	@Test
	public void testToken() {
		var t = new Generics.Token<>() {};
		Assert.equal(t.type(), Object.class);
		Assert.equal(t.cls(), Object.class);
		Assert.equal(t_list.cls(), List.class);
		Assert.equal(new Generics.Token<List<?>[]>() {}.cls(), List[].class);
	}

	@Test
	public void testTokenEquals() {
		var t = new Generics.Token<List<String>>() {};
		var eq = new Generics.Token<List<String>>() {};
		var ne0 = new Generics.Token<List<?>>() {};
		var ne1 = new Generics.Token<List<? super String>>() {};
		@SuppressWarnings("rawtypes")
		var ne2 = new Generics.Token<List>() {};
		var ne3 = new Generics.Token<ArrayList<String>>() {};
		Testing.exerciseEquals(t, eq);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void testTokenString() {
		Assert.string(t_listN2d, "List<? extends Number>[][]");
	}

	@Test
	public void testGenericArrayIsNull() {
		Assert.equal(Generics.Array.isNull(null), true);
		Assert.equal(Generics.Array.isNull(Generics.Array.NULL), true);
		Assert.equal(Generics.Array.isNull(Generics.Typed.NULL.array()), true);
		Assert.equal(Generics.Array.isNull(Generics.Typed.VOID.array()), false);
		Assert.equal(Generics.Array.isNull(Generics.typed(int[].class).array()), false);
	}

	@Test
	public void testGenericArrayOf() {
		assertArray(Generics.Array.of(null, 0), Generics.Typed.NULL, 0);
		Assert.string(Generics.Array.of(int.class, 2), "int[][]");
	}

	@Test
	public void testArrayStringRepresentation() {
		var dims = Dimensions.of(1, 2, 3);
		Assert.string(Generics.Array.of(int.class, 0).toString(dims), "int");
		Assert.string(Generics.Array.of(int.class, 1).toString(dims), "int[1]");
		Assert.string(Generics.Array.of(int.class, 3).toString(dims), "int[1][2][3]");
	}

	@Test
	public void testTypedEquals() {
		Assert.same(Generics.typed(Object.class), Generics.typed(Object.class));
		var t = new Generics.Token<List<String>>() {}.typed;
		var eq = new Generics.Token<List<String>>() {}.typed;
		var ne0 = new Generics.Token<List<?>>() {}.typed;
		var ne1 = new Generics.Token<ArrayList<String>>() {}.typed;
		var ne2 = new Generics.Token<List<? super String>>() {}.typed;
		var ne3 = Generics.typed(void.class);
		Testing.exerciseEquals(t, eq);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void testTypedIsNull() {
		Assert.equal(Generics.Typed.isNull(null), true);
		Assert.equal(Generics.Typed.isNull(Generics.Typed.NULL), true);
		Assert.equal(Generics.Typed.isNull(Generics.Typed.VOID), false);
	}

	@Test
	public void testTypedAnnotated() {
		Assert.equal(Generics.Typed.NULL.annotated(), null);
		Assert.equal(Testing.i(Generics.typed(Types.class).annotated()), -1);
		Assert.equal(Testing.i(Generics.typed(list).annotated()), 1);
	}

	@Test
	public void testTypedOwner() {
		assertType(Generics.Typed.NULL.owner(), NULL);
		assertType(Generics.typed(inner).owner(), types.token());
		assertType(Generics.typed(nested).owner(), Types.class);
		assertType(d(Generics.typed(inner)).owner(), types.token());
		assertType(d(Generics.typed(nested)).owner(), Types.class);
	}

	@Test
	public void testTypedFrom() {
		Assert.equal(Generics.typedFrom(null), Typed.NULL);
		assertType(Generics.typedFrom(new int[1][1]), int[][].class);
		assertType(Generics.typedFrom(TYPE), TYPE);
		assertType(Generics.typedFrom(Annotations.NULL), NULL);
		assertType(Generics.typedFrom(listS), t_listS);
		assertType(Generics.typedFrom(t2d), types.t_t2d);
	}

	@Test
	public void testTypedParameter() {
		Assert.equal(Generics.typed((Parameter) null).isNull(), true);
		Assert.equal(Generics.typed(t2d).component().component().varName(), "T");
		Assert.equal(Generics.typed(t2d).components().varName(), "T");
	}

	@Test
	public void testTypedReturn() {
		assertType(Generics.typedReturn((Method) null), NULL);
		Assert.equal(Generics.typedReturn(method).cls(), List.class);
		Assert.equal(Generics.typedReturn(method).type(0).varName(), "T");
	}

	@Test
	public void testTypedField() {
		assertType(Generics.typed((Field) null), NULL);
	}

	@Test
	public void testTypedRecordComponent() {
		assertType(Generics.typed((RecordComponent) null), NULL);
		assertType(Generics.typed(rp), t_list);
	}

	@Test
	public void testTypedIsUnbounded() {
		Assert.equal(Generics.Typed.NULL.isUnbounded(), false);
		Assert.equal(Generics.typed(list).isUnbounded(), false);
		Assert.equal(Generics.typed(list).type(0).isUnbounded(), true);
		Assert.equal(Generics.typed(listT).type(0).isUnbounded(), true);
		Assert.equal(Generics.typed(listT2).type(0).isUnbounded(), false);
		Assert.equal(Generics.typed(listT3).type(0).isUnbounded(), false);
		Assert.equal(Generics.typed(listS).type(0).isUnbounded(), false);
		Assert.equal(Generics.typed(listN2d).component().component().type(0).isUnbounded(), false);
		Assert.equal(Generics.typed(mapListListT).type(0).type(0).isUnbounded(), true);
		Assert.equal(new Generics.Token<>() {}.typed.isUnbounded(), false); // Object.class
	}

	@Test
	public void testTypedVarName() {
		Assert.equal(Generics.typed(u).varName(), "U");
		Assert.equal(Generics.typed(listT).type(0).varName(), "T");
		Assert.equal(Generics.typed(listT2).type(0).varName(), "");
		Assert.equal(Generics.typed(listT2).type(0).upper(0).varName(), "T");
		Assert.equal(Generics.typed(listT3).type(0).lower(0).varName(), "T");
		Assert.equal(Generics.typed(mapListListT).type(1).type(0).varName(), "T");
	}

	@Test
	public void testTypedIsArray() {
		Assert.equal(Generics.Typed.NULL.isArray(), false);
		Assert.equal(Generics.typed(int.class).isArray(), false);
		Assert.equal(Generics.typed(int[].class).isArray(), true);
		Assert.equal(Generics.typed(listN2d).isArray(), true);
	}

	@Test
	public void testTypedArray() {
		assertArray(Generics.Typed.NULL.array(), Generics.Typed.NULL, 0);
		assertArray(Generics.typed(listN2d).array(), t_listN, 2);
		assertArray(d(Generics.typed(listN2d)).array(), t_listN, 2);
		Assert.string(Generics.typed(listN2d).array(), "List<? extends Number>[][]");
	}

	@Test
	public void testGenericArrayIsArray() {
		Assert.equal(Generics.Array.NULL.isArray(), false);
		Assert.equal(Generics.Typed.NULL.array().isArray(), false);
		Assert.equal(Generics.Typed.VOID.array().isArray(), false);
		Assert.equal(Generics.typed(int[].class).array().isArray(), true);
	}

	@Test
	public void testGenericArrayClass() {
		Assert.equal(Generics.Array.NULL.cls(), null);
		Assert.equal(new Generics.Array(null, 1).cls(), null);
		Assert.equal(Generics.Typed.NULL.array().cls(), null);
		Assert.equal(Generics.Typed.VOID.array().cls(), void.class);
		Assert.equal(Generics.typed(int[].class).array().cls(), int.class);
	}

	@Test
	public void testTypedTypes() {
		assertType(Generics.Typed.NULL.type(0), NULL);
		assertType(Generics.typed(TYPE).type(1), NULL);
		assertType(Generics.typed(Annotations.NULL).type(0), NULL);
		assertType(Generics.typed(mapListListT).type(0), t_list);
		assertType(Generics.typed(mapListListT).type(1), types.t_listT);
		assertType(Generics.typed(mapListListT).type(2), NULL);
		assertType(d(Generics.typed(mapListListT)).type(0), t_list);
		assertType(d(Generics.typed(mapListListT)).type(1), types.t_listT);
		assertType(d(Generics.typed(mapListListT)).type(2), NULL);
		assertType(Generics.Typed.NULL.lower(0).type(0), NULL);
		assertType(Generics.Typed.NULL.lower(1).type(0), NULL);
	}

	@Test
	public void testTypedUpper() {
		var t = new Generics.Token<>() {};
		assertType(Generics.Typed.NULL.upper(0), NULL);
		assertType(Generics.Typed.NULL.upper(0), NULL); // uses cached value
		assertType(Generics.typed(TYPE).upper(0), Object.class);
		assertType(Generics.typed(Annotations.NULL).upper(0), Object.class);
		assertType(t.typed.upper(0), Object.class);
		assertType(t_list.typed.upper(0), t_list);
		assertType(t_list.typed.type(0).upper(0), Object.class);
		Assert.equal(t_listd.typed.upper(0).cls(), List[].class);
		assertType(types.t_listT.typed.type(0).upper(0), Object.class);
		assertType(d(t.typed).upper(0), Object.class);
		assertType(d(t_list.typed).upper(0), t_list);
		assertType(d(t_list.typed).type(0).upper(0), Object.class);
		Assert.equal(d(t_listd.typed).upper(0).cls(), List[].class);
		assertType(d(types.t_listT.typed).type(0).upper(0), Object.class);
	}

	@Test
	public void testTypedLower() {
		var t = new Generics.Token<>() {};
		Assert.ordered(Generics.Typed.NULL.lower());
		Assert.ordered(Generics.typed(TYPE).lower());
		Assert.ordered(Generics.typed(Annotations.NULL).lower());
		assertType(t.typed.lower(0), Object.class);
		assertType(t_list.typed.lower(0), t_list);
		assertType(t_list.typed.type(0).lower(0), NULL);
		Assert.equal(t_listd.typed.lower(0).cls(), List[].class);
		assertType(types.t_listT.typed.type(0).lower(0), NULL);
		assertType(d(t.typed).lower(0), Object.class);
		assertType(d(t_list.typed).lower(0), t_list);
		assertType(d(t_list.typed).type(0).lower(0), NULL);
		Assert.equal(d(t_listd.typed).lower(0).cls(), List[].class);
		assertType(d(types.t_listT.typed).type(0).lower(0), NULL);
	}

	@Test
	public void testTypedAnnotation() {
		Assert.same(Generics.typed((AnnotatedType) null), Generics.Typed.NULL);
	}

	@Test
	public void testTypedFullString() {
		Assert.string(Generics.typed(listN2d).fullString(),
			"java.util.List<? extends java.lang.Number>[][]");
	}

	@Test
	public void testTypedString() {
		Assert.string(Generics.Typed.NULL, "null");
		Assert.string(Generics.typed(listT3), "List<? super T>");
		Assert.string(Generics.typed(listN2d), "List<? extends Number>[][]");
		Assert.string(Generics.typed(mapListListT), "Map<List<?>, List<T>>");
	}

	@Test
	public void testClassFrom() {
		Assert.same(Generics.classFrom((AnnotatedType) null), null);
		Assert.same(Generics.classFrom(Annotations.NULL), null);
		Assert.same(Generics.classFrom(listN2d.getAnnotatedType()), List[][].class);
	}

	@Test
	public void testFullString() {
		Assert.string(Generics.fullString(null), "null");
		Assert.string(Generics.fullString(List.class),
			"public abstract interface java.util.List<E>");
		Assert.string(Generics.fullString(list), "public java.util.List<?> %s.list",
			Types.class.getName());
		Assert.string(Generics.fullString(Generics.typed(listT).type(0).raw()), "T");
		Assert.string(Generics.fullString(u),
			"U extends java.lang.Number & java.lang.Comparable<U>");
		Assert.string(Generics.fullString(method),
			"public <%s> java.util.List<T> %s.method(T[][],U)", Generics.fullString(u),
			Types.class.getName());
		Assert.string(Generics.fullString(listN2d),
			"public java.util.List<? extends java.lang.Number>[][] %s.listN2d",
			Types.class.getName());
		Assert.string(Generics.fullString(TYPE), TYPE.toString());
	}

	/**
	 * Change to direct type without annotations.
	 */
	private static Generics.Typed d(Generics.Typed typed) {
		return Generics.typed(typed.raw());
	}

	private static void assertType(Generics.Typed typed, Type type) {
		Assert.equal(typed.raw(), type);
	}

	private static void assertType(Generics.Typed typed, Generics.Token<?> token) {
		assertType(typed, token.type());
	}

	private static void assertArray(Generics.Array array, Type type, int dims) {
		assertType(array.component(), type);
		Assert.equal(array.dimensions(), dims);
	}

	private static void assertArray(Generics.Array array, Generics.Typed typed, int dims) {
		assertArray(array, typed.raw(), dims);
	}

	private static void assertArray(Generics.Array array, Generics.Token<?> token, int dims) {
		assertArray(array, token.typed, dims);
	}
}
