package ceri.ent.server.web;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JspTestService {
	private static final Logger logger = LogManager.getFormatterLogger();
	private volatile String message;

	public JspTestService(String message) {
		message(message);
	}

	public void log(String format, Object... args) {
		logger.info(format, args);
	}

	public String message() {
		return message;
	}

	public void message(String message) {
		this.message = message;
	}

}
