package ceri.common.io;

import static ceri.common.test.TestUtil.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;

public class StateChangeBehavior {

	@Test
	public void shouldConvertBooleanToStateChange() {
		assertThat(StateChange.from(null), is(StateChange.none));
		assertThat(StateChange.from(true), is(StateChange.fixed));
		assertThat(StateChange.from(false), is(StateChange.broken));
	}

	@Test
	public void shouldDecodeIntToStateChange() {
		assertNull(StateChange.xcoder.decode(-1));
		assertNull(StateChange.xcoder.decode(3));
		assertThat(StateChange.xcoder.decode(0), is(StateChange.none));
		assertThat(StateChange.xcoder.decode(1), is(StateChange.fixed));
		assertThat(StateChange.xcoder.decode(2), is(StateChange.broken));
	}

}
