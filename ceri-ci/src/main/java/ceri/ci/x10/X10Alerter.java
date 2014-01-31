package ceri.ci.x10;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import x10.Command;
import x10.Controller;
import ceri.common.collection.ImmutableUtil;
import ceri.x10.X10ControllerType;
import ceri.x10.X10Util;

public class X10Alerter implements Closeable {
	private static final int DEVICE_DEF = 1;
	private final Controller x10;
	private final Map<String, String> addresses;
	private final Set<String> houseCodes;

	public static X10Alerter create(Properties properties, String prefix) throws IOException {
		return builder(properties, prefix).build();
	}

	static Builder builder(Properties properties, String prefix) {
		X10AlerterProperties x10Properties = new X10AlerterProperties(properties, prefix);
		String commPort = x10Properties.commPort();
		X10ControllerType controllerType = x10Properties.controllerType();
		Builder builder = builder(commPort);
		if (controllerType != null) builder.controllerType(controllerType);
		for (String name : x10Properties.names()) {
			String address = x10Properties.address(name);
			builder.address(name, address);
		}
		return builder;
	}

	public static class Builder {
		final Map<String, String> addresses = new HashMap<>();
		final String commPort;
		X10ControllerType controllerType = X10ControllerType.cm17a;

		Builder(String commPort) {
			if (commPort == null) throw new NullPointerException("Comm port cannot be null");
			this.commPort = commPort;
		}

		public Builder controllerType(X10ControllerType controllerType) {
			if (controllerType == null) throw new NullPointerException(
				"Controller type cannot be null");
			this.controllerType = controllerType;
			return this;
		}

		public Builder address(String name, String address) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			if (!X10Util.isValidAddress(address)) throw new IllegalArgumentException(
				"Not a valid x10 address for " + name + ": " + address);
			addresses.put(name, address);
			return this;
		}

		public X10Alerter build() throws IOException {
			return new X10Alerter(this);
		}
	}

	public static Builder builder(String commPort) {
		return new Builder(commPort);
	}

	X10Alerter(Builder builder) throws IOException {
		x10 = createController(builder.commPort, builder.controllerType);
		addresses = ImmutableUtil.copyAsMap(builder.addresses);
		houseCodes = Collections.unmodifiableSet(houseCodes(addresses));
	}

	public void alert(String... keys) {
		alert(Arrays.asList(keys));
	}

	public void alert(Collection<String> keys) {
		clearAlerts();
		for (String key : keys)
			doAlert(key);
	}

	public void clear() {
		clearAlerts();
	}

	@Override
	public void close() throws IOException {
		if (x10 != null) x10.close();
	}

	private void clearAlerts() {
		for (String houseCode : houseCodes)
			x10.addCommand(new Command(houseCode, Command.ALL_UNITS_OFF));
	}

	private void doAlert(String key) {
		String address = addresses.get(key);
		if (address == null) return;
		x10.addCommand(new Command(address, Command.ON));
	}

	private static Set<String> houseCodes(Map<String, String> addresses) {
		Set<String> houseCodes = new HashSet<>();
		for (String address : addresses.values())
			houseCodes.add("" + address.charAt(0) + DEVICE_DEF);
		return houseCodes;
	}

	Controller createController(String commPort, X10ControllerType controllerType)
		throws IOException {
		return X10Util.createController(commPort, controllerType);
	}

}
