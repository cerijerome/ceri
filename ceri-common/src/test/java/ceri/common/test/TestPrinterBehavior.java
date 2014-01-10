package ceri.common.test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.Description;
import ceri.common.util.StringUtil;

public class TestPrinterBehavior {

	@Test
	public void shouldConvertBehaviorMethodNameToPhrase() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(getClass(), "shouldConvertToPhrase");
		p.testStarted(d);
		assertTrue(p.tests().isEmpty());
		assertThat(p.behaviors().size(), is(1));
		assertThat(p.behaviors().iterator().next().description, is("should convert to phrase"));
	}

	@Test
	public void shouldExtractTestMethodName() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(getClass(), "testThisMethod");
		p.testStarted(d);
		assertTrue(p.behaviors().isEmpty());
		assertThat(p.tests().iterator().next().description, is("test thisMethod"));
	}

	@Test
	public void shouldPrintBehaviorsAndTests() {
		TestPrinter p = new TestPrinter();
		Description db = Description.createTestDescription(getClass(), "shouldBe");
		Description dt = Description.createTestDescription(getClass(), "testThis");
		p.testStarted(db);
		p.testStarted(dt);
		StringBuilder b = new StringBuilder();
		p.print(StringUtil.asPrintStream(b));
		String output = b.toString();
		assertThat(output, containsString("should be\n"));
		assertThat(output, containsString("test this\n"));
	}

}
