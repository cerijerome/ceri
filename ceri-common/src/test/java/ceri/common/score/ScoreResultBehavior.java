package ceri.common.score;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.exerciseEquals;
import org.junit.Test;

public class ScoreResultBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		ScoreResult<String> r0 = ScoreResult.of("abc", 3);
		ScoreResult<String> r1 = ScoreResult.of("abc", 3);
		ScoreResult<String> r2 = ScoreResult.of("Abc", 3);
		ScoreResult<String> r3 = ScoreResult.of("abc", 4);
		exerciseEquals(r0, r1);
		assertAllNotEqual(r0, r2, r3);
	}

}
