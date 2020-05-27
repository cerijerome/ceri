package ceri.serial.mlx90640;

import static ceri.common.data.ByteUtil.bit;
import static ceri.common.text.StringUtil.SHORT_BINARY_DIGITS;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;

/**
 * Base class for read-only access to 16-bit data copied from MLX90640 device. The passed-in byte
 * array may be modified after creation, and changes will appear here.
 */
public class MlxBuffer {
	private static final int PRINT_WORDS_PER_LINE = 16;
	private static final int PRINT_GROUP_BITS = 4;
	private final ShortBuffer buffer;

	protected MlxBuffer(byte[] data, int offset, int length) {
		// ShortBuffer is msb-first by default.
		buffer = ByteBuffer.wrap(data, offset, length).asShortBuffer().asReadOnlyBuffer();
	}

	/* Buffer access */

	/**
	 * Returns unsigned 16-bit value.
	 */
	protected int uvalue(int index) {
		return MathUtil.ushort(value(index));
	}

	/**
	 * Returns signed 16-bit value.
	 */
	protected int value(int index) {
		return buffer.get(index);
	}

	/**
	 * Extracts signed value from bits by index and count.
	 */
	protected int bitsIndex(int index, int n, int bits) {
		index += (n * bits) / Short.SIZE;
		int offset = (n * bits) % Short.SIZE;
		return bits(index, offset, bits);
	}

	/**
	 * Extracts unsigned value from bits by index and count.
	 */
	protected int ubitsIndex(int index, int n, int bits) {
		index += (n * bits) / Short.SIZE;
		int offset = (n * bits) % Short.SIZE;
		return ubits(index, offset, bits);
	}

	/**
	 * Extract unsigned value from bits.
	 */
	protected int ubits(int index, int bit, int size) {
		int value = value(index) >>> bit;
		int mask = ByteUtil.maskInt(size);
		return value & mask;
	}

	/**
	 * Extract signed value from bits.
	 */
	protected int bits(int index, int bit, int size) {
		int value = value(index) >>> bit;
		int mask = ByteUtil.maskInt(size);
		return ByteUtil.bit(value, size - 1) ? value | ~mask : value & mask;
	}

	/* Debug support */

	/**
	 * Prints 16-bit values in 16 columns.
	 */
	protected void printWords(PrintStream out, int index, int length) {
		out.print("Address");
		for (int i = 0; i < Math.min(length, PRINT_WORDS_PER_LINE); i++)
			out.printf("   %x ", i);
		for (int i = 0; i < length; i++, index++) {
			if (i % PRINT_WORDS_PER_LINE == 0) out.printf("%n0x%04x:", index);
			out.printf(" %04x", value(index));
		}
		out.println();
	}

	/**
	 * Prints 16-bit value and bits per line.
	 */
	protected void printBits(PrintStream out, int index, int length) {
		out.print("Address  word  ");
		for (int i = SHORT_BINARY_DIGITS - 1; i >= 0; i--)
			out.printf(" %2s%s", i, separator(i));
		for (int i = 0; i < length; i++, index++) {
			int value = value(index);
			out.printf("%n0x%04x:  %04x  ", index, value);
			for (int j = SHORT_BINARY_DIGITS - 1; j >= 0; j--)
				out.printf(" %2s%s", bit(value, j) ? 1 : 0, separator(j));
		}
		out.println();
	}

	private static String separator(int i) {
		return i > 0 && i % PRINT_GROUP_BITS == 0 ? "  " : "";
	}

}
