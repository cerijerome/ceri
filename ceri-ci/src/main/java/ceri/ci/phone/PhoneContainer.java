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
		PhoneProperties phoneProperties = new PhoneProperties(properties, GROUP);
		if (!phoneProperties.enabled()) {
			logger.info("Phone alerter disabled");
			alerter = null;
		} else {
			alerter = createAlerter(phoneProperties);
		}
	}

	private PhoneAlerter createAlerter(PhoneProperties properties) {
		logger.info("Creating phone client");
		PhoneClient client = new TwilioClient(properties.accountSid(), properties.authToken(),
			properties.fromPhoneNumber());
		logger.info("Creating phone alerter");
		PhoneAlerter.Builder builder = PhoneAlerter.builder(client);
		for (String name : properties.names()) {
			String phoneNumber = properties.phoneNumber(name);
			builder.phoneNumber(name, phoneNumber);
		}
		return builder.build();
	}

}
