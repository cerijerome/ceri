package ceri.ci.email;

import java.util.Properties;
import ceri.common.property.BaseProperties;

public class EmailAdapterProperties extends BaseProperties {
	private static final int LOOKBACK_DEF = 10;
	private static final String FOLDER_DEF = "inbox";
	private static final String PROTOCOL_DEF = "imaps";
	private static final String ENABLED_KEY = "enabled";
	private static final String ACCOUNT_KEY = "account";
	private static final String PASSWORD_KEY = "password";
	private static final String LOOKBACK_KEY = "lookback";
	private static final String HOST_KEY = "host";
	private static final String FOLDER_KEY = "folder";
	private static final String PROTOCOL_KEY = "protocol";

	public EmailAdapterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public String account() {
		return value(ACCOUNT_KEY);
	}

	public String password() {
		return value(PASSWORD_KEY);
	}

	public int lookBack() {
		return intValue(LOOKBACK_DEF, LOOKBACK_KEY);
	}

	public String host() {
		return value(HOST_KEY);
	}

	public String folder() {
		return stringValue(FOLDER_DEF, FOLDER_KEY);
	}
	
	public String protocol() {
		return stringValue(PROTOCOL_DEF, PROTOCOL_KEY);
	}
	
}
