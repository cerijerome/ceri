package ceri.home.teatree;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.common.property.PropertyUtil;
import ceri.speech.grammar.ActionGrammar;

public class ControlGrammar extends ActionGrammar {
	private static final Logger logger = LogManager.getLogger();
	private static final String GRAMMAR_NAME = ControlGrammar.class.getName();
	private final CountDownLatch countDownLatch;
	private final Properties state;
	private final File stateFile;

	public ControlGrammar(CountDownLatch countDownLatch, Properties state, File stateFile) {
		super(GRAMMAR_NAME, createJsgf());
		this.countDownLatch = countDownLatch;
		this.state = state;
		this.stateFile = stateFile;
	}

	private static String createJsgf() {
		try {
			return IoUtil.getClassResourceAsString(ControlGrammar.class, "jsgf");
		} catch (IOException e) {
			throw new IllegalStateException("Unable to load resource", e);
		}
	}

	@Override
	public boolean parseRule(String rule, List<String> tags) {
		if ("exit".equals(rule)) countDownLatch.countDown();
		else if ("state".equals(rule)) {
			try {
				PropertyUtil.store(state, stateFile);
			} catch (IOException e) {
				logger.catching(e);
			}
		} else return false;
		return true;
	}

}
