package ceri.common.svg;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.exerciseEnum;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Before;
import org.junit.Test;

public class LargeArcFlagBehavior {

	@Before
	public void init() {
		exerciseEnum(LargeArcFlag.class);
	}

	@Test
	public void shouldReverse() {
		assertThat(LargeArcFlag.large.reverse(), is(LargeArcFlag.small));
		assertThat(LargeArcFlag.small.reverse(), is(LargeArcFlag.large));
	}

}
