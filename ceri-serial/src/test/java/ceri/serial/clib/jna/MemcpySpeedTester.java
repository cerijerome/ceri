package ceri.serial.clib.jna;

import java.util.stream.IntStream;
import com.sun.jna.Memory;
import ceri.common.data.ByteUtil;

/**
 * Tests the speed of copying chunks back by a number of bytes within a block of allocated memory.
 * On Mac, results show read/write byte[] is best for small chunks, otherwise memcpy.
 */
public class MemcpySpeedTester {
	private static final int KB = 1024;
	private static final int MB = 1024 * KB;

	public static void main(String[] args) {
		compareTest(1 * MB, 8 * KB, 64); // 110/188/[24] ms
		compareTest(64 * MB, 64 * KB, KB); // [405]/1158/466 ms
		compareTest(256 * MB, 8 * KB, 8 * KB); // [127]/174/134 ms
		compareTest(32 * MB, 64 * KB, 64); // [3052]/8190/3266 ms
	}

	private static void compareTest(int size, int chunk, int inc) {
		System.out.printf("Testing: size=%d chunk=%d inc=%d%n", size, chunk, inc);
		long t = testMemcpySpeed(size, chunk, inc);
		System.out.printf("Memcpy time: %d ms%n", t);
		t = testMemmoveSpeed(size, chunk, inc);
		System.out.printf("Memmove time: %d ms%n", t);
		t = testReuseBufferSpeed(size, chunk, inc);
		System.out.printf("R-buffer time: %d ms%n", t);
		System.out.println();
	}

	private static long testMemcpySpeed(int size, int chunk, int inc) {
		byte[] b = ByteUtil.bytes(IntStream.range(0, size));
		Memory m0 = CUtil.malloc(b);
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < size - chunk; i += inc)
			CUtil.memcpy(m0, i, i + inc, chunk); // memcpy each chunk back by inc bytes
		return System.currentTimeMillis() - t0;
	}

	private static long testMemmoveSpeed(int size, int chunk, int inc) {
		byte[] b = ByteUtil.bytes(IntStream.range(0, size));
		Memory m0 = CUtil.malloc(b);
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < size - chunk; i += inc)
			CUtil.memmove(m0, i, i + inc, chunk); // memmove each chunk forward by inc bytes
		return System.currentTimeMillis() - t0;
	}

	private static long testReuseBufferSpeed(int size, int chunk, int inc) {
		byte[] b = ByteUtil.bytes(IntStream.range(0, size));
		Memory m0 = CUtil.malloc(b);
		byte[] buffer = new byte[chunk];
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < size - chunk; i += inc) {
			m0.read(i + inc, buffer, 0, chunk); // read each chunk into byte[]
			m0.write(i, buffer, 0, buffer.length); // write byte[] forward by inc bytes
		}
		return System.currentTimeMillis() - t0;
	}

}
