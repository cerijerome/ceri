package ceri.common.test;

import java.io.PrintStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import ceri.common.collect.Immutable;
import ceri.common.collect.Sets;
import ceri.common.function.Compares;
import ceri.common.text.Text;

/**
 * Prints out tests (traditional unit tests) and behaviors (BDD-style) in readable phrases. Works
 * with JUnitCore running a suite of test classes. Used by TestUtil.exec
 */
public class TestPrinter implements TestExecutionListener {
	private static final Pattern BEHAVIOR_METHOD_PATTERN = Pattern.compile("^(should.*)$");
	private static final Pattern TEST_METHOD_PATTERN = Pattern.compile("^test(.*)$");
	private final Set<Test> tests = Sets.tree();
	private final Set<Test> behaviors = Sets.tree();

	public record Test(String testClassName, String className, String description)
		implements Comparable<Test> {

		@Override
		public int compareTo(Test test) {
			return Compares.STRING.compare(toString(), test.toString());
		}

		@Override
		public String toString() {
			return testClassName() + ": " + className() + " " + description();
		}
	}

	/**
	 * Prints captured phrases to given PrintStream.
	 */
	public void print(PrintStream out) {
		out.println("Behaviors:");
		for (Test test : behaviors)
			out.println(test);
		out.println("Tests:");
		for (Test test : tests)
			out.println(test);
	}

	/**
	 * Returns the list of tests only.
	 */
	public Set<Test> tests() {
		return Immutable.wrap(tests);
	}

	/**
	 * Returns the list of behavior tests only.
	 */
	public Set<Test> behaviors() {
		return Immutable.wrap(behaviors);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (!testIdentifier.isTest()) return;
		var source = testIdentifier.getSource().orElse(null);
		if (!(source instanceof MethodSource method)) return;
		var testClass = method.getJavaClass();
		var testClassName = testClass.getSimpleName();
		var methodName = method.getMethodName();
		var className = Testing.Style.target(testClassName);
		captureTest(testClassName, className, methodName);
	}

	private void captureTest(String testClassName, String className, String methodName) {
		Matcher m;
		m = BEHAVIOR_METHOD_PATTERN.matcher(methodName);
		if (m.find()) {
			behaviors.add(new Test(testClassName, className, getBehaviorDescription(m.group(1))));
			return;
		}
		m = TEST_METHOD_PATTERN.matcher(methodName);
		if (m.find()) {
			tests.add(new Test(testClassName, className, getTestDescription(m.group(1))));
			return;
		}
		tests.add(new Test(testClassName, className, methodName));
	}

	private static String getBehaviorDescription(String methodName) {
		return Text.toPhrase(methodName);
	}

	private static String getTestDescription(String methodName) {
		return "test " + Text.firstToLower(methodName);
	}
}
