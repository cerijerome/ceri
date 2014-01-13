package ceri.zwave.upnp;

public class InsteonNetwork {
	public static final String file = "S_InsteonNetwork1.xml";
	public static final String sid = "urn:micasaverde-com:serviceId:InsteonNetwork1";
	public static final String sType = "urn:schemas-micasaverde-org:service:InsteonNetwork:1";

	public static enum Actions implements ceri.zwave.command.Action {
		ResetNetwork,
		RemoveNodes,
		AddNodes,
		StopAddRemoveNodes,
		SendData;
	}

	public static enum Variable implements ceri.zwave.command.Variable {
		ComPort, // DEVICEDATA_COM_Port_on_PC_CONST
		LockComPort,
		LastError,
		LastUpdate, // DEVICEDATA_LastUpdate_CONST
		NetStatusID,
		NetStatusText,
		PollingEnabled,
		PollDelayInitial,
		PollDelayDeadTime,
		PollMinDelay,
		PollFrequency,
		NodeID, // The dongle's id
		sl_X10Code;
	}
	
}
