package ceri.serial.jna;

import java.io.IOException;
import java.util.function.IntSupplier;

public class JnaUtil {
	public static final int INVALID_FILE_DESCRIPTOR = -1;

	private JnaUtil() {}

	public static int exec(IntSupplier supplier, String name) throws IOException {
		int result = supplier.getAsInt();
		if (result < 0) throw new IOException("JNA " + name + " failed (" + result + ")");
		return result;
	}

	public static boolean isValidFileDescriptor(int fileDescriptor) {
		return fileDescriptor != INVALID_FILE_DESCRIPTOR;
	}
	
	public static int validateFileDescriptor(int fileDescriptor) throws IOException {
		if (isValidFileDescriptor(fileDescriptor)) return fileDescriptor;
		throw new IOException("Invalid file descriptor: " + fileDescriptor);
	}
	
}
