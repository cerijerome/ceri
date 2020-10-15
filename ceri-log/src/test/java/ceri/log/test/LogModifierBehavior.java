package ceri.log.test;

import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import ceri.log.util.LogUtil;

public class LogModifierBehavior {
	private static final Logger logger = LogManager.getLogger();
	private static int count = 0;

	@Before
	public void before() {
		count = 0;
	}

	@Test
	public void shouldTemporarilyChangeNamedLogLevel() {
		log(logger);
		LogModifier.run(() -> {
			assertThat(logger.getLevel(), is(Level.ERROR));
			log(logger);
		}, Level.ERROR, LogUtil.loggerName(getClass()));
		log(logger);
	}

	@Test
	public void shouldTemporarilyChangeClassLogLevel() {
		log(logger);
		LogModifier.run(() -> {
			assertThat(logger.getLevel(), is(Level.ERROR));
			log(logger);
		}, Level.ERROR, getClass());
		log(logger);
	}

	@Test
	public void shouldAllowAnonymousClassLoggers() {
		Object obj = new Object() {
			@Override
			public String toString() {
				log(LogManager.getLogger());
				return super.toString();
			}
		};
		obj.toString();
		try (LogModifier mod = LogModifier.builder().set(Level.OFF, obj.getClass()).build()) {
			obj.toString();
		}
		obj.toString();
	}

	private static void log(Logger logger) {
		int n = count++;
		logger.debug("debug {}", n);
		logger.trace("trace {}", n);
	}

}
