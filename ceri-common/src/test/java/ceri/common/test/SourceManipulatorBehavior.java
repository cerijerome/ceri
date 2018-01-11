package ceri.common.test;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.PrintStream;
import java.util.regex.Matcher;
import org.junit.Test;
import ceri.common.text.StringUtil;

public class SourceManipulatorBehavior {

	@Test
	public void shouldPrintSource() {
		PrintStream stdout = System.out;
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			System.setOut(out);
			SourceManipulator.main(new String[] {});
			out.flush();
			String s = b.toString();
			assertTrue(s.contains("class " + SourceManipulator.class.getSimpleName()));
		} finally {
			System.setOut(stdout);
		}
	}

	@Test
	public void shouldLoadCallerSource() {
		SourceManipulator sm = SourceManipulator.fromTestCaller();
		Matcher m = sm.matcher("class (.*) \\{");
		assertTrue(m.find());
		assertThat(m.group(1), is(getClass().getSimpleName()));
		SourceManipulator sm2 = SourceManipulator.fromTest(SourceManipulatorBehavior.class);
		assertThat(print(sm), is(print(sm2)));
		assertException(() -> SourceManipulator.fromCaller());
	}

	@Test
	public void shouldAllowSourceTextReplacement() {
		String s = "abc0 abc() (abc) _abc :abc;";
		SourceManipulator sm = SourceManipulator.from(s);
		sm.replaceIdentifier("abc", "def");
		sm.replaceAll("def", "DEF");
		sm.replaceText("(", "[");
		assertThat(print(sm).trim(), is("abc0 DEF[) [DEF) _abc :DEF;"));

	}

	private String print(SourceManipulator sm) {
		PrintStream stdout = System.out;
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			System.setOut(out);
			sm.print();
			out.flush();
			return b.toString();
		} finally {
			System.setOut(stdout);
		}
	}

}
