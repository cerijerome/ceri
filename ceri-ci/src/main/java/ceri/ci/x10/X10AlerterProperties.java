package ceri.ci.x10;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;
import ceri.x10.X10ControllerType;

public class X10AlerterProperties extends BaseProperties {
	private static final String COMM_PORT_KEY = "comm.port";
	private static final String CONTROLLER_KEY = "controller";
	private static final String ADDRESS_KEY = "address";

	public X10AlerterProperties(Properties properties) {
		this(properties, null);
	}

	public X10AlerterProperties(Properties properties, String prefix) {
		super(properties, prefix);
	}

	public String commPort() {
		return value(COMM_PORT_KEY);
	}

	public X10ControllerType controllerType() {
		String controllerType = value(CONTROLLER_KEY);
		if (controllerType == null) return null;
		try {
			return X10ControllerType.valueOf(controllerType.toLowerCase());
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Invalid type for " + key(CONTROLLER_KEY) + ": " +
				controllerType, e);
		}
	}

	public String address(String name) {
		return value(ADDRESS_KEY, name);
	}

	public Collection<String> names() {
		String prefix = key(ADDRESS_KEY) + Key.SEPARATOR;
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
