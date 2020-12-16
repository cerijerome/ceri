package ceri.serial.libusb;

import static ceri.common.test.AssertUtil.*;
import static ceri.common.test.ErrorGen.*;
import static ceri.common.test.TestUtil.*;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.*;
import java.io.IOException;
import org.junit.Test;
import ceri.common.test.CallSync;
import ceri.serial.libusb.jna.TestLibUsbNative;

public class UsbBehavior {

	@Test
	public void should() throws IOException {
		//try (var lib = TestLibUsbNative.register()) {
			try (Usb usb = Usb.of()) {
				try (var usbList = usb.deviceList()) {
					for (var device : usbList.devices()) {
						System.out.println(device);
					}
				}
			}
		//}
	}

}
