package ceri.common.test;

import org.junit.Test;

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

}
