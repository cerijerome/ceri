package ceri.common.io;

import org.junit.Test;
import ceri.common.test.Assert;

public class StateChangeBehavior {

	@Test
	public void shouldConvertBooleanToStateChange() {
		Assert.equal(StateChange.from(null), StateChange.none);
		Assert.equal(StateChange.from(true), StateChange.fixed);
		Assert.equal(StateChange.from(false), StateChange.broken);
	}

	@Test
	public void shouldDecodeIntToStateChange() {
		Assert.isNull(StateChange.xcoder.decode(-1));
		Assert.isNull(StateChange.xcoder.decode(3));
		Assert.equal(StateChange.xcoder.decode(0), StateChange.none);
		Assert.equal(StateChange.xcoder.decode(1), StateChange.fixed);
		Assert.equal(StateChange.xcoder.decode(2), StateChange.broken);
	}

}
