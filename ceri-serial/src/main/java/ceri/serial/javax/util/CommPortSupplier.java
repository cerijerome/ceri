package ceri.serial.javax.util;

import java.io.IOException;

/**
 * Provides the name of the comm port dynamically.
 */
public interface CommPortSupplier {
	String get() throws IOException;

	static CommPortSupplier named(CommPortSupplier supplier, String name) {
		return new CommPortSupplier() {
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

	static CommPortSupplier fixed(String commPort) {
		if (commPort == null) return null;
		return named(() -> commPort, commPort);
	}

}