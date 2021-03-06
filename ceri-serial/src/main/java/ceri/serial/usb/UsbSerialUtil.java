package ceri.serial.usb;

import java.io.IOException;
import ceri.common.util.OsUtil;
import ceri.serial.javax.util.CommPortSupplier;
import ceri.serial.usb.mac.MacUsbSerialUtil;

public class UsbSerialUtil {

	private UsbSerialUtil() {}

	/**
	 * Looks up the serial port name based on location id. Used by SelfHealingSerialConnector.
	 */
	public static CommPortSupplier deviceByLocationId(int locationId) {
		if (!OsUtil.IS_MAC) throw new UnsupportedOperationException("Only Mac is supported");
		return CommPortSupplier.named(() -> device(locationId),
			String.format("locationId:0x%x", locationId));
	}

	private static String device(int locationId) throws IOException {
		String device = MacUsbSerialUtil.devices().device(locationId);
		if (device != null) return device;
		throw new IOException(
			"Device not available at location 0x" + Integer.toHexString(locationId));
	}

}
