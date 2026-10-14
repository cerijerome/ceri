package ceri.common.test;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import ceri.common.collect.Immutable;
import ceri.common.collect.Sets;
import ceri.common.function.Compares;
import ceri.common.text.Regex;
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

	public static void exec(PrintStream out, Class<?>... classes) {
		var discoveryRequest = LauncherDiscoveryRequestBuilder.request()
			.selectors(DiscoverySelectors.selectClasses(classes)).build();
		var summaryListener = new SummaryGeneratingListener();
		var tp = new TestPrinter();
		try (var session = LauncherFactory.openSession(); var writer = new PrintWriter(out)) {
			var launcher = session.getLauncher();
			var testPlan = launcher.discover(discoveryRequest);
			launcher.execute(testPlan, tp, summaryListener);
			tp.print(out);
			summaryListener.getSummary().printTo(writer);
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

	private boolean captureTest(String testClassName, String className, String methodName) {
		var group = Regex.findGroup(BEHAVIOR_METHOD_PATTERN, methodName, 1);
		if (group != null)
			return behaviors.add(new Test(testClassName, className, getBehaviorDescription(group)));
		group = Regex.findGroup(TEST_METHOD_PATTERN, methodName, 1);
		if (group != null)
			return tests.add(new Test(testClassName, className, getTestDescription(group)));
		return tests.add(new Test(testClassName, className, methodName));
	}

	private static String getBehaviorDescription(String methodName) {
		return Text.toPhrase(methodName);
	}

	private static String getTestDescription(String methodName) {
		return "test " + Text.firstToLower(methodName);
	}
}
