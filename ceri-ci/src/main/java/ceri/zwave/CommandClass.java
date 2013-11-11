package ceri.zwave;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

public enum CommandClass {
	NO_OPERATION(0),					// 0x00
	BASIC(32),							// 0x20
	CONTROLLER_REPLICATION(33),			// 0x21
	APPLICATION_STATUS(34),				// 0x22
	ZIP_SERVICES(35),					// 0x23
	ZIP_SERVER(36),						// 0x24
	SWITCH_BINARY(37),					// 0x25
	SWITCH_MULTILEVEL(38),				// 0x26
	SWITCH_MULTILEVEL_V2(38),			// 0x26
	SWITCH_ALL(39),						// 0x27
	SWITCH_TOGGLE_BINARY(40),			// 0x28
	SWITCH_TOGGLE_MULTILEVEL(41),		// 0x29
	CHIMNEY_FAN(42),					// 0x2A
	SCENE_ACTIVATION(43),				// 0x2B
	SCENE_ACTUATOR_CONF(44),			// 0x2C
	SCENE_CONTROLLER_CONF(45),			// 0x2D
	ZIP_CLIENT(46),						// 0x2E
	ZIP_ADV_SERVICES(47),				// 0x2F
	SENSOR_BINARY(48),					// 0x30
	SENSOR_MULTILEVEL(49),				// 0x31
	SENSOR_MULTILEVEL_V2(49),			// 0x31
	METER(50),							// 0x32
	ZIP_ADV_SERVER(51),					// 0x33
	ZIP_ADV_CLIENT(52),					// 0x34
	METER_PULSE(53),					// 0x35
	THERMOSTAT_HEATING(56),				// 0x38
	METER_TBL_CONFIG(60),				// 0x3C
	METER_TBL_MONITOR(61),				// 0x3D
	METER_TBL_PUSH(62),					// 0x3E
	THERMOSTAT_MODE(64),				// 0x40
	THERMOSTAT_OPERATING_STATE(66),		// 0x42
	THERMOSTAT_SETPOINT(67),			// 0x43
	THERMOSTAT_FAN_MODE(68),			// 0x44
	THERMOSTAT_FAN_STATE(69),			// 0x45
	CLIMATE_CONTROL_SCHEDULE(70),		// 0x46
	THERMOSTAT_SETBACK(71),				// 0x47
	DOOR_LOCK_LOGGING(76),				// 0x4C
	SCHEDULE_ENTRY_LOCK(78),			// 0x4E
	BASIC_WINDOW_COVERING(80),			// 0x50
	MTP_WINDOW_COVERING(81),			// 0x51
	MULTI_CHANNEL_V2(96),				// 0x60
	MULTI_INSTANCE(96),					// 0x60
	DOOR_LOCK(98),						// 0x62
	USER_CODE(99),						// 0x63
	CONFIGURATION(112),					// 0x70
	CONFIGURATION_V2(112),				// 0x70
	ALARM(113),							// 0x71
	MANUFACTURER_SPECIFIC(114),			// 0x72
	POWERLEVEL(115),					// 0x73
	PROTECTION(117),					// 0x75
	PROTECTION_V2(117),					// 0x75
	LOCK(118),							// 0x76
	NODE_NAMING(119),					// 0x77
	FIRMWARE_UPDATE_MD(122),			// 0x7A
	GROUPING_NAME(123),					// 0x7B
	REMOTE_ASSOCIATION_ACTIVATE(124),	// 0x7C
	REMOTE_ASSOCIATION(125),			// 0x7D
	BATTERY(128),						// 0x80
	CLOCK(129),							// 0x81
	HAIL(130),							// 0x82
	WAKE_UP(132),						// 0x84
	WAKE_UP_V2(132),					// 0x84
	ASSOCIATION(133),					// 0x85
	ASSOCIATION_V2(133),				// 0x85
	VERSION(134),						// 0x86
	INDICATOR(135),						// 0x87
	PROPRIETARY(136),					// 0x88
	LANGUAGE(137),						// 0x89
	TIME(138),							// 0x8A
	TIME_PARAMETERS(139),				// 0x8B
	GEOGRAPHIC_LOCATION(140),			// 0x8C
	COMPOSITE(141),						// 0x8D
	MULTI_CHANNEL_ASSOCIATION_V2(142),	// 0x8E
	MULTI_INSTANCE_ASSOCIATION(142),	// 0x8E
	MULTI_CMD(143),						// 0x8F
	ENERGY_PRODUCTION(144),				// 0x90
	MANUFACTURER_PROPRIETARY(145),		// 0x91
	SCREEN_MD(146),						// 0x92
	SCREEN_MD_V2(146),					// 0x92
	SCREEN_ATTRIBUTES(147),				// 0x93
	SCREEN_ATTRIBUTES_V2(147),			// 0x93
	SIMPLE_AV_CONTROL(148),				// 0x94
	AV_CONTENT_DIRECTORY_MD(149),		// 0x95
	AV_RENDERER_STATUS(150),			// 0x96
	AV_CONTENT_SEARCH_MD(151),			// 0x97
	SECURITY(152),						// 0x98
	AV_TAGGING_MD(153),					// 0x99
	IP_CONFIGURATION(154),				// 0x9A
	ASSOCIATION_COMMAND_CONFIGURATION(155),	// 0x9B
	SENSOR_ALARM(156),					// 0x9C
	SILENCE_ALARM(157),					// 0x9D
	SENSOR_CONFIGURATION(158),			// 0x9E
	MARK(239),							// 0xEF
	NON_INTEROPERABLE(240);				// 0xF0

	private static final Map<Integer, CommandClass> map = Collections.unmodifiableMap(createMap());
	public final int id;
	
	private CommandClass(int id) {
		this.id = id;
	}
	
	private static Map<Integer, CommandClass> createMap() {
		Map<Integer, CommandClass> map = new HashMap<>();
		for (CommandClass cc : CommandClass.values()) map.put(cc.id, cc);
		return map;
	}

	public static CommandClass byId(int id) {
		CommandClass cc = map.get(id);
		if (cc == null) throw new IllegalArgumentException("No such id: " + id);
		return cc;
	}
	
	public static Collection<CommandClass> byIds(int...ids) {
		Collection<CommandClass> values = new LinkedHashSet<>();
		for (int id : ids) values.add(byId(id));
		return values;
	}
	
}
