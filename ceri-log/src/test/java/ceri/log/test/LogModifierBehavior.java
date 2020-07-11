package ceri.log.test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class LogModifierBehavior {
	private static final Logger logger = LogManager.getLogger();
	private static int count = 0;

	@Before
	public void before() {
		count = 0;
	}

	@Test
	public void shouldTemporarilyChangeLogLevel() {
		log(logger);
		LogModifier.run(getClass(), Level.ERROR, () -> {
			assertThat(logger.getLevel(), is(Level.ERROR));
			log(logger);
		});
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
		try (LogModifier mod = LogModifier.builder().set(obj.getClass(), Level.OFF).build()) {
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
