package ceri.ci.x10;

import java.util.Collection;
import java.util.LinkedHashSet;
import ceri.common.property.BaseProperties;
import ceri.common.property.Key;
import ceri.x10.util.X10ControllerType;

/**
 * Properties to configure the x10 alerter.
 */
public class X10Properties extends BaseProperties {
	private static final X10ControllerType CONTROLLER_DEF = X10ControllerType.cm17a;
	private static final String ENABLED_KEY = "enabled";
	private static final String COMM_PORT_KEY = "comm.port";
	private static final String CONTROLLER_KEY = "controller";
	private static final String ADDRESS_KEY = "address";

	public X10Properties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public boolean enabled() {
		return booleanValue(false, ENABLED_KEY);
	}

	public String commPort() {
		return value(COMM_PORT_KEY);
	}

	public X10ControllerType controllerType() {
		String controllerType = value(CONTROLLER_KEY);
		if (controllerType == null) return CONTROLLER_DEF;
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
