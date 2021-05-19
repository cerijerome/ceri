package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.assertNotNull;
import java.io.IOException;
import org.junit.Test;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class UsbBehavior {

	@Test
	public void should() throws IOException {
		try (var lib = TestLibUsbNative.register()) {
			try (Usb usb = Usb.of()) {
				try (var usbList = usb.deviceList()) {
					for (var device : usbList.devices()) {
						// TODO: create dummy devices to list
						assertNotNull(device);
					}
				}
			}
		}
	}

}
