package ceri.zwave.upnp;

public enum DeviceSubcategory {
	NONE(DeviceCategory.NONE, 0),
	INTERIOR(DeviceCategory.SWITCH, 1),
	EXTERIOR(DeviceCategory.SWITCH, 2),
	HVAC(DeviceCategory.HVAC, 1),
	HEATER(DeviceCategory.HVAC, 2),
	DOOR(DeviceCategory.SECURITY_SENSOR, 1),
	LEAK(DeviceCategory.SECURITY_SENSOR, 2),
	MOTION(DeviceCategory.SECURITY_SENSOR, 3),
	SMOKE(DeviceCategory.SECURITY_SENSOR, 4),
	CO(DeviceCategory.SECURITY_SENSOR, 5),
	GLASS(DeviceCategory.SECURITY_SENSOR, 6),
	WINDOW_COV(DeviceCategory.WINDOW_COV, 1),
	ZRTSI(DeviceCategory.WINDOW_COV, 2),
	IRT(DeviceCategory.IR_TX, 1),
	USBUIRT(DeviceCategory.IR_TX, 2);

	public final DeviceCategory category;
	public final int id;

	DeviceSubcategory(DeviceCategory category, int id) {
		this.category = category;
		this.id = id;
	}

}
