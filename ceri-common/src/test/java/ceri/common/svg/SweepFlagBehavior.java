package ceri.common.svg;

import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import org.junit.Before;
import org.junit.Test;

public class SweepFlagBehavior {

	@Before
	public void init() {
		exerciseEnum(SweepFlag.class);
	}

	@Test
	public void shouldReverse() {
		assertThat(SweepFlag.negative.reverse(), is(SweepFlag.positive));
		assertThat(SweepFlag.positive.reverse(), is(SweepFlag.negative));
	}

}
