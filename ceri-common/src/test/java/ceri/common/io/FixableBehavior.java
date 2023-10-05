package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.test.TestFixable;

public class FixableBehavior {

	@Test
	public void shouldOpenSilently() throws IOException {
		try (var f = new Fixable.Null() {}) {
			Fixable.openSilently(f).close();
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldWrapDelegate() throws IOException {
		var f = TestFixable.of();
		var w = new Fixable.Wrapper<>(f);
		var c = Captor.<StateChange>of();
		w.listeners().listen(c);
		assertEquals(w.name(), f.name());
		w.open();
		f.open.assertCall(true);
		w.broken();
		f.broken.assertCall(true);
		c.verify(StateChange.broken);
		w.close();
		f.close.assertCalls(1);
	}

}
