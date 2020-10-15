package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertFalse;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertSame;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import ceri.common.test.TestUtil;
import ceri.common.util.PrimitiveUtil;

public class ArrayUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ArrayUtil.class);
	}

	@Test
	public void testHashOfBooleanSubArray() {
		boolean[] array = { true, true, false, true, false };
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((boolean[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfByteSubArray() {
		byte[] array = ArrayUtil.bytes(-1, Byte.MAX_VALUE, 0, Byte.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((byte[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfCharSubArray() {
		char[] array = ArrayUtil.chars(-1, Character.MAX_VALUE, 0, Character.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((char[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfShortSubArray() {
		short[] array = ArrayUtil.shorts(-1, Short.MAX_VALUE, 0, Short.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((short[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfIntegerSubArray() {
		int[] array = ArrayUtil.ints(-1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((int[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfLongSubArray() {
		long[] array = ArrayUtil.longs(-1, Long.MAX_VALUE, 0, Long.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((long[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfFloatSubArray() {
		float[] array = ArrayUtil.floats(-1, Float.MAX_VALUE, Float.NaN, Float.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((float[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfDoubleSubArray() {
		double[] array = ArrayUtil.doubles(-1, Double.MAX_VALUE, Double.NaN, Double.MIN_VALUE, 1);
		var sub = ArrayUtil.copyOf(array, 1, 3);
		assertThat(ArrayUtil.hash((double[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testHashOfSubArray() {
		String[] array = { "", "test", null, "TEST", "x" };
		var sub = ArrayUtil.copyOf(array, 1, 3, String[]::new);
		assertThat(ArrayUtil.hash((String[]) null, 0, 3), is(0));
		assertThat(ArrayUtil.hash(array, 3, 3), is(0));
		assertThat(ArrayUtil.hash(array, 1, 3), is(Arrays.hashCode(sub)));
	}

	@Test
	public void testEqualsForBooleanSubArray() {
		boolean[] array0 = ArrayUtil.booleans(true, true, false, true, false);
		boolean[] array1 = ArrayUtil.booleans(true, false, true, false);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForByteSubArray() {
		byte[] array0 = ArrayUtil.bytes(-1, Byte.MAX_VALUE, 0, Byte.MIN_VALUE, 1);
		byte[] array1 = ArrayUtil.bytes(Byte.MAX_VALUE, 0, Byte.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForCharSubArray() {
		char[] array0 = ArrayUtil.chars(-1, Character.MAX_VALUE, 0, Character.MIN_VALUE, 1);
		char[] array1 = ArrayUtil.chars(Character.MAX_VALUE, 0, Character.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForShortSubArray() {
		short[] array0 = ArrayUtil.shorts(-1, Short.MAX_VALUE, 0, Short.MIN_VALUE, 1);
		short[] array1 = ArrayUtil.shorts(Short.MAX_VALUE, 0, Short.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForIntSubArray() {
		int[] array0 = ArrayUtil.ints(-1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
		int[] array1 = ArrayUtil.ints(Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForLongSubArray() {
		long[] array0 = ArrayUtil.longs(-1, Long.MAX_VALUE, 0, Long.MIN_VALUE, 1);
		long[] array1 = ArrayUtil.longs(Long.MAX_VALUE, 0, Long.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForFloatSubArray() {
		float[] array0 = ArrayUtil.floats(-1, Float.MAX_VALUE, Float.NaN, Float.MIN_VALUE, 1);
		float[] array1 = ArrayUtil.floats(Float.MAX_VALUE, Float.NaN, Float.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForDoubleSubArray() {
		double[] array0 = ArrayUtil.doubles(-1, Double.MAX_VALUE, Double.NaN, Double.MIN_VALUE, 1);
		double[] array1 = ArrayUtil.doubles(Double.MAX_VALUE, Double.NaN, Double.MIN_VALUE, 1);
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
	}

	@Test
	public void testEqualsForSubArray() {
		String[] array0 = { "", "test", null, "TEST", "x" };
		String[] array1 = { "test", null, "TEST", "x" };
		assertTrue(ArrayUtil.equals(array0, 1, array1, 0, 3));
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
		assertThat(ArrayUtil.empty(String.class), is(new String[0]));
		assertThat(ArrayUtil.empty(Object.class), is(new Object[0]));
		assertThat(ArrayUtil.empty(Date.class), is(ArrayUtil.empty(Date.class)));
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
	public void testValidateRange() {
		int[] array = { 1, 2, 3, 4 };
		ArrayUtil.validateRange(array.length, 0, 4);
		ArrayUtil.validateRange(array.length, 1, 3);
		TestUtil.assertThrown(() -> ArrayUtil.validateRange(array.length, -1, 0));
		TestUtil.assertThrown(() -> ArrayUtil.validateRange(array.length, 5, 6));
		TestUtil.assertThrown(() -> ArrayUtil.validateRange(array.length, 2, 1));
		TestUtil.assertThrown(() -> ArrayUtil.validateRange(array.length, 2, 5));
		assertThat(ArrayUtil.isValidRange(array.length, 0, 4), is(true));
		assertThat(ArrayUtil.isValidRange(array.length, 1, 3), is(true));
		assertThat(ArrayUtil.isValidRange(array.length, -1, 0), is(false));
		assertThat(ArrayUtil.isValidRange(array.length, 5, 8), is(false));
		assertThat(ArrayUtil.isValidRange(array.length, 2, 6), is(false));
		assertThat(ArrayUtil.isValidRange(array.length, 2, 1), is(false));
	}

	@Test
	public void testAddAll() {
		Number[] array = { 0, 1 };
		assertThat(ArrayUtil.addAll(array, 2, 3), is(new Number[] { 0, 1, 2, 3 }));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testArrayType() {
		assertSame(ArrayUtil.arrayType(Boolean.class), Boolean[].class);
		ArrayUtil.arrayType(boolean.class); // Primitive types not allowed
	}

	@Test
	public void testToList() {
		List<Integer> list = ArrayUtil.toList(String::length, "A", "ABC", "BC");
		assertIterable(list, 1, 3, 2);
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
	public void testBooleans() {
		assertArray(ArrayUtil.booleans(1, 0, -1), true, false, true);
	}

	@Test
	public void testIsArray() {
		assertTrue(ArrayUtil.isArray(new boolean[] {}));
		assertFalse(ArrayUtil.isArray((boolean[]) null));
	}

	@Test
	public void testLast() {
		assertThat(ArrayUtil.last(ArrayUtil.array("0", "1")), is("1"));
		assertThat(ArrayUtil.last(ArrayUtil.booleans(true, false)), is(false));
		assertThat(ArrayUtil.last(ArrayUtil.chars('\0', '\n')), is('\n'));
		assertThat(ArrayUtil.last(ArrayUtil.bytes(Byte.MAX_VALUE, Byte.MIN_VALUE)),
			is(Byte.MIN_VALUE));
		assertThat(ArrayUtil.last(ArrayUtil.shorts(Short.MIN_VALUE, Short.MAX_VALUE)),
			is(Short.MAX_VALUE));
		assertThat(ArrayUtil.last(ArrayUtil.ints(Integer.MIN_VALUE, Integer.MAX_VALUE)),
			is(Integer.MAX_VALUE));
		assertThat(ArrayUtil.last(ArrayUtil.longs(Long.MIN_VALUE, Long.MAX_VALUE)),
			is(Long.MAX_VALUE));
		assertThat(ArrayUtil.last(ArrayUtil.floats(Float.MIN_VALUE, Float.MAX_VALUE)),
			is(Float.MAX_VALUE));
		assertThat(ArrayUtil.last(ArrayUtil.doubles(Double.MIN_VALUE, Double.MAX_VALUE)),
			is(Double.MAX_VALUE));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_BOOLEAN));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_CHAR));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_BYTE));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_SHORT));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_INT));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_LONG));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_FLOAT));
		assertThrown(() -> ArrayUtil.last(ArrayUtil.EMPTY_DOUBLE));
	}

	@Test
	public void testReverse() {
		assertArray(ArrayUtil.reverseArray(ArrayUtil.booleans(true, false, false)), false, false,
			true);
		assertThrown(() -> ArrayUtil.reverseArray(new Object()));
		assertArray(ArrayUtil.reverse(ArrayUtil.array("0", "1", "2")), "2", "1", "0");
		assertArray(ArrayUtil.reverseBooleans(true, false, false), false, false, true);
		assertArray(ArrayUtil.reverseChars('\n', '\0', '\\'), '\\', '\0', '\n');
		assertArray(ArrayUtil.reverseBytes(Byte.MAX_VALUE, (byte) 0, Byte.MIN_VALUE),
			Byte.MIN_VALUE, 0, Byte.MAX_VALUE);
		assertArray(ArrayUtil.reverseShorts(Short.MAX_VALUE, (short) 0, Short.MIN_VALUE),
			Short.MIN_VALUE, 0, Short.MAX_VALUE);
		assertArray(ArrayUtil.reverseInts(Integer.MAX_VALUE, 0, Integer.MIN_VALUE),
			Integer.MIN_VALUE, 0, Integer.MAX_VALUE);
		assertArray(ArrayUtil.reverseLongs(Long.MAX_VALUE, 0, Long.MIN_VALUE), Long.MIN_VALUE, 0,
			Long.MAX_VALUE);
		assertArray(ArrayUtil.reverseFloats(Float.MAX_VALUE, 0, Float.MIN_VALUE), Float.MIN_VALUE,
			0, Float.MAX_VALUE);
		assertArray(ArrayUtil.reverseDoubles(Double.MAX_VALUE, 0, Double.MIN_VALUE),
			Double.MIN_VALUE, 0, Double.MAX_VALUE);
	}

	@Test
	public void testCopyOf() {
		assertArray(ArrayUtil.copyOf(ArrayUtil.array("a", "b", "c"), 2, String[]::new), "c");
		assertArray(ArrayUtil.copyOf(ArrayUtil.booleans(true, false, true), 1), false, true);
		assertArray(ArrayUtil.copyOf(ArrayUtil.bytes(1, 2, 3), -1), 0, 1, 2, 3);
		assertArray(ArrayUtil.copyOf(ArrayUtil.chars('a', 'b', 'c'), 4));
		assertArray(ArrayUtil.copyOf(ArrayUtil.shorts(1, 2, 3), 3));
		assertArray(ArrayUtil.copyOf(ArrayUtil.ints(1, 2, 3), -2), 0, 0, 1, 2, 3);
		assertArray(ArrayUtil.copyOf(ArrayUtil.ints(1, 2, 3), 2, 3), 3, 0, 0);
		assertArray(ArrayUtil.copyOf(ArrayUtil.longs(1, 2, 3), 0), 1, 2, 3);
		assertArray(ArrayUtil.copyOf(ArrayUtil.floats(1, 2, 3), 1), 2, 3);
		assertArray(ArrayUtil.copyOf(ArrayUtil.doubles(1, 2, 3), 0), 1, 2, 3);
	}

	@Test
	public void testCopy() {
		String[] ss = { "a", "b", "c" };
		assertThat(ArrayUtil.copy(ArrayUtil.array("a", "b", "c"), 0, ss, 2), is(3));
		assertArray(ss, "a", "b", "a");
		boolean[] bools = { true, false, true };
		assertThat(ArrayUtil.copy(bools, 0, bools, 1), is(3));
		assertArray(bools, true, true, false);
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		assertThat(ArrayUtil.copy(bytes, 1, bytes, 0), is(2));
		assertArray(bytes, 2, 3, 3);
		char[] chars = ArrayUtil.chars('1', '2', '3');
		assertThat(ArrayUtil.copy(chars, 3, chars, 0), is(0));
		assertArray(chars, '1', '2', '3');
		short[] shorts = ArrayUtil.shorts(1, 2, 3);
		assertThat(ArrayUtil.copy(shorts, 0, shorts, 0), is(3));
		assertArray(shorts, 1, 2, 3);
		int[] ints = ArrayUtil.ints(1, 2, 3);
		assertThat(ArrayUtil.copy(ints, 3, ints, 2), is(2));
		assertArray(ints, 1, 2, 3);
		long[] longs = ArrayUtil.longs(1, 2, 3);
		assertThat(ArrayUtil.copy(longs, 0, longs, 1), is(3));
		assertArray(longs, 1, 1, 2);
		float[] floats = ArrayUtil.floats(1, 2, 3);
		assertThat(ArrayUtil.copy(floats, 0, floats, 1), is(3));
		assertArray(floats, 1, 1, 2);
		double[] doubles = ArrayUtil.doubles(1, 2, 3);
		assertThat(ArrayUtil.copy(doubles, 0, doubles, 1), is(3));
		assertArray(doubles, 1, 1, 2);
	}

	@Test
	public void testCopyOutOfRange() {
		assertThrown(() -> ArrayUtil.copy(ArrayUtil.ints(1, 2, 3), 0, null, 0));
		assertThrown(() -> ArrayUtil.copy(ArrayUtil.ints(1, 2, 3), 0, new int[3], 0, 4));
		assertThrown(() -> ArrayUtil.copy(ArrayUtil.ints(1, 2, 3), 0, new int[3], 0, -1));
	}

	@Test
	public void testArrayCopy() {
		byte[] b1 = { 0, 1, 2, 3, 4 };
		byte[] b2 = { 4, 3, 2, 1, 0 };
		Object array = ArrayUtil.arrayCopy(b1, 1, b2, 1, 3);
		assertSame(array, b2);
		assertThat(b2, is(new byte[] { 4, 1, 2, 3, 0 }));
	}

	@Test
	public void testFill() {
		String[] ss = { "a", "b", "c" };
		assertThat(ArrayUtil.fill(ss, 1, "x"), is(3));
		assertArray(ss, "a", "x", "x");
		boolean[] bools = { true, false, true };
		assertThat(ArrayUtil.fill(bools, 0, true), is(3));
		assertArray(bools, true, true, true);
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		assertThat(ArrayUtil.fill(bytes, 2, 0), is(3));
		assertArray(bytes, 1, 2, 0);
		char[] chars = ArrayUtil.chars('1', '2', '3');
		assertThat(ArrayUtil.fill(chars, 1, '0'), is(3));
		assertArray(chars, '1', '0', '0');
		short[] shorts = ArrayUtil.shorts(1, 2, 3, 4);
		assertThat(ArrayUtil.fill(shorts, 2, 0), is(4));
		assertArray(shorts, 1, 2, 0, 0);
		int[] ints = ArrayUtil.ints(1, 2, 3, 4, 5);
		assertThat(ArrayUtil.fill(ints, 4, 0), is(5));
		assertArray(ints, 1, 2, 3, 4, 0);
		long[] longs = ArrayUtil.longs(1, 2, 3);
		assertThat(ArrayUtil.fill(longs, 0, 1), is(3));
		assertArray(longs, 1, 1, 1);
		float[] floats = ArrayUtil.floats(1, 2, 3);
		assertThat(ArrayUtil.fill(floats, 2, -0.0f), is(3));
		assertArray(floats, 1, 2, -0.0f);
		double[] doubles = ArrayUtil.doubles(1, 2, 3);
		assertThat(ArrayUtil.fill(doubles, 0, -1), is(3));
		assertArray(doubles, -1, -1, -1);
	}

	@Test
	public void testFillZeroCount() {
		assertThat(ArrayUtil.fill(new String[3], 0, 0, "x"), is(0));
		assertThat(ArrayUtil.fill(new boolean[3], 1, 0, true), is(1));
		assertThat(ArrayUtil.fill(new byte[3], 2, 0, 0), is(2));
		assertThat(ArrayUtil.fill(new char[3], 3, 0, '0'), is(3));
		assertThat(ArrayUtil.fill(new short[3], 0, 0, 7), is(0));
		assertThat(ArrayUtil.fill(new int[3], 1, 0, 7), is(1));
		assertThat(ArrayUtil.fill(new long[3], 2, 0, 7), is(2));
		assertThat(ArrayUtil.fill(new float[3], 3, 0, 7), is(3));
		assertThat(ArrayUtil.fill(new double[3], 0, 0, 7), is(0));
	}

	@Test
	public void testRange() {
		assertArray(ArrayUtil.intRange(0));
		assertArray(ArrayUtil.intRange(3), 0, 1, 2);
		assertArray(ArrayUtil.intRange(3, 7), 3, 4, 5, 6);
		assertArray(ArrayUtil.longRange(0));
		assertArray(ArrayUtil.longRange(3), 0, 1, 2);
		assertArray(ArrayUtil.longRange(3, 7), 3, 4, 5, 6);
	}

	@Test
	public void testToArray() {
		Collection<Number> collection = Arrays.asList(1, 16, 256, 4096, 65536);
		assertArray(ArrayUtil.bytes(collection), 1, 16, 0, 0, 0);
		assertArray(ArrayUtil.shorts(collection), 1, 16, 256, 4096, 0);
		assertArray(ArrayUtil.ints(collection), 1, 16, 256, 4096, 65536);
		assertArray(ArrayUtil.longs(collection), 1, 16, 256, 4096, 65536);
		assertArray(ArrayUtil.floats(collection), 1, 16, 256, 4096, 65536);
		assertArray(ArrayUtil.doubles(collection), 1, 16, 256, 4096, 65536);
	}

	@Test
	public void testAsList() {
		assertCollection(ArrayUtil.asList(1, 2, 3), 1, 2, 3);
		assertCollection(ArrayUtil.asList(0, PrimitiveUtil.convertInts(1, 2, 3)), 0, 1, 2, 3);
		assertCollection(ArrayUtil.asList(PrimitiveUtil.convertInts(1, 2, 3), 4, 5), 1, 2, 3, 4, 5);
	}

	@Test
	public void testPrimitiveList() {
		assertCollection(ArrayUtil.booleanList(Boolean.TRUE, Boolean.FALSE), Boolean.TRUE,
			Boolean.FALSE);
		assertCollection(ArrayUtil.booleanList(Boolean.TRUE, Boolean.FALSE), Boolean.TRUE,
			Boolean.FALSE);
		assertCollection(ArrayUtil.byteList(Byte.MAX_VALUE, Byte.MIN_VALUE), Byte.MAX_VALUE,
			Byte.MIN_VALUE);
		assertCollection(ArrayUtil.shortList(Short.MAX_VALUE, Short.MIN_VALUE), Short.MAX_VALUE,
			Short.MIN_VALUE);
		assertCollection(ArrayUtil.intList(Integer.MAX_VALUE, Integer.MIN_VALUE), Integer.MAX_VALUE,
			Integer.MIN_VALUE);
		assertCollection(ArrayUtil.longList(Long.MAX_VALUE, Long.MIN_VALUE), Long.MAX_VALUE,
			Long.MIN_VALUE);
		assertCollection(ArrayUtil.floatList(Float.MAX_VALUE, Float.MIN_VALUE), Float.MAX_VALUE,
			Float.MIN_VALUE);
		assertCollection(ArrayUtil.doubleList(Double.MAX_VALUE, Double.MIN_VALUE), Double.MAX_VALUE,
			Double.MIN_VALUE);
	}

	static class A {}

	static class B extends A {}

	static class C extends B {}

	@Test
	public void testSuperclass() {
		C[][] obj = new C[0][];
		Class<?> cls = obj.getClass();
		assertSame(cls, C[][].class);
		cls = ArrayUtil.superclass(cls);
		assertSame(cls, B[][].class);
		cls = ArrayUtil.superclass(cls);
		assertSame(cls, A[][].class);
		cls = ArrayUtil.superclass(cls);
		assertSame(cls, Object[][].class);
		cls = ArrayUtil.superclass(cls);
		assertSame(cls, Object[].class);
		cls = ArrayUtil.superclass(cls);
		assertSame(cls, Object.class);
		cls = ArrayUtil.superclass(cls);
		assertSame(cls, null);
	}

}
