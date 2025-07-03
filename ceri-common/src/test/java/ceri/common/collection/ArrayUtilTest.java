package ceri.common.collection;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.ArrayUtil.chars;
import static ceri.common.collection.ArrayUtil.shorts;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import org.junit.Test;

public class ArrayUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ArrayUtil.class);
	}

	@Test
	public void testDeepHash() {
		boolean[] bb = { true, false };
		int[][] ii = { { 3 }, { 1, -1 } };
		assertEquals(ArrayUtil.deepHash(bb, ii), 0x1326b8);
	}

	@Test
	public void testHashOfBooleanSubArray() {
		boolean[] array = { true, true, false, true, false };
		assertEquals(ArrayUtil.hash((boolean[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((boolean[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfByteSubArray() {
		byte[] array = ArrayUtil.bytes(-1, Byte.MAX_VALUE, 0, Byte.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((byte[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((byte[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfCharSubArray() {
		char[] array = ArrayUtil.chars(-1, Character.MAX_VALUE, 0, Character.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((char[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((char[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfShortSubArray() {
		short[] array = ArrayUtil.shorts(-1, Short.MAX_VALUE, 0, Short.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((short[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((short[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfIntegerSubArray() {
		int[] array = ArrayUtil.ints(-1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((int[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((int[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfLongSubArray() {
		long[] array = ArrayUtil.longs(-1, Long.MAX_VALUE, 0, Long.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((long[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((long[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfFloatSubArray() {
		float[] array = ArrayUtil.floats(-1, Float.MAX_VALUE, Float.NaN, Float.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((float[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((float[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfDoubleSubArray() {
		double[] array = ArrayUtil.doubles(-1, Double.MAX_VALUE, Double.NaN, Double.MIN_VALUE, 1);
		assertEquals(ArrayUtil.hash((double[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((double[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1), Arrays.hashCode(ArrayUtil.copyOf(array, 1)));
		assertEquals(ArrayUtil.hash(array, 1, 3), Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3)));
	}

	@Test
	public void testHashOfSubArray() {
		String[] array = { "", "test", null, "TEST", "x" };
		assertEquals(ArrayUtil.hash((String[]) null, 1), 0);
		assertEquals(ArrayUtil.hash((String[]) null, 0, 3), 0);
		assertEquals(ArrayUtil.hash(array, 3, 3), 0);
		assertEquals(ArrayUtil.hash(array, 1),
			Arrays.hashCode(ArrayUtil.copyOf(array, 1, String[]::new)));
		assertEquals(ArrayUtil.hash(array, 1, 3),
			Arrays.hashCode(ArrayUtil.copyOf(array, 1, 3, String[]::new)));
	}

	@Test
	public void testEqualsForBooleanSubArray() {
		boolean[] array0 = ArrayUtil.bools(true, true, false, true, false);
		boolean[] array1 = ArrayUtil.bools(true, false, true, false);
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
	public void testIndexOfForArray() {
		String[] array = { "", "test", null, "test", null, "TEST", "x" };
		assertThrown(() -> ArrayUtil.indexOf(array, -1, "test", null));
		assertThrown(() -> ArrayUtil.indexOf(array, 8, "test", null));
		assertEquals(ArrayUtil.indexOf(array, 0, "test", null), 1);
		assertEquals(ArrayUtil.indexOf(array, 3, "test", null), 3);
		assertEquals(ArrayUtil.indexOf(array, 4, "test", null), -1);
		assertEquals(ArrayUtil.indexOf(array, 7, "test", null), -1);
	}

	@Test
	public void testIndexOfForBooleanArray() {
		boolean[] array = { true, false, true, true, false, true, true };
		assertEquals(ArrayUtil.indexOf(array, 0, false, true, true), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, false, true, true), -1);
	}

	@Test
	public void testIndexOfForByteArray() {
		byte[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.indexOf(array, 0, bytes(-1, 1, 1)), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, bytes(-1, 1, 1)), -1);
	}

	@Test
	public void testIndexOfForCharArray() {
		char[] array = { 1, 0xffff, 1, 1, 0xffff, 1, 1 };
		assertEquals(ArrayUtil.indexOf(array, 0, chars(0xffff, 1, 1)), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, chars(0xffff, 1, 1)), -1);
	}

	@Test
	public void testIndexOfForShortArray() {
		short[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.indexOf(array, 0, shorts(-1, 1, 1)), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, shorts(-1, 1, 1)), -1);
	}

	@Test
	public void testIndexOfForIntArray() {
		int[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.indexOf(array, 0, -1, 1, 1), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, -1, 1, 1), -1);
	}

	@Test
	public void testIndexOfForLongArray() {
		long[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.indexOf(array, 0, -1L, 1L, 1L), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, -1L, 1L, 1L), -1);
	}

	@Test
	public void testIndexOfForFloatArray() {
		float[] array = { 1.1f, -1.1f, 1.1f, 1.1f, -1.1f, 1.1f, 1.1f };
		assertEquals(ArrayUtil.indexOf(array, 0, -1.1f, 1.1f, 1.1f), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, -1.1f, 1.1f, 1.1f), -1);
	}

	@Test
	public void testIndexOfForDoubleArray() {
		double[] array = { 1.1, -1.1, 1.1, 1.1, -1.1, 1.1, 1.1 };
		assertEquals(ArrayUtil.indexOf(array, 0, -1.1, 1.1, 1.1), 1);
		assertEquals(ArrayUtil.indexOf(array, 5, -1.1, 1.1, 1.1), -1);
	}

	@Test
	public void testLastIndexOfForArray() {
		String[] array = { "", "test", null, "test", null, "TEST", "x" };
		assertThrown(() -> ArrayUtil.lastIndexOf(array, -1, "test", null));
		assertThrown(() -> ArrayUtil.lastIndexOf(array, 8, "test", null));
		assertEquals(ArrayUtil.lastIndexOf(array, 0, "test", null), 3);
		assertEquals(ArrayUtil.lastIndexOf(array, 3, "test", null), 3);
		assertEquals(ArrayUtil.lastIndexOf(array, 4, "test", null), -1);
		assertEquals(ArrayUtil.lastIndexOf(array, 7, "test", null), -1);
	}

	@Test
	public void testLastIndexOfForBooleanArray() {
		boolean[] array = { true, false, true, true, false, true, true };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, false, true, true), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, false, true, true), -1);
	}

	@Test
	public void testLastIndexOfForByteArray() {
		byte[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, bytes(-1, 1, 1)), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, bytes(-1, 1, 1)), -1);
	}

	@Test
	public void testLastIndexOfForCharArray() {
		char[] array = { 1, 0xffff, 1, 1, 0xffff, 1, 1 };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, chars(0xffff, 1, 1)), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, chars(0xffff, 1, 1)), -1);
	}

	@Test
	public void testLastIndexOfForShortArray() {
		short[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, shorts(-1, 1, 1)), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, shorts(-1, 1, 1)), -1);
	}

	@Test
	public void testLastIndexOfForIntArray() {
		int[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, -1, 1, 1), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, -1, 1, 1), -1);
	}

	@Test
	public void testLastIndexOfForLongArray() {
		long[] array = { 1, -1, 1, 1, -1, 1, 1 };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, -1L, 1L, 1L), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, -1L, 1L, 1L), -1);
	}

	@Test
	public void testLastIndexOfForFloatArray() {
		float[] array = { 1.1f, -1.1f, 1.1f, 1.1f, -1.1f, 1.1f, 1.1f };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, -1.1f, 1.1f, 1.1f), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, -1.1f, 1.1f, 1.1f), -1);
	}

	@Test
	public void testLastIndexOfForDoubleArray() {
		double[] array = { 1.1, -1.1, 1.1, 1.1, -1.1, 1.1, 1.1 };
		assertEquals(ArrayUtil.lastIndexOf(array, 0, -1.1, 1.1, 1.1), 4);
		assertEquals(ArrayUtil.lastIndexOf(array, 5, -1.1, 1.1, 1.1), -1);
	}

	@Test
	public void testToStringForTypedSubArray() {
		String[] array = { "", "test", null, "TEST", "x" };
		assertEquals(ArrayUtil.toString((String[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((String[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[test, null, TEST]");
	}

	@Test
	public void testToStringForBooleanSubArray() {
		boolean[] array = { true, true, false, true, false };
		assertEquals(ArrayUtil.toString((boolean[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((boolean[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[true, false, true, false]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[true, false, true]");
	}

	@Test
	public void testToStringForByteSubArray() {
		byte[] array = ArrayUtil.bytes(-1, Byte.MAX_VALUE, 0, Byte.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((byte[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((byte[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[127, 0, -128, 1]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[127, 0, -128]");
	}

	@Test
	public void testToStringForCharSubArray() {
		char[] array = ArrayUtil.chars(-1, Character.MAX_VALUE, 0, Character.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((char[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((char[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[\uffff, \0, \0, \u0001]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[\uffff, \0, \0]");
	}

	@Test
	public void testToStringForShortSubArray() {
		short[] array = ArrayUtil.shorts(-1, Short.MAX_VALUE, 0, Short.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((short[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((short[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[32767, 0, -32768, 1]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[32767, 0, -32768]");
	}

	@Test
	public void testToStringForIntSubArray() {
		int[] array = ArrayUtil.ints(-1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((int[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((int[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[2147483647, 0, -2147483648, 1]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[2147483647, 0, -2147483648]");
	}

	@Test
	public void testToStringForLongSubArray() {
		long[] array = ArrayUtil.longs(-1, Long.MAX_VALUE, 0, Long.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((long[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((long[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1),
			"[9223372036854775807, 0, -9223372036854775808, 1]");
		assertEquals(ArrayUtil.toString(array, 1, 3),
			"[9223372036854775807, 0, -9223372036854775808]");
	}

	@Test
	public void testToStringForFloatSubArray() {
		float[] array = ArrayUtil.floats(-1, Float.MAX_VALUE, Float.NaN, Float.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((float[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((float[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[3.4028235E38, NaN, 1.4E-45, 1.0]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[3.4028235E38, NaN, 1.4E-45]");
	}

	@Test
	public void testToStringForDoubleSubArray() {
		double[] array = ArrayUtil.doubles(-1, Double.MAX_VALUE, Double.NaN, Double.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toString((double[]) null, 1), "null");
		assertEquals(ArrayUtil.toString((double[]) null, 0, 5), "null");
		assertEquals(ArrayUtil.toString(array, 6), "null");
		assertEquals(ArrayUtil.toString(array, -1, 5), "null");
		assertEquals(ArrayUtil.toString(array, 5), "[]");
		assertEquals(ArrayUtil.toString(array, 1, 0), "[]");
		assertEquals(ArrayUtil.toString(array, 1), "[1.7976931348623157E308, NaN, 4.9E-324, 1.0]");
		assertEquals(ArrayUtil.toString(array, 1, 3), "[1.7976931348623157E308, NaN, 4.9E-324]");
	}

	@Test
	public void testToHexForByteSubArray() {
		byte[] array = ArrayUtil.bytes(-1, Byte.MAX_VALUE, 0, Byte.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toHex((byte[]) null), "null");
		assertEquals(ArrayUtil.toHex((byte[]) null, 1), "null");
		assertEquals(ArrayUtil.toHex((byte[]) null, 1, 3), "null");
		assertEquals(ArrayUtil.toHex(array), "[0xff, 0x7f, 0x0, 0x80, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1), "[0x7f, 0x0, 0x80, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1, 3), "[0x7f, 0x0, 0x80]");
	}

	@Test
	public void testToHexForShortSubArray() {
		short[] array = ArrayUtil.shorts(-1, Short.MAX_VALUE, 0, Short.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toHex((short[]) null), "null");
		assertEquals(ArrayUtil.toHex((short[]) null, 1), "null");
		assertEquals(ArrayUtil.toHex((short[]) null, 1, 3), "null");
		assertEquals(ArrayUtil.toHex(array), "[0xffff, 0x7fff, 0x0, 0x8000, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1), "[0x7fff, 0x0, 0x8000, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1, 3), "[0x7fff, 0x0, 0x8000]");
	}

	@Test
	public void testToHexForIntSubArray() {
		int[] array = ArrayUtil.ints(-1, Integer.MAX_VALUE, 0, Integer.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toHex((int[]) null), "null");
		assertEquals(ArrayUtil.toHex((int[]) null, 1), "null");
		assertEquals(ArrayUtil.toHex((int[]) null, 1, 3), "null");
		assertEquals(ArrayUtil.toHex(array), "[0xffffffff, 0x7fffffff, 0x0, 0x80000000, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1), "[0x7fffffff, 0x0, 0x80000000, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1, 3), "[0x7fffffff, 0x0, 0x80000000]");
	}

	@Test
	public void testToHexForLongSubArray() {
		long[] array = ArrayUtil.longs(-1, Long.MAX_VALUE, 0, Long.MIN_VALUE, 1);
		assertEquals(ArrayUtil.toHex((long[]) null), "null");
		assertEquals(ArrayUtil.toHex((long[]) null, 1), "null");
		assertEquals(ArrayUtil.toHex((long[]) null, 1, 3), "null");
		assertEquals(ArrayUtil.toHex(array),
			"[0xffffffffffffffff, 0x7fffffffffffffff, 0x0, 0x8000000000000000, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1),
			"[0x7fffffffffffffff, 0x0, 0x8000000000000000, 0x1]");
		assertEquals(ArrayUtil.toHex(array, 1, 3), "[0x7fffffffffffffff, 0x0, 0x8000000000000000]");
	}

	@Test
	public void testDeepToString() {
		assertEquals(ArrayUtil.deepToString(null), "null");
		assertEquals(ArrayUtil.deepToString("test"), "test");
		assertEquals(ArrayUtil.deepToString(new boolean[] { true, false }), "[true, false]");
		assertEquals(ArrayUtil.deepToString(new byte[] { 1, 2 }), "[1, 2]");
		assertEquals(ArrayUtil.deepToString(new char[] { '1', '2' }), "[1, 2]");
		assertEquals(ArrayUtil.deepToString(new short[] { 1, 2 }), "[1, 2]");
		assertEquals(ArrayUtil.deepToString(new int[] { 1, 2 }), "[1, 2]");
		assertEquals(ArrayUtil.deepToString(new long[] { 1, 2 }), "[1, 2]");
		assertEquals(ArrayUtil.deepToString(new float[] { 1, 2 }), "[1.0, 2.0]");
		assertEquals(ArrayUtil.deepToString(new double[] { 1, 2 }), "[1.0, 2.0]");
		assertEquals(ArrayUtil.deepToString(new String[] { "1", "2" }), "[1, 2]");
	}

	@Test
	public void testEmptyArrays() {
		assertArray(ArrayUtil.EMPTY_BOOLEAN);
		assertArray(ArrayUtil.empty(String.class));
		assertArray(ArrayUtil.empty(Object.class));
		assertArray(ArrayUtil.empty(Date.class));
	}

	@Test
	public void testValidIndex() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(ArrayUtil.isValidIndex(array.length, 0));
		assertTrue(ArrayUtil.isValidIndex(array.length, 3));
		assertFalse(ArrayUtil.isValidIndex(array.length, -1));
		assertFalse(ArrayUtil.isValidIndex(array.length, 4));
	}

	@Test
	public void testValidSlice() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(ArrayUtil.isValidSlice(array.length, 0, 4));
		assertTrue(ArrayUtil.isValidSlice(array.length, 1, 2));
		assertFalse(ArrayUtil.isValidSlice(array.length, -1, 1));
		assertFalse(ArrayUtil.isValidSlice(array.length, 5, 1));
		assertFalse(ArrayUtil.isValidSlice(array.length, 2, 4));
		assertFalse(ArrayUtil.isValidSlice(array.length, 2, -1));
	}

	@Test
	public void testValidRange() {
		int[] array = { 1, 2, 3, 4 };
		assertTrue(ArrayUtil.isValidRange(array.length, 0, 4));
		assertTrue(ArrayUtil.isValidRange(array.length, 1, 3));
		assertFalse(ArrayUtil.isValidRange(array.length, -1, 0));
		assertFalse(ArrayUtil.isValidRange(array.length, 5, 8));
		assertFalse(ArrayUtil.isValidRange(array.length, 2, 6));
		assertFalse(ArrayUtil.isValidRange(array.length, 2, 1));
	}

	@Test
	public void testAllEqual() {
		assertTrue(ArrayUtil.allEqual(null, null, null));
		assertFalse(ArrayUtil.allEqual(null, null, 0));
		assertTrue(ArrayUtil.allEqual("", ""));
		assertTrue(ArrayUtil.allEqual("", ""));
		assertFalse(ArrayUtil.allEqual("", "", null));
	}

	@Test
	public void testAddAll() {
		Number[] array = { 0, 1 };
		assertArray(ArrayUtil.addAll(array, 2, 3), new Number[] { 0, 1, 2, 3 });
	}

	@Test
	public void testArrayType() {
		assertSame(ArrayUtil.arrayType(Boolean.class), Boolean[].class);
		assertIllegalArg(() -> ArrayUtil.arrayType(boolean.class)); // Primitive types not allowed
	}

	@Test
	public void testToList() {
		List<Integer> list = ArrayUtil.toList(String::length, "A", "ABC", "BC");
		assertIterable(list, 1, 3, 2);
	}

	@Test
	public void testComponentType() {
		assertSame(ArrayUtil.componentType(Boolean[].class), Boolean.class);
		assertSame(ArrayUtil.componentType(boolean[][].class), boolean[].class);
	}

	@Test
	public void testContainsAll() {
		assertTrue(ArrayUtil.containsAll(new Integer[] { 0, 1, 2, 3, 4 }, 4, 2, 3));
		assertFalse(ArrayUtil.containsAll(new Integer[] { 0, 1, 2, 3, 4 }, 4, 2, 5));
	}

	@Test
	public void testContainsAny() {
		assertTrue(ArrayUtil.containsAny(new Integer[] { 0, 1, 2, 3, 4 }, -2, -1, 0));
		assertFalse(ArrayUtil.containsAny(new Integer[] { 0, 1, 2, 3, 4 }, 5, 6, 7));
	}

	@Test
	public void testCreate() {
		String[] array = ArrayUtil.create(String.class, 3);
		array[0] = "0";
		array[1] = "1";
		array[2] = "2";
		assertArray(array, "0", "1", "2");
	}

	@Test
	public void testBooleans() {
		assertArray(ArrayUtil.bools(1, 0, -1), true, false, true);
	}

	@Test
	public void testInts() {
		assertArray(ArrayUtil.ints(new byte[] { -1, 0, 1 }), -1, 0, 1);
		assertArray(ArrayUtil.ints(new short[] { -1, 0, 1 }), -1, 0, 1);
	}

	@Test
	public void testUints() {
		assertArray(ArrayUtil.uints(new byte[] { -1, 0, 1 }), 0xff, 0, 1);
		assertArray(ArrayUtil.uints(new short[] { -1, 0, 1 }), 0xffff, 0, 1);
	}

	@Test
	public void testIsArray() {
		assertTrue(ArrayUtil.isArray(new boolean[] {}));
		assertFalse(ArrayUtil.isArray((boolean[]) null));
	}

	@Test
	public void testLast() {
		assertEquals(ArrayUtil.last(ArrayUtil.array("0", "1")), "1");
		assertFalse(ArrayUtil.last(ArrayUtil.bools(true, false)));
		assertEquals(ArrayUtil.last(ArrayUtil.chars('\0', '\n')), '\n');
		assertEquals(ArrayUtil.last(ArrayUtil.bytes(Byte.MAX_VALUE, Byte.MIN_VALUE)),
			Byte.MIN_VALUE);
		assertEquals(ArrayUtil.last(ArrayUtil.shorts(Short.MIN_VALUE, Short.MAX_VALUE)),
			Short.MAX_VALUE);
		assertEquals(ArrayUtil.last(ArrayUtil.ints(Integer.MIN_VALUE, Integer.MAX_VALUE)),
			Integer.MAX_VALUE);
		assertEquals(ArrayUtil.last(ArrayUtil.longs(Long.MIN_VALUE, Long.MAX_VALUE)),
			Long.MAX_VALUE);
		assertEquals(ArrayUtil.last(ArrayUtil.floats(Float.MIN_VALUE, Float.MAX_VALUE)),
			Float.MAX_VALUE);
		assertEquals(ArrayUtil.last(ArrayUtil.doubles(Double.MIN_VALUE, Double.MAX_VALUE)),
			Double.MAX_VALUE);
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
		assertArray(ArrayUtil.reverseArray(ArrayUtil.bools(true, false, false)), false, false,
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
		assertArray(ArrayUtil.copyOf(ArrayUtil.bools(true, false, true), 1), false, true);
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
		assertEquals(ArrayUtil.copy(ArrayUtil.array("a", "b", "c"), 0, ss, 2), 3);
		assertArray(ss, "a", "b", "a");
		boolean[] bools = { true, false, true };
		assertEquals(ArrayUtil.copy(bools, 0, bools, 1), 3);
		assertArray(bools, true, true, false);
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		assertEquals(ArrayUtil.copy(bytes, 1, bytes, 0), 2);
		assertArray(bytes, 2, 3, 3);
		char[] chars = ArrayUtil.chars('1', '2', '3');
		assertEquals(ArrayUtil.copy(chars, 3, chars, 0), 0);
		assertArray(chars, '1', '2', '3');
		short[] shorts = ArrayUtil.shorts(1, 2, 3);
		assertEquals(ArrayUtil.copy(shorts, 0, shorts, 0), 3);
		assertArray(shorts, 1, 2, 3);
		int[] ints = ArrayUtil.ints(1, 2, 3);
		assertEquals(ArrayUtil.copy(ints, 3, ints, 2), 2);
		assertArray(ints, 1, 2, 3);
		long[] longs = ArrayUtil.longs(1, 2, 3);
		assertEquals(ArrayUtil.copy(longs, 0, longs, 1), 3);
		assertArray(longs, 1, 1, 2);
		float[] floats = ArrayUtil.floats(1, 2, 3);
		assertEquals(ArrayUtil.copy(floats, 0, floats, 1), 3);
		assertArray(floats, 1, 1, 2);
		double[] doubles = ArrayUtil.doubles(1, 2, 3);
		assertEquals(ArrayUtil.copy(doubles, 0, doubles, 1), 3);
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
		assertArray(b2, 4, 1, 2, 3, 0);
	}

	@Test
	public void testFill() {
		String[] ss = { "a", "b", "c" };
		assertEquals(ArrayUtil.fill(ss, 1, "x"), 3);
		assertArray(ss, "a", "x", "x");
		boolean[] bools = { true, false, true };
		assertEquals(ArrayUtil.fill(bools, 0, true), 3);
		assertArray(bools, true, true, true);
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		assertEquals(ArrayUtil.fill(bytes, 2, 0), 3);
		assertArray(bytes, 1, 2, 0);
		char[] chars = ArrayUtil.chars('1', '2', '3');
		assertEquals(ArrayUtil.fill(chars, 1, '0'), 3);
		assertArray(chars, '1', '0', '0');
		short[] shorts = ArrayUtil.shorts(1, 2, 3, 4);
		assertEquals(ArrayUtil.fill(shorts, 2, 0), 4);
		assertArray(shorts, 1, 2, 0, 0);
		int[] ints = ArrayUtil.ints(1, 2, 3, 4, 5);
		assertEquals(ArrayUtil.fill(ints, 4, 0), 5);
		assertArray(ints, 1, 2, 3, 4, 0);
		long[] longs = ArrayUtil.longs(1, 2, 3);
		assertEquals(ArrayUtil.fill(longs, 0, 1), 3);
		assertArray(longs, 1, 1, 1);
		float[] floats = ArrayUtil.floats(1, 2, 3);
		assertEquals(ArrayUtil.fill(floats, 2, -0.0f), 3);
		assertArray(floats, 1, 2, -0.0f);
		double[] doubles = ArrayUtil.doubles(1, 2, 3);
		assertEquals(ArrayUtil.fill(doubles, 0, -1), 3);
		assertArray(doubles, -1, -1, -1);
	}

	@Test
	public void testFillZeroCount() {
		assertEquals(ArrayUtil.fill(new String[3], 0, 0, "x"), 0);
		assertEquals(ArrayUtil.fill(new boolean[3], 1, 0, true), 1);
		assertEquals(ArrayUtil.fill(new byte[3], 2, 0, 0), 2);
		assertEquals(ArrayUtil.fill(new char[3], 3, 0, '0'), 3);
		assertEquals(ArrayUtil.fill(new short[3], 0, 0, 7), 0);
		assertEquals(ArrayUtil.fill(new int[3], 1, 0, 7), 1);
		assertEquals(ArrayUtil.fill(new long[3], 2, 0, 7), 2);
		assertEquals(ArrayUtil.fill(new float[3], 3, 0, 7), 3);
		assertEquals(ArrayUtil.fill(new double[3], 0, 0, 7), 0);
	}

	@Test
	public void testAt() {
		assertEquals(ArrayUtil.at((String[]) null, 0), null);
		assertEquals(ArrayUtil.at(ArrayUtil.array("a", "b"), -1), null);
		assertEquals(ArrayUtil.at(ArrayUtil.array("a", "b"), 2), null);
		assertEquals(ArrayUtil.at(ArrayUtil.array("a", "b"), 1), "b");
		assertEquals(ArrayUtil.at((String[]) null, 0, "c"), "c");
		assertEquals(ArrayUtil.at(ArrayUtil.array("a", "b"), -1, "c"), "c");
		assertEquals(ArrayUtil.at(ArrayUtil.array("a", "b"), 2, "c"), "c");
		assertEquals(ArrayUtil.at(ArrayUtil.array("a", "b"), 1, "c"), "b");
		assertEquals(ArrayUtil.at((boolean[]) null, 0, true), true);
		assertEquals(ArrayUtil.at(ArrayUtil.bools(false, false), -1, true), true);
		assertEquals(ArrayUtil.at(ArrayUtil.bools(false, false), 2, true), true);
		assertEquals(ArrayUtil.at(ArrayUtil.bools(false, false), 1, true), false);
		assertEquals(ArrayUtil.at((byte[]) null, 0, (byte) 0), (byte) 0);
		assertEquals(ArrayUtil.at(ArrayUtil.bytes(-1, 1), -1, (byte) 0), (byte) 0);
		assertEquals(ArrayUtil.at(ArrayUtil.bytes(-1, 1), 2, (byte) 0), (byte) 0);
		assertEquals(ArrayUtil.at(ArrayUtil.bytes(-1, 1), 1, (byte) 0), (byte) 1);
		assertEquals(ArrayUtil.at((char[]) null, 0, 'c'), 'c');
		assertEquals(ArrayUtil.at(ArrayUtil.chars('a', 'b'), -1, 'c'), 'c');
		assertEquals(ArrayUtil.at(ArrayUtil.chars('a', 'b'), 2, 'c'), 'c');
		assertEquals(ArrayUtil.at(ArrayUtil.chars('a', 'b'), 1, 'c'), 'b');
		assertEquals(ArrayUtil.at((short[]) null, 0, (short) 0), (short) 0);
		assertEquals(ArrayUtil.at(ArrayUtil.shorts(-1, 1), -1, (short) 0), (short) 0);
		assertEquals(ArrayUtil.at(ArrayUtil.shorts(-1, 1), 2, (short) 0), (short) 0);
		assertEquals(ArrayUtil.at(ArrayUtil.shorts(-1, 1), 1, (short) 0), (short) 1);
		assertEquals(ArrayUtil.at((int[]) null, 0, 0), 0);
		assertEquals(ArrayUtil.at(ArrayUtil.ints(-1, 1), -1, 0), 0);
		assertEquals(ArrayUtil.at(ArrayUtil.ints(-1, 1), 2, 0), 0);
		assertEquals(ArrayUtil.at(ArrayUtil.ints(-1, 1), 1, 0), 1);
		assertEquals(ArrayUtil.at((long[]) null, 0, 0), 0L);
		assertEquals(ArrayUtil.at(ArrayUtil.longs(-1, 1), -1, 0), 0L);
		assertEquals(ArrayUtil.at(ArrayUtil.longs(-1, 1), 2, 0), 0L);
		assertEquals(ArrayUtil.at(ArrayUtil.longs(-1, 1), 1, 0), 1L);
		assertEquals(ArrayUtil.at((float[]) null, 0, 0), 0f);
		assertEquals(ArrayUtil.at(ArrayUtil.floats(-1, 1), -1, 0), 0f);
		assertEquals(ArrayUtil.at(ArrayUtil.floats(-1, 1), 2, 0), 0f);
		assertEquals(ArrayUtil.at(ArrayUtil.floats(-1, 1), 1, 0), 1f);
		assertEquals(ArrayUtil.at((double[]) null, 0, 0), 0.0);
		assertEquals(ArrayUtil.at(ArrayUtil.doubles(-1, 1), -1, 0), 0.0);
		assertEquals(ArrayUtil.at(ArrayUtil.doubles(-1, 1), 2, 0), 0.0);
		assertEquals(ArrayUtil.at(ArrayUtil.doubles(-1, 1), 1, 0), 1.0);
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
	public void testAsFixedList() {
		assertCollection(ArrayUtil.asFixedList(ArrayUtil.boxInts(1, 2, 3), 1), 2, 3);
		assertCollection(ArrayUtil.asFixedList(ArrayUtil.boxInts(1, 2, 3), 0, 3), 1, 2, 3);
		assertCollection(ArrayUtil.asFixedList(ArrayUtil.boxInts(1, 2, 3, 4), 1, 3), 2, 3);
		assertCollection(ArrayUtil.asFixedList(ArrayUtil.boxInts(1, 2, 3), 3, 3));
		assertThrown(() -> ArrayUtil.asFixedList(ArrayUtil.boxInts(1, 2, 3), 0, 4));
		assertThrown(() -> ArrayUtil.asFixedList(ArrayUtil.boxInts(1, 2, 3), 4, 0));
	}

	@Test
	public void testAsList() {
		assertCollection(ArrayUtil.asList(1, 2, 3), 1, 2, 3);
		assertCollection(ArrayUtil.asList(0, ArrayUtil.boxInts(1, 2, 3)), 0, 1, 2, 3);
		assertCollection(ArrayUtil.asList(ArrayUtil.boxInts(1, 2, 3), 4, 5), 1, 2, 3, 4, 5);
	}

	@Test
	public void testBoxingArrays() {
		boolean[] b = { true, false };
		Boolean[] B = { true, false };
		assertArray(ArrayUtil.boxBools(b), B);
		assertArray(ArrayUtil.unboxBools(B), b);
		char[] c = { '\0', 'a' };
		Character[] C = { '\0', 'a' };
		assertArray(ArrayUtil.boxChars(c), C);
		assertArray(ArrayUtil.unboxChars(C), c);
		byte[] bt = { -128, 127 };
		Byte[] Bt = { -128, 127 };
		assertArray(ArrayUtil.boxBytes(bt), Bt);
		assertArray(ArrayUtil.unboxBytes(Bt), bt);
		short[] s = { Short.MAX_VALUE, Short.MIN_VALUE };
		Short[] S = { Short.MAX_VALUE, Short.MIN_VALUE };
		assertArray(ArrayUtil.boxShorts(s), S);
		assertArray(ArrayUtil.unboxShorts(S), s);
		int[] i = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		Integer[] I = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		assertArray(ArrayUtil.boxInts(i), I);
		assertArray(ArrayUtil.unboxInts(I), i);
		long[] l = { Long.MAX_VALUE, Long.MIN_VALUE };
		Long[] L = { Long.MAX_VALUE, Long.MIN_VALUE };
		assertArray(ArrayUtil.boxLongs(l), L);
		assertArray(ArrayUtil.unboxLongs(L), l);
		double[] d = { Double.MAX_VALUE, Double.MIN_VALUE };
		Double[] D = { Double.MAX_VALUE, Double.MIN_VALUE };
		assertArray(ArrayUtil.boxDoubles(d), D);
		assertArray(ArrayUtil.unboxDoubles(D), d);
		float[] f = { Float.MAX_VALUE, Float.MIN_VALUE };
		Float[] F = { Float.MAX_VALUE, Float.MIN_VALUE };
		assertArray(ArrayUtil.boxFloats(f), F);
		assertArray(ArrayUtil.unboxFloats(F), f);
	}

	@Test
	public void testPrimitiveList() {
		assertCollection(ArrayUtil.boolList(Boolean.TRUE, Boolean.FALSE), Boolean.TRUE,
			Boolean.FALSE);
		assertCollection(ArrayUtil.boolList(Boolean.TRUE, Boolean.FALSE), Boolean.TRUE,
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
