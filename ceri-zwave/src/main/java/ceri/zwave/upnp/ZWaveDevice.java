package ceri.zwave.upnp;

public class ZWaveDevice {
	public static final String file = "S_ZWaveDevice1.xml";
	public static final String sid = "urn:micasaverde-com:serviceId:ZWaveDevice1";
	public static final String sType = "urn:schemas-micasaverde-com:service:ZWaveDevice:1";

	public enum Variable implements ceri.zwave.command.Variable {
		PollSettings, // DEVICEDATA_Polling_Settings_CONST
		PollCommands, // A comma-separated list of command classes/commands to poll.  X means the device won't respond to any poll
		MultiChEndpoint, // DEVICEDATA_Multi_Channel_End_Point_CONST
		MultiChCapabilities, // DEVICEDATA_Multi_Channel_Capabilities_CONST

		Neighbors, // DEVICEDATA_Neighbors_to_Call_CONST
		NeighborsInverse, // Which neighbors see us
		Capabilities, // DEVICEDATA_Capabilities_CONST
		SecurityFailed, // Don't talk to this node with security even if it reports it can
		Configuration, // DEVICEDATA_Configuration_CONST
		LastReset, // DEVICEDATA_Last_Reset_CONST
		ScenesAsEvents, // DEVICEDATA_Scenes_As_Events_CONST
		ScenesTimestamp, // DEVICEDATA_Scenes_CONST
		WakeupInterval, // DEVICEDATA_Wakeup_Interval_CONST
		LastWakeup, // DEVICEDATA_Last_Wakeup_CONST
		LastRouteUpdate, // DEVICEDATA_Last_Route_Update_CONST
		Health, // A rating from 0-5
		HealthDate, // The date the rating was assigned
		VariablesGet, // DEVICEDATA_Variables_Get_CONST
		VariablesSet, // DEVICEDATA_Variables_Set_CONST
		AssociationGet, // DEVICEDATA_Association_Get_CONST
		AssociationSet, // DEVICEDATA_Association_Set_CONST
		AssociationNum,
		NonceACK,
		ManufacturerInfo, // DEVICEDATA_Model_CONST
		VersionInfo, // DEVICEDATA_Configuration_CONST
		SetPointInfo,
		NodeInfo, // The Z-Wave node info frame
		InitialName, // The name that was initially set when the node was first added
		ConfiguredName, // The user-specified name when we configured the node.  We won't need to set the name unless this has changed
		ConfiguredVariable, // The user-specified VariablesSet when we configured the node.  We won't need to set the name unless this has changed
		ConfiguredAssoc, // The user-specified AssociationSet when we configured the node.  We won't need to set the name unless this has changed
		Documentation, // For the Z-Wave options page
		MeterType,
		MeterScale,
		AlarmType,
		PollOk,
		PollTxFail,
		PollNoReply,
		ManualRoute, // A route specified by an end-user
		AllRoutesFailed,
		AutoRoute, // A route chosen by profilings
		IgnoreDirectScene; // See notes in IsSceneControllerEvent
	}
}
