package ceri.common.test;

import java.io.PrintStream;
import java.util.Collection;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import ceri.common.util.TextUtil;

/**
 * Prints out tests and behaviors in readable phrases.
 */
public class TestPrinter extends RunListener {
	private static final Pattern CLASS_NAME_PATTERN = Pattern.compile("^(.*?)(?:Test|Behavior)$");
	private static final Pattern BEHAVIOR_METHOD_PATTERN = Pattern.compile("^(should.*)$");
	private static final Pattern TEST_METHOD_PATTERN = Pattern.compile("^test(.*)$");
	private final Collection<String> tests = new TreeSet<>();
	private final Collection<String> behaviors = new TreeSet<>();
	
	public void print(PrintStream out) {
		out.println("Behaviors:");
		for (String s : behaviors) out.println(s);
		out.println("Tests:");
		for (String s : tests) out.println(s);
	}
	
	@Override
	public void testStarted(Description description) throws Exception {
		capture(description);
	}
	
	private void capture(Description description) {
		String testClassName = description.getClassName();
		Matcher m = CLASS_NAME_PATTERN.matcher(description.getTestClass().getSimpleName());
		if (!m.find()) return;
		String className = m.group(1);
		
		String methodName = description.getMethodName();
		String prefix = testClassName + ": " + className + " ";
		m = BEHAVIOR_METHOD_PATTERN.matcher(methodName);
		if (m.find()) {
			behaviors.add(prefix + getBehaviorDescription(m.group(1)));
			return;
		}
		m = TEST_METHOD_PATTERN.matcher(methodName);
		if (m.find()) {
			tests.add(prefix + getTestDescription(m.group(1)));
			return;
		}
		tests.add(prefix + methodName);
		
		for (Description child : description.getChildren())
			capture(child);
	}
	
	private static String getBehaviorDescription(String methodName) {
		return TextUtil.toPhrase(methodName);
	}
	
	private static String getTestDescription(String methodName) {
		return "test " + TextUtil.firstToLower(methodName);
	}
	
}
