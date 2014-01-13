package ceri.zwave.upnp;

public class IrDevice1 {
	public static final String sid = "urn:micasaverde-com:serviceId:IrDevice1";

	public static enum Action implements ceri.zwave.command.Action {
		SendCode;
	}

	public static enum Variable implements ceri.zwave.command.Variable {
		Codesets,
		Codeset,
		Remote,
		MfrId;
	}
	
}
