package ceri.process.scutil;

import java.util.regex.Pattern;

public enum NcServiceState {
	unknown, noService, disconnected, connecting, connected;

	private static final Pattern CONNECTED_REGEX = Pattern.compile("Connected");
	private static final Pattern CONNECTING_REGEX = Pattern.compile("Connecting");
	private static final Pattern DISCONNECTED_REGEX = Pattern.compile("Disconnected");
	private static final Pattern NO_SERVICE_REGEX = Pattern.compile("No service");
	
	public static NcServiceState from(String state) {
		if (state == null) return null;
		if (CONNECTING_REGEX.matcher(state).find()) return NcServiceState.connecting;
		if (CONNECTED_REGEX.matcher(state).find()) return NcServiceState.connected;
		if (DISCONNECTED_REGEX.matcher(state).find()) return NcServiceState.disconnected;
		if (NO_SERVICE_REGEX.matcher(state).find()) return NcServiceState.noService;
		return NcServiceState.unknown;
	}

}
