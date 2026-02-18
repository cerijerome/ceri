package ceri.common.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.reflect.Generics.Typed;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class GenericsTest {
	private final Field listN2d = Reflect.publicField(Types.class, "listN2d");
	private final Field listT = Reflect.publicField(Types.class, "listT");
	private final Field listT2 = Reflect.publicField(Types.class, "listT2");
	private final Field listT3 = Reflect.publicField(Types.class, "listT3");
	private final Field list = Reflect.publicField(Types.class, "list");
	private final Field listS = Reflect.publicField(Types.class, "listS");
	private final Field mapListListT = Reflect.publicField(Types.class, "mapListListT");
	private final Method method = Reflect.publicMethod(Types.class, "method");
	private final TypeVariable<Method> u = method.getTypeParameters()[0];
	private final Parameter t2d = method.getParameters()[0];

	static class Types<T> {
		public List<?> list;
		public List<T> listT;
		public List<? extends T> listT2;
		public List<? super T> listT3;
		public List<? super String> listS;
		public List<? extends Number>[][] listN2d;
		public Map<List<?>, List<T>> mapListListT;

		@SuppressWarnings("unused")
		public <U extends Number & Comparable<U>> List<T> method(T[][] t2d, U u) {
			return null;
		}
	}

	@Test
	public void testTyped() {
		Assert.same(Generics.Typed.of(Object.class), Generics.Typed.of(Object.class));
		var t = new Generics.Token<List<String>>() {}.typed;
		var eq = new Generics.Token<List<String>>() {}.typed;
		var ne0 = new Generics.Token<List<?>>() {}.typed;
		var ne1 = new Generics.Token<ArrayList<String>>() {}.typed;
		var ne2 = new Generics.Token<List<? super String>>() {}.typed;
		var ne3 = Generics.Typed.of(void.class);
		Testing.exerciseEquals(t, eq);
		Assert.notEqualAll(t, ne0, ne1, ne2, ne3);
	}

	@Test
	public void testTypedFrom() {
		Assert.equal(Generics.Typed.from(null), Typed.NULL);
		Assert.equal(Generics.Typed.from(new int[1][1]).array(),
			new Generics.Array(Typed.of(int.class), 2));
	}

	@Test
	public void testTypedParameter() {
		Assert.equal(Generics.typed((Parameter) null).isNull(), true);
		Assert.equal(Generics.typed(t2d).component().component().varName(), "T");
	}

	@Test
	public void testTypedReturn() {
		Assert.equal(Generics.typedReturn((Method) null).isNull(), true);
		Assert.equal(Generics.typedReturn(method).cls(), List.class);
		Assert.equal(Generics.typedReturn(method).type(0).varName(), "T");
	}

	@Test
	public void testTypedField() {
		Assert.equal(Generics.typed((Field) null).isNull(), true);
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
		Assert.equal(Generics.Typed.of(u).varName(), "U");
		Assert.equal(Generics.typed(listT).type(0).varName(), "T");
		Assert.equal(Generics.typed(listT2).type(0).varName(), "");
		Assert.equal(Generics.typed(listT2).type(0).upper(0).varName(), "T");
		Assert.equal(Generics.typed(listT3).type(0).lower(0).varName(), "T");
		Assert.equal(Generics.typed(mapListListT).type(1).type(0).varName(), "T");
	}

	@Test
	public void testTypedIsArray() {
		Assert.equal(Generics.Typed.NULL.isArray(), false);
		Assert.equal(Generics.Typed.of(int.class).isArray(), false);
		Assert.equal(Generics.Typed.of(int[].class).isArray(), true);
		Assert.equal(Generics.typed(listN2d).isArray(), true);
	}

	@Test
	public void testTypedArray() {
		assertArray(Generics.Typed.NULL.array(), Generics.Typed.NULL, 0);
		assertArray(Generics.typed(listN2d).array(),
			new Generics.Token<List<? extends Number>>() {}.typed, 2);
		Assert.string(Generics.typed(listN2d).array(), "List<? extends Number>[][]");
	}

	@Test
	public void testGenericArrayIsNull() {
		Assert.equal(Generics.Array.isNull(null), true);
		Assert.equal(Generics.Array.isNull(Generics.Array.NULL), true);
		Assert.equal(Generics.Array.isNull(Generics.Typed.NULL.array()), true);
		Assert.equal(Generics.Array.isNull(Generics.Typed.VOID.array()), false);
		Assert.equal(Generics.Array.isNull(Generics.Typed.of(int[].class).array()), false);
	}

	@Test
	public void testGenericArrayOf() {
		assertArray(Generics.Array.of(null, 0), Generics.Typed.NULL, 0);
		Assert.string(Generics.Array.of(int.class, 2), "int[][]");
	}
	
	@Test
	public void testGenericArrayIsArray() {
		Assert.equal(Generics.Array.NULL.isArray(), false);
		Assert.equal(Generics.Typed.NULL.array().isArray(), false);
		Assert.equal(Generics.Typed.VOID.array().isArray(), false);
		Assert.equal(Generics.Typed.of(int[].class).array().isArray(), true);
	}

	@Test
	public void testGenericArrayClass() {
		Assert.equal(Generics.Array.NULL.cls(), null);
		Assert.equal(new Generics.Array(null, 1).cls(), null);
		Assert.equal(Generics.Typed.NULL.array().cls(), null);
		Assert.equal(Generics.Typed.VOID.array().cls(), void.class);
		Assert.equal(Generics.Typed.of(int[].class).array().cls(), int.class);
	}

	@Test
	public void testTypedType() {
		Assert.equal(Generics.Typed.NULL.type(0), Generics.Typed.NULL);
		Assert.equal(Generics.Typed.NULL.type(1), Generics.Typed.NULL);
		Assert.equal(Generics.Typed.NULL.lower(0).type(0), Generics.Typed.NULL);
		Assert.equal(Generics.Typed.NULL.lower(1).type(0), Generics.Typed.NULL);
	}

	@Test
	public void testTypedUpper() {
		Assert.equal(Generics.Typed.NULL.upper(0).raw(), null);
		Assert.equal(Generics.Typed.of(new Type() {}).upper(0).raw(), Object.class);
		Assert.equal(new Generics.Token<>() {}.typed.upper(0).raw(), Object.class);
		Assert.equal(new Generics.Token<List<?>>() {}.typed.upper(0).raw(), List.class);
		Assert.equal(new Generics.Token<List<?>[]>() {}.typed.upper(0).component().cls(),
			List.class);
	}

	@Test
	public void testTypedLower() {
		Assert.ordered(Generics.Typed.NULL.lower());
		Assert.ordered(Generics.Typed.of(new Type() {}).lower());
		Assert.equal(new Generics.Token<>() {}.typed.lower(0).raw(), Object.class);
		Assert.equal(new Generics.Token<List<?>>() {}.typed.lower(0).raw(), List.class);
		Assert.equal(new Generics.Token<List<?>[]>() {}.typed.lower(0).component().cls(),
			List.class);
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
	public void testToken() {
		Assert.equal(new Generics.Token<>() {}.cls(), Object.class);
		Assert.equal(new Generics.Token<List<?>>() {}.cls(), List.class);
		Assert.equal(new Generics.Token<List<?>[]>() {}.cls(), null);
	}

	@Test
	public void testTokenString() {
		Assert.string(new Generics.Token<List<?>[]>() {}, "List<?>[]");
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
		Assert.string(Generics.fullString(listN2d.getGenericType()),
			"java.util.List<? extends java.lang.Number>[][]");

	}

	private static void assertArray(Generics.Array array, Generics.Typed typed, int dims) {
		Assert.equal(array.component(), typed);
		Assert.equal(array.dimensions(), dims);
	}
}
