package ceri.common.text;

import static ceri.common.test.AssertUtil.assertByte;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertShort;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;
import ceri.common.function.Excepts;

public class NumbersTest {

	@Test
	public void testDecodeByte() {
		assertByte(Numbers.Decode.toByte("0xff"), 0xff);
		assertByte(Numbers.Decode.toByte("+0xff"), 0xff);
		assertByte(Numbers.Decode.toByte("-0xff"), 0x01);
		assertByte(Numbers.Decode.toByte("0xfe"), 0xfe);
		assertByte(Numbers.Decode.toByte("+0xfe"), 0xfe);
		assertByte(Numbers.Decode.toByte("-0xfe"), 0x02);
		assertByte(Numbers.Decode.toByte("0x81"), 0x81);
		assertByte(Numbers.Decode.toByte("+0x81"), 0x81);
		assertByte(Numbers.Decode.toByte("-0x81"), 0x7f);
		assertByte(Numbers.Decode.toByte("0x80"), 0x80);
		assertByte(Numbers.Decode.toByte("+0x80"), 0x80);
		assertByte(Numbers.Decode.toByte("-0x80"), 0x80);
		assertByte(Numbers.Decode.toByte("0x7f"), 0x7f);
		assertByte(Numbers.Decode.toByte("+0x7f"), 0x7f);
		assertByte(Numbers.Decode.toByte("-0x7f"), 0x81);
		assertByte(Numbers.Decode.toByte("0x7e"), 0x7e);
		assertByte(Numbers.Decode.toByte("+0x7e"), 0x7e);
		assertByte(Numbers.Decode.toByte("-0x7e"), 0x82);
		assertByte(Numbers.Decode.toByte("0x0001"), 0x01);
		assertByte(Numbers.Decode.toByte("+0x0001"), 0x01);
		assertByte(Numbers.Decode.toByte("-0x0001"), 0xff);
		assertByte(Numbers.Decode.toByte("0377"), 0xff);
		assertByte(Numbers.Decode.toByte("+0377"), 0xff);
		assertByte(Numbers.Decode.toByte("-0377"), 0x01);
		assertByte(Numbers.Decode.toByte("0b11111111"), 0xff);
		assertByte(Numbers.Decode.toByte("+0b11111111"), 0xff);
		assertByte(Numbers.Decode.toByte("-0b11111111"), 0x01);
		assertByte(Numbers.Decode.toByte("255"), 0xff);
		assertByte(Numbers.Decode.toByte("+255"), 0xff);
		assertByte(Numbers.Decode.toByte("-255"), 0x01);
		assertByte(Numbers.Decode.toByte("1"), 1);
		assertByte(Numbers.Decode.toByte("+1"), 1);
		assertByte(Numbers.Decode.toByte("-1"), -1);
		assertByte(Numbers.Decode.toByte("0"), 0);
		assertByte(Numbers.Decode.toByte("+0"), 0);
		assertByte(Numbers.Decode.toByte("-0"), 0);
	}

	@Test
	public void testDecodeInvalidByte() {
		assertNfe(() -> Numbers.Decode.toByte(""));
		assertNfe(() -> Numbers.Decode.toByte("-+1"));
		assertNfe(() -> Numbers.Decode.toByte("0x"));
		assertNfe(() -> Numbers.Decode.toByte("0x+1"));
		assertNfe(() -> Numbers.Decode.toByte("0-1"));
		assertNfe(() -> Numbers.Decode.toByte("08"));
		assertNfe(() -> Numbers.Decode.toByte("0b"));
		assertNfe(() -> Numbers.Decode.toByte("0x100"));
		assertNfe(() -> Numbers.Decode.toByte("-0x100"));
		assertNfe(() -> Numbers.Decode.toByte("-0x80000000"));
	}

	@Test
	public void testDecodeUbyte() {
		assertByte(Numbers.Decode.toUbyte("0xff"), 0xff);
		assertByte(Numbers.Decode.toUbyte("+0xff"), 0xff);
		assertByte(Numbers.Decode.toUbyte("0x81"), 0x81);
		assertByte(Numbers.Decode.toUbyte("+0x81"), 0x81);
		assertByte(Numbers.Decode.toUbyte("0x80"), 0x80);
		assertByte(Numbers.Decode.toUbyte("+0x80"), 0x80);
		assertByte(Numbers.Decode.toUbyte("0x7f"), 0x7f);
		assertByte(Numbers.Decode.toUbyte("+0x7f"), 0x7f);
		assertByte(Numbers.Decode.toUbyte("0377"), 0xff);
		assertByte(Numbers.Decode.toUbyte("+0377"), 0xff);
		assertByte(Numbers.Decode.toUbyte("255"), 0xff);
		assertByte(Numbers.Decode.toUbyte("+255"), 0xff);
		assertByte(Numbers.Decode.toUbyte("1"), 1);
		assertByte(Numbers.Decode.toUbyte("+1"), 1);
		assertByte(Numbers.Decode.toUbyte("0"), 0);
		assertByte(Numbers.Decode.toUbyte("+0"), 0);
	}

