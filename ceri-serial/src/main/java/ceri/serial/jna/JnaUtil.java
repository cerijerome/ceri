package ceri.serial.jna;

import java.io.IOException;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.BasicUtil;

public class JnaUtil {
	public static final int INVALID_FILE_DESCRIPTOR = -1;

	private JnaUtil() {}

	public static int verify(int result) throws IOException {
		return verify(result, "call");
	}

	public static int verify(int result, String name) throws IOException {
		if (result >= 0) return result;
		throw BasicUtil.exceptionf(IOException::new, "JNA %s failed: %d",  name, result);
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

	public static Memory malloc(byte[] array) {
		return malloc(array, 0);
	}
	
	public static Memory malloc(byte[] array, int offset) {
		return malloc(array, offset, array.length - offset);
	}
	
	public static Memory malloc(byte[] array, int offset, int length) {
		ArrayUtil.validateSlice(array.length, offset, length);
		Memory m = new Memory(length);
		m.write(0, array, offset, length);
		return m;
	}
	
}
