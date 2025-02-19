package ceri.serial.comm.util;

import static ceri.common.validation.ValidationUtil.validateMin;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

/**
 * Provides the name of the serial port dynamically.
 */
public interface PortSupplier {
	static PortSupplier NULL = () -> null;

	String get() throws IOException;

	static PortSupplier named(PortSupplier supplier, String name) {
		return new PortSupplier() {
			@Override
			public String get() throws IOException {
				return supplier.get();
			}

			@Override
			public String toString() {
				return name;
			}
		};
	}

	static PortSupplier fixed(String port) {
		return port == null ? null : named(() -> port, port);
	}

	static Locator locator(Path dir) {
		dir = BasicUtil.defaultValue(dir, () -> Path.of(Locator.DEV_PATH));
		return new Locator(dir);
	}

	/**
	 * Logic for finding serial ports.
	 */
	class Locator {
		private static final String DEV_PATH = "/dev/";
		private static final String USB_PATTERN = "regex:tty\\.(usb|USB).*";
		private final Path dir;

		private Locator(Path dir) {
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
			if (ports.isEmpty())
				throw new IndexOutOfBoundsException("No ports available: " + pattern);
			if (index >= ports.size()) throw new IndexOutOfBoundsException(
				String.format("Not enough ports available: index %d (total %d) %s", index,
					ports.size(), pattern));
			return ports.get(index);
		}
	}

}