package ceri.log.io;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ceri.common.test.Testing;
import ceri.log.util.TestLog;

public class LogStreamBehavior {
	private TestLog testLog;
	private LogStream stream;

	@AfterEach
	public void afterEach() {
		stream = Testing.close(stream);
		testLog = Testing.close(testLog);
	}

	@Test
	public void shouldCreateCreateWithDefaultLogger() {
		stream = LogStream.of();
		stream.print("test");
	}

	@Test
	public void shouldSetLevel() {
		testLog = TestLog.of();
		stream = LogStream.of(testLog.logger());
		stream.print("test1");
		stream.flush();
		testLog.assertFind("DEBUG .* test1");
		stream.level(Level.INFO);
		stream.print("test2");
		stream.flush();
		testLog.assertFind("INFO .* test2");
	}
}
