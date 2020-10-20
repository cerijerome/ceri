package ceri.process.nmcli;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertMap;
import static ceri.common.test.TestUtil.exerciseEquals;
import java.util.Map;
import org.junit.Test;

public class ConShowIdResultBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ConShowIdResult t = ConShowIdResult.of(Map.of("A", "a", "B", "--"));
		ConShowIdResult eq0 = ConShowIdResult.of(Map.of("A", "a", "B", "--"));
		ConShowIdResult ne0 = ConShowIdResult.NULL;
		ConShowIdResult ne1 = ConShowIdResult.of(Map.of("A", "a", "B", ""));
		exerciseEquals(t, eq0);
		assertAllNotEqual(t, ne0, ne1);
	}

	@Test
	public void shouldExposeValues() {
		ConShowIdResult t = ConShowIdResult.of(Map.of("A", "a", "B", "--"));
		assertMap(t.values, "A", "a", "B", "--");
	}

	@Test
	public void shouldSkipBadOutputLines() {
		String output = "A: a\nB\nC: --";
		assertMap(ConShowIdResult.fromOutput(output).values, "A", "a", "C", "--");
	}

}
