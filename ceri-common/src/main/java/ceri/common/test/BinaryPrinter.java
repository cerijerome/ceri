package ceri.common.test;

import static ceri.common.reflect.Reflect.className;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.text.StringUtil;
import ceri.common.text.ToString;
import ceri.common.util.Align;

/**
 * Pretty-prints data in binary, hex and/or char format. e.g.
 *
 * <pre>
 * 00101111 00000000 01000011 10100000  2F 00 43 A0  /.C.
 * </pre>
 *
 * for 4 bytes per column, and 1 column. Unprintable ascii chars will show as "." by default.
 */
public class BinaryPrinter {
	public static final BinaryPrinter STD = builder().build();
	public static final BinaryPrinter ASCII =
		builder().showBinary(false).bytesPerColumn(16).printableSpace(true).build();
	private static final int ASCII_MIN = '!';
	private static final int ASCII_MAX = '~';
	private final Supplier<PrintStream> outSupplier; // works with SystemIo overrides
	private final int bufferSize;
	private final int bytesPerColumn;
	private final int columns;
	private final boolean columnSpace;
	private final boolean showBinary;
	private final boolean showHex;
	private final boolean showChar;
	private final boolean upper;
	private final boolean printableSpace;
	private final char unprintable;

	public static class Builder {
		Supplier<PrintStream> outSupplier = () -> System.out;
		int bufferSize = 32 * 1024;
		int bytesPerColumn = 8;
		int columns = 1;
		boolean columnSpace = true;
		boolean showBinary = true;
		boolean showHex = true;
		boolean showChar = true;
		boolean upper = false;
		boolean printableSpace = false;
		char unprintable = '.';

		Builder() {}

		public Builder out(Supplier<PrintStream> outSupplier) {
			this.outSupplier = outSupplier;
			return this;
		}

		public Builder out(PrintStream out) {
			return out(() -> out);
		}

		public Builder bufferSize(int bufferSize) {
			this.bufferSize = bufferSize;
			return this;
		}

		/**
		 * Specify how many bytes in a column.
		 */
		public Builder bytesPerColumn(int bytesPerColumn) {
			this.bytesPerColumn = bytesPerColumn;
			return this;
		}

		/**
		 * Specify how many columns per line. Applies to each shown type binary/hex/char.
		 */
		public Builder columns(int columns) {
			this.columns = columns;
			return this;
		}

		/**
		 * Specify whether to show a space between columns.
		 */
		public Builder columnSpace(boolean columnSpace) {
			this.columnSpace = columnSpace;
			return this;
		}

		/**
		 * Specify whether to show 8-digit binary per byte.
		 */
		public Builder showBinary(boolean showBinary) {
			this.showBinary = showBinary;
			return this;
		}

		/**
		 * Specify whether to show 2-digit hex per byte.
		 */
		public Builder showHex(boolean showHex) {
			this.showHex = showHex;
			return this;
		}

		/**
		 * Specify whether to show ascii per byte.
		 */
		public Builder showChar(boolean showChar) {
			this.showChar = showChar;
			return this;
		}

		/**
		 * Determines if hex digits are printed as upper-case.
		 */
		public Builder upper(boolean upper) {
			this.upper = upper;
			return this;
		}

		/**
		 * Determines if ascii-32 is printed as a space or as unprintable char.
		 */
		public Builder printableSpace(boolean printableSpace) {
			this.printableSpace = printableSpace;
			return this;
		}

		/**
		 * Sets the character that appears for unprintable bytes.
		 */
		public Builder unprintable(char unprintable) {
			this.unprintable = unprintable;
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
		return new Builder().out(printer.outSupplier).bufferSize(printer.bufferSize)
			.bytesPerColumn(printer.bytesPerColumn).columns(printer.columns)
			.showBinary(printer.showBinary).showHex(printer.showHex).showChar(printer.showChar)
			.printableSpace(printer.printableSpace).unprintable(printer.unprintable);
	}

	BinaryPrinter(Builder builder) {
		outSupplier = builder.outSupplier;
		bufferSize = builder.bufferSize;
		bytesPerColumn = builder.bytesPerColumn;
		columns = builder.columns;
		columnSpace = builder.columnSpace;
		showBinary = builder.showBinary;
		showHex = builder.showHex;
		showChar = builder.showChar;
		upper = builder.upper;
		printableSpace = builder.printableSpace;
		unprintable = builder.unprintable;
	}

	@SuppressWarnings("resource")
	@Override
	public String toString() {
		return ToString.forClass(this, className(out()), bufferSize, bytesPerColumn, columns,
			columnSpace, showBinary, showHex, showChar, upper, printableSpace, unprintable);
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
	public BinaryPrinter printCodePoints(String s) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		s.codePoints().forEach(cp -> ByteUtil.writeTo(out, ByteUtil.toMsb((short) cp)));
		return print(out.toByteArray());
	}

	/**
	 * Print string as Latin-1 bytes.
	 */
	public BinaryPrinter printAscii(String s) {
		return print(s.getBytes(StandardCharsets.ISO_8859_1));
	}

	/**
	 * Print string as UTF8 bytes.
	 */
	public BinaryPrinter print(String s) {
		return print(s.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Print data from the given input stream.
	 */
	public BinaryPrinter print(InputStream in) throws IOException {
		return print(in, in.available());
	}

	/**
	 * Print data from the given input stream up to given number of bytes.
	 */
	public BinaryPrinter print(InputStream in, int len) throws IOException {
		print(in.readNBytes(len));
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
		return print(data.copy(0), offset, length);
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
	public BinaryPrinter print(int... bytes) {
		return print(ArrayUtil.bytes.of(bytes));
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
	@SuppressWarnings("resource")
	public BinaryPrinter print(byte[] bytes, int offset, int length) {
		var out = out();
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

	@SuppressWarnings("resource")
	public BinaryPrinter flush() {
		out().flush();
		return this;
	}

	private PrintStream out() {
		return outSupplier.get();
	}

	private void appendItemSpace(StringBuilder binB, StringBuilder hexB) {
		if (!columnSpace) return;
		binB.append(' ');
		hexB.append(' ');
	}

	private void appendByte(StringBuilder binB, StringBuilder hexB, StringBuilder charB, int b) {
		String s = StringUtil.pad(Integer.toBinaryString(b), Byte.SIZE, "0", Align.H.right);
		binB.append(s);
		s = Integer.toHexString(b);
		if (s.length() == 1) hexB.append('0');
		hexB.append(upper ? s.toUpperCase() : s);
		boolean printable = (b >= ASCII_MIN && b <= ASCII_MAX) || (printableSpace && b == ' ');
		charB.append(printable ? (char) b : unprintable);
	}

	private void resetBuffers(StringBuilder binB, StringBuilder hexB, StringBuilder charB) {
		StringUtil.clear(binB);
		StringUtil.clear(hexB);
		StringUtil.clear(charB);
	}

	private void appendMissingItemSpace(StringBuilder binB, StringBuilder hexB,
		StringBuilder charB) {
		binB.append("        ");
		hexB.append("  ");
		charB.append(' ');
	}

	private void appendColumnSpace(StringBuilder binB, StringBuilder hexB, StringBuilder charB) {
		binB.append(' ');
		hexB.append(' ');
		charB.append(' ');
	}

}
