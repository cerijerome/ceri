package ceri.serial.usb.mac;

import java.io.IOException;
import ceri.common.util.SystemVars;

public class MacUsbSerialUtilTester {

	public static void main(String[] args) throws IOException {
		System.out.println(SystemVars.sys("os.name"));
		MacUsbSerialUtil.devices().devices
			.forEach((id, dev) -> System.out.printf("0x%x = %s%n", id, dev));
	}

}
