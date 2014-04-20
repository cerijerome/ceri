package ceri.ci.email;

import ceri.common.property.BaseProperties;

/**
 * Properties for fetching emails from the server.
 */
public class EmailAdapterProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String ACCOUNT_KEY = "account";
	private static final String PASSWORD_KEY = "password";
	private static final String MAX_LOOK_BACK_MS_KEY = "max.look.back.ms";
	private static final String HOST_KEY = "host";
	private static final String FOLDER_KEY = "folder";
	private static final String PROTOCOL_KEY = "protocol";
	private static final String POLL_MS_KEY = "poll.ms";
	private static final String SENT_DATE_BUFFER_MS_KEY = "sent.date.buffer.ms";
	private static final String SHUTDOWN_TIMEOUT_MS_KEY = "shutdown.timeout.ms";

	public EmailAdapterProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public String host() {
		return value(HOST_KEY);
	}

	public String account() {
		return value(ACCOUNT_KEY);
	}

	public String password() {
		return value(PASSWORD_KEY);
	}

	public String protocol() {
		return value(PROTOCOL_KEY);
	}
	
	public String folder() {
		return value(FOLDER_KEY);
	}
	
	public Long sentDateBufferMs() {
		return longValue(SENT_DATE_BUFFER_MS_KEY);
	}

	public Long maxLookBackMs() {
		return longValue(MAX_LOOK_BACK_MS_KEY);
	}

	public Long pollMs() {
		return longValue(POLL_MS_KEY);
	}

	public Long shutdownTimeoutMs() {
		return longValue(SHUTDOWN_TIMEOUT_MS_KEY);
	}

}
