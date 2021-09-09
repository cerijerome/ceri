package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static java.nio.charset.StandardCharsets.UTF_8;
import org.junit.Test;

public class StringPrintStreamBehavior {

	@Test
	public void shouldCaptureStrings() {
		try (StringPrintStream out = StringPrintStream.of()) {
			out.print(false);
			out.print('a');
			out.print(new char[] { 'b', 'c' });
			out.print("d");
			out.print(0.1);
			out.print(-0.1f);
			out.print(-1);
			out.print(1L);
			out.print((Object) null);
			assertEquals(out.toString(), "falseabcd0.1-0.1-11null");
		}
	}

	@Test
	public void shouldHandleCharsets() {
		try (StringPrintStream out = StringPrintStream.of(UTF_8)) {
			String s = "\0\u0100\u0102\u0104";
			out.print(s);
			assertEquals(out.toString(), s);
		}
	}

	@Test
	public void shouldClear() {
		try (StringPrintStream out = StringPrintStream.of(UTF_8)) {
			String s = "\0\u0100\u0102\u0104";
			out.print(s);
			out.clear();
			assertEquals(out.toString(), "");
		}
	}

}
