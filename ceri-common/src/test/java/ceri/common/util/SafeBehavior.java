package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import org.junit.Test;

public class SafeBehavior {

	private static class Res implements AutoCloseable {
		@Override
		public void close() {}
	}

	private static class Container {
		private final Res r;

		private Container(Res r) {
			this.r = r;
		}

		private Safe<Res> safeR() {
			return Safe.of(r);
		}

		private Res r() {
			return r;
		}
	}

	@Test
	public void shouldProvideEmptyWrappers() {
		var c = new Container(null);
		try (Res r = c.r()) {
			assertEquals(r, c.safeR().res);
		}
	}

	@Test
	public void shouldWrapResources() {
		try (Res r0 = new Res()) {
			var c = new Container(r0);
			try (Res r = c.r()) {
				assertEquals(r0, r);
				assertEquals(r, c.safeR().res);
			}
			assertEquals(c.safeR().res, r0);
		}
	}

	@Test
	public void shouldValidateNonNullResources() {
		assertFalse(Safe.empty().valid());
		assertThrown(() -> Safe.empty().validate());
		try (var r = new Res()) {
			assertTrue(Safe.of(r).valid());
			Safe.of(r).validate();
		}
	}

}
