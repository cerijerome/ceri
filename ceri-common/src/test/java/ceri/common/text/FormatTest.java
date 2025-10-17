package ceri.common.text;

import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertSame;
import static ceri.common.test.AssertUtil.assertString;
import org.junit.Test;
import ceri.common.math.Radix;

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
		assertPrivateConstructor(Format.class);
	}

	@Test
	public void testLongFormats() {
		assertString(Format.DEC_UBYTE.apply(0L), "0");
		assertString(Format.DEC_UBYTE.apply(-1L), "255");
		assertString(Format.DEC_UBYTE.apply(LMAX), "255");
		assertString(Format.DEC_USHORT.apply(0L), "0");
		assertString(Format.DEC_USHORT.apply(-1L), "%s", 0xffff);
		assertString(Format.DEC_USHORT.apply(LMAX), "%s", 0xffff);
		assertString(Format.DEC_UINT.apply(0L), "0");
		assertString(Format.DEC_UINT.apply(-1L), "%s", 0xffffffffL);
		assertString(Format.DEC_UINT.apply(LMAX), "%s", 0xffffffffL);
		assertString(Format.UDEC_HEX.apply(0L), "0");
		assertString(Format.UDEC_OR_HEX.apply(0L), "0");
	}

	@Test
	public void testRound() {
		assertString(Format.ROUND.apply(DNINF), "" + LMIN);
		assertString(Format.ROUND.apply(DPINF), "" + LMAX);
		assertString(Format.ROUND.apply(Double.NaN), "0");
		assertString(Format.ROUND.apply(0), "0");
		assertString(Format.ROUND.apply(1000000000.5), "1000000001");
		assertString(Format.ROUND.apply(1000000000.4), "1000000000");
		assertString(Format.ROUND.apply(-1000000000.5), "-1000000000");
		assertString(Format.ROUND.apply(-1000000000.6), "-1000000001");
	}

	@Test
	public void testHex() {
		assertString(Format.HEX.ubyte(null), "null");
		assertString(Format.HEX.ubyte(BMIN), "0x80");
		assertString(Format.HEX.ubyte(BMAX), "0x7f");
		assertString(Format.HEX.ubyte(123.5), "0x7b");
		assertString(Format.HEX.ushort(null), "null");
		assertString(Format.HEX.ushort(SMIN), "0x8000");
		assertString(Format.HEX.ushort(SMAX), "0x7fff");
		assertString(Format.HEX.ushort(123.5), "0x7b");
		assertString(Format.HEX.uint(null), "null");
		assertString(Format.HEX.uint(IMIN), "0x80000000");
		assertString(Format.HEX.uint(IMAX), "0x7fffffff");
		assertString(Format.HEX.uint(123.5), "0x7b");
		assertString(Format.HEX.apply(null), "null");
		assertString(Format.HEX.apply(LMIN), "0x8000000000000000");
		assertString(Format.HEX.apply(LMAX), "0x7fffffffffffffff");
		assertString(Format.HEX.apply(123.5), "0x7b");
	}

	@Test
	public void testFormatDouble() {
		assertString(Format.format(1.0, 0, 3), "1");
		assertString(Format.format(0.0, 0, 3), "0");
	}

	@Test
	public void testSeparators() {
		assertString(Format.Separator.NONE.accept(0, 0), null);
		assertString(Format.Separator._4.accept(3, 5), null);
		assertString(Format.Separator._4.accept(4, 5), "_");
		assertString(Format.Separator._4.accept(5, 5), null);
	}

	@Test
	public void testSeparator() {
		assertSame(Format.Separator.of(0, ":"), Format.Separator.NONE);
		assertSame(Format.Separator.of(null, ":"), Format.Separator.NONE);
		assertSame(Format.Separator.of(2, null), Format.Separator.NONE);
		var s1 = Format.Separator.of(1, ":");
		var s2 = Format.Separator.of(2, ":");
		assertString(s1.accept(0, 0), null);
		assertString(Format.format(1234, Radix.DEC, 2, 4, s1), "1:2:3:4");
		assertString(Format.format(0x1234, Radix.HEX, 2, 4, s1), "0x1:2:3:4");
		assertString(Format.format(0x1234, Radix.HEX, 2, 4, s2), "0x12:34");
	}

	@Test
	public void testHexExactDigits() {
		assertString(Format.hex(0x12345, 4), "0x2345");
	}

	@Test
	public void testSignedHex() {
		assertString(Format.format(-1, false, "", 10, 2, 4), "-01");
		assertString(Format.format(-11111, false, "", 10, 2, 4), "-1111");
	}

	@Test
	public void testUnsignedDecAndHex() {
		assertString(Format.udecHex(0), "0");
		assertString(Format.udecHex(9), "9");
		assertString(Format.udecHex(15), "15|0xf");
		assertString(Format.udecHex(LMIN), "%s|0x8000000000000000", Long.toUnsignedString(LMIN));
		assertString(Format.udecHex(-1), "%s|0xffffffffffffffff", Long.toUnsignedString(-1));
	}

	@Test
	public void testUnsignedDecOrHex() {
		assertString(Format.udecOrHex(0), "0");
		assertString(Format.udecOrHex(9), "9");
		assertString(Format.udecOrHex(15), "0xf");
		assertString(Format.udecOrHex(-1), "0xffffffffffffffff");
	}

	@Test
	public void testBin() {
		assertString(Format.bin(0x59), "0b1011001");
		assertString(Format.bin(0x59, 6), "0b011001");
	}

	@Test
	public void testFormatDoubleWithDecimalPlaces() {
		assertString(Format.format(1.1, 2, 4), "1.10");
		assertString(Format.format(1.1, 2, 0), "1.10");
		assertString(Format.format(1.12345, 2, 4), "1.1235");
		assertString(Format.format(1.12345, 2, 0), "1.12345");
		assertString(Format.format(DNINF, 2, 4), "-Infinity");
		assertString(Format.format(Double.NaN, 2, 4), "NaN");
	}

}
