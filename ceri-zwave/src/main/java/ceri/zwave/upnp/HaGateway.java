package ceri.zwave.upnp;

/**
 * Basic functionality for the gateway itself
 */
public class HaGateway {
	public final String file = "S_HomeAutomationGateway1.xml";
	public final String sid = "urn:micasaverde-com:serviceId:HomeAutomationGateway1";
	public final String sType = "urn:schemas-micasaverde-org:service:HomeAutomationGateway:1";

	public enum Variable implements ceri.zwave.command.Variable {
		ActiveScenes,
		DataVersionUserData, // The current data version for user_data
		DataVersionStatus; // The current data version for lu_status
	}

	public enum Action implements ceri.zwave.command.Action {
		GetUserData,
		ModifyUserData,
		GetVariable,
		SetVariable,
		GetStatus,
		GetActions,
		CreateDevice,
		DeleteDevice,
		CreatePlugin,
		DeletePlugin,
		CreatePluginDevice,
		ImportUpnpDevice,
		ProcessChildDevices,
		Reload,
		RunScene,
		SceneOff,
		RunLua,
		LogIpRequest;
	}

}
