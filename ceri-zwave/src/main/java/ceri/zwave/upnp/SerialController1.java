package ceri.zwave.upnp;

public class SerialController1 {
	public static final String sid = "urn:micasaverde-com:serviceId:SceneController1";

	public static enum Variable implements ceri.zwave.command.Variable {
		sl_SceneActivated,
		sl_SceneDeactivated,
		Scenes,
		LastSceneID,
		LastSceneTime,
		ManageLeds,
		NumButtons,
		FiresOffEvents,
		SceneShortcuts,
		ActivationMethod; // 1=BASIC_SET only (ignore scene_activate) 2=SCENE_ACTIVATE only (ignore basic set).  0=anything goes
	}
	
}
