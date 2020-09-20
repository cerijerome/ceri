package ceri.serial.jna;

import static ceri.common.data.ByteUtil.BIG_ENDIAN;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.data.ByteWriter;
import ceri.common.util.BasicUtil;

/**
 * Extends ByteWriter for JNA-specific sequential access to bytes. As with ByteWriter, type T must
 * be the sub-class type; this allows fluent method calls without redefining all the methods with
 * covariant return types.
 * <p>
 * For bulk efficiency, consider overriding this method that processes one byte at a time:
 *
 * <pre>
 * T writeFrom(Pointer p, int offset, int length); [1-byte]
 * </pre>
 */
public interface JnaWriter<T extends JnaWriter<T>> extends ByteWriter<T> {

	/**
	 * Writes native-order bytes.
	 */
	default T writeNlong(NativeLong value) {
		return writeEndian(value.longValue(), NativeLong.SIZE, BIG_ENDIAN);
	}

	/**
	 * Writes big-endian bytes.
	 */
	default T writeNlongMsb(NativeLong value) {
		return writeEndian(value.longValue(), NativeLong.SIZE, true);
	}

	/**
	 * Writes little-endian bytes.
	 */
	default T writeNlongLsb(NativeLong value) {
		return writeEndian(value.longValue(), NativeLong.SIZE, false);
	}

	/**
	 * Writes bytes from the memory pointer.
	 */
	default T writeFrom(Memory m) {
		return writeFrom(m, 0);
	}

	/**
	 * Writes bytes from the memory pointer.
	 */
	default T writeFrom(Memory m, int offset) {
		return writeFrom(m, offset, JnaUtil.size(m) - offset);
	}

	/**
	 * Writes bytes from the memory pointer. Default implementation writes one byte at a time;
	 * efficiency may be improved by overriding.
	 */
	default T writeFrom(Pointer p, int offset, int length) {
		for (int i = 0; i < length; i++)
			writeByte(p.getByte(offset + i));
		return BasicUtil.uncheckedCast(this);
	}
}
