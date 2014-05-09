package ceri.ci.phone;

import java.util.Collection;
import java.util.LinkedHashSet;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;

/**
 * Properties to configure the zwave alerter.
 */
public class PhoneProperties extends BaseProperties {
	private static final String ENABLED_KEY = "enabled";
	private static final String ACCOUNT_SID_KEY = "account.sid";
	private static final String AUTH_TOKEN_KEY = "auth.token";
	private static final String FROM_KEY = "from";
	private static final String NUMBER_KEY = "number";

	public PhoneProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public String accountSid() {
		return value(ACCOUNT_SID_KEY);
	}

	public String authToken() {
		return value(AUTH_TOKEN_KEY);
	}

	public String fromNumber() {
		return value(FROM_KEY, NUMBER_KEY);
	}

	public String number(String name) {
		return value(NUMBER_KEY, name);
	}

	public Collection<String> names() {
		String prefix = key(NUMBER_KEY) + Key.SEPARATOR;
		int offset = prefix.length();
		Collection<String> names = new LinkedHashSet<>();
		for (String key : keys()) {
			if (!key.startsWith(prefix)) continue;
			String name = key.substring(offset);
			names.add(name);
		}
		return names;
	}

}
