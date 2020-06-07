package ceri.serial.mlx90640.display;

import java.io.PrintStream;

/**
 * Outputs an animated ASCII-grayscale terminal display of frames.
 */
public class AsciiDisplay extends TerminalDisplay {
	public static final String COURIER_GRAYSCALE =
		"@WMB#80Q&$%bdpOmqUXZkawho*CYJIunx1zfjtLv{}c[]?i()l<>|/\\r+;!~\"^:_-,'.` ";
	public static final String COURIER_GRAYSCALE_COMPACT =
		"@WMB#80Q&$%bOmqUXZkawho*CYJIunx1zfjtLv{c[?i(l<|/r+;!~\"^:_-,'.` ";
	private final String ascii;

	public static AsciiDisplay of(String ascii) {
		return builder().ascii(ascii).build();
	}

	public static class Builder extends TerminalDisplay.Builder {
		String ascii = COURIER_GRAYSCALE_COMPACT;

		Builder() {}

		public Builder ascii(String ascii) {
			this.ascii = ascii;
			return this;
		}

		@Override
		public Builder out(PrintStream out) {
			super.out(out);
			return this;
		}

		@Override
		public Builder min(double min) {
			super.min(min);
			return this;
		}

		@Override
		public Builder max(double max) {
			super.max(max);
			return this;
		}

		@Override
		public AsciiDisplay build() {
			return new AsciiDisplay(this);
		}
	}

	public static Builder builder() {
		return new Builder();
	}

	AsciiDisplay(Builder builder) {
		super(builder);
		ascii = builder.ascii;
	}

	@Override
	protected void printPixel(int row, int column, double value, double min, double max) {
		double ratio = ratio(value, min, max);
		int index = (int) Math.min(ratio * ascii.length(), ascii.length() - 1);
		char c = ascii.charAt(index);
		out.print(c);
		out.print(c);
	}

}
