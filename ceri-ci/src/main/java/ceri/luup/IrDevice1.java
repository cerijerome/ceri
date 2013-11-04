package ceri.luup;

public class IrDevice1 {
	public static final String sid = "urn:micasaverde-com:serviceId:IrDevice1";

	public static enum Action implements ceri.luup.Action {
		SendCode;
	}

	public static enum Variable implements ceri.luup.Variable {
		Codesets,
		Codeset,
		Remote,
		MfrId;
	}
	
}
