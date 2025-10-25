package ceri.common.io;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class StateChangeBehavior {

	@Test
	public void shouldConvertBooleanToStateChange() {
		assertEquals(StateChange.from(null), StateChange.none);
		assertEquals(StateChange.from(true), StateChange.fixed);
		assertEquals(StateChange.from(false), StateChange.broken);
	}

	@Test
	public void shouldDecodeIntToStateChange() {
		Assert.isNull(StateChange.xcoder.decode(-1));
		Assert.isNull(StateChange.xcoder.decode(3));
		assertEquals(StateChange.xcoder.decode(0), StateChange.none);
		assertEquals(StateChange.xcoder.decode(1), StateChange.fixed);
		assertEquals(StateChange.xcoder.decode(2), StateChange.broken);
	}

}
