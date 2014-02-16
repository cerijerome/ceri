package ceri.common.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import ceri.common.util.StringUtil;
import ceri.common.util.ToStringHelper;

/**
 * Pretty-prints data in binary, hex and/or char format. e.g.
 * 
 * <pre>
 * 00101111 00000000 01000011 10100000  2F 00 43 A0  /.C.
 * </pre>
 * 
 * for 4 bytes per column, and 1 column. Unprintable ascii chars will show as "."
 */
public class BinaryPrinter {
	private static final int BUFFER_SIZE = 32 * 1024;
	private static final int BITS_IN_BYTE = 8;
	private static final int ASCII_MIN = '!';
	private static final int ASCII_MAX = '~';
	private final PrintStream out;
	private final int bytesPerColumn;
	private final int columns;
	private final boolean showBinary;
	private final boolean showHex;
	private final boolean showChar;

	public static class Builder {
		PrintStream out = System.out;
		int bytesPerColumn = 8;
		int columns = 1;
		boolean showBinary = true;
		boolean showHex = true;
		boolean showChar = true;

		Builder() {}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public Builder bytesPerColumn(int bytesPerColumn) {
			this.bytesPerColumn = bytesPerColumn;
			return this;
		}

		public Builder columns(int columns) {
			this.columns = columns;
			return this;
		}

		public Builder showBinary(boolean showBinary) {
			this.showBinary = showBinary;
			return this;
		}

		public Builder showHex(boolean showHex) {
			this.showHex = showHex;
			return this;
		}

		public Builder showChar(boolean showChar) {
			this.showChar = showChar;
			return this;
		}

		public BinaryPrinter build() {
			return new BinaryPrinter(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	BinaryPrinter(Builder builder) {
		out = builder.out;
		bytesPerColumn = builder.bytesPerColumn;
		columns = builder.columns;
		showBinary = builder.showBinary;
		showHex = builder.showHex;
		showChar = builder.showChar;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this,
			out == null ? null : out.getClass().getSimpleName(), bytesPerColumn, columns,
			showBinary, showHex, showChar).toString();
	}

	/**
	 * Constructor that prints to stdout with 4 columns and a space between each column.
	 */
	public BinaryPrinter() {
		this(new Builder());
	}

	/**
	 * Print data from the given input stream.
	 */
	public void print(InputStream in) throws IOException {
		print(in, 0);
	}

	/**
	 * Print data from the given input stream up to given number of bytes.
	 */
	public void print(InputStream in, long len) throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];
		long n = 0;
		while (true) {
			int readLen = buffer.length;
			if (len > 0 && len - n < buffer.length) readLen = (int) (len - n);
			if (readLen == 0) break;
			int count = in.read(buffer, 0, readLen);
			if (count == -1) break;
			print(buffer, 0, count);
			n += count;
		}
	}

	/**
	 * Print binary data.
	 */
	public void print(byte[] bytes) {
		print(bytes, 0, bytes.length);
	}

	/**
	 * Print binary data from given offset with given length.
	 */
	public void print(byte[] bytes, int off, int len) {
		StringBuilder binB = new StringBuilder();
		StringBuilder hexB = new StringBuilder();
		StringBuilder charB = new StringBuilder();
		int rowLen = bytesPerColumn * columns;
		for (int i = 0; i < len; i += rowLen) {
			binB.setLength(0);
			hexB.setLength(0);
			charB.setLength(0);
			for (int j = 0; j < rowLen; j++) {
				if (j > 0 && j % bytesPerColumn == 0) {
					binB.append(' ');
					hexB.append(' ');
					charB.append(' ');
				}
				if (i + j >= len) {
					binB.append("        ");
					hexB.append("  ");
					charB.append(' ');
				} else {
					int b = 0xff & bytes[off + i + j];
					String s =
						StringUtil.pad(Integer.toBinaryString(b), BITS_IN_BYTE, "0",
							StringUtil.Align.RIGHT);
					binB.append(s);
					s = Integer.toHexString(b).toUpperCase();
					if (s.length() == 1) hexB.append('0');
					hexB.append(s);
					if (b >= ASCII_MIN && b <= ASCII_MAX) charB.append((char) b);
					else charB.append('.');
				}
				binB.append(' ');
				hexB.append(' ');
			}
			if (showBinary) out.print(binB.append(' ').toString());
			if (showHex) out.print(hexB.append(' ').toString());
			if (showChar) out.print(charB.toString());
			out.println();
		}
	}

}
