package ceri.serial.jna;

import java.io.Closeable;
import java.nio.ByteBuffer;
import com.sun.jna.Native;

public class CloseableMalloc implements Closeable {
	private final long nativePtr;
	private final int size;

	public static CloseableMalloc of(int size) {
		if (size == 0) return new CloseableMalloc(0L, size);
		long nativePtr = Native.malloc(size);
		if (nativePtr == 0) throw new OutOfMemoryError("Failed to malloc size: " + size);
		return new CloseableMalloc(nativePtr, size);
	}

	private CloseableMalloc(long nativePtr, int size) {
		this.nativePtr = nativePtr;
		this.size = size;
	}

	public ByteBuffer directBuffer() {
		return Native.getDirectByteBuffer(nativePtr, size);
	}

	@Override
	public void close() {
		if (nativePtr != 0) Native.free(nativePtr);
	}
}
