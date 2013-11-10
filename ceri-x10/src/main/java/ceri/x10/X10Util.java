package ceri.x10;

import java.io.IOException;
import x10.CM11ASerialController;
import x10.CM17ASerialController;
import x10.Command;
import x10.Controller;

public class X10Util {

	private X10Util() {}

	public static boolean isValidAddress(String address) {
		return address != null && address.length() > 1 && Command.isValid(address);
	}

	public static Controller createController(String commPort, X10ControllerType type)
		throws IOException {
		if (commPort == null) throw new NullPointerException("Comm port must be specified");
		if (type == X10ControllerType.cm11a) return new CM11ASerialController(commPort);
		if (type == X10ControllerType.cm17a) return new CM17ASerialController(commPort);
		throw new IllegalArgumentException("X10 controller type not supported: " + type);
	}

}
