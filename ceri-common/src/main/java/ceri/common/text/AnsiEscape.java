package ceri.common.text;

import static ceri.common.color.ColorUtil.CHANNEL_MAX;
import static ceri.common.text.StringUtil.ESC;
import static ceri.common.validation.ValidationUtil.validateMin;
import static ceri.common.validation.ValidationUtil.validateRange;
import java.awt.Color;
import ceri.common.color.ColorUtil;

/**
 * ANSI-escape codes for terminal operations and formatting.
 * https://en.wikipedia.org/wiki/ANSI_escape_code
 */
public class AnsiEscape {
	public static final C1 singleShift2 = new BaseC1('N');
	public static final C1 singleShift3 = new BaseC1('O');
	public static final C1 deviceControl = new BaseC1('P');
	public static final C1 start = new BaseC1('X');
	public static final Csi csi = new Csi();
	public static final C1 terminator = new BaseC1('\\');
	public static final C1 osCommand = new BaseC1(']');
	public static final C1 privacyMessage = new BaseC1('^');
	public static final C1 appCommand = new BaseC1('_');
	public static final String reset = Escaper.escape('c');

	public interface C1 {
		byte c1();
	}

	private static class BaseC1 implements C1 {
		private final byte c1;
		private final String escape;

		private BaseC1(char prefix) {
			this.c1 = (byte) (prefix + 0x40);
			this.escape = Escaper.escape(prefix);
		}

		@Override
		public byte c1() {
			return c1;
		}

		@Override
		public String toString() {
			return escape;
		}
	}

	public static class Csi extends BaseC1 {
		private static final char PREFIX = '[';
		public static String deviceStatus = escape('n', 0, 6);

		private Csi() {
			super(PREFIX);
		}

		public String cursorUp(int n) {
			if (n < 0) return cursorDown(-n);
			return n == 0 ? "" : escape('A', 1, n);
		}

		public String cursorDown(int n) {
			if (n < 0) return cursorUp(-n);
			return n == 0 ? "" : escape('B', 1, n);
		}

		public String cursorForward(int n) {
			if (n < 0) return cursorBack(-n);
			return n == 0 ? "" : escape('C', 1, n);
		}

		public String cursorBack(int n) {
			if (n < 0) return cursorForward(-n);
			return n == 0 ? "" : escape('D', 1, n);
		}

		public String cursorNextLine(int n) {
			if (n < 0) return cursorPrevLine(-n);
			return n == 0 ? "" : escape('E', 1, n);
		}

		public String cursorPrevLine(int n) {
			if (n < 0) return cursorNextLine(-n);
			return n == 0 ? "" : escape('F', 1, n);
		}

		public String cursorColumn(int n) {
			validateMin(n, 1);
			return escape('G', 1, n);
		}

		public String cursorPosition(int row, int col) {
			validateMin(row, 1);
			validateMin(col, 1);
			if (col == 1) return escape('H', 1, row);
			return escape('H', 1, row, col);
		}

		/**
		 * Clears part of the screen: 0 = cursor to end, 1 = beginning to cursor, 2 = screen, 3 =
		 * screen and buffer.
		 */
		public String eraseInDisplay(int n) {
			validateMin(n, 0);
			return escape('J', 0, n);
		}

		/**
		 * Clears part of the line: 0 = cursor to end, 1 = beginning to cursor, 2 = all.
		 */
		public String eraseInLine(int n) {
			validateMin(n, 0);
			return escape('K', 0, n);
		}

		public String scrollUp(int n) {
			if (n < 0) return scrollDown(-n);
			return n == 0 ? "" : escape('S', 1, n);
		}

		public String scrollDown(int n) {
			if (n < 0) return scrollUp(-n);
			return n == 0 ? "" : escape('T', 1, n);
		}

		public String hvPosition(int row, int col) {
			validateMin(row, 1);
			validateMin(col, 1);
			if (col == 1) return escape('f', 1, row);
			return escape('f', 1, row, col);
		}

		public Sgr sgr() {
			return new Sgr();
		}

		public String auxPort(boolean on) {
			return escape('i', 0, on ? 5 : 4);
		}