	@Test
	public void testDecodeInvalidUbyte() {
		assertNfe(() -> Numbers.Decode.toUbyte(""));
		assertNfe(() -> Numbers.Decode.toUbyte("-1"));
		assertNfe(() -> Numbers.Decode.toUbyte("++1"));
		assertNfe(() -> Numbers.Decode.toUbyte("0x"));
		assertNfe(() -> Numbers.Decode.toUbyte("0x+1"));
		assertNfe(() -> Numbers.Decode.toUbyte("0-1"));
		assertNfe(() -> Numbers.Decode.toUbyte("08"));
		assertNfe(() -> Numbers.Decode.toUbyte("0b"));
		assertNfe(() -> Numbers.Decode.toUbyte("0x100"));
		assertNfe(() -> Numbers.Decode.toUbyte("+0x100"));
	}

	@Test
	public void testDecodeShort() {
		assertShort(Numbers.Decode.toShort("0xffff"), 0xffff);
		assertShort(Numbers.Decode.toShort("+0xffff"), 0xffff);
		assertShort(Numbers.Decode.toShort("-0xffff"), 0x0001);
		assertShort(Numbers.Decode.toShort("0xfffe"), 0xfffe);
		assertShort(Numbers.Decode.toShort("+0xfffe"), 0xfffe);
		assertShort(Numbers.Decode.toShort("-0xfffe"), 0x0002);
		assertShort(Numbers.Decode.toShort("0x8001"), 0x8001);
		assertShort(Numbers.Decode.toShort("+0x8001"), 0x8001);
		assertShort(Numbers.Decode.toShort("-0x8001"), 0x7fff);
		assertShort(Numbers.Decode.toShort("0x8000"), 0x8000);
		assertShort(Numbers.Decode.toShort("+0x8000"), 0x8000);
		assertShort(Numbers.Decode.toShort("-0x8000"), 0x8000);
		assertShort(Numbers.Decode.toShort("0x7fff"), 0x7fff);
		assertShort(Numbers.Decode.toShort("+0x7fff"), 0x7fff);
		assertShort(Numbers.Decode.toShort("-0x7fff"), 0x8001);
		assertShort(Numbers.Decode.toShort("0x7ffe"), 0x7ffe);
		assertShort(Numbers.Decode.toShort("+0x7ffe"), 0x7ffe);
		assertShort(Numbers.Decode.toShort("-0x7ffe"), 0x8002);
		assertShort(Numbers.Decode.toShort("0x000001"), 0x0001);
		assertShort(Numbers.Decode.toShort("+0x000001"), 0x0001);
		assertShort(Numbers.Decode.toShort("-0x000001"), 0xffff);
		assertShort(Numbers.Decode.toShort("0177777"), 0xffff);
		assertShort(Numbers.Decode.toShort("+0177777"), 0xffff);
		assertShort(Numbers.Decode.toShort("-0177777"), 0x0001);
		assertShort(Numbers.Decode.toShort("0b1111111111111111"), 0xffff);
		assertShort(Numbers.Decode.toShort("+0b1111111111111111"), 0xffff);
		assertShort(Numbers.Decode.toShort("-0b1111111111111111"), 0x0001);
		assertShort(Numbers.Decode.toShort("65535"), 0xffff);
		assertShort(Numbers.Decode.toShort("+65535"), 0xffff);
		assertShort(Numbers.Decode.toShort("-65535"), 0x0001);
		assertShort(Numbers.Decode.toShort("1"), 1);
		assertShort(Numbers.Decode.toShort("+1"), 1);
		assertShort(Numbers.Decode.toShort("-1"), -1);
		assertShort(Numbers.Decode.toShort("0"), 0);
		assertShort(Numbers.Decode.toShort("+0"), 0);
		assertShort(Numbers.Decode.toShort("-0"), 0);
	}

