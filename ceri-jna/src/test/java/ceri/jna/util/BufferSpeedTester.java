package ceri.jna.util;

import static ceri.common.time.TimeSupplier.micros;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteReceiver;

/**
 * Compare memory access 1-byte vs byte[] buffer. Copies between Memory and
 * ByteProvider/ByteReceiver wrapper for byte[]. Results show buffer is far better, even for small
 * reads/writes.
 *
 * <pre>
 * Results:
 * []->P  [16B x100000] => single=27ms(17.1ns/B) buffer=14ms(8.8ns/B)
 *  P->[] [16B x100000] => single=29ms(18.0ns/B) buffer=15ms(9.6ns/B)
 * []->P  [256B x10000] => single=39ms(15.1ns/B) buffer=4ms(1.4ns/B)
 *  P->[] [256B x10000] => single=40ms(15.8ns/B) buffer=2ms(0.7ns/B)
 * []->P  [4096B x1000] => single=61ms(14.9ns/B) buffer=2ms(0.6ns/B)
 *  P->[] [4096B x1000] => single=59ms(14.4ns/B) buffer=1ms(0.2ns/B)
 * []->P  [65536B x100] => single=106ms(16.2ns/B) buffer=5ms(0.8ns/B)
 *  P->[] [65536B x100] => single=97ms(14.8ns/B) buffer=5ms(0.8ns/B)
 * </pre>
 */
public class BufferSpeedTester {
	private final int RUNS = 3;
	private final int REP_SKIP = 10;

	public static void main(String[] args) {
		BufferSpeedTester tester = new BufferSpeedTester();
		tester.copyCompare(16, 100000);
		tester.copyCompare(256, 10000);
		tester.copyCompare(4096, 1000);
		tester.copyCompare(64 * 1024, 100);
	}

	private void copyCompare(int size, int reps) {
		// copies from ByteProvider to Pointer
		copyCompare(" []->P ", size, reps, this::copyFrom, this::copyBufferFrom);
		// copies to ByteReceiver from Pointer
		copyCompare("  P->[]", size, reps, this::copyTo, this::copyBufferTo);
		System.out.println();
	}

	private void copyCompare(String desc, int size, int reps, BiConsumer<Pointer, Mutable> singleFn,
		BiConsumer<Pointer, Mutable> bufferFn) {
		long[] t = new long[2];
		for (int i = 0; i < RUNS; i++) { // overwrite first runs
			t[0] = copy(size, reps, singleFn);
			t[1] = copy(size, reps, bufferFn);
		}
		print(desc, size, reps, t);
	}

	private void print(String desc, int size, int reps, long[] t) {
		long bytes = size * reps;
		System.out.printf("%s [%dB x%d] => single=%s buffer=%s%n", desc, size, reps,
			value(t[0], bytes), value(t[1], bytes));
	}

	private String value(long micros, long bytes) {
		return String.format("%.0fms(%.1fns/B)", micros / 1000.0, (1000.0 * micros) / bytes);
	}

	private long copy(int size, int reps, BiConsumer<Pointer, Mutable> fn) {
		long t = 0;
		for (int i = 0; i < reps + REP_SKIP; i++) {
			Mutable m = Mutable.wrap(random(size));
			try (Memory p = new Memory(size)) {
				p.write(0, random(size), 0, size);
				long t0 = micros.time();
				fn.accept(p, m);
				if (i >= REP_SKIP) t += micros.time() - t0;
			}
		}
		return t;
	}

	private byte[] random(int size) {
		byte[] bytes = new byte[size];
		ThreadLocalRandom.current().nextBytes(bytes);
		return bytes;
	}

	/**
	 * Copies from ByteProvider to Pointer, 1 byte at a time.
	 */
	private void copyFrom(Pointer p, ByteProvider provider) {
		for (int i = 0; i < provider.length(); i++)
			p.setByte(i, provider.getByte(i));
	}

	/**
	 * Copies from ByteProvider to Pointer, using a byte array buffer.
	 */
	private void copyBufferFrom(Pointer p, ByteProvider provider) {
		byte[] b = new byte[provider.length()];
		provider.copyTo(0, b);
		p.write(0, b, 0, b.length);
	}

	/**
	 * Copies to ByteReceiver from Pointer, 1 byte at a time.
	 */
	private void copyTo(Pointer p, ByteReceiver receiver) {
		for (int i = 0; i < receiver.length(); i++)
			receiver.setByte(i, p.getByte(i));
	}

	/**
	 * Copies to ByteReceiver from Pointer, using a byte array buffer.
	 */
	private void copyBufferTo(Pointer p, ByteReceiver receiver) {
		byte[] b = new byte[receiver.length()];
		p.write(0, b, 0, b.length);
		receiver.copyFrom(0, b);
	}

}
