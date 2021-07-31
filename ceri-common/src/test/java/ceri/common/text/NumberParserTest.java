package ceri.common.text;

import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertShort;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class NumberParserTest {

	@Test
	public void testParseByte() {
		assertByte(NumberParser.parseByte("ff", 16), 0xff);
		assertByte(NumberParser.parseByte("+ff", 16), 0xff);
		assertByte(NumberParser.parseByte("-ff", 16), 0x01);
		assertByte(NumberParser.parseByte("80", 16), 0x80);
		assertByte(NumberParser.parseByte("+80", 16), 0x80);
		assertByte(NumberParser.parseByte("-80", 16), 0x80);
		assertByte(NumberParser.parseByte("377", 8), 0xff);
		assertByte(NumberParser.parseByte("+377", 8), 0xff);
		assertByte(NumberParser.parseByte("-377", 8), 0x01);
		assertByte(NumberParser.parseByte("11111111", 2), 0xff);
		assertByte(NumberParser.parseByte("+11111111", 2), 0xff);
		assertByte(NumberParser.parseByte("-11111111", 2), 0x01);
		assertByte(NumberParser.parseByte("255"), 0xff);
		assertByte(NumberParser.parseByte("+255"), 0xff);
		assertByte(NumberParser.parseByte("-255"), 0x01);
		assertByte(NumberParser.parseByte("1"), 1);
		assertByte(NumberParser.parseByte("+1"), 1);
		assertByte(NumberParser.parseByte("-1"), -1);
		assertByte(NumberParser.parseByte("0"), 0);
		assertByte(NumberParser.parseByte("+0"), 0);
		assertByte(NumberParser.parseByte("-0"), 0);
	}

	@Test
	public void testParseInvalidByte() {
		assertThrown(() -> NumberParser.parseByte(""));
		assertThrown(() -> NumberParser.parseByte("-+1"));
		assertThrown(() -> NumberParser.parseByte("1", 1));
		assertThrown(() -> NumberParser.parseByte("1", 37));
		assertThrown(() -> NumberParser.parseByte("100", 16));
		assertThrown(() -> NumberParser.parseByte("-100", 16));
	}

	@Test
	public void testParseShort() {
		assertShort(NumberParser.parseShort("ffff", 16), 0xffff);
		assertShort(NumberParser.parseShort("+ffff", 16), 0xffff);
		assertShort(NumberParser.parseShort("-ffff", 16), 0x0001);
		assertShort(NumberParser.parseShort("8000", 16), 0x8000);
		assertShort(NumberParser.parseShort("+8000", 16), 0x8000);
		assertShort(NumberParser.parseShort("-8000", 16), 0x8000);
		assertShort(NumberParser.parseShort("177777", 8), 0xffff);
		assertShort(NumberParser.parseShort("+177777", 8), 0xffff);
		assertShort(NumberParser.parseShort("-177777", 8), 0x0001);
		assertShort(NumberParser.parseShort("1111111111111111", 2), 0xffff);
		assertShort(NumberParser.parseShort("+1111111111111111", 2), 0xffff);
		assertShort(NumberParser.parseShort("-1111111111111111", 2), 0x0001);
		assertShort(NumberParser.parseShort("65535"), 0xffff);
		assertShort(NumberParser.parseShort("+65535"), 0xffff);
		assertShort(NumberParser.parseShort("-65535"), 0x0001);
		assertShort(NumberParser.parseShort("1"), 1);
		assertShort(NumberParser.parseShort("+1"), 1);
		assertShort(NumberParser.parseShort("-1"), -1);
		assertShort(NumberParser.parseShort("0"), 0);
		assertShort(NumberParser.parseShort("+0"), 0);
		assertShort(NumberParser.parseShort("-0"), 0);
	}

	@Test
	public void testParseInvalidShort() {
		assertThrown(() -> NumberParser.parseShort(""));
		assertThrown(() -> NumberParser.parseShort("-+1"));
		assertThrown(() -> NumberParser.parseShort("1", 1));
		assertThrown(() -> NumberParser.parseShort("1", 37));
		assertThrown(() -> NumberParser.parseShort("10000", 16));
		assertThrown(() -> NumberParser.parseShort("-10000", 16));
	}

	@Test
	public void testParseInt() {
		assertEquals(NumberParser.parseInt("ffffffff", 16), 0xffffffff);
		assertEquals(NumberParser.parseInt("+ffffffff", 16), 0xffffffff);
		assertEquals(NumberParser.parseInt("-ffffffff", 16), 0x00000001);
		assertEquals(NumberParser.parseInt("80000000", 16), 0x80000000);
		assertEquals(NumberParser.parseInt("+80000000", 16), 0x80000000);
		assertEquals(NumberParser.parseInt("-80000000", 16), 0x80000000);
		assertEquals(NumberParser.parseInt("37777777777", 8), 0xffffffff);
		assertEquals(NumberParser.parseInt("+37777777777", 8), 0xffffffff);
		assertEquals(NumberParser.parseInt("-37777777777", 8), 0x00000001);
		assertEquals(NumberParser.parseInt("11111111111111111111111111111111", 2), 0xffffffff);
		assertEquals(NumberParser.parseInt("+11111111111111111111111111111111", 2), 0xffffffff);
		assertEquals(NumberParser.parseInt("-11111111111111111111111111111111", 2), 0x00000001);
		assertEquals(NumberParser.parseInt("4294967295"), 0xffffffff);
		assertEquals(NumberParser.parseInt("+4294967295"), 0xffffffff);
		assertEquals(NumberParser.parseInt("-4294967295"), 0x00000001);
		assertEquals(NumberParser.parseInt("1"), 1);
		assertEquals(NumberParser.parseInt("+1"), 1);
		assertEquals(NumberParser.parseInt("-1"), -1);
		assertEquals(NumberParser.parseInt("0"), 0);
		assertEquals(NumberParser.parseInt("+0"), 0);
		assertEquals(NumberParser.parseInt("-0"), 0);
	}

	@Test
	public void testParseInvalidInt() {
		assertThrown(() -> NumberParser.parseInt(""));
		assertThrown(() -> NumberParser.parseInt("-+1"));
		assertThrown(() -> NumberParser.parseInt("1", 1));
		assertThrown(() -> NumberParser.parseInt("1", 37));
		assertThrown(() -> NumberParser.parseInt("100000000", 16));
		assertThrown(() -> NumberParser.parseInt("-100000000", 16));
	}

	@Test
	public void testParseLong() {
		assertEquals(NumberParser.parseLong("ffffffffffffffff", 16), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong("+ffffffffffffffff", 16), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong("-ffffffffffffffff", 16), 0x0000000000000001L);
		assertEquals(NumberParser.parseLong("8000000000000000", 16), 0x8000000000000000L);
		assertEquals(NumberParser.parseLong("+8000000000000000", 16), 0x8000000000000000L);
		assertEquals(NumberParser.parseLong("-8000000000000000", 16), 0x8000000000000000L);
		assertEquals(NumberParser.parseLong("1777777777777777777777", 8), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong("+1777777777777777777777", 8), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong("-1777777777777777777777", 8), 0x0000000000000001L);
		assertEquals(NumberParser.parseLong( //
			"1111111111111111111111111111111111111111111111111111111111111111", 2),
			0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong( //
			"+1111111111111111111111111111111111111111111111111111111111111111", 2),
			0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong( //
			"-1111111111111111111111111111111111111111111111111111111111111111", 2),
			0x0000000000000001L);
		assertEquals(NumberParser.parseLong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseLong("-18446744073709551615"), 0x0000000000000001L);
		assertEquals(NumberParser.parseLong("1"), 1L);
		assertEquals(NumberParser.parseLong("+1"), 1L);
		assertEquals(NumberParser.parseLong("-1"), -1L);
		assertEquals(NumberParser.parseLong("0"), 0L);
		assertEquals(NumberParser.parseLong("+0"), 0L);
		assertEquals(NumberParser.parseLong("-0"), 0L);
	}

	@Test
	public void testParseInvalidLong() {
		assertThrown(() -> NumberParser.parseLong(""));
		assertThrown(() -> NumberParser.parseLong("-+1"));
		assertThrown(() -> NumberParser.parseLong("1", 1));
		assertThrown(() -> NumberParser.parseLong("1", 37));
		assertThrown(() -> NumberParser.parseLong("10000000000000000", 16));
		assertThrown(() -> NumberParser.parseLong("-10000000000000000", 16));
	}

	@Test
	public void testParseUbyte() {
		assertByte(NumberParser.parseUbyte("ff", 16), 0xff);
		assertByte(NumberParser.parseUbyte("+ff", 16), 0xff);
		assertByte(NumberParser.parseUbyte("80", 16), 0x80);
		assertByte(NumberParser.parseUbyte("+80", 16), 0x80);
		assertByte(NumberParser.parseUbyte("377", 8), 0xff);
		assertByte(NumberParser.parseUbyte("+377", 8), 0xff);
		assertByte(NumberParser.parseUbyte("11111111", 2), 0xff);
		assertByte(NumberParser.parseUbyte("+11111111", 2), 0xff);
		assertByte(NumberParser.parseUbyte("255"), 0xff);
		assertByte(NumberParser.parseUbyte("+255"), 0xff);
		assertByte(NumberParser.parseUbyte("1"), 1);
		assertByte(NumberParser.parseUbyte("+1"), 1);
		assertByte(NumberParser.parseUbyte("0"), 0);
		assertByte(NumberParser.parseUbyte("+0"), 0);
	}

	@Test
	public void testParseInvalidUbyte() {
		assertThrown(() -> NumberParser.parseUbyte(""));
		assertThrown(() -> NumberParser.parseUbyte("-1"));
		assertThrown(() -> NumberParser.parseUbyte("++1"));
		assertThrown(() -> NumberParser.parseUbyte("1", 1));
		assertThrown(() -> NumberParser.parseUbyte("1", 37));
		assertThrown(() -> NumberParser.parseUbyte("100", 16));
	}

	@Test
	public void testParseUshort() {
		assertShort(NumberParser.parseUshort("ffff", 16), 0xffff);
		assertShort(NumberParser.parseUshort("+ffff", 16), 0xffff);
		assertShort(NumberParser.parseUshort("8000", 16), 0x8000);
		assertShort(NumberParser.parseUshort("+8000", 16), 0x8000);
		assertShort(NumberParser.parseUshort("177777", 8), 0xffff);
		assertShort(NumberParser.parseUshort("+177777", 8), 0xffff);
		assertShort(NumberParser.parseUshort("1111111111111111", 2), 0xffff);
		assertShort(NumberParser.parseUshort("+1111111111111111", 2), 0xffff);
		assertShort(NumberParser.parseUshort("65535"), 0xffff);
		assertShort(NumberParser.parseUshort("+65535"), 0xffff);
		assertShort(NumberParser.parseUshort("1"), 1);
		assertShort(NumberParser.parseUshort("+1"), 1);
		assertShort(NumberParser.parseUshort("0"), 0);
		assertShort(NumberParser.parseUshort("+0"), 0);
	}

	@Test
	public void testParseInvalidUshort() {
		assertThrown(() -> NumberParser.parseUshort(""));
		assertThrown(() -> NumberParser.parseUshort("-1"));
		assertThrown(() -> NumberParser.parseUshort("++1"));
		assertThrown(() -> NumberParser.parseUshort("1", 1));
		assertThrown(() -> NumberParser.parseUshort("1", 37));
		assertThrown(() -> NumberParser.parseUshort("10000", 16));
	}

	@Test
	public void testParseUint() {
		assertEquals(NumberParser.parseUint("ffffffff", 16), 0xffffffff);
		assertEquals(NumberParser.parseUint("+ffffffff", 16), 0xffffffff);
		assertEquals(NumberParser.parseUint("80000000", 16), 0x80000000);
		assertEquals(NumberParser.parseUint("+80000000", 16), 0x80000000);
		assertEquals(NumberParser.parseUint("37777777777", 8), 0xffffffff);
		assertEquals(NumberParser.parseUint("+37777777777", 8), 0xffffffff);
		assertEquals(NumberParser.parseUint("11111111111111111111111111111111", 2), 0xffffffff);
		assertEquals(NumberParser.parseUint("+11111111111111111111111111111111", 2), 0xffffffff);
		assertEquals(NumberParser.parseUint("4294967295"), 0xffffffff);
		assertEquals(NumberParser.parseUint("+4294967295"), 0xffffffff);
		assertEquals(NumberParser.parseUint("1"), 1);
		assertEquals(NumberParser.parseUint("+1"), 1);
		assertEquals(NumberParser.parseUint("0"), 0);
		assertEquals(NumberParser.parseUint("+0"), 0);
	}

	@Test
	public void testParseInvalidUint() {
		assertThrown(() -> NumberParser.parseUint(""));
		assertThrown(() -> NumberParser.parseUint("-1"));
		assertThrown(() -> NumberParser.parseUint("++1"));
		assertThrown(() -> NumberParser.parseUint("1", 1));
		assertThrown(() -> NumberParser.parseUint("1", 37));
		assertThrown(() -> NumberParser.parseUint("100000000", 16));
	}

	@Test
	public void testParseUlong() {
		assertEquals(NumberParser.parseUlong("ffffffffffffffff", 16), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong("+ffffffffffffffff", 16), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong("8000000000000000", 16), 0x8000000000000000L);
		assertEquals(NumberParser.parseUlong("+8000000000000000", 16), 0x8000000000000000L);
		assertEquals(NumberParser.parseUlong("1777777777777777777777", 8), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong("+1777777777777777777777", 8), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong( //
			"1111111111111111111111111111111111111111111111111111111111111111", 2),
			0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong( //
			"+1111111111111111111111111111111111111111111111111111111111111111", 2),
			0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.parseUlong("1"), 1L);
		assertEquals(NumberParser.parseUlong("+1"), 1L);
		assertEquals(NumberParser.parseUlong("0"), 0L);
		assertEquals(NumberParser.parseUlong("+0"), 0L);
	}

	@Test
	public void testParseInvalidUlong() {
		assertThrown(() -> NumberParser.parseUlong(""));
		assertThrown(() -> NumberParser.parseUlong("-1"));
		assertThrown(() -> NumberParser.parseUlong("++1"));
		assertThrown(() -> NumberParser.parseUlong("1", 1));
		assertThrown(() -> NumberParser.parseUlong("1", 37));
		assertThrown(() -> NumberParser.parseUlong("10000000000000000", 16));
	}

	@Test
	public void testDecodeByte() {
		assertByte(NumberParser.decodeByte("0xff"), 0xff);
		assertByte(NumberParser.decodeByte("+0xff"), 0xff);
		assertByte(NumberParser.decodeByte("-0xff"), 0x01);
		assertByte(NumberParser.decodeByte("0xfe"), 0xfe);
		assertByte(NumberParser.decodeByte("+0xfe"), 0xfe);
		assertByte(NumberParser.decodeByte("-0xfe"), 0x02);
		assertByte(NumberParser.decodeByte("0x81"), 0x81);
		assertByte(NumberParser.decodeByte("+0x81"), 0x81);
		assertByte(NumberParser.decodeByte("-0x81"), 0x7f);
		assertByte(NumberParser.decodeByte("0x80"), 0x80);
		assertByte(NumberParser.decodeByte("+0x80"), 0x80);
		assertByte(NumberParser.decodeByte("-0x80"), 0x80);
		assertByte(NumberParser.decodeByte("0x7f"), 0x7f);
		assertByte(NumberParser.decodeByte("+0x7f"), 0x7f);
		assertByte(NumberParser.decodeByte("-0x7f"), 0x81);
		assertByte(NumberParser.decodeByte("0x7e"), 0x7e);
		assertByte(NumberParser.decodeByte("+0x7e"), 0x7e);
		assertByte(NumberParser.decodeByte("-0x7e"), 0x82);
		assertByte(NumberParser.decodeByte("0x0001"), 0x01);
		assertByte(NumberParser.decodeByte("+0x0001"), 0x01);
		assertByte(NumberParser.decodeByte("-0x0001"), 0xff);
		assertByte(NumberParser.decodeByte("0377"), 0xff);
		assertByte(NumberParser.decodeByte("+0377"), 0xff);
		assertByte(NumberParser.decodeByte("-0377"), 0x01);
		assertByte(NumberParser.decodeByte("0b11111111"), 0xff);
		assertByte(NumberParser.decodeByte("+0b11111111"), 0xff);
		assertByte(NumberParser.decodeByte("-0b11111111"), 0x01);
		assertByte(NumberParser.decodeByte("255"), 0xff);
		assertByte(NumberParser.decodeByte("+255"), 0xff);
		assertByte(NumberParser.decodeByte("-255"), 0x01);
		assertByte(NumberParser.decodeByte("1"), 1);
		assertByte(NumberParser.decodeByte("+1"), 1);
		assertByte(NumberParser.decodeByte("-1"), -1);
		assertByte(NumberParser.decodeByte("0"), 0);
		assertByte(NumberParser.decodeByte("+0"), 0);
		assertByte(NumberParser.decodeByte("-0"), 0);
	}

	@Test
	public void testDecodeInvalidByte() {
		assertThrown(() -> NumberParser.decodeByte(""));
		assertThrown(() -> NumberParser.decodeByte("-+1"));
		assertThrown(() -> NumberParser.decodeByte("0x"));
		assertThrown(() -> NumberParser.decodeByte("0x+1"));
		assertThrown(() -> NumberParser.decodeByte("0-1"));
		assertThrown(() -> NumberParser.decodeByte("08"));
		assertThrown(() -> NumberParser.decodeByte("0b"));
		assertThrown(() -> NumberParser.decodeByte("0x100"));
		assertThrown(() -> NumberParser.decodeByte("-0x100"));
		assertThrown(() -> NumberParser.decodeByte("-0x80000000"));
	}

	@Test
	public void testDecodeShort() {
		assertShort(NumberParser.decodeShort("0xffff"), 0xffff);
		assertShort(NumberParser.decodeShort("+0xffff"), 0xffff);
		assertShort(NumberParser.decodeShort("-0xffff"), 0x0001);
		assertShort(NumberParser.decodeShort("0xfffe"), 0xfffe);
		assertShort(NumberParser.decodeShort("+0xfffe"), 0xfffe);
		assertShort(NumberParser.decodeShort("-0xfffe"), 0x0002);
		assertShort(NumberParser.decodeShort("0x8001"), 0x8001);
		assertShort(NumberParser.decodeShort("+0x8001"), 0x8001);
		assertShort(NumberParser.decodeShort("-0x8001"), 0x7fff);
		assertShort(NumberParser.decodeShort("0x8000"), 0x8000);
		assertShort(NumberParser.decodeShort("+0x8000"), 0x8000);
		assertShort(NumberParser.decodeShort("-0x8000"), 0x8000);
		assertShort(NumberParser.decodeShort("0x7fff"), 0x7fff);
		assertShort(NumberParser.decodeShort("+0x7fff"), 0x7fff);
		assertShort(NumberParser.decodeShort("-0x7fff"), 0x8001);
		assertShort(NumberParser.decodeShort("0x7ffe"), 0x7ffe);
		assertShort(NumberParser.decodeShort("+0x7ffe"), 0x7ffe);
		assertShort(NumberParser.decodeShort("-0x7ffe"), 0x8002);
		assertShort(NumberParser.decodeShort("0x000001"), 0x0001);
		assertShort(NumberParser.decodeShort("+0x000001"), 0x0001);
		assertShort(NumberParser.decodeShort("-0x000001"), 0xffff);
		assertShort(NumberParser.decodeShort("0177777"), 0xffff);
		assertShort(NumberParser.decodeShort("+0177777"), 0xffff);
		assertShort(NumberParser.decodeShort("-0177777"), 0x0001);
		assertShort(NumberParser.decodeShort("0b1111111111111111"), 0xffff);
		assertShort(NumberParser.decodeShort("+0b1111111111111111"), 0xffff);
		assertShort(NumberParser.decodeShort("-0b1111111111111111"), 0x0001);
		assertShort(NumberParser.decodeShort("65535"), 0xffff);
		assertShort(NumberParser.decodeShort("+65535"), 0xffff);
		assertShort(NumberParser.decodeShort("-65535"), 0x0001);
		assertShort(NumberParser.decodeShort("1"), 1);
		assertShort(NumberParser.decodeShort("+1"), 1);
		assertShort(NumberParser.decodeShort("-1"), -1);
		assertShort(NumberParser.decodeShort("0"), 0);
		assertShort(NumberParser.decodeShort("+0"), 0);
		assertShort(NumberParser.decodeShort("-0"), 0);
	}

	@Test
	public void testDecodeInvalidShort() {
		assertThrown(() -> NumberParser.decodeShort(""));
		assertThrown(() -> NumberParser.decodeShort("-+1"));
		assertThrown(() -> NumberParser.decodeShort("0x"));
		assertThrown(() -> NumberParser.decodeShort("0x+1"));
		assertThrown(() -> NumberParser.decodeShort("0-1"));
		assertThrown(() -> NumberParser.decodeShort("08"));
		assertThrown(() -> NumberParser.decodeShort("0b"));
		assertThrown(() -> NumberParser.decodeShort("0x10000"));
		assertThrown(() -> NumberParser.decodeShort("-0x10000"));
		assertThrown(() -> NumberParser.decodeShort("-0x80000000"));
	}

	@Test
	public void testDecodeInt() {
		assertEquals(NumberParser.decodeInt("0xffffffff"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("+0xffffffff"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("-0xffffffff"), 0x00000001);
		assertEquals(NumberParser.decodeInt("0xfffffffe"), 0xfffffffe);
		assertEquals(NumberParser.decodeInt("+0xfffffffe"), 0xfffffffe);
		assertEquals(NumberParser.decodeInt("-0xfffffffe"), 0x00000002);
		assertEquals(NumberParser.decodeInt("0x80000001"), 0x80000001);
		assertEquals(NumberParser.decodeInt("+0x80000001"), 0x80000001);
		assertEquals(NumberParser.decodeInt("-0x80000001"), 0x7fffffff);
		assertEquals(NumberParser.decodeInt("0x80000000"), 0x80000000);
		assertEquals(NumberParser.decodeInt("+0x80000000"), 0x80000000);
		assertEquals(NumberParser.decodeInt("-0x80000000"), 0x80000000);
		assertEquals(NumberParser.decodeInt("0x7fffffff"), 0x7fffffff);
		assertEquals(NumberParser.decodeInt("+0x7fffffff"), 0x7fffffff);
		assertEquals(NumberParser.decodeInt("-0x7fffffff"), 0x80000001);
		assertEquals(NumberParser.decodeInt("0x7ffffffe"), 0x7ffffffe);
		assertEquals(NumberParser.decodeInt("+0x7ffffffe"), 0x7ffffffe);
		assertEquals(NumberParser.decodeInt("-0x7ffffffe"), 0x80000002);
		assertEquals(NumberParser.decodeInt("0x0000000001"), 0x00000001);
		assertEquals(NumberParser.decodeInt("+0x0000000001"), 0x00000001);
		assertEquals(NumberParser.decodeInt("-0x0000000001"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("037777777777"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("+037777777777"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("-037777777777"), 0x00000001);
		assertEquals(NumberParser.decodeInt("0b11111111111111111111111111111111"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("+0b11111111111111111111111111111111"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("-0b11111111111111111111111111111111"), 0x00000001);
		assertEquals(NumberParser.decodeInt("4294967295"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("+4294967295"), 0xffffffff);
		assertEquals(NumberParser.decodeInt("-4294967295"), 0x00000001);
		assertEquals(NumberParser.decodeInt("1"), 1);
		assertEquals(NumberParser.decodeInt("+1"), 1);
		assertEquals(NumberParser.decodeInt("-1"), -1);
		assertEquals(NumberParser.decodeInt("0"), 0);
		assertEquals(NumberParser.decodeInt("+0"), 0);
		assertEquals(NumberParser.decodeInt("-0"), 0);
	}

	@Test
	public void testDecodeInvalidInt() {
		assertThrown(() -> NumberParser.decodeInt(""));
		assertThrown(() -> NumberParser.decodeInt("-+1"));
		assertThrown(() -> NumberParser.decodeInt("0x"));
		assertThrown(() -> NumberParser.decodeInt("0x+1"));
		assertThrown(() -> NumberParser.decodeInt("0-1"));
		assertThrown(() -> NumberParser.decodeInt("08"));
		assertThrown(() -> NumberParser.decodeInt("0b"));
		assertThrown(() -> NumberParser.decodeInt("0x100000000"));
		assertThrown(() -> NumberParser.decodeInt("-0x100000000"));
	}

	@Test
	public void testDecodeLong() {
		assertEquals(NumberParser.decodeLong("0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("+0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("-0xffffffffffffffff"), 0x0000000000000001L);
		assertEquals(NumberParser.decodeLong("0xfffffffffffffffe"), 0xfffffffffffffffeL);
		assertEquals(NumberParser.decodeLong("+0xfffffffffffffffe"), 0xfffffffffffffffeL);
		assertEquals(NumberParser.decodeLong("-0xfffffffffffffffe"), 0x0000000000000002L);
		assertEquals(NumberParser.decodeLong("0x8000000000000001"), 0x8000000000000001L);
		assertEquals(NumberParser.decodeLong("+0x8000000000000001"), 0x8000000000000001L);
		assertEquals(NumberParser.decodeLong("-0x8000000000000001"), 0x7fffffffffffffffL);
		assertEquals(NumberParser.decodeLong("0x8000000000000000"), 0x8000000000000000L);
		assertEquals(NumberParser.decodeLong("+0x8000000000000000"), 0x8000000000000000L);
		assertEquals(NumberParser.decodeLong("-0x8000000000000000"), 0x8000000000000000L);
		assertEquals(NumberParser.decodeLong("0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(NumberParser.decodeLong("+0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(NumberParser.decodeLong("-0x7fffffffffffffff"), 0x8000000000000001L);
		assertEquals(NumberParser.decodeLong("0x7ffffffffffffffe"), 0x7ffffffffffffffeL);
		assertEquals(NumberParser.decodeLong("+0x7ffffffffffffffe"), 0x7ffffffffffffffeL);
		assertEquals(NumberParser.decodeLong("-0x7ffffffffffffffe"), 0x8000000000000002L);
		assertEquals(NumberParser.decodeLong("0x000000000000000001"), 0x0000000000000001L);
		assertEquals(NumberParser.decodeLong("+0x000000000000000001"), 0x0000000000000001L);
		assertEquals(NumberParser.decodeLong("-0x000000000000000001"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("+01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("-01777777777777777777777"), 0x0000000000000001L);
		assertEquals(NumberParser.decodeLong( //
			"0b1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong( //
			"+0b1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong( //
			"-0b1111111111111111111111111111111111111111111111111111111111111111"),
			0x0000000000000001L);
		assertEquals(NumberParser.decodeLong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeLong("-18446744073709551615"), 0x0000000000000001L);
		assertEquals(NumberParser.decodeLong("1"), 1L);
		assertEquals(NumberParser.decodeLong("+1"), 1L);
		assertEquals(NumberParser.decodeLong("-1"), -1L);
		assertEquals(NumberParser.decodeLong("0"), 0L);
		assertEquals(NumberParser.decodeLong("+0"), 0L);
		assertEquals(NumberParser.decodeLong("-0"), 0L);
	}

	@Test
	public void testDecodeInvalidLong() {
		assertThrown(() -> NumberParser.decodeLong(""));
		assertThrown(() -> NumberParser.decodeLong("-+1"));
		assertThrown(() -> NumberParser.decodeLong("0x"));
		assertThrown(() -> NumberParser.decodeLong("0x+1"));
		assertThrown(() -> NumberParser.decodeLong("0-1"));
		assertThrown(() -> NumberParser.decodeLong("08"));
		assertThrown(() -> NumberParser.decodeLong("0b"));
		assertThrown(() -> NumberParser.decodeLong("0x10000000000000000"));
		assertThrown(() -> NumberParser.decodeLong("-0x10000000000000000"));
	}

	@Test
	public void testDecodeUbyte() {
		assertByte(NumberParser.decodeUbyte("0xff"), 0xff);
		assertByte(NumberParser.decodeUbyte("+0xff"), 0xff);
		assertByte(NumberParser.decodeUbyte("0x81"), 0x81);
		assertByte(NumberParser.decodeUbyte("+0x81"), 0x81);
		assertByte(NumberParser.decodeUbyte("0x80"), 0x80);
		assertByte(NumberParser.decodeUbyte("+0x80"), 0x80);
		assertByte(NumberParser.decodeUbyte("0x7f"), 0x7f);
		assertByte(NumberParser.decodeUbyte("+0x7f"), 0x7f);
		assertByte(NumberParser.decodeUbyte("0377"), 0xff);
		assertByte(NumberParser.decodeUbyte("+0377"), 0xff);
		assertByte(NumberParser.decodeUbyte("255"), 0xff);
		assertByte(NumberParser.decodeUbyte("+255"), 0xff);
		assertByte(NumberParser.decodeUbyte("1"), 1);
		assertByte(NumberParser.decodeUbyte("+1"), 1);
		assertByte(NumberParser.decodeUbyte("0"), 0);
		assertByte(NumberParser.decodeUbyte("+0"), 0);
	}

	@Test
	public void testDecodeInvalidUbyte() {
		assertThrown(() -> NumberParser.decodeUbyte(""));
		assertThrown(() -> NumberParser.decodeUbyte("-1"));
		assertThrown(() -> NumberParser.decodeUbyte("++1"));
		assertThrown(() -> NumberParser.decodeUbyte("0x"));
		assertThrown(() -> NumberParser.decodeUbyte("0x+1"));
		assertThrown(() -> NumberParser.decodeUbyte("0-1"));
		assertThrown(() -> NumberParser.decodeUbyte("08"));
		assertThrown(() -> NumberParser.decodeUbyte("0b"));
		assertThrown(() -> NumberParser.decodeUbyte("0x100"));
		assertThrown(() -> NumberParser.decodeUbyte("+0x100"));
	}

	@Test
	public void testDecodeUshort() {
		assertShort(NumberParser.decodeUshort("0xffff"), 0xffff);
		assertShort(NumberParser.decodeUshort("+0xffff"), 0xffff);
		assertShort(NumberParser.decodeUshort("0x8001"), 0x8001);
		assertShort(NumberParser.decodeUshort("+0x8001"), 0x8001);
		assertShort(NumberParser.decodeUshort("0x8000"), 0x8000);
		assertShort(NumberParser.decodeUshort("+0x8000"), 0x8000);
		assertShort(NumberParser.decodeUshort("0x7fff"), 0x7fff);
		assertShort(NumberParser.decodeUshort("+0x7fff"), 0x7fff);
		assertShort(NumberParser.decodeUshort("0177777"), 0xffff);
		assertShort(NumberParser.decodeUshort("+0177777"), 0xffff);
		assertShort(NumberParser.decodeUshort("65535"), 0xffff);
		assertShort(NumberParser.decodeUshort("+65535"), 0xffff);
		assertShort(NumberParser.decodeUshort("1"), 1);
		assertShort(NumberParser.decodeUshort("+1"), 1);
		assertShort(NumberParser.decodeUshort("0"), 0);
		assertShort(NumberParser.decodeUshort("+0"), 0);
	}

	@Test
	public void testDecodeInvalidUshort() {
		assertThrown(() -> NumberParser.decodeUshort(""));
		assertThrown(() -> NumberParser.decodeUshort("-1"));
		assertThrown(() -> NumberParser.decodeUshort("++1"));
		assertThrown(() -> NumberParser.decodeUshort("0x"));
		assertThrown(() -> NumberParser.decodeUshort("0x+1"));
		assertThrown(() -> NumberParser.decodeUshort("0-1"));
		assertThrown(() -> NumberParser.decodeUshort("08"));
		assertThrown(() -> NumberParser.decodeUshort("0b"));
		assertThrown(() -> NumberParser.decodeUshort("0x10000"));
		assertThrown(() -> NumberParser.decodeUshort("+0x10000"));
	}

	@Test
	public void testDecodeUint() {
		assertEquals(NumberParser.decodeUint("0xffffffff"), 0xffffffff);
		assertEquals(NumberParser.decodeUint("+0xffffffff"), 0xffffffff);
		assertEquals(NumberParser.decodeUint("0x80000001"), 0x80000001);
		assertEquals(NumberParser.decodeUint("+0x80000001"), 0x80000001);
		assertEquals(NumberParser.decodeUint("0x80000000"), 0x80000000);
		assertEquals(NumberParser.decodeUint("+0x80000000"), 0x80000000);
		assertEquals(NumberParser.decodeUint("0x7fffffff"), 0x7fffffff);
		assertEquals(NumberParser.decodeUint("+0x7fffffff"), 0x7fffffff);
		assertEquals(NumberParser.decodeUint("037777777777"), 0xffffffff);
		assertEquals(NumberParser.decodeUint("+037777777777"), 0xffffffff);
		assertEquals(NumberParser.decodeUint("4294967295"), 0xffffffff);
		assertEquals(NumberParser.decodeUint("+4294967295"), 0xffffffff);
		assertEquals(NumberParser.decodeUint("1"), 1);
		assertEquals(NumberParser.decodeUint("+1"), 1);
		assertEquals(NumberParser.decodeUint("0"), 0);
		assertEquals(NumberParser.decodeUint("+0"), 0);
	}

	@Test
	public void testDecodeInvalidUint() {
		assertThrown(() -> NumberParser.decodeUint(""));
		assertThrown(() -> NumberParser.decodeUint("-1"));
		assertThrown(() -> NumberParser.decodeUint("++1"));
		assertThrown(() -> NumberParser.decodeUint("0x"));
		assertThrown(() -> NumberParser.decodeUint("0x+1"));
		assertThrown(() -> NumberParser.decodeUint("0-1"));
		assertThrown(() -> NumberParser.decodeUint("08"));
		assertThrown(() -> NumberParser.decodeUint("0b"));
		assertThrown(() -> NumberParser.decodeUint("0x100000000"));
		assertThrown(() -> NumberParser.decodeUint("+0x100000000"));
	}

	@Test
	public void testDecodeUlong() {
		assertEquals(NumberParser.decodeUlong("0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("+0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("0x8000000000000001"), 0x8000000000000001L);
		assertEquals(NumberParser.decodeUlong("+0x8000000000000001"), 0x8000000000000001L);
		assertEquals(NumberParser.decodeUlong("0x8000000000000000"), 0x8000000000000000L);
		assertEquals(NumberParser.decodeUlong("+0x8000000000000000"), 0x8000000000000000L);
		assertEquals(NumberParser.decodeUlong("0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("+0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("+01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(NumberParser.decodeUlong("1"), 1L);
		assertEquals(NumberParser.decodeUlong("+1"), 1L);
		assertEquals(NumberParser.decodeUlong("0"), 0L);
		assertEquals(NumberParser.decodeUlong("+0"), 0L);
	}

	@Test
	public void testDecodeInvalidUlong() {
		assertThrown(() -> NumberParser.decodeUlong(""));
		assertThrown(() -> NumberParser.decodeUlong("-1"));
		assertThrown(() -> NumberParser.decodeUlong("++1"));
		assertThrown(() -> NumberParser.decodeUlong("0x"));
		assertThrown(() -> NumberParser.decodeUlong("0x+1"));
		assertThrown(() -> NumberParser.decodeUlong("0-1"));
		assertThrown(() -> NumberParser.decodeUlong("08"));
		assertThrown(() -> NumberParser.decodeUlong("0b"));
		assertThrown(() -> NumberParser.decodeUlong("0x10000000000000000"));
		assertThrown(() -> NumberParser.decodeUlong("+0x10000000000000000"));
	}

}
