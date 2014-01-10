package ceri.common.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Pretty-prints binary data.
 */
public class HexPrinter {
	private static final int BUFFER_SIZE = 32 * 1024;
	private static final int COLS_DEF = 4;
	private static final int COL_BYTES = 8;
	private static final int ASCII_MIN = '!';
	private static final int ASCII_MAX = '~';
	private final PrintStream out;
	private final int columns;
	private final boolean colSpace;

	/**
	 * Constructor that prints to stdout with 4 columns and a space between each column.
	 */
	public HexPrinter() {
		this(System.out, COLS_DEF, true);
	}

	/**
	 * Constructor that prints to the given PrintStream. columns specify the
	 * number 8-byte columns, colSpace determines whether a space is placed
	 * between columns.
	 */
	public HexPrinter(PrintStream out, int columns, boolean colSpace) {
		this.out = out;
		this.columns = columns;
		this.colSpace = colSpace;
	}

	/**
	 * Hex-print data from the given input stream.
	 */
	public void print(InputStream in) throws IOException {
		print(in, 0);
	}

	/**
	 * Hex-print data from the given input stream up to given number of bytes.
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
	 * Hex-print binary data.
	 */
	public void print(byte[] bytes) {
		print(bytes, 0, bytes.length);
	}

	/**
	 * Hex-print binary data from given offset with given length.
	 */
	public void print(byte[] bytes, int off, int len) {
		StringBuilder hexB = new StringBuilder();
		StringBuilder charB = new StringBuilder();
		int rowLen = COL_BYTES * columns;
		for (int i = 0; i < len; i += rowLen) {
			hexB.setLength(0);
			charB.setLength(0);
			for (int j = 0; j < rowLen; j++) {
				if (colSpace && j > 0 && j % COL_BYTES == 0) {
					hexB.append(' ');
					charB.append(' ');
				}
				if (i + j >= len) {
					hexB.append("  ");
					charB.append(' ');
				} else {
					int b = 0xff & bytes[off + i + j];
					String s = Integer.toHexString(b).toUpperCase();
					if (s.length() == 1) hexB.append('0');
					hexB.append(s);
					if (b >= ASCII_MIN && b <= ASCII_MAX) charB.append((char) b);
					else charB.append('.');
				}
				hexB.append(' ');
			}
			out.print(hexB.toString());
			out.print(' ');
			out.print(charB.toString());
			out.println();
		}
	}

}
