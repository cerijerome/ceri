package ceri.common.io;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.nio.file.Path;
import java.util.function.Predicate;
import org.junit.Test;

public class PathPatternBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		PathPattern p = PathPattern.glob("*.java");
		PathPattern eq0 = PathPattern.glob("*.java");
		PathPattern eq1 = PathPattern.of("glob:*.java");
		PathPattern ne0 = PathPattern.glob("*.javax");
		PathPattern ne1 = PathPattern.of("*.java");
		PathPattern ne2 = PathPattern.regex("*.java");
		exerciseEquals(p, eq0, eq1);
		assertAllNotEqual(p, ne0, ne1, ne2);
	}

	@Test
	public void shouldProvideMatcher() {
		Predicate<Path> test = PathPattern.glob("*.java").matcher();
		assertThat(test.test(Path.of("Test.java")), is(true));
		assertThat(test.test(Path.of("org/junit/Test.java")), is(false));
		test = PathPattern.glob("**/*.java").matcher();
		assertThat(test.test(Path.of("Test.java")), is(false));
		assertThat(test.test(Path.of("org/junit/Test.java")), is(true));
	}

}
