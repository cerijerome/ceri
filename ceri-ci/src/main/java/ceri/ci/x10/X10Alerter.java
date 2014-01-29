package ceri.ci.x10;

import java.io.Closeable;
import java.io.File;
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
import ceri.common.property.PropertyUtil;
import ceri.x10.X10ControllerType;
import ceri.x10.X10Util;

public class X10Alerter implements Closeable {
	private static final int DEVICE_DEF = 1;
	private final Controller x10;
	private final Map<String, String> addresses;
	private final Set<String> houseCodes;

	public static X10Alerter create(File propertyFile, String prefix) throws IOException {
		return create(PropertyUtil.load(propertyFile), prefix);
	}

	public static X10Alerter create(Properties properties, String prefix) throws IOException {
		X10AlerterProperties x10Properties = new X10AlerterProperties(properties, prefix);
		String commPort = x10Properties.commPort();
		X10ControllerType type = x10Properties.controllerType();
		if (type == null) type = X10ControllerType.cm17a;
		Controller x10 = X10Util.createController(commPort, type);
		Builder builder = builder(x10);
		for (String name : x10Properties.names()) {
			String address = x10Properties.address(name);
			builder.address(name, address);
		}
		return builder.build();
	}

	public static class Builder {
		final Map<String, String> addresses = new HashMap<>();
		final Controller x10;

		Builder(Controller x10) {
			if (x10 == null) throw new NullPointerException("X10 Controller cannot be null");
			this.x10 = x10;
		}

		public Builder address(String name, String address) {
			if (name == null) throw new NullPointerException("Name cannot be null");
			if (!X10Util.isValidAddress(address)) throw new IllegalArgumentException(
				"Not a valid x10 address for " + name + ": " + address);
			addresses.put(name, address);
			return this;
		}

		public X10Alerter build() {
			return new X10Alerter(this);
		}
	}

	public static Builder builder(Controller x10) {
		return new Builder(x10);
	}

	X10Alerter(Builder builder) {
		x10 = builder.x10;
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

}
