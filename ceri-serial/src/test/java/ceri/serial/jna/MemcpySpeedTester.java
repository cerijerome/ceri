package ceri.serial.jna;

import java.util.stream.IntStream;
import com.sun.jna.Memory;
import ceri.common.data.ByteUtil;

public class MemcpySpeedTester {
	private static final int KB = 1024;
	private static final int MB = 1024 * KB;

	public static void main(String[] args) {
		compareTest(1 * MB, 8 * KB, 64);
		compareTest(64 * MB, 64 * KB, KB);
		compareTest(256 * MB, 8 * KB, 8 * KB);
		compareTest(32 * MB, 64 * KB, 64);
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
		byte[] b = ByteUtil.toByteArray(IntStream.range(0, size));
		Memory m0 = JnaUtil.malloc(b);
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < size - chunk; i += inc)
			JnaUtil.memcpy(m0, i, i + inc, chunk);
		return System.currentTimeMillis() - t0;
	}

	private static long testMemmoveSpeed(int size, int chunk, int inc) {
		byte[] b = ByteUtil.toByteArray(IntStream.range(0, size));
		Memory m0 = JnaUtil.malloc(b);
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < size - chunk; i += inc)
			JnaUtil.memmove(m0, i, i + inc, chunk);
		return System.currentTimeMillis() - t0;
	}

	private static long testReuseBufferSpeed(int size, int chunk, int inc) {
		byte[] b = ByteUtil.toByteArray(IntStream.range(0, size));
		Memory m0 = JnaUtil.malloc(b);
		byte[] buffer = new byte[chunk];
		long t0 = System.currentTimeMillis();
		for (int i = 0; i < size - chunk; i += inc) {
			m0.read(i + inc, buffer, 0, chunk);
			m0.write(i, buffer, 0, buffer.length);
		}
		return System.currentTimeMillis() - t0;
	}

}
