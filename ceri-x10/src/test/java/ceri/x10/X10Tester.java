package ceri.x10;

import ceri.common.util.BasicUtil;
import ceri.x10.cm17a.Cm17aConfig;
import ceri.x10.cm17a.Cm17aContainer;
import ceri.x10.command.Command;

/**
 * Example code for connecting to x10 device.
 */
public class X10Tester {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		String commPort = "/dev/tty.usbserial";
		Cm17aConfig config = Cm17aConfig.of(commPort);
		try (Cm17aContainer con = Cm17aContainer.of(config)) {
			con.cm17a().command(Command.from("A1:on"));
			BasicUtil.delay(3000);
			con.cm17a().command(Command.from("A1:off"));
			BasicUtil.delay(3000);
		}
	}
}
