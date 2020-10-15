package ceri.x10.command;

import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class UnitBehavior {

	@Test
	public void shouldLookupFromIndex() {
		assertThat(Unit.from(1), is(Unit._1));
		assertThat(Unit.from(10), is(Unit._10));
		assertThat(Unit.from(16), is(Unit._16));
		assertThrown(() -> Unit.from(0));
		assertThrown(() -> Unit.from(17));
	}

}
