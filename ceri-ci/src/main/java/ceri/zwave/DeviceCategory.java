package ceri.zwave;

public enum DeviceCategory {
	NONE			(0),
	INTERFACE       (1),
	DIMMABLE_LIGHT  (2),
	SWITCH          (3),
	SECURITY_SENSOR (4),
	HVAC            (5),
	CAMERA          (6),
	DOOR_LOCK       (7),
	WINDOW_COV      (8),
	REMOTE_CONTROL  (9),
	IR_TX           (10),
	GENERIC_IO      (11),
	GENERIC_SENSOR  (12),
	SERIAL_PORT     (13),
	SCENE_CONTROLLER (14),
	AV              (15),
	HUMIDITY        (16),
	TEMPERATURE     (17),
	LIGHT_SENSOR    (18),
	ZWAVE_INT       (19),
	INSTEON_INT     (20),
	POWER_METER     (21),
	ALARM_PANEL     (22),
	ALARM_PARTITION (23),
	SIREN           (24);
	
	public static final int MAX_ID = SIREN.id;
	public final int id;
	
	private DeviceCategory(int id) {
		this.id = id;
	}
	
}
