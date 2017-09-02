package ceri.serial.usb.mac;

import java.io.IOException;
import org.junit.Test;
import ceri.serial.usb.mac.MacUsbSerialUtil;

public class MacUsbSerialUtilTest {

	public static void main(String[] args) throws IOException {
		System.out.println(System.getProperty("os.name"));
		MacUsbSerialUtil.devices().devices.forEach((id, dev) -> System.out.printf("0x%x = %s%n", id,
			dev));
	}

	@Test
	public void test() {}

}
