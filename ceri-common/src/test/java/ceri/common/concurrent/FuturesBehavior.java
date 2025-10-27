package ceri.common.concurrent;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import ceri.common.test.Assert;

public class FuturesBehavior {

	@Test
	public void shouldProvideDoneFuture() throws Exception {
		var f = Futures.done("test");
		Assert.equal(f.cancel(true), false);
		Assert.equal(f.isCancelled(), false);
		Assert.equal(f.isDone(), true);
		Assert.equal(f.get(1000, TimeUnit.SECONDS), "test");
		f.run();
	}

	@Test
	public void shouldProvideCancelledFuture() throws Exception {
		var f = Futures.cancelled();
		Assert.equal(f.cancel(true), false);
		Assert.equal(f.isCancelled(), true);
		Assert.equal(f.isDone(), true);
		Assert.equal(f.get(1000, TimeUnit.SECONDS), null);
		f.run();
	}

	@Test
	public void shouldProvideErrorFuture() throws Exception {
		var e = new IOException("test");
		var f = Futures.error(e);
		Assert.equal(f.cancel(true), false);
		Assert.equal(f.isCancelled(), false);
		Assert.equal(f.isDone(), true);
		try {
			Assert.equal(f.get(1000, TimeUnit.SECONDS), "test");
			Assert.fail();
		} catch (ExecutionException ee) {
			Assert.same(ee.getCause(), e);
		}
		f.run();
	}
}
