package ceri.jna.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ceri.common.test.Assert;
import ceri.common.test.TestConcurrent;

public class ThreadBuffersBehavior {
	private ThreadBuffers buffers;

	@BeforeEach
	public void before() {
		buffers = ThreadBuffers.of();
		buffers.size(3);
	}

	@AfterEach
	public void after() {
		buffers.close();
	}

	@Test
	public void shouldSetSize() {
		buffers.size(2);
		Assert.equal(buffers.size(), 2L);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldChangeSize() {
		Assert.equal(buffers.get().size(), 3L);
		buffers.size(2);
		Assert.equal(buffers.get().size(), 2L);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReallocateFreedBuffer() {
		var m = buffers.get();
		Assert.same(buffers.get(), m);
		m.close();
		Assert.notSame(buffers.get(), m);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideBufferPerThread() {
		var m = TestConcurrent.threadCall(buffers::get);
		Assert.notSame(buffers.get(), m);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldRemoveBuffer() {
		var m = buffers.get();
		buffers.remove();
		Assert.notSame(buffers.get(), m);
	}

}
