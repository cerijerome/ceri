package ceri.common.test;

import java.util.Iterator;
import org.junit.Test;
import org.junit.runner.Description;
import ceri.common.test.TestPrinterBehavior.MyClass.MySubClass;
import ceri.common.text.StringBuilders;

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
		Testing.exerciseEquals(test, test1);
		Assert.notEqualAll(test, test2, test3, test4);
	}

	@Test
	public void shouldIterateOverChildren() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(MyClass.class, "myMethod");
		d.addChild(Description.createTestDescription(MySubClass.class, "mySubMethod"));
		p.testStarted(d);
		Assert.equal(p.tests().size(), 2);
		Iterator<TestPrinter.Test> i = p.tests().iterator();
		Assert.equal(i.next().description, "mySubMethod");
		Assert.equal(i.next().description, "myMethod");
	}

	@Test
	public void shouldConvertBehaviorMethodNameToPhrase() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(getClass(), "shouldConvertToPhrase");
		p.testStarted(d);
		Assert.yes(p.tests().isEmpty());
		Assert.equal(p.behaviors().size(), 1);
		Assert.equal(p.behaviors().iterator().next().description, "should convert to phrase");
	}

	@Test
	public void shouldExtractTestMethodName() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(getClass(), "testThisMethod");
		p.testStarted(d);
		Assert.yes(p.behaviors().isEmpty());
		Assert.equal(p.tests().iterator().next().description, "test thisMethod");
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldPrintBehaviorsAndTests() {
		TestPrinter p = new TestPrinter();
		Description db = Description.createTestDescription(getClass(), "shouldBe");
		Description dt = Description.createTestDescription(getClass(), "testThis");
		p.testStarted(db);
		p.testStarted(dt);
		StringBuilder b = new StringBuilder();
		p.print(StringBuilders.printStream(b));
		String output = b.toString();
		Assert.yes(output.contains("should be\n"));
		Assert.yes(output.contains("test this\n"));
	}

}
