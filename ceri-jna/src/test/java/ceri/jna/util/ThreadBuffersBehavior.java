package ceri.jna.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotSame;
import static ceri.common.test.AssertUtil.assertSame;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ThreadBuffersBehavior {
	private ThreadBuffers buffers;

	@Before
	public void before() {
		buffers = ThreadBuffers.of();
		buffers.size(3);
	}

	@After
	public void after() {
		buffers.close();
	}

	@Test
	public void shouldSetSize() {
		buffers.size(2);
		assertEquals(buffers.size(), 2L);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldChangeSize() {
		assertEquals(buffers.get().size(), 3L);
		buffers.size(2);
		assertEquals(buffers.get().size(), 2L);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldReallocateFreedBuffer() {
		var m = buffers.get();
		assertSame(buffers.get(), m);
		m.close();
		assertNotSame(buffers.get(), m);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldProvideBufferPerThread() {
		var m = TestUtil.threadCall(buffers::get);
		assertNotSame(buffers.get(), m);
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldRemoveBuffer() {
		var m = buffers.get();
		buffers.remove();
		assertNotSame(buffers.get(), m);
	}

}
