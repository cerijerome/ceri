package ceri.serial.comm.util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathExpression;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.except.ExceptionAdapter;
import ceri.common.process.Parameters;
import ceri.common.text.Parse;
import ceri.common.text.Strings;
import ceri.common.util.OsUtil;
import ceri.common.xml.XPaths;
import ceri.common.xml.Xml;
import ceri.process.ioreg.Ioreg;

/**
 * Finds USB device location ids and device paths for MacOS. When a device is replugged, its path
 * may change; this class allows a USB path to be found by a USB location id. The location id of a
 * USB device can be found in System Report / Hardware / USB, and is an 8 hex-digit value indicating
 * its location in the USB Hub tree.
 * <p/>
 * Updated for OSX Mojave, optimized for minimal ioreg output.
 */
public class MacUsbLocator {
	private static final XPathExpression USB_XPATH =
		XPaths.compile("/plist/array/dict//dict/dict/dict/key[text()='IODialinDevice']");
	private static final XPathExpression LOCATION_ID_XPATH =
		XPaths.compile("parent::dict/parent::dict/parent::dict/"
			+ "key[text()='locationID']/following-sibling::integer");
	private static final XPathExpression DEVICE_XPATH =
		XPaths.compile("following-sibling::string");
	private static final Parameters IOREG_ARGS = Parameters.of("-arltx", "-c", "IOSerialBSDClient");
	private final Ioreg ioreg;

	public static MacUsbLocator of() {
		return of(Ioreg.of());
	}

	public static MacUsbLocator of(Ioreg ioreg) {
		return new MacUsbLocator(ioreg);
	}

	private MacUsbLocator(Ioreg ioreg) {
		this.ioreg = ioreg;
	}

	/**
	 * Looks up the serial port name based on location id. Used by SelfHealingSerialConnector.
	 */
	public PortSupplier portSupplier(int locationId) {
		if (OsUtil.os().mac) return PortSupplier.named(() -> port(locationId),
			String.format("locationId:0x%x", locationId));
		throw new UnsupportedOperationException("Only Mac is supported");
	}

	/**
	 * Returns a map of USB device location ids and paths.
	 */
	public Map<Integer, String> ports() throws IOException {
		return ExceptionAdapter.io.get(() -> {
			var devices = Maps.<Integer, String>tree();
			for (Node usb : usbNodes()) {
				int id = locationId(usb);
				if (id == 0) continue;
				String device = device(usb);
				if (device != null) devices.put(id, device);
			}
			return Immutable.wrap(devices);
		});
	}

	/**
	 * Scans all USB devices to find a matching location id, and returns the device path.
	 */
	public String port(int locationId) throws IOException {
		return ExceptionAdapter.io.get(() -> {
			for (Node usb : usbNodes()) {
				int id = locationId(usb);
				if (id == 0 || id != locationId) continue;
				String port = device(usb);
				if (port != null) return port;
				break;
			}
			throw new IOException(
				"Device not available at location 0x" + Integer.toHexString(locationId));
		});
	}

	private List<Node> usbNodes() throws XPathException, SAXException, IOException {
		String ioregXml = ioreg.exec(IOREG_ARGS);
		return XPaths.nodeList(USB_XPATH, Xml.unvalidatedDocument(ioregXml));
	}

	private static int locationId(Node usb) throws XPathException {
		return Parse.parseInt(LOCATION_ID_XPATH.evaluate(usb), 0);
	}

	private static String device(Node usb) throws XPathException {
		String device = Strings.trim(DEVICE_XPATH.evaluate(usb));
		return Strings.isEmpty(device) ? null : device;
	}
}
