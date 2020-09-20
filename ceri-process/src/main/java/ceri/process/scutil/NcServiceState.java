package ceri.process.scutil;

import java.util.regex.Pattern;

public enum NcServiceState {
	unknown(null),
	connecting("Connecting"),
	connected("Connected"),
	disconnected("Disconnected"),
	noService("No service");

	private final Pattern regex;

	private NcServiceState(String regex) {
		this.regex = regex == null ? null : Pattern.compile(regex);
	}

	public static NcServiceState from(String state) {
		if (state != null) for (NcServiceState en : NcServiceState.values()) {
			if (en.regex == null) continue;
			if (en.regex.matcher(state).find()) return en;
		}
		return NcServiceState.unknown;
	}

}