		private static String escape(char suffix, int blankCode, int... codes) {
			return Escaper.escape(PREFIX, suffix, ';', blankCode, codes);
		}

		public static class Sgr extends Escaper {

			public static enum BasicColor {
				black(0),
				red(1),
				green(2),
				yellow(3),
				blue(4),
				magenta(5),
				cyan(6),
				white(7);

				final int offset;

				private BasicColor(int offset) {
					this.offset = offset;
				}
			}

			private Sgr() {
				super(Csi.PREFIX, 'm', ';', 0);
			}

			public Sgr reset() {
				return add(0);
			}

			/**
			 * Set text intensity: -1 = faint, 0 = off, 1 = bold
			 */
			public Sgr intensity(int n) {
				validateRange(n, -1, 1);
				if (n == 1) return add(1);
				if (n == -1) return add(2);
				return add(22);
			}

			/**
			 * Set italics: 0 = off, 1 = italic, 2 = Fraktur
			 */
			public Sgr italic(int n) {
				validateRange(n, 0, 2);
				if (n == 1) return add(3);
				if (n == 2) return add(20);
				return add(23);
			}

			/**
			 * Set underline: 0 = off, 1 = regular, 2 = double
			 */
			public Sgr underline(int n) {
				validateRange(n, 0, 2);
				if (n == 1) return add(4);
				if (n == 2) return add(21);
				return add(24);
			}

			/**
			 * Set blink: 0 = off, 1 = slow, 2 = rapid
			 */
			public Sgr blink(int n) {
				validateRange(n, 0, 2);
				if (n == 1) return add(5);
				if (n == 2) return add(6);
				return add(25);
			}

			public Sgr reverse(boolean on) {
				return add(on ? 7 : 27);
			}

			public Sgr conceal(boolean on) {
				return add(on ? 8 : 28);
			}

			public Sgr strikeThrough(boolean on) {
				return add(on ? 9 : 29);
			}

			/**
			 * Set font: 0 = primary, 1-9 = alternative.
			 */
			public Sgr font(int font) {
				validateRange(font, 0, 9);
				return add(10 + font);
			}

			/**
			 * Set default foreground color.
			 */
			public Sgr fgColor() {
				return add(39);
			}

			public Sgr fgColor(BasicColor color, boolean bright) {
				if (color == null) return fgColor();
				return add((bright ? 90 : 30) + color.offset);
			}

			/**
			 * Set grey from 0 (black) to 23 (white).
			 */
			public Sgr fgGray(int level) {
				validateRange(level, 0, 23);
				return add(38, 5, 232 + level);
			}

			/**
			 * Set 8-bit color approximation, each component 0-5.
			 */
			public Sgr fgColor8(Color color) {
				return fgColor8(color.getRGB());
			}

			/**
			 * Set 8-bit color approximation, each component 0-5.
			 */
			public Sgr fgColor8(int rgb) {
				return fgColor8(to8Bit(ColorUtil.r(rgb)), to8Bit(ColorUtil.g(rgb)),
					to8Bit(ColorUtil.b(rgb)));
			}

			/**
			 * Set 8-bit color, each component 0-5.
			 */
			public Sgr fgColor8(int r, int g, int b) {
				validateRange(r, 0, 5);
				validateRange(g, 0, 5);
				validateRange(b, 0, 5);
				return add(38, 5, 16 + (r * 36) + (g * 6) + b);
			}

			/**
			 * Set 24-bit color, each component 0-255.
			 */
			public Sgr fgColor24(Color color) {
				return fgColor24(color.getRGB());
			}

			/**
			 * Set 24-bit color, each component 0-255.
			 */
			public Sgr fgColor24(int rgb) {
				return fgColor24(ColorUtil.r(rgb), ColorUtil.g(rgb), ColorUtil.b(rgb));
			}

			/**
			 * Set 24-bit color, each component 0-255.
			 */
			public Sgr fgColor24(int r, int g, int b) {
				validateRange(r, 0, CHANNEL_MAX);
				validateRange(g, 0, CHANNEL_MAX);
				validateRange(b, 0, CHANNEL_MAX);
				return add(38, 2, r, g, b);
			}

