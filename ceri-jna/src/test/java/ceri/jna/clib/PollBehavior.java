package ceri.jna.clib;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.jna.clib.Poll.Error.POLLNVAL;
import static ceri.jna.clib.Poll.Event.POLLIN;
import static ceri.jna.clib.Poll.Event.POLLOUT;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.log.util.LogUtil;

public class PollBehavior {
	private Pipe pipe;
	private Poll poll;

	@After
	public void after() {
		LogUtil.close(pipe);
		pipe = null;
		poll = null;
	}

	@Test
	public void shouldProvideAllEvents() throws IOException {
		initPipe();
		assertFalse(poll.fd(0).has(POLLIN));
		assertFalse(poll.fd(1).has(POLLOUT));
		writeToPipe(0);
		assertEquals(poll.poll(), 2);
		assertCollection(poll.responses(), POLLOUT, POLLIN);
		assertTrue(poll.fd(0).has(POLLIN));
		assertTrue(poll.fd(1).has(POLLOUT));
		assertCollection(poll.fd(0).responses(), POLLIN);
		assertCollection(poll.fd(1).responses(), POLLOUT);
	}

	@Test
	public void shouldProvideAllErrors() throws IOException {
		initPipe();
		assertFalse(poll.fd(0).has(POLLNVAL));
		assertFalse(poll.fd(1).has(POLLNVAL));
		writeToPipe(0);
		pipe.close();
		pipe = null;
		assertEquals(poll.poll(), 2);
		assertCollection(poll.responses());
		assertCollection(poll.errors(), POLLNVAL);
		assertTrue(poll.fd(0).has(POLLNVAL));
		assertTrue(poll.fd(1).has(POLLNVAL));
		assertCollection(poll.fd(0).errors(), POLLNVAL);
		assertCollection(poll.fd(1).errors(), POLLNVAL);
	}

	@Test
	public void shouldValidateForErrors() throws IOException {
		initPipe();
		assertEquals(poll.poll(), 1);
		poll.validate();
		pipe.close();
		pipe = null;
		assertEquals(poll.poll(), 2);
		assertThrown(IOException.class, ".*POLLNVAL.*", () -> poll.validate());
	}

	@SuppressWarnings("resource")
	private void writeToPipe(int... bytes) throws IOException {
		pipe.out().write(ArrayUtil.bytes(bytes));
	}

	private void initPipe() throws IOException {
		pipe = Pipe.of();
		pipe.blocking(false);
		poll = Poll.of(2);
		assertEquals(poll.size(), 2);
		poll.fd(0).fd(pipe.read).request(POLLIN);
		poll.fd(1).fd(pipe.write).request(POLLOUT);
	}
}
