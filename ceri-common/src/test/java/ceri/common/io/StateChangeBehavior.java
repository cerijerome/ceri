package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import org.junit.Test;
import ceri.common.event.Listenable;

public class StateChangeBehavior {

	@Test
	public void testFixable() {
		var fixable = new StateChange.Fixable() {
			@Override
			public Listenable<StateChange> listeners() {
				return Listenable.ofNull();
			}
		};
		fixable.broken(); // does nothing
	}

	@Test
	public void shouldConvertBooleanToStateChange() {
		assertEquals(StateChange.from(null), StateChange.none);
		assertEquals(StateChange.from(true), StateChange.fixed);
		assertEquals(StateChange.from(false), StateChange.broken);
	}

	@Test
	public void shouldDecodeIntToStateChange() {
		assertNull(StateChange.xcoder.decode(-1));
		assertNull(StateChange.xcoder.decode(3));
		assertEquals(StateChange.xcoder.decode(0), StateChange.none);
		assertEquals(StateChange.xcoder.decode(1), StateChange.fixed);
		assertEquals(StateChange.xcoder.decode(2), StateChange.broken);
	}

}
