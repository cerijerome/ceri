package ceri.common.test;

import java.io.PrintStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;
import ceri.common.collect.Iterables;
import ceri.common.reflect.Reflect;
import ceri.common.text.StringBuilders;

public class TestPrinterBehavior {
	private static final String CLS_NAME = TestPrinterBehavior.class.getName();
	private PrintStream out = null;

	@AfterEach
	public void after() {
		out = Testing.close(out);
	}

	@Test
	public void shouldConvertBehaviorMethodNameToPhrase() {
		var tp = new TestPrinter();
		tp.executionStarted(identifier());
		Assert.equal(tp.tests().size(), 0);
		Assert.equal(tp.behaviors().size(), 1);
		Assert.string(Iterables.first(tp.behaviors()).description(),
			"should convert behavior method name to phrase");
	}

	@Test
	public void testExtractTestMethodName() {
		var tp = new TestPrinter();
		tp.executionStarted(identifier());
		Assert.equal(tp.behaviors().size(), 0);
		Assert.equal(tp.tests().size(), 1);
		Assert.string(Iterables.first(tp.tests()).description(), "test extractTestMethodName");
	}

	@Test
	public void shouldPrintBehaviorsAndTests() {
		var tp = new TestPrinter();
		tp.executionStarted(identifier("shouldBeGood"));
		tp.executionStarted(identifier("testThisMethod"));
		tp.executionStarted(identifier("unknownTest"));
		tp.executionStarted(identifier(null));
		var b = new StringBuilder();
		out = StringBuilders.printStream(b);
		tp.print(out);
		var output = b.toString();
		Assert.find(output, "should be good\n");
		Assert.find(output, "test thisMethod\n");
		Assert.find(output, "unknownTest\n");
	}

	private static TestIdentifier identifier() {
		return identifier(Reflect.previousMethodName(1));
	}

	private static TestIdentifier identifier(String method) {
		return TestIdentifier.from(
			new AbstractTestDescriptor(uniqueId(method), displayName(method), source(method)) {
				@Override
				public TestDescriptor.Type getType() {
					return TestDescriptor.Type.TEST;
				}
			});
	}

	private static UniqueId uniqueId(String method) {
		var id = UniqueId.forEngine(TestPrinter.class.getName()).append("class", CLS_NAME);
		return method == null ? id : id.append("method", method);
	}

	private static String displayName(String method) {
		return method == null ? CLS_NAME : CLS_NAME + "." + method;
	}

	private static TestSource source(String method) {
		return method == null ? ClassSource.from(CLS_NAME) : MethodSource.from(CLS_NAME, method);
	}
}
