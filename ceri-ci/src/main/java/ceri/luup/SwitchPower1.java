package ceri.luup;

public class SwitchPower1 {
	public static final String sid = "urn:upnp-org:serviceId:SwitchPower1";

	public static enum Action implements ceri.luup.Action {
		SetTarget;
	}

	public static enum Variable implements ceri.luup.Variable {
		Status,
		Target;
	}
	
	
}
