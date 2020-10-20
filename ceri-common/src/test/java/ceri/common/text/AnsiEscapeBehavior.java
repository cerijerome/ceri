package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.blue;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.cyan;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.magenta;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.yellow;
import java.awt.Color;
import org.junit.Test;

public class AnsiEscapeBehavior {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(AnsiEscape.class);
	}

	@Test
	public void shouldProvideC1Codes() {
		assertEquals(AnsiEscape.singleShift2.c1(), (byte) 0x8e);
		assertEquals(AnsiEscape.singleShift3.c1(), (byte) 0x8f);
		assertEquals(AnsiEscape.csi.c1(), (byte) 0x9b);
		assertEquals(AnsiEscape.privacyMessage.c1(), (byte) 0x9e);
		assertEquals(AnsiEscape.appCommand.c1(), (byte) 0x9f);
	}

	@Test
	public void shouldProvideEscapes() {
		assertEquals(AnsiEscape.deviceControl.toString(), "\u001bP");
		assertEquals(AnsiEscape.osCommand.toString(), "\u001b]");
		assertEquals(AnsiEscape.start.toString(), "\u001bX");
		assertEquals(AnsiEscape.terminator.toString(), "\u001b\\");
		assertEquals(AnsiEscape.reset.toString(), "\u001bc");
		assertEquals(AnsiEscape.csi.toString(), "\u001b[");
	}

	@Test
	public void shouldProvideCsiCursorUpEscapes() {
		assertEquals(AnsiEscape.csi.cursorUp(0), "");
		assertEquals(AnsiEscape.csi.cursorUp(1), "\u001b[A");
		assertEquals(AnsiEscape.csi.cursorUp(20), "\u001b[20A");
		assertEquals(AnsiEscape.csi.cursorUp(-2), "\u001b[2B");
	}

	@Test
	public void shouldProvideCsiCursorDownEscapes() {
		assertEquals(AnsiEscape.csi.cursorDown(0), "");
		assertEquals(AnsiEscape.csi.cursorDown(1), "\u001b[B");
		assertEquals(AnsiEscape.csi.cursorDown(20), "\u001b[20B");
		assertEquals(AnsiEscape.csi.cursorDown(-2), "\u001b[2A");
	}

	@Test
	public void shouldProvideCsiCursorForwardEscapes() {
		assertEquals(AnsiEscape.csi.cursorForward(0), "");
		assertEquals(AnsiEscape.csi.cursorForward(1), "\u001b[C");
		assertEquals(AnsiEscape.csi.cursorForward(20), "\u001b[20C");
		assertEquals(AnsiEscape.csi.cursorForward(-2), "\u001b[2D");
	}

	@Test
	public void shouldProvideCsiCursorBackEscapes() {
		assertEquals(AnsiEscape.csi.cursorBack(0), "");
		assertEquals(AnsiEscape.csi.cursorBack(1), "\u001b[D");
		assertEquals(AnsiEscape.csi.cursorBack(20), "\u001b[20D");
		assertEquals(AnsiEscape.csi.cursorBack(-2), "\u001b[2C");
	}

	@Test
	public void shouldProvideCsiCursorNextLineEscapes() {
		assertEquals(AnsiEscape.csi.cursorNextLine(0), "");
		assertEquals(AnsiEscape.csi.cursorNextLine(1), "\u001b[E");
		assertEquals(AnsiEscape.csi.cursorNextLine(20), "\u001b[20E");
		assertEquals(AnsiEscape.csi.cursorNextLine(-2), "\u001b[2F");
	}

	@Test
	public void shouldProvideCsiCursorPreviousLineEscapes() {
		assertEquals(AnsiEscape.csi.cursorPrevLine(0), "");
		assertEquals(AnsiEscape.csi.cursorPrevLine(1), "\u001b[F");
		assertEquals(AnsiEscape.csi.cursorPrevLine(20), "\u001b[20F");
		assertEquals(AnsiEscape.csi.cursorPrevLine(-2), "\u001b[2E");
	}

	@Test
	public void shouldProvideCsiCursorColumnEscapes() {
		assertThrown(() -> AnsiEscape.csi.cursorColumn(0));
		assertEquals(AnsiEscape.csi.cursorColumn(1), "\u001b[G");
		assertEquals(AnsiEscape.csi.cursorColumn(20), "\u001b[20G");
	}

	@Test
	public void shouldProvideCsiCursorPositionEscapes() {
		assertThrown(() -> AnsiEscape.csi.cursorPosition(0, 1));
		assertThrown(() -> AnsiEscape.csi.cursorPosition(1, 0));
		assertEquals(AnsiEscape.csi.cursorPosition(1, 1), "\u001b[H");
		assertEquals(AnsiEscape.csi.cursorPosition(20, 1), "\u001b[20H");
		assertEquals(AnsiEscape.csi.cursorPosition(1, 20), "\u001b[;20H");
		assertEquals(AnsiEscape.csi.cursorPosition(5, 6), "\u001b[5;6H");
	}

	@Test
	public void shouldProvideCsiEraseInDisplayEscapes() {
		assertThrown(() -> AnsiEscape.csi.eraseInDisplay(-1));
		assertEquals(AnsiEscape.csi.eraseInDisplay(0), "\u001b[J");
		assertEquals(AnsiEscape.csi.eraseInDisplay(10), "\u001b[10J");
	}

	@Test
	public void shouldProvideCsiEraseInLineEscapes() {
		assertThrown(() -> AnsiEscape.csi.eraseInLine(-1));
		assertEquals(AnsiEscape.csi.eraseInLine(0), "\u001b[K");
		assertEquals(AnsiEscape.csi.eraseInLine(10), "\u001b[10K");
	}

	@Test
	public void shouldProvideCsiScrollUpEscapes() {
		assertEquals(AnsiEscape.csi.scrollUp(0), "");
		assertEquals(AnsiEscape.csi.scrollUp(1), "\u001b[S");
		assertEquals(AnsiEscape.csi.scrollUp(20), "\u001b[20S");
		assertEquals(AnsiEscape.csi.scrollUp(-2), "\u001b[2T");
	}

	@Test
	public void shouldProvideCsiScrollDownEscapes() {
		assertEquals(AnsiEscape.csi.scrollDown(0), "");
		assertEquals(AnsiEscape.csi.scrollDown(1), "\u001b[T");
		assertEquals(AnsiEscape.csi.scrollDown(20), "\u001b[20T");
		assertEquals(AnsiEscape.csi.scrollDown(-2), "\u001b[2S");
	}

	@Test
	public void shouldProvideCsiHvPositionEscapes() {
		assertThrown(() -> AnsiEscape.csi.hvPosition(0, 1));
		assertThrown(() -> AnsiEscape.csi.hvPosition(1, 0));
		assertEquals(AnsiEscape.csi.hvPosition(1, 1), "\u001b[f");
		assertEquals(AnsiEscape.csi.hvPosition(20, 1), "\u001b[20f");
		assertEquals(AnsiEscape.csi.hvPosition(1, 20), "\u001b[;20f");
		assertEquals(AnsiEscape.csi.hvPosition(5, 6), "\u001b[5;6f");
	}

	@Test
	public void shouldProvideCsiAuxPortEscapes() {
		assertEquals(AnsiEscape.csi.auxPort(true), "\u001b[5i");
		assertEquals(AnsiEscape.csi.auxPort(false), "\u001b[4i");
	}

	@Test
	public void shouldProvideSgrEscapes() {
		assertEquals(AnsiEscape.csi.sgr().toString(), "\u001b[m");
		assertEquals(AnsiEscape.csi.sgr().reset().toString(), "\u001b[m");
	}

	@Test
	public void shouldProvideSgrIntensityEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().intensity(-2));
		assertThrown(() -> AnsiEscape.csi.sgr().intensity(2));
		assertEquals(AnsiEscape.csi.sgr().intensity(0).toString(), "\u001b[22m");
		assertEquals(AnsiEscape.csi.sgr().intensity(1).toString(), "\u001b[1m");
		assertEquals(AnsiEscape.csi.sgr().intensity(-1).toString(), "\u001b[2m");
	}

	@Test
	public void shouldProvideSgrItalicEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().italic(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().italic(3));
		assertEquals(AnsiEscape.csi.sgr().italic(0).toString(), "\u001b[23m");
		assertEquals(AnsiEscape.csi.sgr().italic(1).toString(), "\u001b[3m");
		assertEquals(AnsiEscape.csi.sgr().italic(2).toString(), "\u001b[20m");
	}

	@Test
	public void shouldProvideSgrUnderlineEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().underline(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().underline(3));
		assertEquals(AnsiEscape.csi.sgr().underline(0).toString(), "\u001b[24m");
		assertEquals(AnsiEscape.csi.sgr().underline(1).toString(), "\u001b[4m");
		assertEquals(AnsiEscape.csi.sgr().underline(2).toString(), "\u001b[21m");
	}

	@Test
	public void shouldProvideSgrBlinkEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().blink(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().blink(3));
		assertEquals(AnsiEscape.csi.sgr().blink(0).toString(), "\u001b[25m");
		assertEquals(AnsiEscape.csi.sgr().blink(1).toString(), "\u001b[5m");
		assertEquals(AnsiEscape.csi.sgr().blink(2).toString(), "\u001b[6m");
	}

	@Test
	public void shouldProvideSgrReverseEscapes() {
		assertEquals(AnsiEscape.csi.sgr().reverse(true).toString(), "\u001b[7m");
		assertEquals(AnsiEscape.csi.sgr().reverse(false).toString(), "\u001b[27m");
	}

	@Test
	public void shouldProvideSgrConcealEscapes() {
		assertEquals(AnsiEscape.csi.sgr().conceal(true).toString(), "\u001b[8m");
		assertEquals(AnsiEscape.csi.sgr().conceal(false).toString(), "\u001b[28m");
	}

	@Test
	public void shouldProvideSgrStrikeThroughEscapes() {
		assertEquals(AnsiEscape.csi.sgr().strikeThrough(true).toString(), "\u001b[9m");
		assertEquals(AnsiEscape.csi.sgr().strikeThrough(false).toString(), "\u001b[29m");
	}

	@Test
	public void shouldProvideSgrFontEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().font(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().font(10));
		assertEquals(AnsiEscape.csi.sgr().font(0).toString(), "\u001b[10m");
		assertEquals(AnsiEscape.csi.sgr().font(1).toString(), "\u001b[11m");
		assertEquals(AnsiEscape.csi.sgr().font(9).toString(), "\u001b[19m");
	}

	@Test
	public void shouldProvideFgColorEscapes() {
		assertEquals(AnsiEscape.csi.sgr().fgColor().toString(), "\u001b[39m");
		assertEquals(AnsiEscape.csi.sgr().fgColor(null, false).toString(), "\u001b[39m");
		assertEquals(AnsiEscape.csi.sgr().fgColor(blue, false).toString(), "\u001b[34m");
		assertEquals(AnsiEscape.csi.sgr().fgColor(yellow, true).toString(), "\u001b[93m");
	}

	@Test
	public void shouldProvideFgGreyEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().fgGray(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().fgGray(24));
		assertEquals(AnsiEscape.csi.sgr().fgGray(10).toString(), "\u001b[38;5;242m");
	}

	@Test
	public void shouldProvideFgColor8BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(6, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 6, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 0, 6));
		assertEquals(AnsiEscape.csi.sgr().fgColor8(2, 3, 4).toString(), "\u001b[38;5;110m");
		assertEquals(AnsiEscape.csi.sgr().fgColor8(Color.cyan).toString(), "\u001b[38;5;51m");
	}

	@Test
	public void shouldProvideFgColor24BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(256, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 256, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 0, 256));
		assertEquals(AnsiEscape.csi.sgr().fgColor24(55, 66, 77).toString(),
			"\u001b[38;2;55;66;77m");
		assertEquals(AnsiEscape.csi.sgr().fgColor24(Color.cyan).toString(),
			"\u001b[38;2;;255;255m");
	}

	@Test
	public void shouldProvideBgColorEscapes() {
		assertEquals(AnsiEscape.csi.sgr().bgColor().toString(), "\u001b[49m");
		assertEquals(AnsiEscape.csi.sgr().bgColor(null, false).toString(), "\u001b[49m");
		assertEquals(AnsiEscape.csi.sgr().bgColor(magenta, false).toString(), "\u001b[45m");
		assertEquals(AnsiEscape.csi.sgr().bgColor(cyan, true).toString(), "\u001b[106m");
	}

	@Test
	public void shouldProvideBgGreyEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().bgGray(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().bgGray(24));
		assertEquals(AnsiEscape.csi.sgr().bgGray(20).toString(), "\u001b[48;5;252m");
	}

	@Test
	public void shouldProvideBgColor8BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(6, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 6, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 0, 6));
		assertEquals(AnsiEscape.csi.sgr().bgColor8(2, 3, 4).toString(), "\u001b[48;5;110m");
		assertEquals(AnsiEscape.csi.sgr().bgColor8(Color.cyan).toString(), "\u001b[48;5;51m");
	}

	@Test
	public void shouldProvideBgColor24BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(256, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 256, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 0, 256));
		assertEquals(AnsiEscape.csi.sgr().bgColor24(55, 66, 77).toString(),
			"\u001b[48;2;55;66;77m");
		assertEquals(AnsiEscape.csi.sgr().bgColor24(Color.cyan).toString(),
			"\u001b[48;2;;255;255m");
	}

	@Test
	public void shouldProvideSgrFrameEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().frame(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().frame(3));
		assertEquals(AnsiEscape.csi.sgr().frame(0).toString(), "\u001b[54m");
		assertEquals(AnsiEscape.csi.sgr().frame(1).toString(), "\u001b[51m");
		assertEquals(AnsiEscape.csi.sgr().frame(2).toString(), "\u001b[52m");
	}

	@Test
	public void shouldProvideSgrOverlineEscapes() {
		assertEquals(AnsiEscape.csi.sgr().overline(true).toString(), "\u001b[53m");
		assertEquals(AnsiEscape.csi.sgr().overline(false).toString(), "\u001b[55m");
	}

	@Test
	public void shouldProvideSgrIdeogramEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().ideogram(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().ideogram(6));
		assertEquals(AnsiEscape.csi.sgr().ideogram(0).toString(), "\u001b[65m");
		assertEquals(AnsiEscape.csi.sgr().ideogram(5).toString(), "\u001b[64m");
	}

	@Test
	public void shouldCombineSgrCodes() {
		assertEquals(AnsiEscape.csi.sgr().reset().blink(1).underline(1).fgColor8(1, 2, 3)
			.reverse(true).toString(), "\u001b[;5;4;38;5;67;7m");
	}

}
