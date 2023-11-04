package ceri.serial.comm.util;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

/**
 * Logic for finding serial ports.
 */
public class SerialPortLocator {
	private static final String DEV_PATH = "/dev/";
	private static final String USB_PATTERN = "regex:tty\\.(usb|USB).*";
	private final Path dir;

	public static SerialPortLocator of() {
		return of(null);
	}

	public static SerialPortLocator of(Path dir) {
		dir = BasicUtil.defaultValue(dir, () -> Path.of(DEV_PATH));
		return new SerialPortLocator(dir);
	}

	private SerialPortLocator(Path dir) {
		this.dir = dir;
	}

	public PortSupplier usbPortSupplier(int index) {
		return portSupplier(USB_PATTERN, index);
	}

	public PortSupplier portSupplier(String pattern, int index) {
		return PortSupplier.named(() -> port(pattern, index),
			String.format("%s/{%s}[%d]", dir, pattern, index));
	}

	public String usbPort(int index) throws IOException {
		return port(USB_PATTERN, index);
	}

	public List<String> usbPorts() throws IOException {
		return ports(USB_PATTERN);
	}

	public String port(String pattern, int index) throws IOException {
		return index(ports(pattern), index, pattern);
	}

	public List<String> ports(String pattern) throws IOException {
		return IoUtil.list(dir, pattern).stream().map(Path::toString).sorted().toList();
	}

	private static String index(List<String> ports, int index, String pattern) {
		validateMin(index, 0);
		if (ports.isEmpty()) throw new IndexOutOfBoundsException("No ports available: " + pattern);
		if (index >= ports.size()) throw new IndexOutOfBoundsException(String.format(
			"Not enough ports available: index %d (total %d) %s", index, ports.size(), pattern));
		return ports.get(index);
	}

}
