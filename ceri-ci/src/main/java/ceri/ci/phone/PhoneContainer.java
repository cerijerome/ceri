package ceri.ci.phone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.property.BaseProperties;

/**
 * Creates the phone alerter.
 */
public class PhoneContainer {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "phone";
	public final PhoneAlerter alerter;

	public PhoneContainer(BaseProperties properties) {
		this(properties, new PhoneFactoryImpl());
	}

	public PhoneContainer(BaseProperties properties, PhoneFactory factory) {
		PhoneProperties phoneProperties = new PhoneProperties(properties, GROUP);
		if (!phoneProperties.enabled()) {
			logger.info("Phone alerter disabled");
			alerter = null;
		} else {
			alerter = createAlerter(phoneProperties, factory);
		}
	}

	private PhoneAlerter createAlerter(PhoneProperties properties, PhoneFactory factory) {
		logger.info("Creating phone client");
		PhoneClient client = factory.createClient(properties.accountSid(), properties.authToken(),
			properties.fromNumber());
		logger.info("Creating phone alerter");
		PhoneAlerter.Builder builder = factory.builder(client);
		for (String name : properties.names()) {
			String number = properties.number(name);
			builder.number(name, number);
		}
		return builder.build();
	}

}
