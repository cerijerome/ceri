package ceri.common.text;

import static ceri.common.text.AnsiEscape.csi;
import ceri.common.color.Colors;
import ceri.common.text.AnsiEscape.Sgr;

/**
 * Simple tester for ANSI escape codes.
 */
public class AnsiTester {

	public static void main(String[] args) {
		System.out.printf("%s 8-bit color %s ", csi.sgr().bgColor8(2, 0, 1).fgColor8(3, 4, 5),
			Sgr.reset);
		System.out.printf("%s 24-bit color %s ",
			csi.sgr().bgColor24(Colors.random()).fgColor24(Colors.random()), Sgr.reset);
		System.out.printf("%s frame %s ", csi.sgr().frame(1), csi.sgr().frame(0));
		System.out.printf("%sunderline1%s %sunderline2%s ", csi.sgr().underline(1),
			csi.sgr().underline(0), csi.sgr().underline(2), csi.sgr().underline(0));
		System.out.println();
		for (int i = 20; i >= 0; i--)
			System.out.printf("%s %02d %s ", csi.sgr().reverse(true), i, csi.sgr().reverse(false));
		System.out.println();
	}

}
