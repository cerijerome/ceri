package ceri.common.text;

import static ceri.common.text.AnsiEscape.csi;
import ceri.common.util.BasicUtil;

/**
 * Simple tester for ANSI escape codes.
 */
public class AnsiTester {

	public static void main(String[] args) {
		System.out.printf("%s Hello %s%n", csi.sgr().bgColor8(2, 0, 1).fgColor8(3, 4, 5),
			csi.sgr().reset());
		for (int i = 1000; i >= 0; i--) {
			System.out.printf("%s %d %s %n", csi.sgr().reverse(true), i, csi.sgr().reverse(false));
			BasicUtil.delay(10);
			if (i > 0) System.out.print(csi.cursorPrevLine(1));
		}
		System.out.print(csi.sgr().reset());
	}
	
}
