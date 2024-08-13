package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import org.junit.Test;

public class RefBehavior {

	private static class TestRef extends Ref<AutoCloseable> {
		private TestRef(AutoCloseable c) {
			super(c);
		}
	}

	@Test
	public void shouldProvideRefAccess() throws Exception {
		try (AutoCloseable c = () -> {}) {
			var r = new TestRef(c);
			assertEquals(r.ref, c);
		}
	}

}
