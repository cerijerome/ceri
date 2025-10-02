package ceri.common.text;

import static ceri.common.test.AssertUtil.assertString;
import org.junit.Test;
import ceri.common.math.Maths;

public class FormatTest {

	@Test
	public void testHex() {
		assertString(Format.HEX.ubyte(Byte.MIN_VALUE), "0x80");
		assertString(Format.HEX.ushort(Short.MIN_VALUE), "0x8000");
		assertString(Format.HEX.uint(Integer.MIN_VALUE), "0x80000000");
		assertString(Format.HEX.apply(Long.MIN_VALUE), "0x8000000000000000");
	}

	@Test
	public void testFormatDouble() {
		assertString(Format.format(1.0, 0, 3), "1");
		assertString(Format.format(0.0, 0, 3), "0");
	}
	
	public static void main0(String[] args) {
		var fmt = new Format.OfLong(true, "#", 16, 0, 8, Format.Separator._4);
		System.out.println(fmt);
		System.out.println();
		print(fmt, Long.MIN_VALUE, "Long.MIN_VALUE");
		print(fmt, Long.MIN_VALUE + 1, "Long.MIN_VALUE + 1");
		print(fmt, Integer.MIN_VALUE, "Integer.MIN_VALUE");
		print(fmt, Integer.MIN_VALUE + 1, "Integer.MIN_VALUE + 1");
		print(fmt, Short.MIN_VALUE, "Short.MIN_VALUE");
		print(fmt, Short.MIN_VALUE + 1, "Short.MIN_VALUE + 1");
		print(fmt, Byte.MIN_VALUE, "Byte.MIN_VALUE");
		print(fmt, Byte.MIN_VALUE + 1, "Byte.MIN_VALUE + 1");
		print(fmt, -1, "");
		print(fmt, 0, "");
		print(fmt, 1, "");
		print(fmt, Byte.MAX_VALUE - 1, "Byte.MAX_VALUE - 1");
		print(fmt, Byte.MAX_VALUE, "Byte.MAX_VALUE");
		print(fmt, Short.MAX_VALUE - 1, "Short.MAX_VALUE - 1");
		print(fmt, Short.MAX_VALUE, "Short.MAX_VALUE");
		print(fmt, Integer.MAX_VALUE - 1, "Integer.MAX_VALUE - 1");
		print(fmt, Integer.MAX_VALUE, "Integer.MAX_VALUE");
		print(fmt, Long.MAX_VALUE - 1, "Long.MAX_VALUE - 1");
		print(fmt, Long.MAX_VALUE, "Long.MAX_VALUE");
	}

	private static void print(Format.OfLong fmt, long value, String desc) {
		p(fmt, value, desc);
		p(fmt, Maths.ubyte(value), "ubyte");
		p(fmt, Maths.ushort(value), "ushort");
		p(fmt, Maths.uint(value), "uint");
		System.out.println();
	}

	private static void p(Format.OfLong fmt, long value, String desc) {
		System.out.println(fmt.apply(value) + " \t" + desc + " \t" + value);
	}
}
