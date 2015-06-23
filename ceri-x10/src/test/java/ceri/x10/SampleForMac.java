package ceri.x10;

import java.io.IOException;
import ceri.common.util.BasicUtil;
import ceri.x10.cm17a.Cm17aController;
import ceri.x10.cm17a.Cm17aSerialConnector;
import ceri.x10.command.CommandFactory;
import ceri.x10.util.X10Controller;

/**
 * Example code for connecting to x10 devices via MacOSX.
 * Install PL2303 driver and rxtx lib shown in README.
 */
public class SampleForMac {

	public static void main(String[] args) throws IOException {
		String commPort = "/dev/tty.usbserial";
		try (Cm17aSerialConnector connector = new Cm17aSerialConnector(commPort)) {
			try (X10Controller controller = new Cm17aController(connector, null)) {
				controller.command(CommandFactory.on("A1"));
				BasicUtil.delay(3000);
				controller.command(CommandFactory.off("A1"));
				BasicUtil.delay(3000);
			}
		}
	}
}
