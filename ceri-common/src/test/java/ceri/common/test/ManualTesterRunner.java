package ceri.common.test;

import ceri.common.text.Strings;

public class ManualTesterRunner {

	public static void main(String[] args) {
		try (var tester = ManualTester.builderArray("abc", 123, 0.123).separator("----------")
			.command("L", (t, _, s) -> t.outf("Len(%s) = %d", s, Strings.length(s.toString())),
				"L = print length of string")
			.command("R", (t, _, s) -> t.outf("%s => %s", s, Strings.reverse(s.toString())),
				"R = reverse string")
			.build()) {
			tester.run();
		}
	}
}
