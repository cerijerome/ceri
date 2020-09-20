package ceri.serial.jna;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.data.ByteReader;

/**
 * Extends ByteReader for JNA-specific sequential access to bytes. As with ByteReader, reads are of
 * known length, or require a given length. For bulk efficiency, consider overriding this method
 * that processes one byte at a time:
 *
 * <pre>
 * int readInto(Pointer p, int offset, int length); [1-byte]
 * </pre>
 */
public interface JnaReader extends ByteReader {

	/**
	 * Returns the value from native-order bytes.
	 */
	default NativeLong readNlong() {
		return new NativeLong(readEndian(NativeLong.SIZE, BIG_ENDIAN), false);
	}

	/**
	 * Returns the value from big-endian bytes.
	 */
	default NativeLong readNlongMsb() {
		return new NativeLong(readEndian(NativeLong.SIZE, false), false);
	}

	/**
	 * Returns the value from little-endian bytes.
	 */
	default NativeLong readNlongLsb() {
		return new NativeLong(readEndian(NativeLong.SIZE, false), false);
	}

	/**
	 * Returns the unsigned value from native-order bytes.
	 */
	default NativeLong readUnlong() {
		return new NativeLong(readEndian(NativeLong.SIZE, BIG_ENDIAN), true);
	}

	/**
	 * Returns the unsigned value from big-endian bytes.
	 */
	default NativeLong readUnlongMsb() {
		return new NativeLong(readEndian(NativeLong.SIZE, true), true);
	}

	/**
	 * Returns the unsigned value from little-endian bytes.
	 */
	default NativeLong readUnlongLsb() {
		return new NativeLong(readEndian(NativeLong.SIZE, false), true);
	}

	/**
	 * Reads bytes into the memory pointer. Returns the destination offset after reading.
	 */
	default int readInto(Memory m) {
		return readInto(m, 0);
	}

	/**
	 * Reads bytes into the memory pointer. Returns the destination offset after reading.
	 */
	default int readInto(Memory m, int offset) {
		return readInto(m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Reads bytes into the memory pointer. Returns the destination offset after reading. Default
	 * implementation reads one byte at a time; efficiency may be improved by overriding.
	 */
	default int readInto(Pointer p, int offset, int length) {
		while (length-- > 0)
			p.setByte(offset++, readByte());
		return offset;
	}
}
