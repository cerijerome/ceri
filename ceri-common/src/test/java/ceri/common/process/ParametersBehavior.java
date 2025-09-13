package ceri.common.process;

import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertOrdered;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ParametersBehavior {

	@Test
	public void shouldNotBreachEqualsContract() {
		Parameters t = Parameters.of(1, "a", 0.1);
		Parameters eq0 = Parameters.of(1, "a", 0.1);
		Parameters eq1 = Parameters.ofAll(new Object[] { 1, "a", 0.1 });
		Parameters eq2 = Parameters.ofAll(Arrays.asList(1, "a", 0.1));
		Parameters eq3 = Parameters.ofAll(t);
		Parameters ne0 = Parameters.of();
		Parameters ne1 = Parameters.of(1, "a");
		Parameters ne2 = Parameters.of(1, "b", 0.1);
		TestUtil.exerciseEquals(t, eq0, eq1, eq2, eq3);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldAcceptNullValues() {
		assertOrdered(Parameters.of().add(null, "a", null, 1).list(), null, "a", null, "1");
	}

}
