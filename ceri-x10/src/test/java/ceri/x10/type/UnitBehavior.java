package ceri.x10.type;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class UnitBehavior {

	@Test
	public void shouldCreateFromIndex() {
		assertThat(Unit.from(1), is(Unit._1));
		assertThat(Unit.from(10), is(Unit._10));
		assertThat(Unit.from(16), is(Unit._16));
	}

}