	@Test
	public void testDecodeInvalidShort() {
		assertNfe(() -> Numbers.Decode.toShort(""));
		assertNfe(() -> Numbers.Decode.toShort("-+1"));
		assertNfe(() -> Numbers.Decode.toShort("0x"));
		assertNfe(() -> Numbers.Decode.toShort("0x+1"));
		assertNfe(() -> Numbers.Decode.toShort("0-1"));
		assertNfe(() -> Numbers.Decode.toShort("08"));
		assertNfe(() -> Numbers.Decode.toShort("0b"));
		assertNfe(() -> Numbers.Decode.toShort("0x10000"));
		assertNfe(() -> Numbers.Decode.toShort("-0x10000"));
		assertNfe(() -> Numbers.Decode.toShort("-0x80000000"));
	}

	@Test
	public void testDecodeUshort() {
		assertShort(Numbers.Decode.toUshort("0xffff"), 0xffff);
		assertShort(Numbers.Decode.toUshort("+0xffff"), 0xffff);
		assertShort(Numbers.Decode.toUshort("0x8001"), 0x8001);
		assertShort(Numbers.Decode.toUshort("+0x8001"), 0x8001);
		assertShort(Numbers.Decode.toUshort("0x8000"), 0x8000);
		assertShort(Numbers.Decode.toUshort("+0x8000"), 0x8000);
		assertShort(Numbers.Decode.toUshort("0x7fff"), 0x7fff);
		assertShort(Numbers.Decode.toUshort("+0x7fff"), 0x7fff);
		assertShort(Numbers.Decode.toUshort("0177777"), 0xffff);
		assertShort(Numbers.Decode.toUshort("+0177777"), 0xffff);
		assertShort(Numbers.Decode.toUshort("65535"), 0xffff);
		assertShort(Numbers.Decode.toUshort("+65535"), 0xffff);
		assertShort(Numbers.Decode.toUshort("1"), 1);
		assertShort(Numbers.Decode.toUshort("+1"), 1);
		assertShort(Numbers.Decode.toUshort("0"), 0);
		assertShort(Numbers.Decode.toUshort("+0"), 0);
	}

	@Test
	public void testDecodeInvalidUshort() {
		assertNfe(() -> Numbers.Decode.toUshort(""));
		assertNfe(() -> Numbers.Decode.toUshort("-1"));
		assertNfe(() -> Numbers.Decode.toUshort("++1"));
		assertNfe(() -> Numbers.Decode.toUshort("0x"));
		assertNfe(() -> Numbers.Decode.toUshort("0x+1"));
		assertNfe(() -> Numbers.Decode.toUshort("0-1"));
		assertNfe(() -> Numbers.Decode.toUshort("08"));
		assertNfe(() -> Numbers.Decode.toUshort("0b"));
		assertNfe(() -> Numbers.Decode.toUshort("0x10000"));
		assertNfe(() -> Numbers.Decode.toUshort("+0x10000"));
	}

