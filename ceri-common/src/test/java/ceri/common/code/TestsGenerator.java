package ceri.common.code;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import ceri.common.io.RuntimeIoException;
import ceri.common.test.TestUtil;
import ceri.common.text.RegexUtil;

public class TestsGenerator {
	private static final Pattern PKG_MOD_REGEX = Pattern.compile("([^\\.]+\\.){2}");
	private static final Pattern TEST_FILE_REGEX =
		Pattern.compile("^(.+(?:Test|Behavior))\\.java$");
	private final Path testRoot;
	private final String className;

	public static void main(String[] args) {
		TestsGenerator gen = new TestsGenerator(Paths.get("src/test/java"), "_Tests");
		System.out.println(gen.generate().toString());
	}

	private TestsGenerator(Path testRoot, String className) {
		this.testRoot = testRoot;
		this.className = className;
	}

	public Context generate() {
		Context con = new Context();
		generate(con, pkgTests(testRoot));
		return con;
	}

	private Map<String, List<Class<?>>> pkgTests(Path dir) {
		try {
			return new TreeMap<>(Files.walk(dir).filter(this::isTestClass).map(this::classFrom)
				.collect(Collectors.groupingBy(this::pkgName)));
		} catch (IOException e) {
			throw new RuntimeIoException(e);
		}
	}

	private Class<?> classFrom(Path path) {
		try {
			String relative = testRoot.relativize(path).toString();
			String fullName =
				RegexUtil.find(TEST_FILE_REGEX, relative).replace(File.separator, ".");
			return Class.forName(fullName);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

	private String pkgName(Class<?> cls) {
		return PKG_MOD_REGEX.matcher(cls.getPackage().getName()).replaceFirst("");
	}

	private boolean isTestClass(Path path) {
		if (!Files.isRegularFile(path)) return false;
		String filename = path.getFileName().toString();
		boolean result = TEST_FILE_REGEX.matcher(filename).matches();
		return result;
	}

	private void generate(Context con, Map<String, List<Class<?>>> pkgTests) {
		con.imports(RunWith.class, Suite.class, TestUtil.class);
		con.println("@RunWith(Suite.class)");
		con.println("@Suite.SuiteClasses({");
		pkgTests.forEach((pkgName, testClasses) -> generatePkgGroup(con, pkgName, testClasses));
		con.println("})");
		con.printf("public class %s {%n", className);
		con.println("\tpublic static void main(String... args) {");
		con.printf("\t\tTestUtil.exec(%s.class);%n", className);
		con.println("\t}");
		con.println("}");
	}

	private void generatePkgGroup(Context con, String pkgName, List<Class<?>> testClasses) {
		con.imports(testClasses);
		con.printf("\t// %s%n", pkgName);
		testClasses.stream().map(cls -> cls.getSimpleName()).sorted()
			.forEach(clsName -> con.printf("\t%s.class, //%n", clsName));
	}

}
