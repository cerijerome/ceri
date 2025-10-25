package ceri.serial.libusb.jna;

import static ceri.common.test.Assert.throwable;
import java.io.IOException;
import org.junit.Test;
import ceri.serial.libusb.jna.LibUsb.libusb_error;

public class LibUsbExceptionBehavior {

	@Test
	public void shouldAllowNullError() {
		var e = LibUsbException.full((libusb_error) null, "fail");
		throwable(e, "\\Q[-99] fail\\E");
	}

	@Test
	public void shouldAdaptThrowable() {
		var ioe = new IOException((String) null);
		var e = LibUsbException.ADAPTER.apply(ioe);
		throwable(e, "LIBUSB_ERROR_OTHER");
	}

}
