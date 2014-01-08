package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.UnsupportedEncodingException;
import org.junit.Test;

public class StringPrintStreamBehavior {

	@Test
	public void shouldCaptureStrings() {
		try (StringPrintStream out = new StringPrintStream()) {
			out.print(false);
			out.print('a');
			out.print(new char[] { 'b', 'c' });
			out.print("d");
			out.print(0.1);
			out.print(-0.1f);
			out.print(-1);
			out.print(1L);
			out.print((Object) null);
			assertThat(out.toString(), is("falseabcd0.1-0.1-11null"));
		}
	}

	@Test
	public void shouldHandleCharsets() throws UnsupportedEncodingException {
		try (StringPrintStream out = new StringPrintStream("UTF8")) {
			String s = "\0\u0100\u0102\u0104";
			out.print(s);
			assertThat(out.toString(), is(s));
		}
	}

}
