package ceri.luup;

public class Dimming1 {
	public static final String sid = "urn:upnp-org:serviceId:Dimming1";

	public static enum Action implements ceri.luup.Action {
		SetLoadLevelTarget;
	}

	public static enum Variable implements ceri.luup.Variable {
		LoadLevelStatus,
		LoadLevelTarget;
	}

}