			/**
			 * Set default background color.
			 */
			public Sgr bgColor() {
				return add(49);
			}

			public Sgr bgColor(BasicColor color, boolean bright) {
				if (color == null) return bgColor();
				return add((bright ? 100 : 40) + color.offset);
			}

			/**
			 * Set grey from 0 (black) to 23 (white).
			 */
			public Sgr bgGray(int level) {
				validateRange(level, 0, 23);
				return add(48, 5, 232 + level);
			}

			/**
			 * Set 8-bit color approximation, each component 0-5.
			 */
			public Sgr bgColor8(Color color) {
				return bgColor8(color.getRGB());
			}

			/**
			 * Set 8-bit color approximation, each component 0-5.
			 */
			public Sgr bgColor8(int rgb) {
				return bgColor8(to8Bit(ColorUtil.r(rgb)), to8Bit(ColorUtil.g(rgb)),
					to8Bit(ColorUtil.b(rgb)));
			}

			/**
			 * Set 8-bit color, each component 0-5.
			 */
			public Sgr bgColor8(int r, int g, int b) {
				validateRange(r, 0, 5);
				validateRange(g, 0, 5);
				validateRange(b, 0, 5);
				return add(48, 5, 16 + (r * 36) + (g * 6) + b);
			}

			/**
			 * Set 24-bit color, each component 0-255.
			 */
			public Sgr bgColor24(Color color) {
				return bgColor24(color.getRGB());
			}

			/**
			 * Set 24-bit color, each component 0-255.
			 */
			public Sgr bgColor24(int rgb) {
				return bgColor24(ColorUtil.r(rgb), ColorUtil.g(rgb), ColorUtil.b(rgb));
			}

			/**
			 * Set 24-bit color, each component 0-255.
			 */
			public Sgr bgColor24(int r, int g, int b) {
				validateRange(r, 0, CHANNEL_MAX);
				validateRange(g, 0, CHANNEL_MAX);
				validateRange(b, 0, CHANNEL_MAX);
				return add(48, 2, r, g, b);
			}

			/**
			 * Set framing: 0 = off, 1 = rectangle, 2 = circle
			 */
			public Sgr frame(int n) {
				validateRange(n, 0, 2);
				if (n == 1) return add(51);
				if (n == 2) return add(52);
				return add(54);
			}

			public Sgr overline(boolean on) {
				return add(on ? 53 : 55);
			}

			/**
			 * Set ideogram format: 0 = off, 1 = underline, 2 = double underline, 3 = overline, 4 =
			 * double overline, 5 = stress marking
			 */
			public Sgr ideogram(int n) {
				validateRange(n, 0, 5);
				return add(n == 0 ? 65 : 59 + n);
			}

			private Sgr add(int... codes) {
				super.add(codes);
				return this;
			}

			private static int to8Bit(int c) {
				return (c * 6) >>> Byte.SIZE;
			}
		}
	}

	private static class Escaper {
		private final char suffix;
		private final Character separator; // separates multiple codes
		private final StringBuilder b;
		private final int blankCode; // if added code is this value, leave blank
		private boolean empty = true;

		public static String escape(char prefix) {
			return StringUtil.toString(ESC, prefix);
		}

		public static String escape(char prefix, char suffix, char separator, int blankCode,
			int... codes) {
			return new Escaper(prefix, suffix, separator, blankCode).add(codes).toString();
		}

		public Escaper(char prefix, char suffix, Character separator, int blankCode) {
			b = new StringBuilder().append(ESC).append(prefix);
			this.suffix = suffix;
			this.separator = separator;
			this.blankCode = blankCode;
		}

		private Escaper add(int... codes) {
			for (int code : codes) {
				separate();
				if (code != blankCode) b.append(code);
			}
			return this;
		}

		private void separate() {
			if (!empty) b.append(separator);
			empty = false;
		}

		@Override
		public String toString() {
			return b.toString() + suffix;
		}
	}

	private AnsiEscape() {}

}
