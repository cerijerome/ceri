package ceri.zwave.upnp;

/**
 * Basic functionality for all home automation devices
 */
public class HaDevice {
	public static final String file = "S_HaDevice1.xml";
	public static final String sid = "urn:micasaverde-com:serviceId:HaDevice1";
	public static final String sType = "urn:schemas-micasaverde-com:service:HaDevice:1";

	public enum Variable implements ceri.zwave.command.Variable {
		EnergyLog,		// DEVICEDATA_Energy_Log_CONST
		IODevice,		// The device number to connect to for IO (this is on the master device)
		IODeviceXRef,	// On the port, this is the device number that connects to it
		MaxTime,		// The maximum time to leave the socket open before closing and re-opening
		IOPort,			// The port to receive connections on from HAD_IOPORT_DEVICE (this is on the serial port)
		IOPortPath,		// Read-only, created at each boot, has the current path to the port
		IgnoreRoom,		// DEVICEDATA_Room_Not_Required_CONST
		CommFailure,
		PollingEnabled,
		PollMinDelay,
		Configured,
		JobID,
		ReverseOnOff,	// DEVICEDATA_Reverse_CONST
		LastUpdate,		// DEVICEDATA_LastUpdate_CONST
		LastActivity,	// Used by the OPower plugin
		AutoConfigure,	// DEVICEDATA_Auto_Configure_CONST
		LastTimeCheck,	// Only for devices that are capable of implementing time this is when the time was last checked
		LastTimeOffset,	// For HAD_LAST_TIME_CHECK this is the timezone offset from UTC when the time was last checked
		FirstConfigured,// The date this was first configured
		BatteryLevel,	// DEVICEDATA_Battery_Level_CONST
		BatteryDate,	// The date when the battery level was reported
		BatteryAlarm,	// DEVICEDATA_Battery_Alarm_CONST
		Documentation,
		sl_Alarm;
	}

	public enum Action implements ceri.zwave.command.Action {
		Reconfigure,
		Remove,
		Poll,
		SetPollFrequency,
		StressTest,
		ToggleState;
	}
	
}
