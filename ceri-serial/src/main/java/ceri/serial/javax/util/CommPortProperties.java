package ceri.serial.javax.util;

import ceri.common.property.BaseProperties;

public class CommPortProperties extends BaseProperties {
	private static final String LOCATION_ID_KEY = "location.id";
	private static final String PORT_KEY = "port";

	public CommPortProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
	}

	public CommPortSupplier supplier() {
		Integer locationId = locationId();
		if (locationId != null) return MacUsbLocator.of().deviceByLocationId(locationId);
		String port = port();
		if (port != null) return CommPortSupplier.fixed(port);
		return null;
	}

	private String port() {
		return value(PORT_KEY);
	}

	private Integer locationId() {
		return intValue(LOCATION_ID_KEY);
	}

}
