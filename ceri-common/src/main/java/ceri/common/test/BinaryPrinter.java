package ceri.common.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.text.StringUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.Align;

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
	public static final BinaryPrinter DEFAULT = builder().build();
	public static final BinaryPrinter ASCII =
		builder().showBinary(false).bytesPerColumn(16).printableSpace(true).build();
	private static final int ASCII_MIN = '!';
	private static final int ASCII_MAX = '~';
	private final PrintStream out;
	private final int bufferSize;
	private final int bytesPerColumn;
	private final int columns;
	private final boolean showBinary;
	private final boolean showHex;
	private final boolean showChar;
	private final boolean printableSpace;

	public static class Builder {
		PrintStream out = System.out;
		int bufferSize = 32 * 1024;
		int bytesPerColumn = 8;
		int columns = 1;
		boolean showBinary = true;
		boolean showHex = true;
		boolean showChar = true;
		boolean printableSpace = false;

		Builder() {}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public Builder printableSpace(boolean printableSpace) {
			this.printableSpace = printableSpace;
			return this;
		}

		public Builder bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
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

	public static Builder builder(BinaryPrinter printer) {
		return new Builder().out(printer.out).bufferSize(printer.bufferSize)
			.bytesPerColumn(printer.bytesPerColumn).columns(printer.columns)
			.showBinary(printer.showBinary).showHex(printer.showHex).showChar(printer.showChar)
			.printableSpace(printer.printableSpace);
	}

	BinaryPrinter(Builder builder) {
		out = builder.out;
		bufferSize = builder.bufferSize;
		bytesPerColumn = builder.bytesPerColumn;
		columns = builder.columns;
		showBinary = builder.showBinary;
		showHex = builder.showHex;
		showChar = builder.showChar;
		printableSpace = builder.printableSpace;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, //
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
	 * Print string as unicode code points.
	 */
	public BinaryPrinter print(String s) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		s.codePoints().forEach(cp -> ByteUtil.writeTo(out, ByteUtil.toBigEndian((short) cp)));
		return print(out.toByteArray());
	}

	/**
	 * Print data from the given input stream.
	 */
	public BinaryPrinter print(InputStream in) throws IOException {
		return print(in, 0);
	}

	/**
	 * Print data from the given input stream up to given number of bytes.
	 */
	public BinaryPrinter print(InputStream in, long len) throws IOException {
		byte[] buffer = new byte[bufferSize];
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
		return this;
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(ByteProvider data) {
		return print(data, 0);
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(ByteProvider data, int offset) {
		return print(data, offset, data.length() - offset);
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(ByteProvider data, int offset, int length) {
		return print(data.copy(), offset, length);
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(ByteBuffer buffer) {
		return print(buffer, buffer.remaining());
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(ByteBuffer buffer, int length) {
		byte[] b = new byte[length];
		buffer.get(b);
		return print(b);
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(byte[] bytes) {
		return print(bytes, 0);
	}

	/**
	 * Print binary data.
	 */
	public BinaryPrinter print(byte[] bytes, int offset) {
		return print(bytes, offset, bytes.length - offset);
	}

	/**
	 * Print binary data from given offset with given length.
	 */
	public BinaryPrinter print(byte[] bytes, int offset, int length) {
		StringBuilder binB = new StringBuilder();
		StringBuilder hexB = new StringBuilder();
		StringBuilder charB = new StringBuilder();
		int rowLen = bytesPerColumn * columns;
		for (int i = 0; i < length; i += rowLen) {
			resetBuffers(binB, hexB, charB);
			for (int j = 0; j < rowLen; j++) {
				if (j > 0 && j % bytesPerColumn == 0) appendColumnSpace(binB, hexB, charB);
				if (i + j >= length) appendMissingItemSpace(binB, hexB, charB);
				else {
					int b = 0xff & bytes[offset + i + j];
					appendByte(binB, hexB, charB, b);
				}
				appendItemSpace(binB, hexB);
			}
			if (showBinary) out.print(binB.append(' ').toString());
			if (showHex) out.print(hexB.append(' ').toString());
			if (showChar) out.print(charB.toString());
			out.println();
		}
		return this;
	}

	public BinaryPrinter flush() {
		out.flush();
		return this;
	}

	private void appendItemSpace(StringBuilder binB, StringBuilder hexB) {
		binB.append(' ');
		hexB.append(' ');
	}

	private void appendByte(StringBuilder binB, StringBuilder hexB, StringBuilder charB, int b) {
		String s = StringUtil.pad(Integer.toBinaryString(b), Byte.SIZE, "0", Align.H.right);
		binB.append(s);
		s = Integer.toHexString(b).toUpperCase();
		if (s.length() == 1) hexB.append('0');
		hexB.append(s);
		boolean printable = (b >= ASCII_MIN && b <= ASCII_MAX) || (printableSpace && b == ' ');
		charB.append(printable ? (char) b : '.');
	}

	private void resetBuffers(StringBuilder binB, StringBuilder hexB, StringBuilder charB) {
		binB.setLength(0);
		hexB.setLength(0);
		charB.setLength(0);
	}

	private void appendMissingItemSpace(StringBuilder binB, StringBuilder hexB,
		StringBuilder charB) {
		binB.append("        ");
		hexB.append("  ");
		charB.append(' ');
	}

	private void appendColumnSpace(StringBuilder binB, StringBuilder hexB, StringBuilder charB) {
		appendItemSpace(binB, hexB);
		charB.append(' ');
	}

}
