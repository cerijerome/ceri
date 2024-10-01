package ceri.common.score;

import static ceri.common.test.AssertUtil.assertString;
import org.junit.Test;
import ceri.common.score.Scorer.Result;

public class ScorerBehavior {

	@Test
	public void shouldProvideResultStringRepresentation() {
		var result = new Result<>("test", 123);
		assertString(result, "test=123.0");
	}

}
