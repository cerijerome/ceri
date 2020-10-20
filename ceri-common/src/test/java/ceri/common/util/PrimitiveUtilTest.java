package ceri.common.util;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;

public class PrimitiveUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(PrimitiveUtil.class);
	}

	@Test
	public void testDecode() {
		assertEquals(PrimitiveUtil.decode("", Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("088", Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("", Short.MIN_VALUE), Short.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("088", Short.MIN_VALUE), Short.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("", Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("088", Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("", Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(PrimitiveUtil.decode("088", Long.MIN_VALUE), Long.MIN_VALUE);
	}

	@Test
	public void testConvertArrays() {
		boolean[] b = { true, false };
		Boolean[] B = { true, false };
		assertArray(PrimitiveUtil.convertBooleans(b), B);
		assertArray(PrimitiveUtil.convertBooleans(B), b);
		char[] c = { '\0', 'a' };
		Character[] C = { '\0', 'a' };
		assertArray(PrimitiveUtil.convertChars(c), C);
		assertArray(PrimitiveUtil.convertChars(C), c);
		byte[] bt = { -128, 127 };
		Byte[] Bt = { -128, 127 };
		assertArray(PrimitiveUtil.convertBytes(bt), Bt);
		assertArray(PrimitiveUtil.convertBytes(Bt), bt);
		short[] s = { Short.MAX_VALUE, Short.MIN_VALUE };
		Short[] S = { Short.MAX_VALUE, Short.MIN_VALUE };
		assertArray(PrimitiveUtil.convertShorts(s), S);
		assertArray(PrimitiveUtil.convertShorts(S), s);
		int[] i = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		Integer[] I = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		assertArray(PrimitiveUtil.convertInts(i), I);
		assertArray(PrimitiveUtil.convertInts(I), i);
		long[] l = { Long.MAX_VALUE, Long.MIN_VALUE };
		Long[] L = { Long.MAX_VALUE, Long.MIN_VALUE };
		assertArray(PrimitiveUtil.convertLongs(l), L);
		assertArray(PrimitiveUtil.convertLongs(L), l);
		double[] d = { Double.MAX_VALUE, Double.MIN_VALUE };
		Double[] D = { Double.MAX_VALUE, Double.MIN_VALUE };
		assertArray(PrimitiveUtil.convertDoubles(d), D);
		assertArray(PrimitiveUtil.convertDoubles(D), d);
		float[] f = { Float.MAX_VALUE, Float.MIN_VALUE };
		Float[] F = { Float.MAX_VALUE, Float.MIN_VALUE };
		assertArray(PrimitiveUtil.convertFloats(f), F);
		assertArray(PrimitiveUtil.convertFloats(F), f);
	}

	@Test
	public void testGetPrimitiveClass() {
		assertSame(double.class, PrimitiveUtil.primitiveClass(Double.class));
		assertThrown(IllegalArgumentException.class,
			() -> PrimitiveUtil.primitiveClass(Boolean.TYPE));
		assertThrown(IllegalArgumentException.class,
			() -> PrimitiveUtil.primitiveClass(String.class));
	}

	@Test
	public void testGetObjectClass() {
		assertSame(Double.class, PrimitiveUtil.boxedClass(double.class));
		assertThrown(IllegalArgumentException.class, () -> PrimitiveUtil.boxedClass(Boolean.class));
	}

	@Test
	public void testAutoBoxAssignable() {
		assertTrue(PrimitiveUtil.isAutoBoxAssignable(Long.class, long.class));
		assertTrue(PrimitiveUtil.isAutoBoxAssignable(long.class, Long.class));
		assertTrue(PrimitiveUtil.isAutoBoxAssignable(double.class, Number.class));
		assertFalse(PrimitiveUtil.isAutoBoxAssignable(Number.class, double.class));
	}

	@Test
	public void testValueOf() {
		assertTrue(PrimitiveUtil.valueOf(null, true));
		assertFalse(PrimitiveUtil.valueOf("FALSE", true));
		assertFalse(PrimitiveUtil.valueOf("1", false));
		assertEquals(PrimitiveUtil.valueOf(null, '\n'), '\n');
		assertEquals(PrimitiveUtil.valueOf("A", 'a'), 'A');
		assertEquals(PrimitiveUtil.valueOf(null, (byte) 0), (byte) 0);
		assertEquals(PrimitiveUtil.valueOf("x", Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(PrimitiveUtil.valueOf("-1", Byte.MIN_VALUE), (byte) -1);
		assertEquals(PrimitiveUtil.valueOf(null, (short) -1), (short) -1);
		assertEquals(PrimitiveUtil.valueOf("x", Short.MIN_VALUE), Short.MIN_VALUE);
		assertEquals(PrimitiveUtil.valueOf("1", Short.MIN_VALUE), (short) 1);
		assertEquals(PrimitiveUtil.valueOf(null, Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(PrimitiveUtil.valueOf("--", Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(PrimitiveUtil.valueOf("100", Integer.MAX_VALUE), 100);
		assertEquals(PrimitiveUtil.valueOf(null, 1L), 1L);
		assertEquals(PrimitiveUtil.valueOf("x", 1L), 1L);
		assertEquals(PrimitiveUtil.valueOf("-100000000000", 1L), -100000000000L);
		assertEquals(PrimitiveUtil.valueOf(null, Double.NaN), Double.NaN);
		assertEquals(PrimitiveUtil.valueOf("a", Double.NaN), Double.NaN);
		assertEquals(PrimitiveUtil.valueOf("-0.1E300", Double.NaN), -0.1E300);
		assertEquals(PrimitiveUtil.valueOf(null, Float.NaN), Float.NaN);
		assertEquals(PrimitiveUtil.valueOf("a", Float.NaN), Float.NaN);
		assertEquals(PrimitiveUtil.valueOf("1E30", Float.NaN), 1E30f);
	}

	@Test
	public void testValue() {
		assertTrue(PrimitiveUtil.booleanValue("TRUE"));
		assertEquals(PrimitiveUtil.byteValue("127"), (byte) 127);
		assertEquals(PrimitiveUtil.byteValue("7f", 16), (byte) 0x7f);
		assertEquals(PrimitiveUtil.charValue("\u00b2"), '\u00b2');
		assertEquals(PrimitiveUtil.shortValue("32767"), (short) 32767);
		assertEquals(PrimitiveUtil.shortValue("7fff", 16), (short) 0x7fff);
		assertEquals(PrimitiveUtil.intValue("2147483647"), 2147483647);
		assertEquals(PrimitiveUtil.intValue("7fffffff", 16), 0x7fffffff);
		assertEquals(PrimitiveUtil.longValue("9223372036854775807"), 9223372036854775807L);
		assertEquals(PrimitiveUtil.longValue("7fffffffffffffff", 16), 0x7fffffffffffffffL);
		assertEquals(PrimitiveUtil.floatValue("0.0000000000000001"), 0.0000000000000001f);
		assertEquals(PrimitiveUtil.doubleValue("0.0000000000000000001"), 0.0000000000000000001);
	}

	@Test
	public void testLookup() {
		assertFalse(PrimitiveUtil.lookupBoolean(null, "a"));
		assertTrue(PrimitiveUtil.lookupBoolean(map("a", true, "b", false), "a"));
		assertTrue(PrimitiveUtil.lookupBoolean(map("a", true, "b", false), "c", true));
		assertEquals(PrimitiveUtil.lookupChar(null, "a"), '\0');
		assertEquals(PrimitiveUtil.lookupChar(map("a", 'A', "b", 'B'), "a"), 'A');
		assertEquals(PrimitiveUtil.lookupChar(map("a", 'A', "b", 'B'), "c", 'C'), 'C');
		assertEquals(PrimitiveUtil.lookupByte(null, "a"), (byte) 0);
		assertEquals(PrimitiveUtil.lookupByte(map("a", Byte.MIN_VALUE, "b", Byte.MAX_VALUE), "a"),
			Byte.MIN_VALUE);
		assertEquals(
			PrimitiveUtil.lookupByte(map("a", Byte.MIN_VALUE, "b", Byte.MAX_VALUE), "c", (byte) -1),
			(byte) -1);
		assertEquals(PrimitiveUtil.lookupShort(null, "a"), (short) 0);
		assertEquals(
			PrimitiveUtil.lookupShort(map("a", Short.MIN_VALUE, "b", Short.MAX_VALUE), "a"),
			Short.MIN_VALUE);
		assertEquals(PrimitiveUtil.lookupShort(map("a", Short.MIN_VALUE, "b", Short.MAX_VALUE), "c",
			(short) -1), (short) -1);
		assertEquals(PrimitiveUtil.lookupInt(null, "a"), 0);
		assertEquals(
			PrimitiveUtil.lookupInt(map("a", Integer.MIN_VALUE, "b", Integer.MAX_VALUE), "a"),
			Integer.MIN_VALUE);
		assertEquals(
			PrimitiveUtil.lookupInt(map("a", Integer.MIN_VALUE, "b", Integer.MAX_VALUE), "c", -1),
			-1);
		assertEquals(PrimitiveUtil.lookupLong(null, "a"), 0L);
		assertEquals(PrimitiveUtil.lookupLong(map("a", Long.MIN_VALUE, "b", Long.MAX_VALUE), "a"),
			Long.MIN_VALUE);
		assertEquals(
			PrimitiveUtil.lookupLong(map("a", Long.MIN_VALUE, "b", Long.MAX_VALUE), "c", -1), -1L);
		assertEquals(PrimitiveUtil.lookupFloat(null, "a"), 0.0f);
		assertEquals(
			PrimitiveUtil.lookupFloat(map("a", Float.MIN_VALUE, "b", Float.MAX_VALUE), "a"),
			Float.MIN_VALUE);
		assertEquals(
			PrimitiveUtil.lookupFloat(map("a", Float.MIN_VALUE, "b", Float.MAX_VALUE), "c", -1),
			-1.0f);
		assertEquals(PrimitiveUtil.lookupDouble(null, "a"), 0.0);
		assertEquals(
			PrimitiveUtil.lookupDouble(map("a", Double.MIN_VALUE, "b", Double.MAX_VALUE), "a"),
			Double.MIN_VALUE);
		assertEquals(
			PrimitiveUtil.lookupDouble(map("a", Double.MIN_VALUE, "b", Double.MAX_VALUE), "c", -1),
			-1.0);
	}

	private static <T, U> Map<T, U> map(T t1, U u1, T t2, U u2) {
		Map<T, U> map = new HashMap<>();
		map.put(t1, u1);
		map.put(t2, u2);
		return map;
	}

}
