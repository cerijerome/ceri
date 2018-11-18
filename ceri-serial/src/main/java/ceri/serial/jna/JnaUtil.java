package ceri.serial.jna;

import java.io.IOException;
import java.util.function.IntSupplier;
import com.sun.jna.Native;
import ceri.common.util.BasicUtil;

public class JnaUtil {
	public static final int INVALID_FILE_DESCRIPTOR = -1;

	private JnaUtil() {}

	public static int exec(IntSupplier supplier, String name) throws IOException {
		return verify(supplier.getAsInt(), name);
	}

	public static int verify(int result, String name) throws IOException {
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

	public static <T> T loadLibrary(String name, Class<T> cls) {
		return BasicUtil.uncheckedCast(Native.loadLibrary(name, cls));
	}

	public static void setProtected() {
		Native.setProtected(true);
	}

}
