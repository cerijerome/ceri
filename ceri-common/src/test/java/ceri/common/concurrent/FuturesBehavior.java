package ceri.common.concurrent;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.fail;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.Assert;

public class FuturesBehavior {

	@Test
	public void shouldProvideDoneFuture() throws Exception {
		var f = Futures.done("test");
		assertEquals(f.cancel(true), false);
		assertEquals(f.isCancelled(), false);
		assertEquals(f.isDone(), true);
		assertEquals(f.get(1000, TimeUnit.SECONDS), "test");
		f.run();
	}

	@Test
	public void shouldProvideCancelledFuture() throws Exception {
		var f = Futures.cancelled();
		assertEquals(f.cancel(true), false);
		assertEquals(f.isCancelled(), true);
		assertEquals(f.isDone(), true);
		assertEquals(f.get(1000, TimeUnit.SECONDS), null);
		f.run();
	}

	@Test
	public void shouldProvideErrorFuture() throws Exception {
		var e = new IOException("test");
		var f = Futures.error(e);
		assertEquals(f.cancel(true), false);
		assertEquals(f.isCancelled(), false);
		assertEquals(f.isDone(), true);
		try {
			assertEquals(f.get(1000, TimeUnit.SECONDS), "test");
			fail();
		} catch (ExecutionException ee) {
			Assert.same(ee.getCause(), e);
		}
		f.run();
	}

}
