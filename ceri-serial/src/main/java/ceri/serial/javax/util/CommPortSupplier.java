package ceri.serial.javax.util;

import java.io.IOException;

/**
 * Provides the name of the comm port dynamically.
 */
public interface CommPortSupplier {
	String get() throws IOException;

	static CommPortSupplier fixed(String commPort) {
		return () -> commPort;
	}
}