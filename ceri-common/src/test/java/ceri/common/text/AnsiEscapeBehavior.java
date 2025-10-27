package ceri.common.text;

import static ceri.common.text.AnsiEscape.Sgr.BasicColor.blue;
import static ceri.common.text.AnsiEscape.Sgr.BasicColor.cyan;
import static ceri.common.text.AnsiEscape.Sgr.BasicColor.magenta;
import static ceri.common.text.AnsiEscape.Sgr.BasicColor.yellow;
import java.awt.Color;
import org.junit.Test;
import ceri.common.test.Assert;

public class AnsiEscapeBehavior {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(AnsiEscape.class);
	}

	@Test
	public void shouldProvideC1Codes() {
		Assert.equal(AnsiEscape.singleShift2.c1, (byte) 0x8e);
		Assert.equal(AnsiEscape.singleShift3.c1, (byte) 0x8f);
		Assert.equal(AnsiEscape.csi.c1, (byte) 0x9b);
		Assert.equal(AnsiEscape.privacyMessage.c1, (byte) 0x9e);
		Assert.equal(AnsiEscape.appCommand.c1, (byte) 0x9f);
	}

	@Test
	public void shouldProvideEscapes() {
		assertString(AnsiEscape.deviceControl("test"), "\u001bPtest\u001b\\");
		assertString(AnsiEscape.osCommand("test"), "\u001b]test\u001b\\");
		assertString(AnsiEscape.start, "\u001bX");
		assertString(AnsiEscape.reset, "\u001bc");
		assertString(AnsiEscape.csi, "\u001b[");
	}

	@Test
	public void shouldProvideCsiCursorUpEscapes() {
		Assert.equal(AnsiEscape.csi.cursorUp(0), "");
		Assert.equal(AnsiEscape.csi.cursorUp(1), "\u001b[A");
		Assert.equal(AnsiEscape.csi.cursorUp(20), "\u001b[20A");
		Assert.equal(AnsiEscape.csi.cursorUp(-2), "\u001b[2B");
	}

	@Test
	public void shouldProvideCsiCursorDownEscapes() {
		Assert.equal(AnsiEscape.csi.cursorDown(0), "");
		Assert.equal(AnsiEscape.csi.cursorDown(1), "\u001b[B");
		Assert.equal(AnsiEscape.csi.cursorDown(20), "\u001b[20B");
		Assert.equal(AnsiEscape.csi.cursorDown(-2), "\u001b[2A");
	}

	@Test
	public void shouldProvideCsiCursorForwardEscapes() {
		Assert.equal(AnsiEscape.csi.cursorForward(0), "");
		Assert.equal(AnsiEscape.csi.cursorForward(1), "\u001b[C");
		Assert.equal(AnsiEscape.csi.cursorForward(20), "\u001b[20C");
		Assert.equal(AnsiEscape.csi.cursorForward(-2), "\u001b[2D");
	}

	@Test
	public void shouldProvideCsiCursorBackEscapes() {
		Assert.equal(AnsiEscape.csi.cursorBack(0), "");
		Assert.equal(AnsiEscape.csi.cursorBack(1), "\u001b[D");
		Assert.equal(AnsiEscape.csi.cursorBack(20), "\u001b[20D");
		Assert.equal(AnsiEscape.csi.cursorBack(-2), "\u001b[2C");
	}

	@Test
	public void shouldProvideCsiCursorNextLineEscapes() {
		Assert.equal(AnsiEscape.csi.cursorNextLine(0), "");
		Assert.equal(AnsiEscape.csi.cursorNextLine(1), "\u001b[E");
		Assert.equal(AnsiEscape.csi.cursorNextLine(20), "\u001b[20E");
		Assert.equal(AnsiEscape.csi.cursorNextLine(-2), "\u001b[2F");
	}

	@Test
	public void shouldProvideCsiCursorPreviousLineEscapes() {
		Assert.equal(AnsiEscape.csi.cursorPrevLine(0), "");
		Assert.equal(AnsiEscape.csi.cursorPrevLine(1), "\u001b[F");
		Assert.equal(AnsiEscape.csi.cursorPrevLine(20), "\u001b[20F");
		Assert.equal(AnsiEscape.csi.cursorPrevLine(-2), "\u001b[2E");
	}

	@Test
	public void shouldProvideCsiCursorColumnEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.cursorColumn(0));
		Assert.equal(AnsiEscape.csi.cursorColumn(1), "\u001b[G");
		Assert.equal(AnsiEscape.csi.cursorColumn(20), "\u001b[20G");
	}

	@Test
	public void shouldProvideCsiCursorPositionEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.cursorPosition(0, 1));
		Assert.thrown(() -> AnsiEscape.csi.cursorPosition(1, 0));
		Assert.equal(AnsiEscape.csi.cursorPosition(1, 1), "\u001b[H");
		Assert.equal(AnsiEscape.csi.cursorPosition(20, 1), "\u001b[20H");
		Assert.equal(AnsiEscape.csi.cursorPosition(1, 20), "\u001b[;20H");
		Assert.equal(AnsiEscape.csi.cursorPosition(5, 6), "\u001b[5;6H");
	}

	@Test
	public void shouldProvideCsiEraseInDisplayEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.eraseInDisplay(-1));
		Assert.equal(AnsiEscape.csi.eraseInDisplay(0), "\u001b[J");
		Assert.equal(AnsiEscape.csi.eraseInDisplay(10), "\u001b[10J");
	}

	@Test
	public void shouldProvideCsiEraseInLineEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.eraseInLine(-1));
		Assert.equal(AnsiEscape.csi.eraseInLine(0), "\u001b[K");
		Assert.equal(AnsiEscape.csi.eraseInLine(10), "\u001b[10K");
	}

	@Test
	public void shouldProvideCsiScrollUpEscapes() {
		Assert.equal(AnsiEscape.csi.scrollUp(0), "");
		Assert.equal(AnsiEscape.csi.scrollUp(1), "\u001b[S");
		Assert.equal(AnsiEscape.csi.scrollUp(20), "\u001b[20S");
		Assert.equal(AnsiEscape.csi.scrollUp(-2), "\u001b[2T");
	}

	@Test
	public void shouldProvideCsiScrollDownEscapes() {
		Assert.equal(AnsiEscape.csi.scrollDown(0), "");
		Assert.equal(AnsiEscape.csi.scrollDown(1), "\u001b[T");
		Assert.equal(AnsiEscape.csi.scrollDown(20), "\u001b[20T");
		Assert.equal(AnsiEscape.csi.scrollDown(-2), "\u001b[2S");
	}

	@Test
	public void shouldProvideCsiHvPositionEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.hvPosition(0, 1));
		Assert.thrown(() -> AnsiEscape.csi.hvPosition(1, 0));
		Assert.equal(AnsiEscape.csi.hvPosition(1, 1), "\u001b[f");
		Assert.equal(AnsiEscape.csi.hvPosition(20, 1), "\u001b[20f");
		Assert.equal(AnsiEscape.csi.hvPosition(1, 20), "\u001b[;20f");
		Assert.equal(AnsiEscape.csi.hvPosition(5, 6), "\u001b[5;6f");
	}

	@Test
	public void shouldProvideCsiAuxPortEscapes() {
		Assert.equal(AnsiEscape.csi.auxPort(true), "\u001b[5i");
		Assert.equal(AnsiEscape.csi.auxPort(false), "\u001b[4i");
	}

	@Test
	public void shouldCopySgr() {
		var sgr = AnsiEscape.csi.sgr().bgColor24(0x123456);
		var copy = sgr.copy();
		assertString(copy, sgr.toString());
		sgr.intensity(1);
		assertString(sgr, "\u001b[48;2;18;52;86;1m");
		assertString(copy, "\u001b[48;2;18;52;86m");
	}

	@Test
	public void shouldProvideSgrEscapes() {
		assertString(AnsiEscape.csi.sgr(), "\u001b[m");
		assertString(AnsiEscape.Sgr.reset, "\u001b[m");
	}

	@Test
	public void shouldProvideSgrIntensityEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().intensity(-2));
		Assert.thrown(() -> AnsiEscape.csi.sgr().intensity(2));
		assertString(AnsiEscape.csi.sgr().intensity(0), "\u001b[22m");
		assertString(AnsiEscape.csi.sgr().intensity(1), "\u001b[1m");
		assertString(AnsiEscape.csi.sgr().intensity(-1), "\u001b[2m");
	}

	@Test
	public void shouldProvideSgrItalicEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().italic(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().italic(3));
		assertString(AnsiEscape.csi.sgr().italic(0), "\u001b[23m");
		assertString(AnsiEscape.csi.sgr().italic(1), "\u001b[3m");
		assertString(AnsiEscape.csi.sgr().italic(2), "\u001b[20m");
	}

	@Test
	public void shouldProvideSgrUnderlineEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().underline(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().underline(3));
		assertString(AnsiEscape.csi.sgr().underline(0), "\u001b[24m");
		assertString(AnsiEscape.csi.sgr().underline(1), "\u001b[4m");
		assertString(AnsiEscape.csi.sgr().underline(2), "\u001b[21m");
	}

	@Test
	public void shouldProvideSgrBlinkEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().blink(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().blink(3));
		assertString(AnsiEscape.csi.sgr().blink(0), "\u001b[25m");
		assertString(AnsiEscape.csi.sgr().blink(1), "\u001b[5m");
		assertString(AnsiEscape.csi.sgr().blink(2), "\u001b[6m");
	}

	@Test
	public void shouldProvideSgrReverseEscapes() {
		assertString(AnsiEscape.csi.sgr().reverse(true), "\u001b[7m");
		assertString(AnsiEscape.csi.sgr().reverse(false), "\u001b[27m");
	}

	@Test
	public void shouldProvideSgrConcealEscapes() {
		assertString(AnsiEscape.csi.sgr().conceal(true), "\u001b[8m");
		assertString(AnsiEscape.csi.sgr().conceal(false), "\u001b[28m");
	}

	@Test
	public void shouldProvideSgrStrikeThroughEscapes() {
		assertString(AnsiEscape.csi.sgr().strikeThrough(true), "\u001b[9m");
		assertString(AnsiEscape.csi.sgr().strikeThrough(false), "\u001b[29m");
	}

	@Test
	public void shouldProvideSgrFontEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().font(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().font(10));
		assertString(AnsiEscape.csi.sgr().font(0), "\u001b[10m");
		assertString(AnsiEscape.csi.sgr().font(1), "\u001b[11m");
		assertString(AnsiEscape.csi.sgr().font(9), "\u001b[19m");
	}

	@Test
	public void shouldProvideFgColorEscapes() {
		assertString(AnsiEscape.csi.sgr().fgColor(), "\u001b[39m");
		assertString(AnsiEscape.csi.sgr().fgColor(null, false), "\u001b[39m");
		assertString(AnsiEscape.csi.sgr().fgColor(blue, false), "\u001b[34m");
		assertString(AnsiEscape.csi.sgr().fgColor(yellow, true), "\u001b[93m");
	}

	@Test
	public void shouldProvideFgGreyEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgGray(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgGray(24));
		assertString(AnsiEscape.csi.sgr().fgGray(10), "\u001b[38;5;242m");
	}

	@Test
	public void shouldProvideFgColor8BitEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor8(-1, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor8(0, -1, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 0, -1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor8(6, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 6, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 0, 6));
		assertString(AnsiEscape.csi.sgr().fgColor8(2, 3, 4), "\u001b[38;5;110m");
		assertString(AnsiEscape.csi.sgr().fgColor8(Color.cyan), "\u001b[38;5;51m");
	}

	@Test
	public void shouldProvideFgColor24BitEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor24(-1, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor24(0, -1, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 0, -1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor24(256, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 256, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 0, 256));
		Assert.equal(AnsiEscape.csi.sgr().fgColor24(55, 66, 77).toString(),
			"\u001b[38;2;55;66;77m");
		Assert.equal(AnsiEscape.csi.sgr().fgColor24(Color.cyan).toString(),
			"\u001b[38;2;;255;255m");
	}

	@Test
	public void shouldProvideBgColorEscapes() {
		assertString(AnsiEscape.csi.sgr().bgColor(), "\u001b[49m");
		assertString(AnsiEscape.csi.sgr().bgColor(null, false), "\u001b[49m");
		assertString(AnsiEscape.csi.sgr().bgColor(magenta, false), "\u001b[45m");
		assertString(AnsiEscape.csi.sgr().bgColor(cyan, true), "\u001b[106m");
	}

	@Test
	public void shouldProvideBgGreyEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgGray(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgGray(24));
		assertString(AnsiEscape.csi.sgr().bgGray(20), "\u001b[48;5;252m");
	}

	@Test
	public void shouldProvideBgColor8BitEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor8(-1, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor8(0, -1, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 0, -1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor8(6, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 6, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 0, 6));
		assertString(AnsiEscape.csi.sgr().bgColor8(2, 3, 4), "\u001b[48;5;110m");
		assertString(AnsiEscape.csi.sgr().bgColor8(Color.cyan), "\u001b[48;5;51m");
	}

	@Test
	public void shouldProvideBgColor24BitEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor24(-1, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor24(0, -1, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 0, -1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor24(256, 0, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 256, 0));
		Assert.thrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 0, 256));
		Assert.equal(AnsiEscape.csi.sgr().bgColor24(55, 66, 77).toString(),
			"\u001b[48;2;55;66;77m");
		Assert.equal(AnsiEscape.csi.sgr().bgColor24(Color.cyan).toString(),
			"\u001b[48;2;;255;255m");
	}

	@Test
	public void shouldProvideSgrFrameEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().frame(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().frame(3));
		assertString(AnsiEscape.csi.sgr().frame(0), "\u001b[54m");
		assertString(AnsiEscape.csi.sgr().frame(1), "\u001b[51m");
		assertString(AnsiEscape.csi.sgr().frame(2), "\u001b[52m");
	}

	@Test
	public void shouldProvideSgrOverlineEscapes() {
		assertString(AnsiEscape.csi.sgr().overline(true), "\u001b[53m");
		assertString(AnsiEscape.csi.sgr().overline(false), "\u001b[55m");
	}

	@Test
	public void shouldProvideSgrIdeogramEscapes() {
		Assert.thrown(() -> AnsiEscape.csi.sgr().ideogram(-1));
		Assert.thrown(() -> AnsiEscape.csi.sgr().ideogram(6));
		assertString(AnsiEscape.csi.sgr().ideogram(0), "\u001b[65m");
		assertString(AnsiEscape.csi.sgr().ideogram(5), "\u001b[64m");
	}

	@Test
	public void shouldCombineSgrCodes() {
		Assert.equal(AnsiEscape.csi.sgr().reset().blink(1).underline(1).fgColor8(1, 2, 3)
			.reverse(true).toString(), "\u001b[;5;4;38;5;67;7m");
	}

	private static void assertString(Object actual, String expected) {
		Assert.equal(actual.toString(), expected);
	}
}
