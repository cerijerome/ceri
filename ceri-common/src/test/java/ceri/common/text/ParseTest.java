package ceri.common.text;

import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;

public class ParseTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Parse.class);
	}

	@Test
	public void testFunctions() {
		Assert.equal(Parse.BOOL.apply(null), null);
		Assert.equal(Parse.BOOL.apply("1"), true);
		Assert.equal(Parse.BOOL.apply("2"), null);
		Assert.equal(Parse.BYTE.apply(null), null);
		Assert.equal(Parse.BYTE.apply("1"), (byte) 1);
		Assert.equal(Parse.BYTE.apply("0x1"), null);
		Assert.equal(Parse.UBYTE.apply(null), null);
		Assert.equal(Parse.UBYTE.apply("1"), (byte) 1);
		Assert.equal(Parse.UBYTE.apply("0x1"), null);
		Assert.equal(Parse.SHORT.apply(null), null);
		Assert.equal(Parse.SHORT.apply("1"), (short) 1);
		Assert.equal(Parse.SHORT.apply("0x1"), null);
		Assert.equal(Parse.USHORT.apply(null), null);
		Assert.equal(Parse.USHORT.apply("1"), (short) 1);
		Assert.equal(Parse.USHORT.apply("0x1"), null);
		Assert.equal(Parse.INT.apply(null), null);
		Assert.equal(Parse.INT.apply("1"), 1);
		Assert.equal(Parse.INT.apply("0x1"), null);
		Assert.equal(Parse.UINT.apply(null), null);
		Assert.equal(Parse.UINT.apply("1"), 1);
		Assert.equal(Parse.UINT.apply("0x1"), null);
		Assert.equal(Parse.LONG.apply(null), null);
		Assert.equal(Parse.LONG.apply("1"), 1L);
		Assert.equal(Parse.LONG.apply("0x1"), null);
		Assert.equal(Parse.ULONG.apply(null), null);
		Assert.equal(Parse.ULONG.apply("1"), 1L);
		Assert.equal(Parse.ULONG.apply("0x1"), null);
		Assert.equal(Parse.FLOAT.apply(null), null);
		Assert.equal(Parse.FLOAT.apply("0"), 0.0f);
		Assert.equal(Parse.FLOAT.apply("a"), null);
		Assert.equal(Parse.DOUBLE.apply(null), null);
		Assert.equal(Parse.DOUBLE.apply("0"), 0.0);
		Assert.equal(Parse.DOUBLE.apply("a"), null);
	}

	@Test
	public void testDecodeByte() {
		Assert.equals(Parse.decodeByte("0xff"), 0xff);
		Assert.equals(Parse.decodeByte("+0xff"), 0xff);
		Assert.equals(Parse.decodeByte("-0xff"), 0x01);
		Assert.equals(Parse.decodeByte("0xfe"), 0xfe);
		Assert.equals(Parse.decodeByte("+0xfe"), 0xfe);
		Assert.equals(Parse.decodeByte("-0xfe"), 0x02);
		Assert.equals(Parse.decodeByte("0x81"), 0x81);
		Assert.equals(Parse.decodeByte("+0x81"), 0x81);
		Assert.equals(Parse.decodeByte("-0x81"), 0x7f);
		Assert.equals(Parse.decodeByte("0x80"), 0x80);
		Assert.equals(Parse.decodeByte("+0x80"), 0x80);
		Assert.equals(Parse.decodeByte("-0x80"), 0x80);
		Assert.equals(Parse.decodeByte("0x7f"), 0x7f);
		Assert.equals(Parse.decodeByte("+0x7f"), 0x7f);
		Assert.equals(Parse.decodeByte("-0x7f"), 0x81);
		Assert.equals(Parse.decodeByte("0x7e"), 0x7e);
		Assert.equals(Parse.decodeByte("+0x7e"), 0x7e);
		Assert.equals(Parse.decodeByte("-0x7e"), 0x82);
		Assert.equals(Parse.decodeByte("0x0001"), 0x01);
		Assert.equals(Parse.decodeByte("+0x0001"), 0x01);
		Assert.equals(Parse.decodeByte("-0x0001"), 0xff);
		Assert.equals(Parse.decodeByte("0377"), 0xff);
		Assert.equals(Parse.decodeByte("+0377"), 0xff);
		Assert.equals(Parse.decodeByte("-0377"), 0x01);
		Assert.equals(Parse.decodeByte("0b11111111"), 0xff);
		Assert.equals(Parse.decodeByte("+0b11111111"), 0xff);
		Assert.equals(Parse.decodeByte("-0b11111111"), 0x01);
		Assert.equals(Parse.decodeByte("255"), 0xff);
		Assert.equals(Parse.decodeByte("+255"), 0xff);
		Assert.equals(Parse.decodeByte("-255"), 0x01);
		Assert.equals(Parse.decodeByte("1"), 1);
		Assert.equals(Parse.decodeByte("+1"), 1);
		Assert.equals(Parse.decodeByte("-1"), -1);
		Assert.equals(Parse.decodeByte("0"), 0);
		Assert.equals(Parse.decodeByte("+0"), 0);
		Assert.equals(Parse.decodeByte("-0"), 0);
		Assert.equals(Parse.decodeByte(null, Byte.MIN_VALUE), Byte.MIN_VALUE);
		Assert.equals(Parse.decodeByte("0x80", Byte.MAX_VALUE), 0x80);
		Assert.equals(Parse.decodeByte("0x100", Byte.MIN_VALUE), Byte.MIN_VALUE);
		Assert.equals(Parse.decodeByte("abc", Byte.MAX_VALUE), Byte.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidByte() {
		assertNfe(() -> Parse.decodeByte(null));
		assertNfe(() -> Parse.decodeByte(""));
		assertNfe(() -> Parse.decodeByte("-+1"));
		assertNfe(() -> Parse.decodeByte("0x"));
		assertNfe(() -> Parse.decodeByte("0x+1"));
		assertNfe(() -> Parse.decodeByte("0-1"));
		assertNfe(() -> Parse.decodeByte("08"));
		assertNfe(() -> Parse.decodeByte("0b"));
		assertNfe(() -> Parse.decodeByte("0x100"));
		assertNfe(() -> Parse.decodeByte("-0x100"));
		assertNfe(() -> Parse.decodeByte("-0x80000000"));
	}

	@Test
	public void testDecodeUbyte() {
		Assert.equals(Parse.decodeUbyte("0xff"), 0xff);
		Assert.equals(Parse.decodeUbyte("+0xff"), 0xff);
		Assert.equals(Parse.decodeUbyte("0x81"), 0x81);
		Assert.equals(Parse.decodeUbyte("+0x81"), 0x81);
		Assert.equals(Parse.decodeUbyte("0x80"), 0x80);
		Assert.equals(Parse.decodeUbyte("+0x80"), 0x80);
		Assert.equals(Parse.decodeUbyte("0x7f"), 0x7f);
		Assert.equals(Parse.decodeUbyte("+0x7f"), 0x7f);
		Assert.equals(Parse.decodeUbyte("0377"), 0xff);
		Assert.equals(Parse.decodeUbyte("+0377"), 0xff);
		Assert.equals(Parse.decodeUbyte("255"), 0xff);
		Assert.equals(Parse.decodeUbyte("+255"), 0xff);
		Assert.equals(Parse.decodeUbyte("1"), 1);
		Assert.equals(Parse.decodeUbyte("+1"), 1);
		Assert.equals(Parse.decodeUbyte("0"), 0);
		Assert.equals(Parse.decodeUbyte("+0"), 0);
		Assert.equals(Parse.decodeUbyte(null, Byte.MIN_VALUE), Byte.MIN_VALUE);
		Assert.equals(Parse.decodeUbyte("0x80", Byte.MAX_VALUE), 0x80);
		Assert.equals(Parse.decodeUbyte("0x100", Byte.MIN_VALUE), Byte.MIN_VALUE);
		Assert.equals(Parse.decodeUbyte("abc", Byte.MAX_VALUE), Byte.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidUbyte() {
		assertNfe(() -> Parse.decodeUbyte(null));
		assertNfe(() -> Parse.decodeUbyte(""));
		assertNfe(() -> Parse.decodeUbyte("-1"));
		assertNfe(() -> Parse.decodeUbyte("++1"));
		assertNfe(() -> Parse.decodeUbyte("0x"));
		assertNfe(() -> Parse.decodeUbyte("0x+1"));
		assertNfe(() -> Parse.decodeUbyte("0-1"));
		assertNfe(() -> Parse.decodeUbyte("08"));
		assertNfe(() -> Parse.decodeUbyte("0b"));
		assertNfe(() -> Parse.decodeUbyte("0x100"));
		assertNfe(() -> Parse.decodeUbyte("+0x100"));
	}

	@Test
	public void testDecodeShort() {
		Assert.equals(Parse.decodeShort("0xffff"), 0xffff);
		Assert.equals(Parse.decodeShort("+0xffff"), 0xffff);
		Assert.equals(Parse.decodeShort("-0xffff"), 0x0001);
		Assert.equals(Parse.decodeShort("0xfffe"), 0xfffe);
		Assert.equals(Parse.decodeShort("+0xfffe"), 0xfffe);
		Assert.equals(Parse.decodeShort("-0xfffe"), 0x0002);
		Assert.equals(Parse.decodeShort("0x8001"), 0x8001);
		Assert.equals(Parse.decodeShort("+0x8001"), 0x8001);
		Assert.equals(Parse.decodeShort("-0x8001"), 0x7fff);
		Assert.equals(Parse.decodeShort("0x8000"), 0x8000);
		Assert.equals(Parse.decodeShort("+0x8000"), 0x8000);
		Assert.equals(Parse.decodeShort("-0x8000"), 0x8000);
		Assert.equals(Parse.decodeShort("0x7fff"), 0x7fff);
		Assert.equals(Parse.decodeShort("+0x7fff"), 0x7fff);
		Assert.equals(Parse.decodeShort("-0x7fff"), 0x8001);
		Assert.equals(Parse.decodeShort("0x7ffe"), 0x7ffe);
		Assert.equals(Parse.decodeShort("+0x7ffe"), 0x7ffe);
		Assert.equals(Parse.decodeShort("-0x7ffe"), 0x8002);
		Assert.equals(Parse.decodeShort("0x000001"), 0x0001);
		Assert.equals(Parse.decodeShort("+0x000001"), 0x0001);
		Assert.equals(Parse.decodeShort("-0x000001"), 0xffff);
		Assert.equals(Parse.decodeShort("0177777"), 0xffff);
		Assert.equals(Parse.decodeShort("+0177777"), 0xffff);
		Assert.equals(Parse.decodeShort("-0177777"), 0x0001);
		Assert.equals(Parse.decodeShort("0b1111111111111111"), 0xffff);
		Assert.equals(Parse.decodeShort("+0b1111111111111111"), 0xffff);
		Assert.equals(Parse.decodeShort("-0b1111111111111111"), 0x0001);
		Assert.equals(Parse.decodeShort("65535"), 0xffff);
		Assert.equals(Parse.decodeShort("+65535"), 0xffff);
		Assert.equals(Parse.decodeShort("-65535"), 0x0001);
		Assert.equals(Parse.decodeShort("1"), 1);
		Assert.equals(Parse.decodeShort("+1"), 1);
		Assert.equals(Parse.decodeShort("-1"), -1);
		Assert.equals(Parse.decodeShort("0"), 0);
		Assert.equals(Parse.decodeShort("+0"), 0);
		Assert.equals(Parse.decodeShort("-0"), 0);
		Assert.equals(Parse.decodeShort(null, Short.MIN_VALUE), Short.MIN_VALUE);
		Assert.equals(Parse.decodeShort("0x8000", Short.MAX_VALUE), 0x8000);
		Assert.equals(Parse.decodeShort("0x10000", Short.MIN_VALUE), Short.MIN_VALUE);
		Assert.equals(Parse.decodeShort("abc", Short.MAX_VALUE), Short.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidShort() {
		assertNfe(() -> Parse.decodeShort(null));
		assertNfe(() -> Parse.decodeShort(""));
		assertNfe(() -> Parse.decodeShort("-+1"));
		assertNfe(() -> Parse.decodeShort("0x"));
		assertNfe(() -> Parse.decodeShort("0x+1"));
		assertNfe(() -> Parse.decodeShort("0-1"));
		assertNfe(() -> Parse.decodeShort("08"));
		assertNfe(() -> Parse.decodeShort("0b"));
		assertNfe(() -> Parse.decodeShort("0x10000"));
		assertNfe(() -> Parse.decodeShort("-0x10000"));
		assertNfe(() -> Parse.decodeShort("-0x80000000"));
	}

	@Test
	public void testDecodeUshort() {
		Assert.equals(Parse.decodeUshort("0xffff"), 0xffff);
		Assert.equals(Parse.decodeUshort("+0xffff"), 0xffff);
		Assert.equals(Parse.decodeUshort("0x8001"), 0x8001);
		Assert.equals(Parse.decodeUshort("+0x8001"), 0x8001);
		Assert.equals(Parse.decodeUshort("0x8000"), 0x8000);
		Assert.equals(Parse.decodeUshort("+0x8000"), 0x8000);
		Assert.equals(Parse.decodeUshort("0x7fff"), 0x7fff);
		Assert.equals(Parse.decodeUshort("+0x7fff"), 0x7fff);
		Assert.equals(Parse.decodeUshort("0177777"), 0xffff);
		Assert.equals(Parse.decodeUshort("+0177777"), 0xffff);
		Assert.equals(Parse.decodeUshort("65535"), 0xffff);
		Assert.equals(Parse.decodeUshort("+65535"), 0xffff);
		Assert.equals(Parse.decodeUshort("1"), 1);
		Assert.equals(Parse.decodeUshort("+1"), 1);
		Assert.equals(Parse.decodeUshort("0"), 0);
		Assert.equals(Parse.decodeUshort("+0"), 0);
		Assert.equals(Parse.decodeUshort(null, Short.MIN_VALUE), Short.MIN_VALUE);
		Assert.equals(Parse.decodeUshort("0x8000", Short.MAX_VALUE), 0x8000);
		Assert.equals(Parse.decodeUshort("0x10000", Short.MIN_VALUE), Short.MIN_VALUE);
		Assert.equals(Parse.decodeUshort("abc", Short.MAX_VALUE), Short.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidUshort() {
		assertNfe(() -> Parse.decodeUshort(null));
		assertNfe(() -> Parse.decodeUshort(""));
		assertNfe(() -> Parse.decodeUshort("-1"));
		assertNfe(() -> Parse.decodeUshort("++1"));
		assertNfe(() -> Parse.decodeUshort("0x"));
		assertNfe(() -> Parse.decodeUshort("0x+1"));
		assertNfe(() -> Parse.decodeUshort("0-1"));
		assertNfe(() -> Parse.decodeUshort("08"));
		assertNfe(() -> Parse.decodeUshort("0b"));
		assertNfe(() -> Parse.decodeUshort("0x10000"));
		assertNfe(() -> Parse.decodeUshort("+0x10000"));
	}

	@Test
	public void testDecodeInt() {
		Assert.equal(Parse.decodeInt("0xffffffff"), 0xffffffff);
		Assert.equal(Parse.decodeInt("+0xffffffff"), 0xffffffff);
		Assert.equal(Parse.decodeInt("-0xffffffff"), 0x00000001);
		Assert.equal(Parse.decodeInt("0xfffffffe"), 0xfffffffe);
		Assert.equal(Parse.decodeInt("+0xfffffffe"), 0xfffffffe);
		Assert.equal(Parse.decodeInt("-0xfffffffe"), 0x00000002);
		Assert.equal(Parse.decodeInt("0x80000001"), 0x80000001);
		Assert.equal(Parse.decodeInt("+0x80000001"), 0x80000001);
		Assert.equal(Parse.decodeInt("-0x80000001"), 0x7fffffff);
		Assert.equal(Parse.decodeInt("0x80000000"), 0x80000000);
		Assert.equal(Parse.decodeInt("+0x80000000"), 0x80000000);
		Assert.equal(Parse.decodeInt("-0x80000000"), 0x80000000);
		Assert.equal(Parse.decodeInt("0x7fffffff"), 0x7fffffff);
		Assert.equal(Parse.decodeInt("+0x7fffffff"), 0x7fffffff);
		Assert.equal(Parse.decodeInt("-0x7fffffff"), 0x80000001);
		Assert.equal(Parse.decodeInt("0x7ffffffe"), 0x7ffffffe);
		Assert.equal(Parse.decodeInt("+0x7ffffffe"), 0x7ffffffe);
		Assert.equal(Parse.decodeInt("-0x7ffffffe"), 0x80000002);
		Assert.equal(Parse.decodeInt("0x0000000001"), 0x00000001);
		Assert.equal(Parse.decodeInt("+0x0000000001"), 0x00000001);
		Assert.equal(Parse.decodeInt("-0x0000000001"), 0xffffffff);
		Assert.equal(Parse.decodeInt("037777777777"), 0xffffffff);
		Assert.equal(Parse.decodeInt("+037777777777"), 0xffffffff);
		Assert.equal(Parse.decodeInt("-037777777777"), 0x00000001);
		Assert.equal(Parse.decodeInt("0b11111111111111111111111111111111"), 0xffffffff);
		Assert.equal(Parse.decodeInt("+0b11111111111111111111111111111111"), 0xffffffff);
		Assert.equal(Parse.decodeInt("-0b11111111111111111111111111111111"), 0x00000001);
		Assert.equal(Parse.decodeInt("4294967295"), 0xffffffff);
		Assert.equal(Parse.decodeInt("+4294967295"), 0xffffffff);
		Assert.equal(Parse.decodeInt("-4294967295"), 0x00000001);
		Assert.equal(Parse.decodeInt("1"), 1);
		Assert.equal(Parse.decodeInt("+1"), 1);
		Assert.equal(Parse.decodeInt("-1"), -1);
		Assert.equal(Parse.decodeInt("0"), 0);
		Assert.equal(Parse.decodeInt("+0"), 0);
		Assert.equal(Parse.decodeInt("-0"), 0);
		Assert.equal(Parse.decodeInt(null, Integer.MIN_VALUE), Integer.MIN_VALUE);
		Assert.equal(Parse.decodeInt("0x80000000", Integer.MAX_VALUE), 0x80000000);
		Assert.equal(Parse.decodeInt("0x100000000", Integer.MIN_VALUE), Integer.MIN_VALUE);
		Assert.equal(Parse.decodeInt("abc", Integer.MAX_VALUE), Integer.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidInt() {
		assertNfe(() -> Parse.decodeInt(null));
		assertNfe(() -> Parse.decodeInt(""));
		assertNfe(() -> Parse.decodeInt("-+1"));
		assertNfe(() -> Parse.decodeInt("0x"));
		assertNfe(() -> Parse.decodeInt("0x+1"));
		assertNfe(() -> Parse.decodeInt("0-1"));
		assertNfe(() -> Parse.decodeInt("08"));
		assertNfe(() -> Parse.decodeInt("0b"));
		assertNfe(() -> Parse.decodeInt("0x100000000"));
		assertNfe(() -> Parse.decodeInt("-0x100000000"));
	}

	@Test
	public void testDecodeUint() {
		Assert.equal(Parse.decodeUint("0xffffffff"), 0xffffffff);
		Assert.equal(Parse.decodeUint("+0xffffffff"), 0xffffffff);
		Assert.equal(Parse.decodeUint("0x80000001"), 0x80000001);
		Assert.equal(Parse.decodeUint("+0x80000001"), 0x80000001);
		Assert.equal(Parse.decodeUint("0x80000000"), 0x80000000);
		Assert.equal(Parse.decodeUint("+0x80000000"), 0x80000000);
		Assert.equal(Parse.decodeUint("0x7fffffff"), 0x7fffffff);
		Assert.equal(Parse.decodeUint("+0x7fffffff"), 0x7fffffff);
		Assert.equal(Parse.decodeUint("037777777777"), 0xffffffff);
		Assert.equal(Parse.decodeUint("+037777777777"), 0xffffffff);
		Assert.equal(Parse.decodeUint("4294967295"), 0xffffffff);
		Assert.equal(Parse.decodeUint("+4294967295"), 0xffffffff);
		Assert.equal(Parse.decodeUint("1"), 1);
		Assert.equal(Parse.decodeUint("+1"), 1);
		Assert.equal(Parse.decodeUint("0"), 0);
		Assert.equal(Parse.decodeUint("+0"), 0);
		Assert.equal(Parse.decodeUint(null, Integer.MIN_VALUE), Integer.MIN_VALUE);
		Assert.equal(Parse.decodeUint("0x80000000", Integer.MAX_VALUE), 0x80000000);
		Assert.equal(Parse.decodeUint("0x100000000", Integer.MIN_VALUE), Integer.MIN_VALUE);
		Assert.equal(Parse.decodeUint("abc", Integer.MAX_VALUE), Integer.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidUint() {
		assertNfe(() -> Parse.decodeUint(null));
		assertNfe(() -> Parse.decodeUint(""));
		assertNfe(() -> Parse.decodeUint("-1"));
		assertNfe(() -> Parse.decodeUint("++1"));
		assertNfe(() -> Parse.decodeUint("0x"));
		assertNfe(() -> Parse.decodeUint("0x+1"));
		assertNfe(() -> Parse.decodeUint("0-1"));
		assertNfe(() -> Parse.decodeUint("08"));
		assertNfe(() -> Parse.decodeUint("0b"));
		assertNfe(() -> Parse.decodeUint("0x100000000"));
		assertNfe(() -> Parse.decodeUint("+0x100000000"));
	}

	@Test
	public void testDecodeLong() {
		Assert.equal(Parse.decodeLong("0xffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("+0xffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("-0xffffffffffffffff"), 0x0000000000000001L);
		Assert.equal(Parse.decodeLong("0xfffffffffffffffe"), 0xfffffffffffffffeL);
		Assert.equal(Parse.decodeLong("+0xfffffffffffffffe"), 0xfffffffffffffffeL);
		Assert.equal(Parse.decodeLong("-0xfffffffffffffffe"), 0x0000000000000002L);
		Assert.equal(Parse.decodeLong("0x8000000000000001"), 0x8000000000000001L);
		Assert.equal(Parse.decodeLong("+0x8000000000000001"), 0x8000000000000001L);
		Assert.equal(Parse.decodeLong("-0x8000000000000001"), 0x7fffffffffffffffL);
		Assert.equal(Parse.decodeLong("0x8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.decodeLong("+0x8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.decodeLong("-0x8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.decodeLong("0x7fffffffffffffff"), 0x7fffffffffffffffL);
		Assert.equal(Parse.decodeLong("+0x7fffffffffffffff"), 0x7fffffffffffffffL);
		Assert.equal(Parse.decodeLong("-0x7fffffffffffffff"), 0x8000000000000001L);
		Assert.equal(Parse.decodeLong("0x7ffffffffffffffe"), 0x7ffffffffffffffeL);
		Assert.equal(Parse.decodeLong("+0x7ffffffffffffffe"), 0x7ffffffffffffffeL);
		Assert.equal(Parse.decodeLong("-0x7ffffffffffffffe"), 0x8000000000000002L);
		Assert.equal(Parse.decodeLong("0x000000000000000001"), 0x0000000000000001L);
		Assert.equal(Parse.decodeLong("+0x000000000000000001"), 0x0000000000000001L);
		Assert.equal(Parse.decodeLong("-0x000000000000000001"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("01777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("+01777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("-01777777777777777777777"), 0x0000000000000001L);
		Assert.equal(Parse.decodeLong( //
			"0b1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong( //
			"+0b1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong( //
			"-0b1111111111111111111111111111111111111111111111111111111111111111"),
			0x0000000000000001L);
		Assert.equal(Parse.decodeLong("18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("+18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeLong("-18446744073709551615"), 0x0000000000000001L);
		Assert.equal(Parse.decodeLong("1"), 1L);
		Assert.equal(Parse.decodeLong("+1"), 1L);
		Assert.equal(Parse.decodeLong("-1"), -1L);
		Assert.equal(Parse.decodeLong("0"), 0L);
		Assert.equal(Parse.decodeLong("+0"), 0L);
		Assert.equal(Parse.decodeLong("-0"), 0L);
		Assert.equal(Parse.decodeLong(null, Long.MIN_VALUE), Long.MIN_VALUE);
		Assert.equal(Parse.decodeLong("0x8000000000000000", Long.MAX_VALUE), 0x80000000_00000000L);
		Assert.equal(Parse.decodeLong("0x10000000000000000", Long.MIN_VALUE), Long.MIN_VALUE);
		Assert.equal(Parse.decodeLong("abc", Long.MAX_VALUE), Long.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidLong() {
		assertNfe(() -> Parse.decodeLong(null));
		assertNfe(() -> Parse.decodeLong(""));
		assertNfe(() -> Parse.decodeLong("-+1"));
		assertNfe(() -> Parse.decodeLong("0x"));
		assertNfe(() -> Parse.decodeLong("0x+1"));
		assertNfe(() -> Parse.decodeLong("0-1"));
		assertNfe(() -> Parse.decodeLong("08"));
		assertNfe(() -> Parse.decodeLong("0b"));
		assertNfe(() -> Parse.decodeLong("0x10000000000000000"));
		assertNfe(() -> Parse.decodeLong("-0x10000000000000000"));
	}

	@Test
	public void testDecodeUlong() {
		Assert.equal(Parse.decodeUlong("0xffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeUlong("+0xffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeUlong("0x8000000000000001"), 0x8000000000000001L);
		Assert.equal(Parse.decodeUlong("+0x8000000000000001"), 0x8000000000000001L);
		Assert.equal(Parse.decodeUlong("0x8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.decodeUlong("+0x8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.decodeUlong("0x7fffffffffffffff"), 0x7fffffffffffffffL);
		Assert.equal(Parse.decodeUlong("+0x7fffffffffffffff"), 0x7fffffffffffffffL);
		Assert.equal(Parse.decodeUlong("01777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeUlong("+01777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeUlong("18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeUlong("+18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.decodeUlong("1"), 1L);
		Assert.equal(Parse.decodeUlong("+1"), 1L);
		Assert.equal(Parse.decodeUlong("0"), 0L);
		Assert.equal(Parse.decodeUlong("+0"), 0L);
		Assert.equal(Parse.decodeUlong(null, Long.MIN_VALUE), Long.MIN_VALUE);
		Assert.equal(Parse.decodeUlong("0x8000000000000000", Long.MAX_VALUE), 0x80000000_00000000L);
		Assert.equal(Parse.decodeUlong("0x10000000000000000", Long.MIN_VALUE), Long.MIN_VALUE);
		Assert.equal(Parse.decodeUlong("abc", Long.MAX_VALUE), Long.MAX_VALUE);
	}

	@Test
	public void testDecodeInvalidUlong() {
		assertNfe(() -> Parse.decodeUlong(null));
		assertNfe(() -> Parse.decodeUlong(""));
		assertNfe(() -> Parse.decodeUlong("-1"));
		assertNfe(() -> Parse.decodeUlong("++1"));
		assertNfe(() -> Parse.decodeUlong("0x"));
		assertNfe(() -> Parse.decodeUlong("0x+1"));
		assertNfe(() -> Parse.decodeUlong("0-1"));
		assertNfe(() -> Parse.decodeUlong("08"));
		assertNfe(() -> Parse.decodeUlong("0b"));
		assertNfe(() -> Parse.decodeUlong("0x10000000000000000"));
		assertNfe(() -> Parse.decodeUlong("+0x10000000000000000"));
	}

	@Test
	public void testParseBool() {
		Assert.equal(Parse.parseBool("TRUE"), true);
		Assert.equal(Parse.parseBool("1"), true);
		Assert.equal(Parse.parseBool("False"), false);
		Assert.equal(Parse.parseBool("0"), false);
	}

	@Test
	public void testParseInvalidBool() {
		assertNfe(() -> Parse.parseBool(null));
		assertNfe(() -> Parse.parseBool(""));
		assertNfe(() -> Parse.parseBool("-1"));
	}

	@Test
	public void testParseByte() {
		Assert.equals(Parse.parseByte(16, "ff"), 0xff);
		Assert.equals(Parse.parseByte(16, "+ff"), 0xff);
		Assert.equals(Parse.parseByte(16, "-ff"), 0x01);
		Assert.equals(Parse.parseByte(16, "80"), 0x80);
		Assert.equals(Parse.parseByte(16, "+80"), 0x80);
		Assert.equals(Parse.parseByte(16, "-80"), 0x80);
		Assert.equals(Parse.parseByte(8, "377"), 0xff);
		Assert.equals(Parse.parseByte(8, "+377"), 0xff);
		Assert.equals(Parse.parseByte(8, "-377"), 0x01);
		Assert.equals(Parse.parseByte(2, "11111111"), 0xff);
		Assert.equals(Parse.parseByte(2, "+11111111"), 0xff);
		Assert.equals(Parse.parseByte(2, "-11111111"), 0x01);
		Assert.equals(Parse.parseByte("255"), 0xff);
		Assert.equals(Parse.parseByte("+255"), 0xff);
		Assert.equals(Parse.parseByte("-255"), 0x01);
		Assert.equals(Parse.parseByte("1"), 1);
		Assert.equals(Parse.parseByte("+1"), 1);
		Assert.equals(Parse.parseByte("-1"), -1);
		Assert.equals(Parse.parseByte("0"), 0);
		Assert.equals(Parse.parseByte("+0"), 0);
		Assert.equals(Parse.parseByte("-0"), 0);
	}

	@Test
	public void testParseInvalidByte() {
		assertNfe(() -> Parse.parseByte(null));
		assertNfe(() -> Parse.parseByte(""));
		assertNfe(() -> Parse.parseByte("-+1"));
		assertNfe(() -> Parse.parseByte(16, "100"));
		assertNfe(() -> Parse.parseByte(16, "-100"));
		Assert.illegalArg(() -> Parse.parseByte(1, "1"));
		Assert.illegalArg(() -> Parse.parseByte(37, "1"));
	}

	@Test
	public void testParseUbyte() {
		Assert.equals(Parse.parseUbyte(16, "ff"), 0xff);
		Assert.equals(Parse.parseUbyte(16, "+ff"), 0xff);
		Assert.equals(Parse.parseUbyte(16, "80"), 0x80);
		Assert.equals(Parse.parseUbyte(16, "+80"), 0x80);
		Assert.equals(Parse.parseUbyte(8, "377"), 0xff);
		Assert.equals(Parse.parseUbyte(8, "+377"), 0xff);
		Assert.equals(Parse.parseUbyte(2, "11111111"), 0xff);
		Assert.equals(Parse.parseUbyte(2, "+11111111"), 0xff);
		Assert.equals(Parse.parseUbyte("255"), 0xff);
		Assert.equals(Parse.parseUbyte("+255"), 0xff);
		Assert.equals(Parse.parseUbyte("1"), 1);
		Assert.equals(Parse.parseUbyte("+1"), 1);
		Assert.equals(Parse.parseUbyte("0"), 0);
		Assert.equals(Parse.parseUbyte("+0"), 0);
	}

	@Test
	public void testParseInvalidUbyte() {
		assertNfe(() -> Parse.parseUbyte(null));
		assertNfe(() -> Parse.parseUbyte(""));
		assertNfe(() -> Parse.parseUbyte("-1"));
		assertNfe(() -> Parse.parseUbyte("++1"));
		assertNfe(() -> Parse.parseUbyte(16, "100"));
		Assert.illegalArg(() -> Parse.parseUbyte(1, "1"));
		Assert.illegalArg(() -> Parse.parseUbyte(37, "1"));
	}

	@Test
	public void testParseShort() {
		Assert.equals(Parse.parseShort(16, "ffff"), 0xffff);
		Assert.equals(Parse.parseShort(16, "+ffff"), 0xffff);
		Assert.equals(Parse.parseShort(16, "-ffff"), 0x0001);
		Assert.equals(Parse.parseShort(16, "8000"), 0x8000);
		Assert.equals(Parse.parseShort(16, "+8000"), 0x8000);
		Assert.equals(Parse.parseShort(16, "-8000"), 0x8000);
		Assert.equals(Parse.parseShort(8, "177777"), 0xffff);
		Assert.equals(Parse.parseShort(8, "+177777"), 0xffff);
		Assert.equals(Parse.parseShort(8, "-177777"), 0x0001);
		Assert.equals(Parse.parseShort(2, "1111111111111111"), 0xffff);
		Assert.equals(Parse.parseShort(2, "+1111111111111111"), 0xffff);
		Assert.equals(Parse.parseShort(2, "-1111111111111111"), 0x0001);
		Assert.equals(Parse.parseShort("65535"), 0xffff);
		Assert.equals(Parse.parseShort("+65535"), 0xffff);
		Assert.equals(Parse.parseShort("-65535"), 0x0001);
		Assert.equals(Parse.parseShort("1"), 1);
		Assert.equals(Parse.parseShort("+1"), 1);
		Assert.equals(Parse.parseShort("-1"), -1);
		Assert.equals(Parse.parseShort("0"), 0);
		Assert.equals(Parse.parseShort("+0"), 0);
		Assert.equals(Parse.parseShort("-0"), 0);
	}

	@Test
	public void testParseInvalidShort() {
		assertNfe(() -> Parse.parseShort(null));
		assertNfe(() -> Parse.parseShort(""));
		assertNfe(() -> Parse.parseShort("-+1"));
		assertNfe(() -> Parse.parseShort(16, "10000"));
		assertNfe(() -> Parse.parseShort(16, "-10000"));
		Assert.illegalArg(() -> Parse.parseShort(1, "1"));
		Assert.illegalArg(() -> Parse.parseShort(37, "1"));
	}

	@Test
	public void testParseUshort() {
		Assert.equals(Parse.parseUshort(16, "ffff"), 0xffff);
		Assert.equals(Parse.parseUshort(16, "+ffff"), 0xffff);
		Assert.equals(Parse.parseUshort(16, "8000"), 0x8000);
		Assert.equals(Parse.parseUshort(16, "+8000"), 0x8000);
		Assert.equals(Parse.parseUshort(8, "177777"), 0xffff);
		Assert.equals(Parse.parseUshort(8, "+177777"), 0xffff);
		Assert.equals(Parse.parseUshort(2, "1111111111111111"), 0xffff);
		Assert.equals(Parse.parseUshort(2, "+1111111111111111"), 0xffff);
		Assert.equals(Parse.parseUshort("65535"), 0xffff);
		Assert.equals(Parse.parseUshort("+65535"), 0xffff);
		Assert.equals(Parse.parseUshort("1"), 1);
		Assert.equals(Parse.parseUshort("+1"), 1);
		Assert.equals(Parse.parseUshort("0"), 0);
		Assert.equals(Parse.parseUshort("+0"), 0);
	}

	@Test
	public void testParseInvalidUshort() {
		assertNfe(() -> Parse.parseUshort(null));
		assertNfe(() -> Parse.parseUshort(""));
		assertNfe(() -> Parse.parseUshort("-1"));
		assertNfe(() -> Parse.parseUshort("++1"));
		assertNfe(() -> Parse.parseUshort(16, "10000"));
		Assert.illegalArg(() -> Parse.parseUshort(1, "1"));
		Assert.illegalArg(() -> Parse.parseUshort(37, "1"));
	}

	@Test
	public void testParseInt() {
		Assert.equal(Parse.parseInt(16, "ffffffff"), 0xffffffff);
		Assert.equal(Parse.parseInt(16, "+ffffffff"), 0xffffffff);
		Assert.equal(Parse.parseInt(16, "-ffffffff"), 0x00000001);
		Assert.equal(Parse.parseInt(16, "80000000"), 0x80000000);
		Assert.equal(Parse.parseInt(16, "+80000000"), 0x80000000);
		Assert.equal(Parse.parseInt(16, "-80000000"), 0x80000000);
		Assert.equal(Parse.parseInt(8, "37777777777"), 0xffffffff);
		Assert.equal(Parse.parseInt(8, "+37777777777"), 0xffffffff);
		Assert.equal(Parse.parseInt(8, "-37777777777"), 0x00000001);
		Assert.equal(Parse.parseInt(2, "11111111111111111111111111111111"), 0xffffffff);
		Assert.equal(Parse.parseInt(2, "+11111111111111111111111111111111"), 0xffffffff);
		Assert.equal(Parse.parseInt(2, "-11111111111111111111111111111111"), 0x00000001);
		Assert.equal(Parse.parseInt("4294967295"), 0xffffffff);
		Assert.equal(Parse.parseInt("+4294967295"), 0xffffffff);
		Assert.equal(Parse.parseInt("-4294967295"), 0x00000001);
		Assert.equal(Parse.parseInt("1"), 1);
		Assert.equal(Parse.parseInt("+1"), 1);
		Assert.equal(Parse.parseInt("-1"), -1);
		Assert.equal(Parse.parseInt("0"), 0);
		Assert.equal(Parse.parseInt("+0"), 0);
		Assert.equal(Parse.parseInt("-0"), 0);
	}

	@Test
	public void testParseInvalidInt() {
		assertNfe(() -> Parse.parseInt(null));
		assertNfe(() -> Parse.parseInt(""));
		assertNfe(() -> Parse.parseInt("-+1"));
		assertNfe(() -> Parse.parseInt(16, "100000000"));
		assertNfe(() -> Parse.parseInt(16, "-100000000"));
		Assert.illegalArg(() -> Parse.parseInt(1, "1"));
		Assert.illegalArg(() -> Parse.parseInt(37, "1"));
	}

	@Test
	public void testParseUint() {
		Assert.equal(Parse.parseUint(16, "ffffffff"), 0xffffffff);
		Assert.equal(Parse.parseUint(16, "+ffffffff"), 0xffffffff);
		Assert.equal(Parse.parseUint(16, "80000000"), 0x80000000);
		Assert.equal(Parse.parseUint(16, "+80000000"), 0x80000000);
		Assert.equal(Parse.parseUint(8, "37777777777"), 0xffffffff);
		Assert.equal(Parse.parseUint(8, "+37777777777"), 0xffffffff);
		Assert.equal(Parse.parseUint(2, "11111111111111111111111111111111"), 0xffffffff);
		Assert.equal(Parse.parseUint(2, "+11111111111111111111111111111111"), 0xffffffff);
		Assert.equal(Parse.parseUint("4294967295"), 0xffffffff);
		Assert.equal(Parse.parseUint("+4294967295"), 0xffffffff);
		Assert.equal(Parse.parseUint("1"), 1);
		Assert.equal(Parse.parseUint("+1"), 1);
		Assert.equal(Parse.parseUint("0"), 0);
		Assert.equal(Parse.parseUint("+0"), 0);
	}

	@Test
	public void testParseInvalidUint() {
		assertNfe(() -> Parse.parseUint(null));
		assertNfe(() -> Parse.parseUint(""));
		assertNfe(() -> Parse.parseUint("-1"));
		assertNfe(() -> Parse.parseUint("++1"));
		assertNfe(() -> Parse.parseUint(16, "100000000"));
		Assert.illegalArg(() -> Parse.parseUint(1, "1"));
		Assert.illegalArg(() -> Parse.parseUint(37, "1"));
	}

	@Test
	public void testParseLong() {
		Assert.equal(Parse.parseLong(16, "ffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseLong(16, "+ffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseLong(16, "-ffffffffffffffff"), 0x0000000000000001L);
		Assert.equal(Parse.parseLong(16, "8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.parseLong(16, "+8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.parseLong(16, "-8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.parseLong(8, "1777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseLong(8, "+1777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseLong(8, "-1777777777777777777777"), 0x0000000000000001L);
		Assert.equal(Parse.parseLong( //
			2, "1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		Assert.equal(Parse.parseLong( //
			2, "+1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		Assert.equal(Parse.parseLong( //
			2, "-1111111111111111111111111111111111111111111111111111111111111111"),
			0x0000000000000001L);
		Assert.equal(Parse.parseLong("18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseLong("+18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseLong("-18446744073709551615"), 0x0000000000000001L);
		Assert.equal(Parse.parseLong("1"), 1L);
		Assert.equal(Parse.parseLong("+1"), 1L);
		Assert.equal(Parse.parseLong("-1"), -1L);
		Assert.equal(Parse.parseLong("0"), 0L);
		Assert.equal(Parse.parseLong("+0"), 0L);
		Assert.equal(Parse.parseLong("-0"), 0L);
	}

	@Test
	public void testParseInvalidLong() {
		assertNfe(() -> Parse.parseLong(null));
		assertNfe(() -> Parse.parseLong(""));
		assertNfe(() -> Parse.parseLong("-+1"));
		assertNfe(() -> Parse.parseLong(16, "10000000000000000"));
		assertNfe(() -> Parse.parseLong(16, "-10000000000000000"));
		Assert.illegalArg(() -> Parse.parseLong(1, "1"));
		Assert.illegalArg(() -> Parse.parseLong(37, "1"));
	}

	@Test
	public void testParseUlong() {
		Assert.equal(Parse.parseUlong(16, "ffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong(16, "+ffffffffffffffff"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong(16, "8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.parseUlong(16, "+8000000000000000"), 0x8000000000000000L);
		Assert.equal(Parse.parseUlong(8, "1777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong(8, "+1777777777777777777777"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong( //
			2, "1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong( //
			2, "+1111111111111111111111111111111111111111111111111111111111111111"),
			0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong("18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong("+18446744073709551615"), 0xffffffffffffffffL);
		Assert.equal(Parse.parseUlong("1"), 1L);
		Assert.equal(Parse.parseUlong("+1"), 1L);
		Assert.equal(Parse.parseUlong("0"), 0L);
		Assert.equal(Parse.parseUlong("+0"), 0L);
	}

	@Test
	public void testParseInvalidUlong() {
		assertNfe(() -> Parse.parseUlong(null));
		assertNfe(() -> Parse.parseUlong(""));
		assertNfe(() -> Parse.parseUlong("-1"));
		assertNfe(() -> Parse.parseUlong("++1"));
		assertNfe(() -> Parse.parseUlong(16, "10000000000000000"));
		Assert.illegalArg(() -> Parse.parseUlong(1, "1"));
		Assert.illegalArg(() -> Parse.parseUlong(37, "1"));
	}

	@Test
	public void testParseFloat() {
		Assert.equal(Parse.parseFloat("1e10"), 1e10f);
		Assert.equal(Parse.parseFloat("-.1e-10"), -1e-11f);
		Assert.equal(Parse.parseFloat("NaN"), Float.NaN);
		Assert.equal(Parse.parseFloat("-Infinity"), Float.NEGATIVE_INFINITY);
		Assert.equal(Parse.parseFloat("Infinity"), Float.POSITIVE_INFINITY);
		Assert.equal(Parse.parseFloat("+Infinity"), Float.POSITIVE_INFINITY);
	}

	@Test
	public void testParseInvalidFloat() {
		assertNfe(() -> Parse.parseFloat(null));
		assertNfe(() -> Parse.parseFloat(""));
		assertNfe(() -> Parse.parseFloat("1e10.0"));
		assertNfe(() -> Parse.parseFloat("nan"));
		assertNfe(() -> Parse.parseFloat("infinity"));
	}

	@Test
	public void testParseDouble() {
		Assert.equal(Parse.parseDouble("1e10"), 1e10);
		Assert.equal(Parse.parseDouble("-.1e-10"), -1e-11);
		Assert.equal(Parse.parseDouble("NaN"), Double.NaN);
		Assert.equal(Parse.parseDouble("-Infinity"), Double.NEGATIVE_INFINITY);
		Assert.equal(Parse.parseDouble("Infinity"), Double.POSITIVE_INFINITY);
		Assert.equal(Parse.parseDouble("+Infinity"), Double.POSITIVE_INFINITY);
	}

	@Test
	public void testParseInvalidDouble() {
		assertNfe(() -> Parse.parseDouble(null));
		assertNfe(() -> Parse.parseDouble(""));
		assertNfe(() -> Parse.parseDouble("1e10.0"));
		assertNfe(() -> Parse.parseDouble("nan"));
		assertNfe(() -> Parse.parseDouble("infinity"));
	}

	private static void assertNfe(Excepts.Runnable<?> runnable) {
		Assert.thrown(NumberFormatException.class, runnable);
	}
}
