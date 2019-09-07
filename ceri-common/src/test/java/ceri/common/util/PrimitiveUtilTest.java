package ceri.common.util;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collection;
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
		assertThat(PrimitiveUtil.decode("", Byte.MIN_VALUE), is(Byte.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("088", Byte.MIN_VALUE), is(Byte.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("", Short.MIN_VALUE), is(Short.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("088", Short.MIN_VALUE), is(Short.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("", Integer.MIN_VALUE), is(Integer.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("088", Integer.MIN_VALUE), is(Integer.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("", Long.MIN_VALUE), is(Long.MIN_VALUE));
		assertThat(PrimitiveUtil.decode("088", Long.MIN_VALUE), is(Long.MIN_VALUE));
	}

	@Test
	public void testConvertArrays() {
		boolean[] b = { true, false };
		Boolean[] B = { true, false };
		assertArray(PrimitiveUtil.convertBooleanArray(b), B);
		assertArray(PrimitiveUtil.convertBooleanArray(B), b);
		char[] c = { '\0', 'a' };
		Character[] C = { '\0', 'a' };
		assertArray(PrimitiveUtil.convertCharArray(c), C);
		assertArray(PrimitiveUtil.convertCharArray(C), c);
	}

	@Test
	public void testConvertNumberArrays() {
		byte[] bt = { -128, 127 };
		Byte[] Bt = { -128, 127 };
		assertArray(PrimitiveUtil.convertByteArray(bt), Bt);
		assertArray(PrimitiveUtil.convertByteArray(Bt), bt);
		short[] s = { Short.MAX_VALUE, Short.MIN_VALUE };
		Short[] S = { Short.MAX_VALUE, Short.MIN_VALUE };
		assertArray(PrimitiveUtil.convertShortArray(s), S);
		assertArray(PrimitiveUtil.convertShortArray(S), s);
		int[] i = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		Integer[] I = { Integer.MAX_VALUE, Integer.MIN_VALUE };
		assertArray(PrimitiveUtil.convertIntArray(i), I);
		assertArray(PrimitiveUtil.convertIntArray(I), i);
		long[] l = { Long.MAX_VALUE, Long.MIN_VALUE };
		Long[] L = { Long.MAX_VALUE, Long.MIN_VALUE };
		assertArray(PrimitiveUtil.convertLongArray(l), L);
		assertArray(PrimitiveUtil.convertLongArray(L), l);
		double[] d = { Double.MAX_VALUE, Double.MIN_VALUE };
		Double[] D = { Double.MAX_VALUE, Double.MIN_VALUE };
		assertArray(PrimitiveUtil.convertDoubleArray(d), D);
		assertArray(PrimitiveUtil.convertDoubleArray(D), d);
		float[] f = { Float.MAX_VALUE, Float.MIN_VALUE };
		Float[] F = { Float.MAX_VALUE, Float.MIN_VALUE };
		assertArray(PrimitiveUtil.convertFloatArray(f), F);
		assertArray(PrimitiveUtil.convertFloatArray(F), f);
	}

	@Test
	public void testGetPrimitiveClass() {
		assertSame(double.class, PrimitiveUtil.getPrimitiveClass(Double.class));
		assertException(IllegalArgumentException.class,
			() -> PrimitiveUtil.getPrimitiveClass(Boolean.TYPE));
		assertException(IllegalArgumentException.class,
			() -> PrimitiveUtil.getPrimitiveClass(String.class));
	}

	@Test
	public void testGetObjectClass() {
		assertSame(Double.class, PrimitiveUtil.getObjectClass(double.class));
		assertException(IllegalArgumentException.class,
			() -> PrimitiveUtil.getObjectClass(Boolean.class));
	}

	@Test
	public void testAutoBoxAssignable() {
		assertThat(PrimitiveUtil.isAutoBoxAssignable(Long.class, long.class), is(true));
		assertThat(PrimitiveUtil.isAutoBoxAssignable(long.class, Long.class), is(true));
		assertThat(PrimitiveUtil.isAutoBoxAssignable(double.class, Number.class), is(true));
		assertThat(PrimitiveUtil.isAutoBoxAssignable(Number.class, double.class), is(false));
	}

	@Test
	public void testToArray() {
		Collection<Number> collection = Arrays.asList(1, 16, 256, 4096, 65536);
		assertThat(PrimitiveUtil.toByteArray(collection), is(new byte[] { 1, 16, 0, 0, 0 }));
		assertThat(PrimitiveUtil.toShortArray(collection), is(new short[] { 1, 16, 256, 4096, 0 }));
		assertThat(PrimitiveUtil.toIntArray(collection), is(new int[] { 1, 16, 256, 4096, 65536 }));
		assertThat(PrimitiveUtil.toLongArray(collection),
			is(new long[] { 1, 16, 256, 4096, 65536 }));
		assertThat(PrimitiveUtil.toFloatArray(collection),
			is(new float[] { 1, 16, 256, 4096, 65536 }));
		assertThat(PrimitiveUtil.toDoubleArray(collection),
			is(new double[] { 1, 16, 256, 4096, 65536 }));
	}

	@Test
	public void testValueOf() {
		assertThat(PrimitiveUtil.valueOf(null, true), is(true));
		assertThat(PrimitiveUtil.valueOf("FALSE", true), is(false));
		assertThat(PrimitiveUtil.valueOf("1", false), is(false));
		assertThat(PrimitiveUtil.valueOf(null, '\n'), is('\n'));
		assertThat(PrimitiveUtil.valueOf("A", 'a'), is('A'));
	}

	@Test
	public void testValueOfNumbers() {
		assertThat(PrimitiveUtil.valueOf(null, (byte) 0), is((byte) 0));
		assertThat(PrimitiveUtil.valueOf("x", Byte.MIN_VALUE), is(Byte.MIN_VALUE));
		assertThat(PrimitiveUtil.valueOf("-1", Byte.MIN_VALUE), is((byte) -1));
		assertThat(PrimitiveUtil.valueOf(null, (short) -1), is((short) -1));
		assertThat(PrimitiveUtil.valueOf("x", Short.MIN_VALUE), is(Short.MIN_VALUE));
		assertThat(PrimitiveUtil.valueOf("1", Short.MIN_VALUE), is((short) 1));
		assertThat(PrimitiveUtil.valueOf(null, Integer.MIN_VALUE), is(Integer.MIN_VALUE));
		assertThat(PrimitiveUtil.valueOf("--", Integer.MAX_VALUE), is(Integer.MAX_VALUE));
		assertThat(PrimitiveUtil.valueOf("100", Integer.MAX_VALUE), is(100));
		assertThat(PrimitiveUtil.valueOf(null, 1L), is(1L));
		assertThat(PrimitiveUtil.valueOf("x", 1L), is(1L));
		assertThat(PrimitiveUtil.valueOf("-100000000000", 1L), is(-100000000000L));
		assertThat(PrimitiveUtil.valueOf(null, Double.NaN), is(Double.NaN));
		assertThat(PrimitiveUtil.valueOf("a", Double.NaN), is(Double.NaN));
		assertThat(PrimitiveUtil.valueOf("-0.1E300", Double.NaN), is(-0.1E300));
		assertThat(PrimitiveUtil.valueOf(null, Float.NaN), is(Float.NaN));
		assertThat(PrimitiveUtil.valueOf("a", Float.NaN), is(Float.NaN));
		assertThat(PrimitiveUtil.valueOf("1E30", Float.NaN), is(1E30f));
	}

	@Test
	public void testValue() {
		assertThat(PrimitiveUtil.booleanValue("TRUE"), is(true));
		assertThat(PrimitiveUtil.byteValue("127"), is((byte) 127));
		assertThat(PrimitiveUtil.byteValue("7f", 16), is((byte) 0x7f));
		assertThat(PrimitiveUtil.charValue("\u00b2"), is('\u00b2'));
		assertThat(PrimitiveUtil.shortValue("32767"), is((short) 32767));
		assertThat(PrimitiveUtil.shortValue("7fff", 16), is((short) 0x7fff));
		assertThat(PrimitiveUtil.intValue("2147483647"), is(2147483647));
		assertThat(PrimitiveUtil.intValue("7fffffff", 16), is(0x7fffffff));
		assertThat(PrimitiveUtil.longValue("9223372036854775807"), is(9223372036854775807L));
		assertThat(PrimitiveUtil.longValue("7fffffffffffffff", 16), is(0x7fffffffffffffffL));
		assertThat(PrimitiveUtil.floatValue("0.0000000000000001"), is(0.0000000000000001f));
		assertThat(PrimitiveUtil.doubleValue("0.0000000000000000001"), is(0.0000000000000000001));
	}

	@Test
	public void testLookup() {
		assertThat(PrimitiveUtil.lookupBoolean(null, "a"), is(false));
		assertThat(PrimitiveUtil.lookupBoolean(map("a", true, "b", false), "a"), is(true));
		assertThat(PrimitiveUtil.lookupBoolean(map("a", true, "b", false), "c", true), is(true));
		assertThat(PrimitiveUtil.lookupChar(null, "a"), is('\0'));
		assertThat(PrimitiveUtil.lookupChar(map("a", 'A', "b", 'B'), "a"), is('A'));
		assertThat(PrimitiveUtil.lookupChar(map("a", 'A', "b", 'B'), "c", 'C'), is('C'));
		assertThat(PrimitiveUtil.lookupByte(null, "a"), is((byte) 0));
		assertThat(PrimitiveUtil.lookupByte(map("a", Byte.MIN_VALUE, "b", Byte.MAX_VALUE), //
			"a"), is(Byte.MIN_VALUE));
		assertThat(PrimitiveUtil.lookupByte(map("a", Byte.MIN_VALUE, "b", Byte.MAX_VALUE), //
			"c", (byte) -1), is((byte) -1));
		assertThat(PrimitiveUtil.lookupShort(null, "a"), is((short) 0));
		assertThat(PrimitiveUtil.lookupShort(map("a", Short.MIN_VALUE, "b", Short.MAX_VALUE), //
			"a"), is(Short.MIN_VALUE));
		assertThat(PrimitiveUtil.lookupShort(map("a", Short.MIN_VALUE, "b", Short.MAX_VALUE), //
			"c", (short) -1), is((short) -1));
		assertThat(PrimitiveUtil.lookupInt(null, "a"), is(0));
		assertThat(PrimitiveUtil.lookupInt(map("a", Integer.MIN_VALUE, "b", Integer.MAX_VALUE), //
			"a"), is(Integer.MIN_VALUE));
		assertThat(PrimitiveUtil.lookupInt(map("a", Integer.MIN_VALUE, "b", Integer.MAX_VALUE), //
			"c", -1), is(-1));
		assertThat(PrimitiveUtil.lookupLong(null, "a"), is(0L));
		assertThat(PrimitiveUtil.lookupLong(map("a", Long.MIN_VALUE, "b", Long.MAX_VALUE), //
			"a"), is(Long.MIN_VALUE));
		assertThat(PrimitiveUtil.lookupLong(map("a", Long.MIN_VALUE, "b", Long.MAX_VALUE), //
			"c", -1), is(-1L));
		assertThat(PrimitiveUtil.lookupFloat(null, "a"), is(0.0f));
		assertThat(PrimitiveUtil.lookupFloat(map("a", Float.MIN_VALUE, "b", Float.MAX_VALUE), //
			"a"), is(Float.MIN_VALUE));
		assertThat(PrimitiveUtil.lookupFloat(map("a", Float.MIN_VALUE, "b", Float.MAX_VALUE), //
			"c", -1), is(-1.0f));
		assertThat(PrimitiveUtil.lookupDouble(null, "a"), is(0.0));
		assertThat(PrimitiveUtil.lookupDouble(map("a", Double.MIN_VALUE, "b", Double.MAX_VALUE), //
			"a"), is(Double.MIN_VALUE));
		assertThat(PrimitiveUtil.lookupDouble(map("a", Double.MIN_VALUE, "b", Double.MAX_VALUE), //
			"c", -1), is(-1.0));
	}

	private static <T, U> Map<T, U> map(T t1, U u1, T t2, U u2) {
		Map<T, U> map = new HashMap<>();
		map.put(t1, u1);
		map.put(t2, u2);
		return map;
	}

	@Test
	public void testAsList() {
		assertThat(PrimitiveUtil.asList(new boolean[] { true, false }),
			is(Arrays.asList(true, false)));
		assertThat(PrimitiveUtil.asList(new char[] { 'a', '\0' }), is(Arrays.asList('a', '\0')));
		assertThat(PrimitiveUtil.asList(Byte.MAX_VALUE, Byte.MIN_VALUE),
			is(Arrays.asList(Byte.MAX_VALUE, Byte.MIN_VALUE)));
		assertThat(PrimitiveUtil.asList(Short.MAX_VALUE, Short.MIN_VALUE),
			is(Arrays.asList(Short.MAX_VALUE, Short.MIN_VALUE)));
		assertThat(PrimitiveUtil.asList(Integer.MAX_VALUE, Integer.MIN_VALUE),
			is(Arrays.asList(Integer.MAX_VALUE, Integer.MIN_VALUE)));
		assertThat(PrimitiveUtil.asList(Long.MAX_VALUE, Long.MIN_VALUE),
			is(Arrays.asList(Long.MAX_VALUE, Long.MIN_VALUE)));
		assertThat(PrimitiveUtil.asList(Double.MAX_VALUE, Double.MIN_VALUE),
			is(Arrays.asList(Double.MAX_VALUE, Double.MIN_VALUE)));
		assertThat(PrimitiveUtil.asList(Float.MAX_VALUE, Float.MIN_VALUE),
			is(Arrays.asList(Float.MAX_VALUE, Float.MIN_VALUE)));
	}

}
