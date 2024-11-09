package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class ParseUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(ParseUtil.class);
	}

	@Test
	public void testDecode() {
		assertEquals(ParseUtil.decodeByte(""), null);
		assertEquals(ParseUtil.decodeByte("x"), null);
		assertEquals(ParseUtil.decodeByte("033"), (byte) 033);
		assertEquals(ParseUtil.decodeShort(""), null);
		assertEquals(ParseUtil.decodeShort("x"), null);
		assertEquals(ParseUtil.decodeShort("033"), (short) 033);
		assertEquals(ParseUtil.decodeInt(""), null);
		assertEquals(ParseUtil.decodeInt("x"), null);
		assertEquals(ParseUtil.decodeInt("033"), 033);
		assertEquals(ParseUtil.decodeLong(""), null);
		assertEquals(ParseUtil.decodeLong("x"), null);
		assertEquals(ParseUtil.decodeLong("033"), 033L);
	}

	@Test
	public void testDecodeWithDefault() {
		assertEquals(ParseUtil.decodeByte("", Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(ParseUtil.decodeByte("033", Byte.MIN_VALUE), (byte) 033);
		assertEquals(ParseUtil.decodeShort("", Short.MIN_VALUE), Short.MIN_VALUE);
		assertEquals(ParseUtil.decodeShort("033", Short.MIN_VALUE), (short) 033);
		assertEquals(ParseUtil.decodeInt("", Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(ParseUtil.decodeInt("033", Integer.MIN_VALUE), 033);
		assertEquals(ParseUtil.decodeLong("", Long.MIN_VALUE), Long.MIN_VALUE);
		assertEquals(ParseUtil.decodeLong("033", Long.MIN_VALUE), 033L);
	}

	@Test
	public void testParseWithDefault() {
		assertTrue(ParseUtil.parseBool(null, true));
		assertFalse(ParseUtil.parseBool("FALSE", true));
		assertFalse(ParseUtil.parseBool("1", false));
		assertEquals(ParseUtil.parseByte(null, (byte) 0), (byte) 0);
		assertEquals(ParseUtil.parseByte("x", Byte.MIN_VALUE), Byte.MIN_VALUE);
		assertEquals(ParseUtil.parseByte("-1", Byte.MIN_VALUE), (byte) -1);
		assertEquals(ParseUtil.parseShort(null, (short) -1), (short) -1);
		assertEquals(ParseUtil.parseShort("x", Short.MIN_VALUE), Short.MIN_VALUE);
		assertEquals(ParseUtil.parseShort("1", Short.MIN_VALUE), (short) 1);
		assertEquals(ParseUtil.parseInt(null, Integer.MIN_VALUE), Integer.MIN_VALUE);
		assertEquals(ParseUtil.parseInt("--", Integer.MAX_VALUE), Integer.MAX_VALUE);
		assertEquals(ParseUtil.parseInt("100", Integer.MAX_VALUE), 100);
		assertEquals(ParseUtil.parseLong(null, 1L), 1L);
		assertEquals(ParseUtil.parseLong("x", 1L), 1L);
		assertEquals(ParseUtil.parseLong("-100000000000", 1L), -100000000000L);
		assertEquals(ParseUtil.parseDouble(null, Double.NaN), Double.NaN);
		assertEquals(ParseUtil.parseDouble("a", Double.NaN), Double.NaN);
		assertEquals(ParseUtil.parseDouble("-0.1E300", Double.NaN), -0.1E300);
		assertEquals(ParseUtil.parseFloat(null, Float.NaN), Float.NaN);
		assertEquals(ParseUtil.parseFloat("a", Float.NaN), Float.NaN);
		assertEquals(ParseUtil.parseFloat("1E30", Float.NaN), 1E30f);
	}

	@Test
	public void testParse() {
		assertTrue(ParseUtil.parseBool("TRUE"));
		assertEquals(ParseUtil.parseByte("127"), (byte) 127);
		assertEquals(ParseUtil.parseByte("7f", null, 16), (byte) 0x7f);
		assertEquals(ParseUtil.parseShort("32767"), (short) 32767);
		assertEquals(ParseUtil.parseShort("7fff", null, 16), (short) 0x7fff);
		assertEquals(ParseUtil.parseInt("2147483647"), 2147483647);
		assertEquals(ParseUtil.parseInt("7fffffff", null, 16), 0x7fffffff);
		assertEquals(ParseUtil.parseLong("9223372036854775807"), 9223372036854775807L);
		assertEquals(ParseUtil.parseLong("7fffffffffffffff", null, 16), 0x7fffffffffffffffL);
		assertEquals(ParseUtil.parseFloat("0.0000000000000001"), 0.0000000000000001f);
		assertEquals(ParseUtil.parseDouble("0.0000000000000000001"), 0.0000000000000000001);
	}
}
