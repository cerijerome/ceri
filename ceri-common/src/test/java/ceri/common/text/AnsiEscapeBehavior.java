package ceri.common.text;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.*;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.cyan;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.magenta;
import static ceri.common.text.AnsiEscape.Csi.Sgr.BasicColor.yellow;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.awt.Color;
import org.junit.Test;

public class AnsiEscapeBehavior {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(AnsiEscape.class);
	}

	@Test
	public void shouldProvideC1Codes() {
		assertThat(AnsiEscape.singleShift2.c1(), is((byte) 0x8e));
		assertThat(AnsiEscape.singleShift3.c1(), is((byte) 0x8f));
		assertThat(AnsiEscape.csi.c1(), is((byte) 0x9b));
		assertThat(AnsiEscape.privacyMessage.c1(), is((byte) 0x9e));
		assertThat(AnsiEscape.appCommand.c1(), is((byte) 0x9f));
	}

	@Test
	public void shouldProvideEscapes() {
		assertThat(AnsiEscape.deviceControl.toString(), is("\u001bP"));
		assertThat(AnsiEscape.osCommand.toString(), is("\u001b]"));
		assertThat(AnsiEscape.start.toString(), is("\u001bX"));
		assertThat(AnsiEscape.terminator.toString(), is("\u001b\\"));
		assertThat(AnsiEscape.reset.toString(), is("\u001bc"));
		assertThat(AnsiEscape.csi.toString(), is("\u001b["));
	}

	@Test
	public void shouldProvideCsiCursorUpEscapes() {
		assertThat(AnsiEscape.csi.cursorUp(0), is(""));
		assertThat(AnsiEscape.csi.cursorUp(1), is("\u001b[A"));
		assertThat(AnsiEscape.csi.cursorUp(20), is("\u001b[20A"));
		assertThat(AnsiEscape.csi.cursorUp(-2), is("\u001b[2B"));
	}

	@Test
	public void shouldProvideCsiCursorDownEscapes() {
		assertThat(AnsiEscape.csi.cursorDown(0), is(""));
		assertThat(AnsiEscape.csi.cursorDown(1), is("\u001b[B"));
		assertThat(AnsiEscape.csi.cursorDown(20), is("\u001b[20B"));
		assertThat(AnsiEscape.csi.cursorDown(-2), is("\u001b[2A"));
	}

	@Test
	public void shouldProvideCsiCursorForwardEscapes() {
		assertThat(AnsiEscape.csi.cursorForward(0), is(""));
		assertThat(AnsiEscape.csi.cursorForward(1), is("\u001b[C"));
		assertThat(AnsiEscape.csi.cursorForward(20), is("\u001b[20C"));
		assertThat(AnsiEscape.csi.cursorForward(-2), is("\u001b[2D"));
	}

	@Test
	public void shouldProvideCsiCursorBackEscapes() {
		assertThat(AnsiEscape.csi.cursorBack(0), is(""));
		assertThat(AnsiEscape.csi.cursorBack(1), is("\u001b[D"));
		assertThat(AnsiEscape.csi.cursorBack(20), is("\u001b[20D"));
		assertThat(AnsiEscape.csi.cursorBack(-2), is("\u001b[2C"));
	}

	@Test
	public void shouldProvideCsiCursorNextLineEscapes() {
		assertThat(AnsiEscape.csi.cursorNextLine(0), is(""));
		assertThat(AnsiEscape.csi.cursorNextLine(1), is("\u001b[E"));
		assertThat(AnsiEscape.csi.cursorNextLine(20), is("\u001b[20E"));
		assertThat(AnsiEscape.csi.cursorNextLine(-2), is("\u001b[2F"));
	}

	@Test
	public void shouldProvideCsiCursorPreviousLineEscapes() {
		assertThat(AnsiEscape.csi.cursorPrevLine(0), is(""));
		assertThat(AnsiEscape.csi.cursorPrevLine(1), is("\u001b[F"));
		assertThat(AnsiEscape.csi.cursorPrevLine(20), is("\u001b[20F"));
		assertThat(AnsiEscape.csi.cursorPrevLine(-2), is("\u001b[2E"));
	}

	@Test
	public void shouldProvideCsiCursorColumnEscapes() {
		assertThrown(() -> AnsiEscape.csi.cursorColumn(0));
		assertThat(AnsiEscape.csi.cursorColumn(1), is("\u001b[G"));
		assertThat(AnsiEscape.csi.cursorColumn(20), is("\u001b[20G"));
	}

	@Test
	public void shouldProvideCsiCursorPositionEscapes() {
		assertThrown(() -> AnsiEscape.csi.cursorPosition(0, 1));
		assertThrown(() -> AnsiEscape.csi.cursorPosition(1, 0));
		assertThat(AnsiEscape.csi.cursorPosition(1, 1), is("\u001b[H"));
		assertThat(AnsiEscape.csi.cursorPosition(20, 1), is("\u001b[20H"));
		assertThat(AnsiEscape.csi.cursorPosition(1, 20), is("\u001b[;20H"));
		assertThat(AnsiEscape.csi.cursorPosition(5, 6), is("\u001b[5;6H"));
	}

	@Test
	public void shouldProvideCsiEraseInDisplayEscapes() {
		assertThrown(() -> AnsiEscape.csi.eraseInDisplay(-1));
		assertThat(AnsiEscape.csi.eraseInDisplay(0), is("\u001b[J"));
		assertThat(AnsiEscape.csi.eraseInDisplay(10), is("\u001b[10J"));
	}

	@Test
	public void shouldProvideCsiEraseInLineEscapes() {
		assertThrown(() -> AnsiEscape.csi.eraseInLine(-1));
		assertThat(AnsiEscape.csi.eraseInLine(0), is("\u001b[K"));
		assertThat(AnsiEscape.csi.eraseInLine(10), is("\u001b[10K"));
	}

	@Test
	public void shouldProvideCsiScrollUpEscapes() {
		assertThat(AnsiEscape.csi.scrollUp(0), is(""));
		assertThat(AnsiEscape.csi.scrollUp(1), is("\u001b[S"));
		assertThat(AnsiEscape.csi.scrollUp(20), is("\u001b[20S"));
		assertThat(AnsiEscape.csi.scrollUp(-2), is("\u001b[2T"));
	}

	@Test
	public void shouldProvideCsiScrollDownEscapes() {
		assertThat(AnsiEscape.csi.scrollDown(0), is(""));
		assertThat(AnsiEscape.csi.scrollDown(1), is("\u001b[T"));
		assertThat(AnsiEscape.csi.scrollDown(20), is("\u001b[20T"));
		assertThat(AnsiEscape.csi.scrollDown(-2), is("\u001b[2S"));
	}

	@Test
	public void shouldProvideCsiHvPositionEscapes() {
		assertThrown(() -> AnsiEscape.csi.hvPosition(0, 1));
		assertThrown(() -> AnsiEscape.csi.hvPosition(1, 0));
		assertThat(AnsiEscape.csi.hvPosition(1, 1), is("\u001b[f"));
		assertThat(AnsiEscape.csi.hvPosition(20, 1), is("\u001b[20f"));
		assertThat(AnsiEscape.csi.hvPosition(1, 20), is("\u001b[;20f"));
		assertThat(AnsiEscape.csi.hvPosition(5, 6), is("\u001b[5;6f"));
	}

	@Test
	public void shouldProvideCsiAuxPortEscapes() {
		assertThat(AnsiEscape.csi.auxPort(true), is("\u001b[5i"));
		assertThat(AnsiEscape.csi.auxPort(false), is("\u001b[4i"));
	}

	@Test
	public void shouldProvideSgrEscapes() {
		assertThat(AnsiEscape.csi.sgr().toString(), is("\u001b[m"));
		assertThat(AnsiEscape.csi.sgr().reset().toString(), is("\u001b[m"));
	}

	@Test
	public void shouldProvideSgrIntensityEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().intensity(-2));
		assertThrown(() -> AnsiEscape.csi.sgr().intensity(2));
		assertThat(AnsiEscape.csi.sgr().intensity(0).toString(), is("\u001b[22m"));
		assertThat(AnsiEscape.csi.sgr().intensity(1).toString(), is("\u001b[1m"));
		assertThat(AnsiEscape.csi.sgr().intensity(-1).toString(), is("\u001b[2m"));
	}

	@Test
	public void shouldProvideSgrItalicEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().italic(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().italic(3));
		assertThat(AnsiEscape.csi.sgr().italic(0).toString(), is("\u001b[23m"));
		assertThat(AnsiEscape.csi.sgr().italic(1).toString(), is("\u001b[3m"));
		assertThat(AnsiEscape.csi.sgr().italic(2).toString(), is("\u001b[20m"));
	}

	@Test
	public void shouldProvideSgrUnderlineEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().underline(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().underline(3));
		assertThat(AnsiEscape.csi.sgr().underline(0).toString(), is("\u001b[24m"));
		assertThat(AnsiEscape.csi.sgr().underline(1).toString(), is("\u001b[4m"));
		assertThat(AnsiEscape.csi.sgr().underline(2).toString(), is("\u001b[21m"));
	}

	@Test
	public void shouldProvideSgrBlinkEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().blink(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().blink(3));
		assertThat(AnsiEscape.csi.sgr().blink(0).toString(), is("\u001b[25m"));
		assertThat(AnsiEscape.csi.sgr().blink(1).toString(), is("\u001b[5m"));
		assertThat(AnsiEscape.csi.sgr().blink(2).toString(), is("\u001b[6m"));
	}

	@Test
	public void shouldProvideSgrReverseEscapes() {
		assertThat(AnsiEscape.csi.sgr().reverse(true).toString(), is("\u001b[7m"));
		assertThat(AnsiEscape.csi.sgr().reverse(false).toString(), is("\u001b[27m"));
	}

	@Test
	public void shouldProvideSgrConcealEscapes() {
		assertThat(AnsiEscape.csi.sgr().conceal(true).toString(), is("\u001b[8m"));
		assertThat(AnsiEscape.csi.sgr().conceal(false).toString(), is("\u001b[28m"));
	}

	@Test
	public void shouldProvideSgrStrikeThroughEscapes() {
		assertThat(AnsiEscape.csi.sgr().strikeThrough(true).toString(), is("\u001b[9m"));
		assertThat(AnsiEscape.csi.sgr().strikeThrough(false).toString(), is("\u001b[29m"));
	}

	@Test
	public void shouldProvideSgrFontEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().font(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().font(10));
		assertThat(AnsiEscape.csi.sgr().font(0).toString(), is("\u001b[10m"));
		assertThat(AnsiEscape.csi.sgr().font(1).toString(), is("\u001b[11m"));
		assertThat(AnsiEscape.csi.sgr().font(9).toString(), is("\u001b[19m"));
	}

	@Test
	public void shouldProvideFgColorEscapes() {
		assertThat(AnsiEscape.csi.sgr().fgColor().toString(), is("\u001b[39m"));
		assertThat(AnsiEscape.csi.sgr().fgColor(null, false).toString(), is("\u001b[39m"));
		assertThat(AnsiEscape.csi.sgr().fgColor(blue, false).toString(), is("\u001b[34m"));
		assertThat(AnsiEscape.csi.sgr().fgColor(yellow, true).toString(), is("\u001b[93m"));
	}

	@Test
	public void shouldProvideFgGreyEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().fgGray(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().fgGray(24));
		assertThat(AnsiEscape.csi.sgr().fgGray(10).toString(), is("\u001b[38;5;242m"));
	}

	@Test
	public void shouldProvideFgColor8BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(6, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 6, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor8(0, 0, 6));
		assertThat(AnsiEscape.csi.sgr().fgColor8(2, 3, 4).toString(), is("\u001b[38;5;110m"));
		assertThat(AnsiEscape.csi.sgr().fgColor8(Color.cyan).toString(), is("\u001b[38;5;51m"));
	}

	@Test
	public void shouldProvideFgColor24BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(256, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 256, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().fgColor24(0, 0, 256));
		assertThat(AnsiEscape.csi.sgr().fgColor24(55, 66, 77).toString(),
			is("\u001b[38;2;55;66;77m"));
		assertThat(AnsiEscape.csi.sgr().fgColor24(Color.cyan).toString(),
			is("\u001b[38;2;;255;255m"));
	}

	@Test
	public void shouldProvideBgColorEscapes() {
		assertThat(AnsiEscape.csi.sgr().bgColor().toString(), is("\u001b[49m"));
		assertThat(AnsiEscape.csi.sgr().bgColor(null, false).toString(), is("\u001b[49m"));
		assertThat(AnsiEscape.csi.sgr().bgColor(magenta, false).toString(), is("\u001b[45m"));
		assertThat(AnsiEscape.csi.sgr().bgColor(cyan, true).toString(), is("\u001b[106m"));
	}

	@Test
	public void shouldProvideBgGreyEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().bgGray(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().bgGray(24));
		assertThat(AnsiEscape.csi.sgr().bgGray(20).toString(), is("\u001b[48;5;252m"));
	}

	@Test
	public void shouldProvideBgColor8BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(6, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 6, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor8(0, 0, 6));
		assertThat(AnsiEscape.csi.sgr().bgColor8(2, 3, 4).toString(), is("\u001b[48;5;110m"));
		assertThat(AnsiEscape.csi.sgr().bgColor8(Color.cyan).toString(), is("\u001b[48;5;51m"));
	}

	@Test
	public void shouldProvideBgColor24BitEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(-1, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, -1, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 0, -1));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(256, 0, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 256, 0));
		assertThrown(() -> AnsiEscape.csi.sgr().bgColor24(0, 0, 256));
		assertThat(AnsiEscape.csi.sgr().bgColor24(55, 66, 77).toString(),
			is("\u001b[48;2;55;66;77m"));
		assertThat(AnsiEscape.csi.sgr().bgColor24(Color.cyan).toString(),
			is("\u001b[48;2;;255;255m"));
	}

	@Test
	public void shouldProvideSgrFrameEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().frame(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().frame(3));
		assertThat(AnsiEscape.csi.sgr().frame(0).toString(), is("\u001b[54m"));
		assertThat(AnsiEscape.csi.sgr().frame(1).toString(), is("\u001b[51m"));
		assertThat(AnsiEscape.csi.sgr().frame(2).toString(), is("\u001b[52m"));
	}

	@Test
	public void shouldProvideSgrOverlineEscapes() {
		assertThat(AnsiEscape.csi.sgr().overline(true).toString(), is("\u001b[53m"));
		assertThat(AnsiEscape.csi.sgr().overline(false).toString(), is("\u001b[55m"));
	}

	@Test
	public void shouldProvideSgrIdeogramEscapes() {
		assertThrown(() -> AnsiEscape.csi.sgr().ideogram(-1));
		assertThrown(() -> AnsiEscape.csi.sgr().ideogram(6));
		assertThat(AnsiEscape.csi.sgr().ideogram(0).toString(), is("\u001b[65m"));
		assertThat(AnsiEscape.csi.sgr().ideogram(5).toString(), is("\u001b[64m"));
	}

	@Test
	public void shouldCombineSgrCodes() {
		assertThat(AnsiEscape.csi.sgr().reset().blink(1).underline(1).fgColor8(1, 2, 3)
			.reverse(true).toString(), is("\u001b[;5;4;38;5;67;7m"));
	}

}
