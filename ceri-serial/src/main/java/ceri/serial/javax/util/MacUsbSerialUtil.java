package ceri.serial.javax.util;

import static ceri.common.xml.XPathUtil.compile;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.xml.xpath.XPathExpression;
import org.w3c.dom.Node;
import ceri.common.io.IoUtil;
import ceri.common.util.OsUtil;
import ceri.common.xml.XPathUtil;
import ceri.common.xml.XmlUtil;
import ceri.process.ioreg.Ioreg;

public class MacUsbSerialUtil {
	private static final XPathExpression USB_XPATH = compile("/plist/array/dict");
	private static final XPathExpression LOCATION_ID_XPATH =
		compile("key[text()='locationID']/following-sibling::integer/text()");
	private static final XPathExpression DEVICE_XPATH =
		compile("array/dict/array/dict/array/dict/key[text()='IODialinDevice']" +
			"/following-sibling::string/text()");
	private static final String IOREG_OPTIONS = "-arlx";
	private static final String IOREG_NAME_OPTION = "-n";
	public static final String USB_SERIAL_NAME = "AppleUSBInterface@0"; // Since OSX El Capitan

	private MacUsbSerialUtil() {}

	/**
	 * Looks up the serial port name based on location id. Used by SelfHealingSerialConnector.
	 */
	public static CommPortSupplier deviceByLocationId(int locationId) {
		if (!OsUtil.IS_MAC) throw new UnsupportedOperationException("Only Mac is supported");
		return CommPortSupplier.named(() -> device(locationId),
			String.format("locationId:0x%x", locationId));
	}

	/**
	 * Returns a map of USB-to-serial device location IDs and names for Mac OSX.
	 */
	public static Map<Integer, String> devices() throws IOException {
		return devices(USB_SERIAL_NAME);
	}

	/**
	 * Returns a map of USB device location IDs and device paths for Mac OSX. To find the name for
	 * new devices, first capture the output from system_profile. Search for the device name that
	 * implements IOUSBInterface. For a location id, the Mac takes the first 3 digits, the remaining
	 * five are 0, or assigned to hub ports.
	 */
	public static Map<Integer, String> devices(String name) throws IOException {
		String ioregXml = Ioreg.of().exec(IOREG_OPTIONS, IOREG_NAME_OPTION, name);
		Map<Integer, String> devices = new LinkedHashMap<>();
		IoUtil.IO_ADAPTER.run(() -> {
			for (Node usb : XPathUtil.nodeList(USB_XPATH, XmlUtil.unvalidatedDocument(ioregXml))) {
				String locationIdStr = LOCATION_ID_XPATH.evaluate(usb);
				String device = DEVICE_XPATH.evaluate(usb);
				if (device.isEmpty()) continue;
				int locationId = Integer.parseInt(locationIdStr);
				devices.put(locationId, device);
			}
		});
		return Collections.unmodifiableMap(devices);
	}

	private static String device(int locationId) throws IOException {
		String device = MacUsbSerialUtil.devices().get(locationId);
		if (device != null) return device;
		throw new IOException(
			"Device not available at location 0x" + Integer.toHexString(locationId));
	}
}
