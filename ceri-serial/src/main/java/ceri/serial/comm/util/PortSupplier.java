package ceri.serial.comm.util;

import java.io.IOException;

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
}