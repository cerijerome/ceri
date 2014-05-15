package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.common.Alerter;
import ceri.ci.email.EmailContainer;
import ceri.ci.email.EmailEventParser;
import ceri.ci.email.EmailService;
import ceri.common.property.BaseProperties;
import ceri.common.property.PropertyUtil;

/**
 * Creates the alert system that fetches emails, parses them to events, and sends alerts to the
 * alert components.
 */
public class AlertContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final String PROPERTY_FILE_DEF = "alert.properties";
	private final EmailContainer email;
	private final AlertServiceContainer alert;

	public static class Builder {
		public final BaseProperties properties;
		Collection<EmailEventParser> parsers = new ArrayList<>();
		Collection<Alerter> alerters = new ArrayList<>();

		Builder(BaseProperties properties) {
			this.properties = properties;
		}

		public Builder parsers(EmailEventParser... parsers) {
			return parsers(Arrays.asList(parsers));
		}

		public Builder parsers(Collection<EmailEventParser> parsers) {
			this.parsers.addAll(parsers);
			return this;
		}

		public Builder alerters(Alerter... alerters) {
			return alerters(Arrays.asList(alerters));
		}

		public Builder alerters(Collection<Alerter> alerters) {
			this.alerters.addAll(alerters);
			return this;
		}

		public AlertContainer build() {
			return new AlertContainer(this);
		}
	}

	public static Builder builder() throws IOException {
		return builder(null);
	}

	public static Builder builder(Properties properties) throws IOException {
		if (properties == null) properties =
			PropertyUtil.load(AlertService.class, PROPERTY_FILE_DEF);
		String prefix = Node.createFromEnv().name;
		logger.info("*** Node = {} ***", prefix);
		BaseProperties baseProperties = new BaseProperties(properties, prefix) {};
		return new Builder(baseProperties);
	}

	AlertContainer(Builder builder) {
		alert = new AlertServiceContainer(builder.properties, builder.alerters);
		email = new EmailContainer(builder.properties, alert.service(), builder.parsers);
	}

	@Override
	public void close() {
		logger.info("Closing");
		email.close();
		alert.close();
	}

	public EmailService email() {
		return email.service;
	}

	public AlertService alert() {
		return alert.service();
	}

}
