package ceri.common.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import ceri.common.text.StringUtil;

public class TestRunPrinterBehavior {

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteOneLinePerMethod() {
		StringBuilder b = new StringBuilder();
		TestRunPrinter p = new TestRunPrinter(StringUtil.asPrintStream(b));
		p.testEnded("1");
		p.testFailed(2, "2.1", "2.2");
		p.testRunEnded(3);
		p.testRunStarted("4", 4);
		p.testRunStopped(5);
		p.testStarted("6");
		String output = b.toString();
		String[] lines = output.split("\r?\n");
		assertThat(lines.length, is(6));
	}

}
