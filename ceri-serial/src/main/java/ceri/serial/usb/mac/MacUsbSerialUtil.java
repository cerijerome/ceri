package ceri.serial.usb.mac;

import static ceri.common.xml.XPathUtil.compile;
import java.io.IOException;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ceri.common.xml.XPathUtil;
import ceri.common.xml.XmlUtil;
import ceri.process.ioreg.Ioreg;
import ceri.serial.usb.UsbSerialDevices;

public class MacUsbSerialUtil {
	private static final XPathExpression USB_XPATH = compile("/plist/array/dict");
	private static final XPathExpression LOCATION_ID_XPATH =
		compile("key[text()='locationID']/following-sibling::integer/text()");
	private static final XPathExpression DEVICE_XPATH =
		compile("array/dict/array/dict/array/dict/key[text()='IODialinDevice']"
			+ "/following-sibling::string/text()");
	private static final String IOREG_OPTIONS = "-arlx";
	private static final String IOREG_NAME_OPTION = "-n";
	public static final String USB_SERIAL_NAME = "AppleUSBInterface@0"; // Since OSX El Capitan

	private MacUsbSerialUtil() {}

	/**
	 * Returns a map of USB-to-serial device location IDs and names for Mac OSX.
	 */
	public static UsbSerialDevices devices() throws IOException {
		return devices(USB_SERIAL_NAME);
	}

	/**
	 * Returns a map of USB device location IDs and device paths for Mac OSX. To find the name for
	 * new devices, first capture the output from system_profile. Search for the device name that
	 * implements IOUSBInterface. For a location id, the Mac takes the first 3 digits, the remaining
	 * five are 0, or assigned to hub ports.
	 */
	public static UsbSerialDevices devices(String name) throws IOException {
		String ioregXml = new Ioreg().exec(IOREG_OPTIONS, IOREG_NAME_OPTION, name);
		try {
			UsbSerialDevices.Builder builder = UsbSerialDevices.builder();
			for (Node usb : XPathUtil.nodeList(USB_XPATH, XmlUtil.unvalidatedDocument(ioregXml))) {
				String locationIdStr = LOCATION_ID_XPATH.evaluate(usb);
				String device = DEVICE_XPATH.evaluate(usb);
				if (device.isEmpty()) continue;
				int locationId = Integer.valueOf(locationIdStr);
				builder.device(locationId, device);
			}
			return builder.build();
		} catch (SAXException | XPathExpressionException e) {
			throw new IOException("Should not happen", e);
		}
	}

}
