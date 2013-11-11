package ceri.zwave.luup;

public class IrDevice1 {
	public static final String sid = "urn:micasaverde-com:serviceId:IrDevice1";

	public static enum Action implements ceri.zwave.veralite.Action {
		SendCode;
	}

	public static enum Variable implements ceri.zwave.veralite.Variable {
		Codesets,
		Codeset,
		Remote,
		MfrId;
	}
	
}
