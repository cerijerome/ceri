package ceri.common.test;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;
import ceri.common.text.StringBuilders;

public class TestRunAdapterBehavior {

	@Test
	public void shouldExecuteEmptyMethods() {
		TestRunAdapter adapter = new TestRunAdapter();
		adapter.testEnded("1");
		adapter.testFailed(2, "2.1", "2.2");
		adapter.testRunEnded(3);
		adapter.testRunStarted("4", 4);
		adapter.testRunStopped(5);
		adapter.testStarted("6");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWriteOneLinePerMethod() {
		StringBuilder b = new StringBuilder();
		TestRunAdapter p = TestRunAdapter.printer(StringBuilders.printStream(b));
		p.testEnded("1");
		p.testFailed(2, "2.1", "2.2");
		p.testRunEnded(3);
		p.testRunStarted("4", 4);
		p.testRunStopped(5);
		p.testStarted("6");
		String output = b.toString();
		String[] lines = output.split("\r?\n");
		assertEquals(lines.length, 6);
	}

}