	@Test
	public void testDecodeInt() {
		assertEquals(Numbers.Decode.toInt("0xffffffff"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("+0xffffffff"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("-0xffffffff"), 0x00000001);
		assertEquals(Numbers.Decode.toInt("0xfffffffe"), 0xfffffffe);
		assertEquals(Numbers.Decode.toInt("+0xfffffffe"), 0xfffffffe);
		assertEquals(Numbers.Decode.toInt("-0xfffffffe"), 0x00000002);
		assertEquals(Numbers.Decode.toInt("0x80000001"), 0x80000001);
		assertEquals(Numbers.Decode.toInt("+0x80000001"), 0x80000001);
		assertEquals(Numbers.Decode.toInt("-0x80000001"), 0x7fffffff);
		assertEquals(Numbers.Decode.toInt("0x80000000"), 0x80000000);
		assertEquals(Numbers.Decode.toInt("+0x80000000"), 0x80000000);
		assertEquals(Numbers.Decode.toInt("-0x80000000"), 0x80000000);
		assertEquals(Numbers.Decode.toInt("0x7fffffff"), 0x7fffffff);
		assertEquals(Numbers.Decode.toInt("+0x7fffffff"), 0x7fffffff);
		assertEquals(Numbers.Decode.toInt("-0x7fffffff"), 0x80000001);
		assertEquals(Numbers.Decode.toInt("0x7ffffffe"), 0x7ffffffe);
		assertEquals(Numbers.Decode.toInt("+0x7ffffffe"), 0x7ffffffe);
		assertEquals(Numbers.Decode.toInt("-0x7ffffffe"), 0x80000002);
		assertEquals(Numbers.Decode.toInt("0x0000000001"), 0x00000001);
		assertEquals(Numbers.Decode.toInt("+0x0000000001"), 0x00000001);
		assertEquals(Numbers.Decode.toInt("-0x0000000001"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("037777777777"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("+037777777777"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("-037777777777"), 0x00000001);
		assertEquals(Numbers.Decode.toInt("0b11111111111111111111111111111111"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("+0b11111111111111111111111111111111"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("-0b11111111111111111111111111111111"), 0x00000001);
		assertEquals(Numbers.Decode.toInt("4294967295"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("+4294967295"), 0xffffffff);
		assertEquals(Numbers.Decode.toInt("-4294967295"), 0x00000001);
		assertEquals(Numbers.Decode.toInt("1"), 1);
		assertEquals(Numbers.Decode.toInt("+1"), 1);
		assertEquals(Numbers.Decode.toInt("-1"), -1);
		assertEquals(Numbers.Decode.toInt("0"), 0);
		assertEquals(Numbers.Decode.toInt("+0"), 0);
		assertEquals(Numbers.Decode.toInt("-0"), 0);
	}

	@Test
	public void testDecodeInvalidInt() {
		assertNfe(() -> Numbers.Decode.toInt(""));
		assertNfe(() -> Numbers.Decode.toInt("-+1"));
		assertNfe(() -> Numbers.Decode.toInt("0x"));
		assertNfe(() -> Numbers.Decode.toInt("0x+1"));
		assertNfe(() -> Numbers.Decode.toInt("0-1"));
		assertNfe(() -> Numbers.Decode.toInt("08"));
		assertNfe(() -> Numbers.Decode.toInt("0b"));
		assertNfe(() -> Numbers.Decode.toInt("0x100000000"));
		assertNfe(() -> Numbers.Decode.toInt("-0x100000000"));
	}

	@Test
	public void testDecodeUint() {
		assertEquals(Numbers.Decode.toUint("0xffffffff"), 0xffffffff);
		assertEquals(Numbers.Decode.toUint("+0xffffffff"), 0xffffffff);
		assertEquals(Numbers.Decode.toUint("0x80000001"), 0x80000001);
		assertEquals(Numbers.Decode.toUint("+0x80000001"), 0x80000001);
		assertEquals(Numbers.Decode.toUint("0x80000000"), 0x80000000);
		assertEquals(Numbers.Decode.toUint("+0x80000000"), 0x80000000);
		assertEquals(Numbers.Decode.toUint("0x7fffffff"), 0x7fffffff);
		assertEquals(Numbers.Decode.toUint("+0x7fffffff"), 0x7fffffff);
		assertEquals(Numbers.Decode.toUint("037777777777"), 0xffffffff);
		assertEquals(Numbers.Decode.toUint("+037777777777"), 0xffffffff);
		assertEquals(Numbers.Decode.toUint("4294967295"), 0xffffffff);
		assertEquals(Numbers.Decode.toUint("+4294967295"), 0xffffffff);
		assertEquals(Numbers.Decode.toUint("1"), 1);
		assertEquals(Numbers.Decode.toUint("+1"), 1);
		assertEquals(Numbers.Decode.toUint("0"), 0);
		assertEquals(Numbers.Decode.toUint("+0"), 0);
	}

	@Test
	public void testDecodeInvalidUint() {
		assertNfe(() -> Numbers.Decode.toUint(""));
		assertNfe(() -> Numbers.Decode.toUint("-1"));
		assertNfe(() -> Numbers.Decode.toUint("++1"));
		assertNfe(() -> Numbers.Decode.toUint("0x"));
		assertNfe(() -> Numbers.Decode.toUint("0x+1"));
		assertNfe(() -> Numbers.Decode.toUint("0-1"));
		assertNfe(() -> Numbers.Decode.toUint("08"));
		assertNfe(() -> Numbers.Decode.toUint("0b"));
		assertNfe(() -> Numbers.Decode.toUint("0x100000000"));
		assertNfe(() -> Numbers.Decode.toUint("+0x100000000"));
	}

	@Test
	public void testDecodeLong() {
		assertEquals(Numbers.Decode.toLong("0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("+0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("-0xffffffffffffffff"), 0x0000000000000001L);
		assertEquals(Numbers.Decode.toLong("0xfffffffffffffffe"), 0xfffffffffffffffeL);
		assertEquals(Numbers.Decode.toLong("+0xfffffffffffffffe"), 0xfffffffffffffffeL);
		assertEquals(Numbers.Decode.toLong("-0xfffffffffffffffe"), 0x0000000000000002L);
		assertEquals(Numbers.Decode.toLong("0x8000000000000001"), 0x8000000000000001L);
		assertEquals(Numbers.Decode.toLong("+0x8000000000000001"), 0x8000000000000001L);
		assertEquals(Numbers.Decode.toLong("-0x8000000000000001"), 0x7fffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("0x8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Decode.toLong("+0x8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Decode.toLong("-0x8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Decode.toLong("0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("+0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("-0x7fffffffffffffff"), 0x8000000000000001L);
		assertEquals(Numbers.Decode.toLong("0x7ffffffffffffffe"), 0x7ffffffffffffffeL);
		assertEquals(Numbers.Decode.toLong("+0x7ffffffffffffffe"), 0x7ffffffffffffffeL);
		assertEquals(Numbers.Decode.toLong("-0x7ffffffffffffffe"), 0x8000000000000002L);
		assertEquals(Numbers.Decode.toLong("0x000000000000000001"), 0x0000000000000001L);
		assertEquals(Numbers.Decode.toLong("+0x000000000000000001"), 0x0000000000000001L);
		assertEquals(Numbers.Decode.toLong("-0x000000000000000001"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("+01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("-01777777777777777777777"), 0x0000000000000001L);
		assertEquals(Numbers.Decode.toLong( //
			"0b1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong( //
			"+0b1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong( //
			"-0b1111111111111111111111111111111111111111111111111111111111111111"),
			0x0000000000000001L);
		assertEquals(Numbers.Decode.toLong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toLong("-18446744073709551615"), 0x0000000000000001L);
		assertEquals(Numbers.Decode.toLong("1"), 1L);
		assertEquals(Numbers.Decode.toLong("+1"), 1L);
		assertEquals(Numbers.Decode.toLong("-1"), -1L);
		assertEquals(Numbers.Decode.toLong("0"), 0L);
		assertEquals(Numbers.Decode.toLong("+0"), 0L);
		assertEquals(Numbers.Decode.toLong("-0"), 0L);
	}

	@Test
	public void testDecodeInvalidLong() {
		assertNfe(() -> Numbers.Decode.toLong(""));
		assertNfe(() -> Numbers.Decode.toLong("-+1"));
		assertNfe(() -> Numbers.Decode.toLong("0x"));
		assertNfe(() -> Numbers.Decode.toLong("0x+1"));
		assertNfe(() -> Numbers.Decode.toLong("0-1"));
		assertNfe(() -> Numbers.Decode.toLong("08"));
		assertNfe(() -> Numbers.Decode.toLong("0b"));
		assertNfe(() -> Numbers.Decode.toLong("0x10000000000000000"));
		assertNfe(() -> Numbers.Decode.toLong("-0x10000000000000000"));
	}

	@Test
	public void testDecodeUlong() {
		assertEquals(Numbers.Decode.toUlong("0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("+0xffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("0x8000000000000001"), 0x8000000000000001L);
		assertEquals(Numbers.Decode.toUlong("+0x8000000000000001"), 0x8000000000000001L);
		assertEquals(Numbers.Decode.toUlong("0x8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Decode.toUlong("+0x8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Decode.toUlong("0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("+0x7fffffffffffffff"), 0x7fffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("+01777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Decode.toUlong("1"), 1L);
		assertEquals(Numbers.Decode.toUlong("+1"), 1L);
		assertEquals(Numbers.Decode.toUlong("0"), 0L);
		assertEquals(Numbers.Decode.toUlong("+0"), 0L);
	}

	@Test
	public void testDecodeInvalidUlong() {
		assertNfe(() -> Numbers.Decode.toUlong(""));
		assertNfe(() -> Numbers.Decode.toUlong("-1"));
		assertNfe(() -> Numbers.Decode.toUlong("++1"));
		assertNfe(() -> Numbers.Decode.toUlong("0x"));
		assertNfe(() -> Numbers.Decode.toUlong("0x+1"));
		assertNfe(() -> Numbers.Decode.toUlong("0-1"));
		assertNfe(() -> Numbers.Decode.toUlong("08"));
		assertNfe(() -> Numbers.Decode.toUlong("0b"));
		assertNfe(() -> Numbers.Decode.toUlong("0x10000000000000000"));
		assertNfe(() -> Numbers.Decode.toUlong("+0x10000000000000000"));
	}

	@Test
	public void testParseBool() {
		assertNfe(() -> Numbers.Parse.toBool(null));
		assertNfe(() -> Numbers.Parse.toBool(""));
		assertNfe(() -> Numbers.Parse.toBool("-1"));
		assertEquals(Numbers.Parse.toBool("TRUE"), true);
		assertEquals(Numbers.Parse.toBool("1"), true);
		assertEquals(Numbers.Parse.toBool("False"), false);
		assertEquals(Numbers.Parse.toBool("0"), false);
	}

	@Test
	public void testParseByte() {
		assertByte(Numbers.Parse.toByte(16, "ff"), 0xff);
		assertByte(Numbers.Parse.toByte(16, "+ff"), 0xff);
		assertByte(Numbers.Parse.toByte(16, "-ff"), 0x01);
		assertByte(Numbers.Parse.toByte(16, "80"), 0x80);
		assertByte(Numbers.Parse.toByte(16, "+80"), 0x80);
		assertByte(Numbers.Parse.toByte(16, "-80"), 0x80);
		assertByte(Numbers.Parse.toByte(8, "377"), 0xff);
		assertByte(Numbers.Parse.toByte(8, "+377"), 0xff);
		assertByte(Numbers.Parse.toByte(8, "-377"), 0x01);
		assertByte(Numbers.Parse.toByte(2, "11111111"), 0xff);
		assertByte(Numbers.Parse.toByte(2, "+11111111"), 0xff);
		assertByte(Numbers.Parse.toByte(2, "-11111111"), 0x01);
		assertByte(Numbers.Parse.toByte("255"), 0xff);
		assertByte(Numbers.Parse.toByte("+255"), 0xff);
		assertByte(Numbers.Parse.toByte("-255"), 0x01);
		assertByte(Numbers.Parse.toByte("1"), 1);
		assertByte(Numbers.Parse.toByte("+1"), 1);
		assertByte(Numbers.Parse.toByte("-1"), -1);
		assertByte(Numbers.Parse.toByte("0"), 0);
		assertByte(Numbers.Parse.toByte("+0"), 0);
		assertByte(Numbers.Parse.toByte("-0"), 0);
	}

	@Test
	public void testParseInvalidByte() {
		assertNfe(() -> Numbers.Parse.toByte(""));
		assertNfe(() -> Numbers.Parse.toByte("-+1"));
		assertNfe(() -> Numbers.Parse.toByte(16, "100"));
		assertNfe(() -> Numbers.Parse.toByte(16, "-100"));
		assertIllegalArg(() -> Numbers.Parse.toByte(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toByte(37, "1"));
	}

	@Test
	public void testParseUbyte() {
		assertByte(Numbers.Parse.toUbyte(16, "ff"), 0xff);
		assertByte(Numbers.Parse.toUbyte(16, "+ff"), 0xff);
		assertByte(Numbers.Parse.toUbyte(16, "80"), 0x80);
		assertByte(Numbers.Parse.toUbyte(16, "+80"), 0x80);
		assertByte(Numbers.Parse.toUbyte(8, "377"), 0xff);
		assertByte(Numbers.Parse.toUbyte(8, "+377"), 0xff);
		assertByte(Numbers.Parse.toUbyte(2, "11111111"), 0xff);
		assertByte(Numbers.Parse.toUbyte(2, "+11111111"), 0xff);
		assertByte(Numbers.Parse.toUbyte("255"), 0xff);
		assertByte(Numbers.Parse.toUbyte("+255"), 0xff);
		assertByte(Numbers.Parse.toUbyte("1"), 1);
		assertByte(Numbers.Parse.toUbyte("+1"), 1);
		assertByte(Numbers.Parse.toUbyte("0"), 0);
		assertByte(Numbers.Parse.toUbyte("+0"), 0);
	}

	@Test
	public void testParseInvalidUbyte() {
		assertNfe(() -> Numbers.Parse.toUbyte(""));
		assertNfe(() -> Numbers.Parse.toUbyte("-1"));
		assertNfe(() -> Numbers.Parse.toUbyte("++1"));
		assertNfe(() -> Numbers.Parse.toUbyte(16, "100"));
		assertIllegalArg(() -> Numbers.Parse.toUbyte(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toUbyte(37, "1"));
	}

	@Test
	public void testParseShort() {
		assertShort(Numbers.Parse.toShort(16, "ffff"), 0xffff);
		assertShort(Numbers.Parse.toShort(16, "+ffff"), 0xffff);
		assertShort(Numbers.Parse.toShort(16, "-ffff"), 0x0001);
		assertShort(Numbers.Parse.toShort(16, "8000"), 0x8000);
		assertShort(Numbers.Parse.toShort(16, "+8000"), 0x8000);
		assertShort(Numbers.Parse.toShort(16, "-8000"), 0x8000);
		assertShort(Numbers.Parse.toShort(8, "177777"), 0xffff);
		assertShort(Numbers.Parse.toShort(8, "+177777"), 0xffff);
		assertShort(Numbers.Parse.toShort(8, "-177777"), 0x0001);
		assertShort(Numbers.Parse.toShort(2, "1111111111111111"), 0xffff);
		assertShort(Numbers.Parse.toShort(2, "+1111111111111111"), 0xffff);
		assertShort(Numbers.Parse.toShort(2, "-1111111111111111"), 0x0001);
		assertShort(Numbers.Parse.toShort("65535"), 0xffff);
		assertShort(Numbers.Parse.toShort("+65535"), 0xffff);
		assertShort(Numbers.Parse.toShort("-65535"), 0x0001);
		assertShort(Numbers.Parse.toShort("1"), 1);
		assertShort(Numbers.Parse.toShort("+1"), 1);
		assertShort(Numbers.Parse.toShort("-1"), -1);
		assertShort(Numbers.Parse.toShort("0"), 0);
		assertShort(Numbers.Parse.toShort("+0"), 0);
		assertShort(Numbers.Parse.toShort("-0"), 0);
	}

	@Test
	public void testParseInvalidShort() {
		assertNfe(() -> Numbers.Parse.toShort(""));
		assertNfe(() -> Numbers.Parse.toShort("-+1"));
		assertNfe(() -> Numbers.Parse.toShort(16, "10000"));
		assertNfe(() -> Numbers.Parse.toShort(16, "-10000"));
		assertIllegalArg(() -> Numbers.Parse.toShort(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toShort(37, "1"));
	}

	@Test
	public void testParseUshort() {
		assertShort(Numbers.Parse.toUshort(16, "ffff"), 0xffff);
		assertShort(Numbers.Parse.toUshort(16, "+ffff"), 0xffff);
		assertShort(Numbers.Parse.toUshort(16, "8000"), 0x8000);
		assertShort(Numbers.Parse.toUshort(16, "+8000"), 0x8000);
		assertShort(Numbers.Parse.toUshort(8, "177777"), 0xffff);
		assertShort(Numbers.Parse.toUshort(8, "+177777"), 0xffff);
		assertShort(Numbers.Parse.toUshort(2, "1111111111111111"), 0xffff);
		assertShort(Numbers.Parse.toUshort(2, "+1111111111111111"), 0xffff);
		assertShort(Numbers.Parse.toUshort("65535"), 0xffff);
		assertShort(Numbers.Parse.toUshort("+65535"), 0xffff);
		assertShort(Numbers.Parse.toUshort("1"), 1);
		assertShort(Numbers.Parse.toUshort("+1"), 1);
		assertShort(Numbers.Parse.toUshort("0"), 0);
		assertShort(Numbers.Parse.toUshort("+0"), 0);
	}

	@Test
	public void testParseInvalidUshort() {
		assertNfe(() -> Numbers.Parse.toUshort(""));
		assertNfe(() -> Numbers.Parse.toUshort("-1"));
		assertNfe(() -> Numbers.Parse.toUshort("++1"));
		assertNfe(() -> Numbers.Parse.toUshort(16, "10000"));
		assertIllegalArg(() -> Numbers.Parse.toUshort(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toUshort(37, "1"));
	}

	@Test
	public void testParseInt() {
		assertEquals(Numbers.Parse.toInt(16, "ffffffff"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt(16, "+ffffffff"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt(16, "-ffffffff"), 0x00000001);
		assertEquals(Numbers.Parse.toInt(16, "80000000"), 0x80000000);
		assertEquals(Numbers.Parse.toInt(16, "+80000000"), 0x80000000);
		assertEquals(Numbers.Parse.toInt(16, "-80000000"), 0x80000000);
		assertEquals(Numbers.Parse.toInt(8, "37777777777"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt(8, "+37777777777"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt(8, "-37777777777"), 0x00000001);
		assertEquals(Numbers.Parse.toInt(2, "11111111111111111111111111111111"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt(2, "+11111111111111111111111111111111"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt(2, "-11111111111111111111111111111111"), 0x00000001);
		assertEquals(Numbers.Parse.toInt("4294967295"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt("+4294967295"), 0xffffffff);
		assertEquals(Numbers.Parse.toInt("-4294967295"), 0x00000001);
		assertEquals(Numbers.Parse.toInt("1"), 1);
		assertEquals(Numbers.Parse.toInt("+1"), 1);
		assertEquals(Numbers.Parse.toInt("-1"), -1);
		assertEquals(Numbers.Parse.toInt("0"), 0);
		assertEquals(Numbers.Parse.toInt("+0"), 0);
		assertEquals(Numbers.Parse.toInt("-0"), 0);
	}

	@Test
	public void testParseInvalidInt() {
		assertNfe(() -> Numbers.Parse.toInt(""));
		assertNfe(() -> Numbers.Parse.toInt("-+1"));
		assertNfe(() -> Numbers.Parse.toInt(16, "100000000"));
		assertNfe(() -> Numbers.Parse.toInt(16, "-100000000"));
		assertIllegalArg(() -> Numbers.Parse.toInt(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toInt(37, "1"));
	}

	@Test
	public void testParseUint() {
		assertEquals(Numbers.Parse.toUint(16, "ffffffff"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint(16, "+ffffffff"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint(16, "80000000"), 0x80000000);
		assertEquals(Numbers.Parse.toUint(16, "+80000000"), 0x80000000);
		assertEquals(Numbers.Parse.toUint(8, "37777777777"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint(8, "+37777777777"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint(2, "11111111111111111111111111111111"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint(2, "+11111111111111111111111111111111"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint("4294967295"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint("+4294967295"), 0xffffffff);
		assertEquals(Numbers.Parse.toUint("1"), 1);
		assertEquals(Numbers.Parse.toUint("+1"), 1);
		assertEquals(Numbers.Parse.toUint("0"), 0);
		assertEquals(Numbers.Parse.toUint("+0"), 0);
	}

	@Test
	public void testParseInvalidUint() {
		assertNfe(() -> Numbers.Parse.toUint(""));
		assertNfe(() -> Numbers.Parse.toUint("-1"));
		assertNfe(() -> Numbers.Parse.toUint("++1"));
		assertNfe(() -> Numbers.Parse.toUint(16, "100000000"));
		assertIllegalArg(() -> Numbers.Parse.toUint(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toUint(37, "1"));
	}

	@Test
	public void testParseLong() {
		assertEquals(Numbers.Parse.toLong(16, "ffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong(16, "+ffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong(16, "-ffffffffffffffff"), 0x0000000000000001L);
		assertEquals(Numbers.Parse.toLong(16, "8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Parse.toLong(16, "+8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Parse.toLong(16, "-8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Parse.toLong(8, "1777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong(8, "+1777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong(8, "-1777777777777777777777"), 0x0000000000000001L);
		assertEquals(Numbers.Parse.toLong( //
			2, "1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong( //
			2, "+1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong( //
			2, "-1111111111111111111111111111111111111111111111111111111111111111"),
			0x0000000000000001L);
		assertEquals(Numbers.Parse.toLong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toLong("-18446744073709551615"), 0x0000000000000001L);
		assertEquals(Numbers.Parse.toLong("1"), 1L);
		assertEquals(Numbers.Parse.toLong("+1"), 1L);
		assertEquals(Numbers.Parse.toLong("-1"), -1L);
		assertEquals(Numbers.Parse.toLong("0"), 0L);
		assertEquals(Numbers.Parse.toLong("+0"), 0L);
		assertEquals(Numbers.Parse.toLong("-0"), 0L);
	}

	@Test
	public void testParseInvalidLong() {
		assertNfe(() -> Numbers.Parse.toLong(""));
		assertNfe(() -> Numbers.Parse.toLong("-+1"));
		assertNfe(() -> Numbers.Parse.toLong(16, "10000000000000000"));
		assertNfe(() -> Numbers.Parse.toLong(16, "-10000000000000000"));
		assertIllegalArg(() -> Numbers.Parse.toLong(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toLong(37, "1"));
	}

	@Test
	public void testParseUlong() {
		assertEquals(Numbers.Parse.toUlong(16, "ffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong(16, "+ffffffffffffffff"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong(16, "8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Parse.toUlong(16, "+8000000000000000"), 0x8000000000000000L);
		assertEquals(Numbers.Parse.toUlong(8, "1777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong(8, "+1777777777777777777777"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong( //
			2, "1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong( //
			2, "+1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong("18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong("+18446744073709551615"), 0xffffffffffffffffL);
		assertEquals(Numbers.Parse.toUlong("1"), 1L);
		assertEquals(Numbers.Parse.toUlong("+1"), 1L);
		assertEquals(Numbers.Parse.toUlong("0"), 0L);
		assertEquals(Numbers.Parse.toUlong("+0"), 0L);
	}

	@Test
	public void testParseInvalidUlong() {
		assertNfe(() -> Numbers.Parse.toUlong(""));
		assertNfe(() -> Numbers.Parse.toUlong("-1"));
		assertNfe(() -> Numbers.Parse.toUlong("++1"));
		assertNfe(() -> Numbers.Parse.toUlong(16, "10000000000000000"));
		assertIllegalArg(() -> Numbers.Parse.toUlong(1, "1"));
		assertIllegalArg(() -> Numbers.Parse.toUlong(37, "1"));
	}

	private static void assertNfe(Excepts.Runnable<?> runnable) {
		assertThrown(NumberFormatException.class, runnable);
	}
}
