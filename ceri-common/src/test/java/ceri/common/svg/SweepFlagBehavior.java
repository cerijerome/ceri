package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Before;
import org.junit.Test;

public class SweepFlagBehavior {

	@Before
	public void init() {
		exerciseEnum(SweepFlag.class);
	}

	@Test
	public void shouldReverse() {
		assertEquals(SweepFlag.negative.reverse(), SweepFlag.positive);
		assertEquals(SweepFlag.positive.reverse(), SweepFlag.negative);
	}

}
