package ceri.common.text;

import org.junit.Test;
import ceri.common.math.Radix;
import ceri.common.test.Assert;

public class FormatTest {
	private static final byte BMIN = Byte.MIN_VALUE;
	private static final byte BMAX = Byte.MAX_VALUE;
	private static final short SMIN = Short.MIN_VALUE;
	private static final short SMAX = Short.MAX_VALUE;
	private static final int IMIN = Integer.MIN_VALUE;
	private static final int IMAX = Integer.MAX_VALUE;
	private static final long LMIN = Long.MIN_VALUE;
	private static final long LMAX = Long.MAX_VALUE;
	private static final double DNINF = Double.NEGATIVE_INFINITY;
	private static final double DPINF = Double.POSITIVE_INFINITY;

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Format.class);
	}

	@Test
	public void testLongFormats() {
		Assert.string(Format.DEC_UBYTE.apply(0L), "0");
		Assert.string(Format.DEC_UBYTE.apply(-1L), "255");
		Assert.string(Format.DEC_UBYTE.apply(LMAX), "255");
		Assert.string(Format.DEC_USHORT.apply(0L), "0");
		Assert.string(Format.DEC_USHORT.apply(-1L), "%s", 0xffff);
		Assert.string(Format.DEC_USHORT.apply(LMAX), "%s", 0xffff);
		Assert.string(Format.DEC_UINT.apply(0L), "0");
		Assert.string(Format.DEC_UINT.apply(-1L), "%s", 0xffffffffL);
		Assert.string(Format.DEC_UINT.apply(LMAX), "%s", 0xffffffffL);
		Assert.string(Format.UDEC_HEX.apply(0L), "0");
		Assert.string(Format.UDEC_OR_HEX.apply(0L), "0");
	}

	@Test
	public void testRound() {
		Assert.string(Format.ROUND.apply(DNINF), "" + LMIN);
		Assert.string(Format.ROUND.apply(DPINF), "" + LMAX);
		Assert.string(Format.ROUND.apply(Double.NaN), "0");
		Assert.string(Format.ROUND.apply(0), "0");
		Assert.string(Format.ROUND.apply(1000000000.5), "1000000001");
		Assert.string(Format.ROUND.apply(1000000000.4), "1000000000");
		Assert.string(Format.ROUND.apply(-1000000000.5), "-1000000000");
		Assert.string(Format.ROUND.apply(-1000000000.6), "-1000000001");
	}

	@Test
	public void testHex() {
		Assert.string(Format.HEX.ubyte(null), "null");
		Assert.string(Format.HEX.ubyte(BMIN), "0x80");
		Assert.string(Format.HEX.ubyte(BMAX), "0x7f");
		Assert.string(Format.HEX.ubyte(123.5), "0x7b");
		Assert.string(Format.HEX.ushort(null), "null");
		Assert.string(Format.HEX.ushort(SMIN), "0x8000");
		Assert.string(Format.HEX.ushort(SMAX), "0x7fff");
		Assert.string(Format.HEX.ushort(123.5), "0x7b");
		Assert.string(Format.HEX.uint(null), "null");
		Assert.string(Format.HEX.uint(IMIN), "0x80000000");
		Assert.string(Format.HEX.uint(IMAX), "0x7fffffff");
		Assert.string(Format.HEX.uint(123.5), "0x7b");
		Assert.string(Format.HEX.apply(null), "null");
		Assert.string(Format.HEX.apply(LMIN), "0x8000000000000000");
		Assert.string(Format.HEX.apply(LMAX), "0x7fffffffffffffff");
		Assert.string(Format.HEX.apply(123.5), "0x7b");
	}

	@Test
	public void testFormatDouble() {
		Assert.string(Format.format(1.0, 0, 3), "1");
		Assert.string(Format.format(0.0, 0, 3), "0");
	}

	@Test
	public void testSeparators() {
		Assert.string(Format.Separator.NONE.accept(0, 0), null);
		Assert.string(Format.Separator._4.accept(3, 5), null);
		Assert.string(Format.Separator._4.accept(4, 5), "_");
		Assert.string(Format.Separator._4.accept(5, 5), null);
	}

	@Test
	public void testSeparator() {
		Assert.same(Format.Separator.of(0, ":"), Format.Separator.NONE);
		Assert.same(Format.Separator.of(null, ":"), Format.Separator.NONE);
		Assert.same(Format.Separator.of(2, null), Format.Separator.NONE);
		var s1 = Format.Separator.of(1, ":");
		var s2 = Format.Separator.of(2, ":");
		Assert.string(s1.accept(0, 0), null);
		Assert.string(Format.format(1234, Radix.DEC, 2, 4, s1), "1:2:3:4");
		Assert.string(Format.format(0x1234, Radix.HEX, 2, 4, s1), "0x1:2:3:4");
		Assert.string(Format.format(0x1234, Radix.HEX, 2, 4, s2), "0x12:34");
	}

	@Test
	public void testHexExactDigits() {
		Assert.string(Format.hex(0x12345, 4), "0x2345");
	}

	@Test
	public void testSignedHex() {
		Assert.string(Format.format(-1, false, "", 10, 2, 4), "-01");
		Assert.string(Format.format(-11111, false, "", 10, 2, 4), "-1111");
	}

	@Test
	public void testUnsignedDecAndHex() {
		Assert.string(Format.udecHex(0), "0");
		Assert.string(Format.udecHex(9), "9");
		Assert.string(Format.udecHex(15), "15|0xf");
		Assert.string(Format.udecHex(LMIN), "%s|0x8000000000000000", Long.toUnsignedString(LMIN));
		Assert.string(Format.udecHex(-1), "%s|0xffffffffffffffff", Long.toUnsignedString(-1));
	}

	@Test
	public void testUnsignedDecOrHex() {
		Assert.string(Format.udecOrHex(0), "0");
		Assert.string(Format.udecOrHex(9), "9");
		Assert.string(Format.udecOrHex(15), "0xf");
		Assert.string(Format.udecOrHex(-1), "0xffffffffffffffff");
	}

	@Test
	public void testBin() {
		Assert.string(Format.bin(0x59), "0b1011001");
		Assert.string(Format.bin(0x59, 6), "0b011001");
	}

	@Test
	public void testFormatDoubleWithDecimalPlaces() {
		Assert.string(Format.format(1.1, 2, 4), "1.10");
		Assert.string(Format.format(1.1, 2, 0), "1.10");
		Assert.string(Format.format(1.12345, 2, 4), "1.1235");
		Assert.string(Format.format(1.12345, 2, 0), "1.12345");
		Assert.string(Format.format(DNINF, 2, 4), "-Infinity");
		Assert.string(Format.format(Double.NaN, 2, 4), "NaN");
	}

}
