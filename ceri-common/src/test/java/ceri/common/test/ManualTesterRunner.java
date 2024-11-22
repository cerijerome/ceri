package ceri.common.test;

import ceri.common.text.StringUtil;

public class ManualTesterRunner {

	public static void main(String[] args) {
		try (var tester = ManualTester.builderArray("abc", 123, 0.123).separator("----------")
			.command("L", (t, m, s) -> t.outf("Len(%s) = %d", s, StringUtil.len(s.toString())),
				"L = print length of string")
			.command("R", (t, m, s) -> t.outf("%s => %s", s, StringUtil.reverse(s.toString())),
				"R = reverse string")
			.build()) {
			tester.run();
		}
	}
}
