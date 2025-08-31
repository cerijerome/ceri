package ceri.common.test;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.TestUtil.exerciseEquals;
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
		exerciseEquals(test, test1);
		assertAllNotEqual(test, test2, test3, test4);
	}

	@Test
	public void shouldIterateOverChildren() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(MyClass.class, "myMethod");
		d.addChild(Description.createTestDescription(MySubClass.class, "mySubMethod"));
		p.testStarted(d);
		assertEquals(p.tests().size(), 2);
		Iterator<TestPrinter.Test> i = p.tests().iterator();
		assertEquals(i.next().description, "mySubMethod");
		assertEquals(i.next().description, "myMethod");
	}

	@Test
	public void shouldConvertBehaviorMethodNameToPhrase() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(getClass(), "shouldConvertToPhrase");
		p.testStarted(d);
		assertTrue(p.tests().isEmpty());
		assertEquals(p.behaviors().size(), 1);
		assertEquals(p.behaviors().iterator().next().description, "should convert to phrase");
	}

	@Test
	public void shouldExtractTestMethodName() {
		TestPrinter p = new TestPrinter();
		Description d = Description.createTestDescription(getClass(), "testThisMethod");
		p.testStarted(d);
		assertTrue(p.behaviors().isEmpty());
		assertEquals(p.tests().iterator().next().description, "test thisMethod");
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
		assertTrue(output.contains("should be\n"));
		assertTrue(output.contains("test this\n"));
	}

}
