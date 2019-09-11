package ceri.common.test;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.Description;
import ceri.common.test.TestPrinterBehavior.MyClass.MySubClass;
import ceri.common.text.StringUtil;

public class TestPrinterBehavior {

	static class MyClass {
		static class MySubClass {
			void mySubMethod() {}
		}

		void myMethod() {}
	}

	@Test
	public void shouldObeyEqualsContract() {
		TestPrinter.Test test = new TestPrinter.Test("testClassName", "className", "description");
		TestPrinter.Test test1 = new TestPrinter.Test("testClassName", "className", "description");
		TestPrinter.Test test2 = new TestPrinter.Test("testClassName0", "className", "description");
		TestPrinter.Test test3 = new TestPrinter.Test("testClassName", "className0", "description");
		TestPrinter.Test test4 = new TestPrinter.Test("testClassName", "className", "description0");
		exerciseEquals(test, test1);
		assertAllNotEqual(test, test2, test3, test4);
	}

	@Test
	public void shouldIterateOverChildren() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(MyClass.class, "myMethod");
		d.addChild(Description.createTestDescription(MySubClass.class, "mySubMethod"));
		p.testStarted(d);
		assertThat(p.tests().size(), is(2));
		Iterator<TestPrinter.Test> i = p.tests().iterator();
		assertThat(i.next().description, is("mySubMethod"));
		assertThat(i.next().description, is("myMethod"));
	}

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
