package ceri.common.collection;

import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.isClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ArrayUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ArrayUtil.class);
	}

	@Test
	public void testDeepToString() {
		assertThat(ArrayUtil.deepToString(null), is("null"));
		assertThat(ArrayUtil.deepToString("test"), is("test"));
		assertThat(ArrayUtil.deepToString(new boolean[] { true, false }), is("[true, false]"));
		assertThat(ArrayUtil.deepToString(new byte[] { 1, 2 }), is("[1, 2]"));
		assertThat(ArrayUtil.deepToString(new char[] { '1', '2' }), is("[1, 2]"));
		assertThat(ArrayUtil.deepToString(new short[] { 1, 2 }), is("[1, 2]"));
		assertThat(ArrayUtil.deepToString(new int[] { 1, 2 }), is("[1, 2]"));
		assertThat(ArrayUtil.deepToString(new long[] { 1, 2 }), is("[1, 2]"));
		assertThat(ArrayUtil.deepToString(new float[] { 1, 2 }), is("[1.0, 2.0]"));
		assertThat(ArrayUtil.deepToString(new double[] { 1, 2 }), is("[1.0, 2.0]"));
		assertThat(ArrayUtil.deepToString(new String[] { "1", "2" }), is("[1, 2]"));
	}

	@Test
	public void testEmptyArrays() {
		assertThat(ArrayUtil.EMPTY_BOOLEAN, is(new boolean[0]));
		assertThat(ArrayUtil.emptyArray(String.class), is(new String[0]));
		assertThat(ArrayUtil.emptyArray(Object.class), is(new Object[0]));
		assertThat(ArrayUtil.emptyArray(Date.class), is(ArrayUtil.emptyArray(Date.class)));
	}

	@Test
	public void testValidateIndex() {
		int[] array = { 1, 2, 3, 4 };
		ArrayUtil.validateIndex(array.length, 0);
		ArrayUtil.validateIndex(array.length, 3);
		TestUtil.assertThrown(() -> ArrayUtil.validateIndex(array.length, -1));
		TestUtil.assertThrown(() -> ArrayUtil.validateIndex(array.length, 4));
		assertThat(ArrayUtil.isValidIndex(array.length, 0), is(true));
		assertThat(ArrayUtil.isValidIndex(array.length, 3), is(true));
		assertThat(ArrayUtil.isValidIndex(array.length, -1), is(false));
		assertThat(ArrayUtil.isValidIndex(array.length, 4), is(false));
	}

	@Test
	public void testValidateSlice() {
		int[] array = { 1, 2, 3, 4 };
		ArrayUtil.validateSlice(array.length, 0, 4);
		ArrayUtil.validateSlice(array.length, 1, 2);
		TestUtil.assertThrown(() -> ArrayUtil.validateSlice(array.length, -1, 1));
		TestUtil.assertThrown(() -> ArrayUtil.validateSlice(array.length, 5, 1));
		TestUtil.assertThrown(() -> ArrayUtil.validateSlice(array.length, 2, 4));
		assertThat(ArrayUtil.isValidSlice(array.length, 0, 4), is(true));
		assertThat(ArrayUtil.isValidSlice(array.length, 1, 2), is(true));
		assertThat(ArrayUtil.isValidSlice(array.length, -1, 1), is(false));
		assertThat(ArrayUtil.isValidSlice(array.length, 5, 1), is(false));
		assertThat(ArrayUtil.isValidSlice(array.length, 2, 4), is(false));
		assertThat(ArrayUtil.isValidSlice(array.length, 2, -1), is(false));
	}

	@Test
	public void testAddAll() {
		Number[] array = { 0, 1 };
		assertThat(ArrayUtil.addAll(array, 2, 3), is(new Number[] { 0, 1, 2, 3 }));
	}

	@Test
	public void testArrayCopy() {
		byte[] b1 = { 0, 1, 2, 3, 4 };
		byte[] b2 = { 4, 3, 2, 1, 0 };
		Object array = ArrayUtil.arrayCopy(b1, 1, b2, 1, 3);
		assertSame(array, b2);
		assertThat(b2, is(new byte[] { 4, 1, 2, 3, 0 }));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArrayType() {
		assertThat(ArrayUtil.arrayType(Boolean.class), isClass(Boolean[].class));
		ArrayUtil.arrayType(boolean.class); // Primitive types not allowed
	}

	@Test
	public void testToList() {
		List<Integer> list = ArrayUtil.toList(String::length, "A", "ABC", "BC");
		assertIterable(list, 1, 3, 2);
	}

	@Test
	public void testAsList() {
		List<Integer> list = ArrayUtil.asList(1, 2, 3);
		list.add(4);
		list.add(5);
		assertIterable(list, 1, 2, 3, 4, 5);
		assertIterable(ArrayUtil.asList("a", new String[] { "b", "c" }), "a", "b", "c");
	}

	@Test
	public void testComponentType() {
		assertThat(ArrayUtil.componentType(Boolean[].class), equalTo(Boolean.class));
		assertThat(ArrayUtil.componentType(boolean[][].class), equalTo(boolean[].class));
	}

	@Test
	public void testContainsAll() {
		assertTrue(ArrayUtil.containsAll(new Integer[] { 0, 1, 2, 3, 4 }, 4, 2, 3));
		assertFalse(ArrayUtil.containsAll(new Integer[] { 0, 1, 2, 3, 4 }, 4, 2, 5));
	}

	@Test
	public void testCreate() {
		String[] array = ArrayUtil.create(String.class, 3);
		array[0] = "0";
		array[1] = "1";
		array[2] = "2";
		assertThat(array, is(new String[] { "0", "1", "2" }));
	}

	@Test
	public void testIsArray() {
		boolean[] array = {};
		assertTrue(ArrayUtil.isArray(array));
		array = null;
		assertFalse(ArrayUtil.isArray(array));
	}

	@Test
	public void testLast() {
		assertThat(ArrayUtil.last(new boolean[] { false }), is(false));
		assertThat(ArrayUtil.last(new String[] { "0", "1" }), is("1"));
		assertThat(ArrayUtil.last(new char[] { '\n' }), is('\n'));
		assertThat(ArrayUtil.last(new byte[] { Byte.MIN_VALUE }), is(Byte.MIN_VALUE));
		assertThat(ArrayUtil.last(new short[] { Short.MAX_VALUE }), is(Short.MAX_VALUE));
		assertThat(ArrayUtil.last(new int[] { 0 }), is(0));
		assertThat(ArrayUtil.last(new long[] { Long.MAX_VALUE }), is(Long.MAX_VALUE));
		assertThat(ArrayUtil.last(new float[] { Float.MAX_VALUE }), is(Float.MAX_VALUE));
		assertThat(ArrayUtil.last(new double[] { Double.MAX_VALUE }), is(Double.MAX_VALUE));
		try {
			ArrayUtil.last(new double[] {});
			fail();
		} catch (ArrayIndexOutOfBoundsException e) {}
	}

	@Test
	public void testReverse() {
		String[] array = { "0", "1", "2" };
		ArrayUtil.reverse(array);
		assertThat(array, is(new String[] { "2", "1", "0" }));
		try {
			ArrayUtil.reverse(new Object());
			fail();
		} catch (IllegalArgumentException e) {}
	}

	static class A {}

	static class B extends A {}

	static class C extends B {}

	@Test
	public void testSuperclass() {
		C[][] obj = new C[0][];
		Class<?> cls = obj.getClass();
		assertThat(cls, isClass(C[][].class));
		cls = ArrayUtil.superclass(cls);
		assertThat(cls, isClass(B[][].class));
		cls = ArrayUtil.superclass(cls);
		assertThat(cls, isClass(A[][].class));
		cls = ArrayUtil.superclass(cls);
		assertThat(cls, isClass(Object[][].class));
		cls = ArrayUtil.superclass(cls);
		assertThat(cls, isClass(Object[].class));
		cls = ArrayUtil.superclass(cls);
		assertThat(cls, isClass(Object.class));
		cls = ArrayUtil.superclass(cls);
		assertThat(cls, isClass(null));
	}

}
