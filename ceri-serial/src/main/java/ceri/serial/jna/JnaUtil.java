package ceri.serial.jna;

import java.io.IOException;
import java.util.function.IntFunction;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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

	public static <T extends Structure> T[] array(Structure p, int count,
		IntFunction<T[]> arrayConstructor) {
		T[] array = BasicUtil.uncheckedCast(array(p, count));
		return array != null ? array : arrayConstructor.apply(0);
	}
	
	public static Structure[] array(Structure p, int count) {
		if (p != null) return BasicUtil.uncheckedCast(p.toArray(count));
		if (count == 0) return null; 
		throw new IllegalArgumentException("Null pointer but count > 0: " + count);
	}
	
	public static byte[] buffer(Pointer p, int len) {
		if (p != null) return p.getByteArray(0, len);
		if (len == 0) return null; 
		throw new IllegalArgumentException("Null pointer but length > 0: " + len);
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
