package ceri.common.collection;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.isClass;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class ArrayUtilTest {
	
	@Test
	public void testEmptyArrays() {
		assertThat(ArrayUtil.EMPTY_BOOLEAN, is(new boolean[0]));
		assertTrue(ArrayUtil.emptyArray(Date.class) == ArrayUtil.emptyArray(Date.class));
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
		assertTrue(array == b2);
		assertThat(b2, is(new byte[] { 4, 1, 2, 3, 0 }));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testArrayType() {
		assertThat(ArrayUtil.arrayType(Boolean.class), isClass(Boolean[].class));
		ArrayUtil.arrayType(boolean.class); // Primitive types not allowed
	}
	
	@Test
	public void testAsList() {
		List<Integer> list = ArrayUtil.asList(1, 2, 3);
		list.add(4);
		list.add(5);
		assertThat(list, is(Arrays.asList(1, 2, 3, 4, 5)));
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
		assertException(ArrayIndexOutOfBoundsException.class, new Runnable() {
			@Override
			public void run() {
				ArrayUtil.last(new double[] {});
			}
		});
	}
	
	@Test
	public void testReverse() {
		String[] array = { "0", "1", "2" };
		ArrayUtil.reverse(array);
		assertThat(array, is(new String[] { "2", "1", "0" }));
	}
	
	class A {}
	class B extends A {}
	class C extends B {}
	
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
