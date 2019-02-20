package ceri.serial.usb;

import ceri.common.property.BaseProperties;
import ceri.serial.javax.util.CommPortSupplier;

public class UsbSerialProperties extends BaseProperties {
	private static final String LOCATION_ID_KEY = "location.id";
	private static final String PORT_KEY = "port";

	public UsbSerialProperties(BaseProperties properties, String group) {
		super(properties, group);
	}

	public CommPortSupplier supplier() {
		Integer locationId = locationId();
		if (locationId != null) return UsbSerialUtil.deviceByLocationId(locationId);
		String port = port();
		return () -> port;
	}

	private String port() {
		return value(PORT_KEY);
	}

	private Integer locationId() {
		return intValue(LOCATION_ID_KEY);
	}

}
