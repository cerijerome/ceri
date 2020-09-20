package ceri.zwave.upnp;

public class SerialPort1 {
	public static final String sid = "urn:micasaverde-org:serviceId:SerialPort1";

	public enum Variable implements ceri.zwave.command.Variable {
		baud,
		vendor,
		product,
		path;
	}

}
