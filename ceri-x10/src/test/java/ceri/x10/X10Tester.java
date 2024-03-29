package ceri.x10;

import java.io.IOException;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.x10.cm17a.Cm17aContainer;
import ceri.x10.command.Command;

/**
 * Example code for connecting to x10 device.
 */
public class X10Tester {

	public static void main(String[] args) throws IOException {
		String commPort = "/dev/tty.usbserial";
		var config = Cm17aContainer.Config.of(commPort);
		try (Cm17aContainer con = Cm17aContainer.of(config)) {
			con.cm17a.command(Command.from("A1:on"));
			ConcurrentUtil.delay(3000);
			con.cm17a.command(Command.from("A1:off"));
			ConcurrentUtil.delay(3000);
		}
	}
}
