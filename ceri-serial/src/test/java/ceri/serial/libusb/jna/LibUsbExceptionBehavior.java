package ceri.serial.libusb.jna;

import static ceri.common.test.AssertUtil.assertThrowable;
import java.io.IOException;
import org.junit.Test;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

public class LibUsbExceptionBehavior {

	@Test
	public void shouldAllowNullError() {
		var e = LibUsbException.full((libusb_error) null, "fail");
		assertThrowable(e, "\\Q[-99] fail\\E");
	}

	@Test
	public void shouldAdaptThrowable() {
		var ioe = new IOException((String) null);
		var e = LibUsbException.ADAPTER.apply(ioe);
		assertThrowable(e, "LIBUSB_ERROR_OTHER");
	}

}
