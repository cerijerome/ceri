package ceri.common.test;

import java.io.PrintStream;
import ceri.common.array.Array;

/**
 * Visualizes binary signal streams.
 */
public class PulsePrinter {
	private final PrintStream out;
	private final char high;
	private final char low;
	private final int bitsPerLine;
	private final boolean lsbFirst;
	private volatile int bits = 0;

	public static PulsePrinter of() {
		return builder().build();
	}

	public static PulsePrinter ofBits(int bitsPerLine) {
		return builder().bitsPerLine(bitsPerLine).build();
	}

	public static PulsePrinter ofBytes(int bytesPerLine) {
		return builder().bytesPerLine(bytesPerLine).build();
	}

	public static class Builder {
		char high = '\u2587';
		char low = '\u2581';
		PrintStream out = System.out;
		int bitsPerLine = 0;
		boolean lsbFirst = false;

		Builder() {}

		public Builder out(PrintStream out) {
			this.out = out;
			return this;
		}

		public Builder high(char high) {
			this.high = high;
			return this;
		}

		public Builder low(char low) {
			this.low = low;
			return this;
		}

		public Builder bitsPerLine(int bitsPerLine) {
			this.bitsPerLine = bitsPerLine;
			return this;
		}

		public Builder bytesPerLine(int bytesPerLine) {
			return bitsPerLine(bytesPerLine * Byte.SIZE);
		}

		public Builder lsbFirst() {
			lsbFirst = true;
			return this;
		}

		public PulsePrinter build() {
			return new PulsePrinter(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	PulsePrinter(Builder builder) {
		out = builder.out;
		high = builder.high;
		low = builder.low;
		bitsPerLine = builder.bitsPerLine;
		lsbFirst = builder.lsbFirst;
	}

	public PulsePrinter print(boolean bit) {
		out.print(bit ? high : low);
		if (bitsPerLine <= 0 || ++bits < bitsPerLine) return this;
		return newLine();
	}

	public PulsePrinter newLine() {
		bits = 0;
		out.println();
		return this;
	}

	public PulsePrinter print(int... bytes) {
		return print(Array.BYTE.of(bytes));
	}

	public PulsePrinter print(byte[] bytes) {
		return print(bytes, 0);
	}

	public PulsePrinter print(byte[] bytes, int offset) {
		return print(bytes, offset, bytes.length - offset);
	}

	public PulsePrinter print(byte[] bytes, int offset, int len) {
		for (int i = 0; i < len; i++)
			printByte(bytes[offset + i]);
		return this;
	}

	public PulsePrinter printBits(int value, int bits) {
		for (int i = 0; i < bits; i++)
			printBit(value, bits, i);
		return this;
	}

	public PulsePrinter printBit(int value, int bit) {
		return printBit(value, Byte.SIZE, bit);
	}

	public PulsePrinter printBit(int value, int bits, int bit) {
		return print(high(value, bits, bit));
	}

	private PulsePrinter printByte(int b) {
		printBits(b, Byte.SIZE);
		return this;
	}

	private boolean high(int value, int bits, int bit) {
		if (!lsbFirst) bit = bits - bit - 1;
		return (value & (1 << bit)) != 0;
	}

}
