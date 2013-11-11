package ceri.zwave.luup;

public class SerialPort1 {
	public static final String sid = "urn:micasaverde-org:serviceId:SerialPort1";

	public static enum Variable implements ceri.zwave.veralite.Variable {
		baud,
		vendor,
		product,
		path;
	}
	
}
