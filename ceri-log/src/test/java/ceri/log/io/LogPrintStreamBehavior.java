package ceri.log.io;

import org.apache.logging.log4j.Level;
import org.junit.Test;
import ceri.log.util.TestLog;

public class LogPrintStreamBehavior {

	@Test
	public void shouldCreateCreateWithDefaultLogger() {
		try (LogPrintStream stream = LogPrintStream.of()) {
			stream.print("test");
		}
	}

	@Test
	public void shouldSetLevel() {
		try (TestLog testLog = TestLog.of()) {
			try (LogPrintStream stream = LogPrintStream.of(testLog.logger())) {
				stream.print("test1");
				stream.flush();
				testLog.assertFind("DEBUG .* test1");
				stream.level(Level.INFO);
				stream.print("test2");
				stream.flush();
				testLog.assertFind("INFO .* test2");
			}
		}
	}

}
