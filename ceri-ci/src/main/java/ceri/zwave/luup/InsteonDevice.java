package ceri.zwave.luup;

public class InsteonDevice {
	public static final String file = "S_InsteonDevice1.xml";
	public static final String sid = "urn:micasaverde-com:serviceId:InsteonDevice1";
	public static final String sType = "urn:schemas-micasaverde-com:service:InsteonDevice:1";

	public static enum Variable implements ceri.zwave.veralite.Variable {
		PollSettings, // DEVICEDATA_Polling_Settings_CONST
		MultiChEndpoint, // DEVICEDATA_Multi_Channel_End_Point_CONST
		MultiChCapabilities, // DEVICEDATA_Multi_Channel_Capabilities_CONST

		Neighbors, // DEVICEDATA_Neighbors_to_Call_CONST
		Capabilities, // DEVICEDATA_Capabilities_CONST
		Configuration, // DEVICEDATA_Configuration_CONST
		LastReset, // DEVICEDATA_Last_Reset_CONST
		ScenesAsEvents, // DEVICEDATA_Scenes_As_Events_CONST
		WakeupInterval, // DEVICEDATA_Wakeup_Interval_CONST
		LastWakeup, // DEVICEDATA_Last_Wakeup_CONST
		LastRouteUpdate, // DEVICEDATA_Last_Route_Update_CONST
		VariablesGet, // DEVICEDATA_Variables_Get_CONST
		VariablesSet, // DEVICEDATA_Variables_Set_CONST
		AssociationGet, // DEVICEDATA_Association_Get_CONST
		AssociationSet, // DEVICEDATA_Association_Set_CONST
		ManufacturerInfo, // DEVICEDATA_Model_CONST
		VersionInfo, // DEVICEDATA_Configuration_CONST
		UpdatedName; // DEVICEDATA_Update_Name_CONST
	}

}
