package ceri.common.test;

import java.io.PrintStream;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import ceri.common.collect.Immutable;
import ceri.common.collect.Sets;
import ceri.common.function.Compares;
import ceri.common.text.Text;

/**
 * Prints out tests (traditionl unit tests) and behaviors (BDD-style) in readable phrases. Works
 * with JUnitCore running a suite of test classes. Used by TestUtil.exec
 */
public class TestPrinter extends RunListener {
	private static final Pattern BEHAVIOR_METHOD_PATTERN = Pattern.compile("^(should.*)$");
	private static final Pattern TEST_METHOD_PATTERN = Pattern.compile("^test(.*)$");
	private final Set<Test> tests = Sets.tree();
	private final Set<Test> behaviors = Sets.tree();

	public static final class Test implements Comparable<Test> {
		public final String testClassName;
		public final String className;
		public final String description;
		private final String toString;
		private final int hashCode;

		Test(String testClassName, String className, String description) {
			this.testClassName = testClassName;
			this.className = className;
			this.description = description;
			toString = testClassName + ": " + className + " " + description;
			hashCode = toString.hashCode();
		}

		@Override
		public int compareTo(Test test) {
			return Compares.STRING.compare(toString(), test.toString());
		}

		@Override
		public String toString() {
			return toString;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			return (obj instanceof Test other) && toString.equals(other.toString);
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

	/**
	 * Called by JUnitCore each time a test starts.
	 */
	@Override
	public void testStarted(Description description) {
		capture(description);
	}

	private void capture(Description description) {
		String testClassName = description.getClassName();
		String simpleName = description.getTestClass().getSimpleName();
		String className = TestStyle.target(simpleName);
		String methodName = description.getMethodName();
		captureTest(testClassName, className, methodName);

		for (Description child : description.getChildren())
			capture(child);
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
