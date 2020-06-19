package ceri.serial.libusb.jna;

import java.nio.ByteBuffer;
import ceri.common.data.ByteUtil;
import ceri.common.math.MathUtil;
import ceri.common.test.BinaryPrinter;
import ceri.serial.jna.JnaUtil;

public class BbTester {
	private static final int PACKET_HEADER_SIZE = 2;
	private static final int readBufferChunkSize = 40;
	private static final ByteBuffer readBuffer = ByteBuffer.allocate(readBufferChunkSize);
	private static final int maxPacketSize = 64;
	private static final BinaryPrinter DBG =
		BinaryPrinter.builder().showBinary(false).columns(2).build();

	public static void main(String[] args) {
		readDataTest();
	}

	public static void readDataTest() {
		byte[] b = new byte[140];
		ByteBuffer buffer = ByteBuffer.wrap(b);
		buffer.clear();
		print(buffer);
		int n = readData(buffer, 132);
		System.out.printf("%n%d bytes copied%n", n);
		print(buffer);
		DBG.print(b);
	}

	public static int readData(ByteBuffer buffer, int size) {
		int remaining = size;
		while (remaining > 0) {
			int len = readLen(remaining);
			readBuffer.clear();
			int n = bulkTransfer(readBuffer, len);
			print(readBuffer);
			if (n <= PACKET_HEADER_SIZE) break;
			int packets = (n + maxPacketSize - 1) / maxPacketSize;
			for (int i = 0; i < packets; i++) {
				int position = (i * maxPacketSize) + PACKET_HEADER_SIZE;
				int limit = Math.min(n, position + maxPacketSize - PACKET_HEADER_SIZE);
				if (limit <= position) break;
				buffer.put(readBuffer.limit(limit).position(position));
				remaining -= (limit - position);
			}
		}
		return size - remaining;
	}

	private static int readLen(int size) {
		int packets = size / (maxPacketSize - PACKET_HEADER_SIZE);
		int rem = size % (maxPacketSize - PACKET_HEADER_SIZE);
		int total = (packets * maxPacketSize) + (rem > 0 ? PACKET_HEADER_SIZE + rem : 0);
		return Math.min(total, readBufferChunkSize);
	}

	private static byte x = 0x10;

	private static int bulkTransfer(ByteBuffer readBuffer, int len) {
		int n = MathUtil.random(0, 2) > 0 ? len : MathUtil.random(len / 2, len);
		byte[] b = new byte[n];
		int packets = 0;
		for (int i = 0; i < b.length; i++) {
			int mod = i % maxPacketSize;
			if (mod == 0) packets++;
			if (mod == PACKET_HEADER_SIZE) x++;
			b[i] = mod < PACKET_HEADER_SIZE ? (byte) 0xff : x;
		}
		readBuffer.put(b);
		System.out.printf("%nbulkTransfer: %d wanted, %d actual, %d packets%n", len, b.length,
			packets);
		DBG.print(b);
		return b.length;
	}

	static void mark(ByteBuffer bb) {
		bb.mark();
		System.out.println("mark()");
		print(bb);
	}

	static void reset(ByteBuffer bb) {
		bb.reset();
		System.out.println("reset()");
		print(bb);
	}

	static void flip(ByteBuffer bb) {
		bb.flip();
		System.out.println("flip()");
		print(bb);
	}

	static void rewind(ByteBuffer bb) {
		bb.rewind();
		System.out.println("rewind()");
		print(bb);
	}

	static void clear(ByteBuffer bb) {
		bb.clear();
		System.out.println("clear()");
		print(bb);
	}

	static void compact(ByteBuffer bb) {
		bb.compact();
		System.out.println("compact()");
		print(bb);
	}

	static void write(ByteBuffer bb, byte[] b, int len) {
		int n = len; // Math.min(len, bb.remaining());
		bb.put(b, 0, n);
		System.out.printf("write(%d) => (%d) [%s]%n", len, n, ByteUtil.toHex(b, 0, n, " "));
		print(bb);
	}

	static void read(ByteBuffer bb, int len) {
		int n = Math.min(len, bb.remaining());
		byte[] b = new byte[n];
		bb.get(b);
		System.out.printf("read(%d) => (%d) [%s]%n", len, n, ByteUtil.toHex(b, " "));
		print(bb);
	}

	public static void bbSettingsTest() {
		ByteBuffer bb = ByteBuffer.allocate(7);
		print(bb);
		int n = read0(bb, bb.capacity());
		print(bb);
		byte[] b = JnaUtil.byteArray(bb, 0, n);
		print(bb);
		System.out.println(ByteUtil.toHex(b, " "));
	}

	private static int read0(ByteBuffer buffer, int size) {
		int n = MathUtil.random(0, size);
		System.out.println("n=" + n);
		for (int i = 0; i < n; i++)
			buffer.put((byte) ('A' + i));
		return n;
	}

	private static void print(ByteBuffer bb) {
		System.out.printf("    pos=%d rem=%d lim=%d cap=%d%n", bb.position(), bb.remaining(),
			bb.limit(), bb.capacity());
	}
}
