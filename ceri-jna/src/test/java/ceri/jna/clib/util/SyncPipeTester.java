package ceri.jna.clib.util;

import static ceri.common.test.TestUtil.threadRun;
import static ceri.log.util.LogUtil.create;
import java.io.IOException;
import java.util.stream.IntStream;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.function.Enclosure;
import ceri.common.test.TestUtil;
import ceri.jna.clib.Poll;
import ceri.log.test.LogModifier;

/**
 * Tests polling on multiple threads, and signaling the pipe from another.
 */
public class SyncPipeTester {
	private static final Logger logger = LogManager.getFormatterLogger();
	private static final int DELAY_MS = 200;
	private static final int THREADS = 10;

	static {
		LogModifier.set(Level.INFO, SyncPipeTester.class);
	}

	public static void main(String[] args) throws Exception {
		var n = IntStream.range(0, THREADS).boxed().toList();
		Poll poll = Poll.of(1);
		try (var pipe = SyncPipe.of(poll.fd(0));
			var pollers = Enclosure.ofAll(create(i -> threadRun(() -> runPoll(poll, i)), n));
			var closer = TestUtil.threadRun(() -> runClose(pipe))) {
			closer.get();
			pollers.ref.forEach(SimpleExecutor::get);
		}
		logger.info("done");
	}

	private static void runPoll(Poll poll, int i) throws IOException {
		Concurrent.delay(DELAY_MS * i);
		logger.info("[%d] poll start", i);
		poll.poll(DELAY_MS * THREADS); // don't clear sync pipe
		logger.info("[%d] poll complete", i);
	}

	private static void runClose(SyncPipe.Fixed pipe) throws IOException {
		Concurrent.delay(DELAY_MS * THREADS / 2);
		logger.info("signal");
		pipe.signal();
		Concurrent.delay(DELAY_MS);
	}
}
