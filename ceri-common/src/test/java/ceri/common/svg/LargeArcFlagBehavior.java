package ceri.common.svg;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.TestUtil.exerciseEnum;
import org.junit.Before;
import org.junit.Test;

public class LargeArcFlagBehavior {

	@Before
	public void init() {
		exerciseEnum(LargeArcFlag.class);
	}

	@Test
	public void shouldReverse() {
		assertEquals(LargeArcFlag.large.reverse(), LargeArcFlag.small);
		assertEquals(LargeArcFlag.small.reverse(), LargeArcFlag.large);
	}

}
