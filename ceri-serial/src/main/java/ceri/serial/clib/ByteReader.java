package ceri.serial.clib;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.serial.clib.jna.CException;
import ceri.serial.jna.JnaUtil;

/**
 * Provides common byte access methods, based on underlying pointer access method.
 */
public interface ByteReader {

	int readInto(Pointer p, int len) throws CException;

	default int readInto(byte[] buffer) throws CException {
		return readInto(buffer, 0);
	}

	default int readInto(byte[] buffer, int offset) throws CException {
		return readInto(buffer, offset, buffer.length - offset);
	}

	default int readInto(byte[] buffer, int offset, int len) throws CException {
		Memory m = new Memory(len);
		int n = readInto(m, len);
		m.read(0, buffer, offset, n);
		return offset + n;
	}

	default int readInto(Pointer p, int offset, int len) throws CException {
		return offset + readInto(p.getPointer(offset), len);
	}

	default byte[] read(int len) throws CException {
		Memory m = new Memory(len);
		int n = readInto(m, len);
		return JnaUtil.byteArray(m, n);
	}

	default int readByte() throws CException {
		ByteByReference ref = new ByteByReference();
		readInto(ref.getPointer(), Byte.BYTES);
		return JnaUtil.ubyte(ref);
	}

	default int readShort() throws CException {
		ShortByReference ref = new ShortByReference();
		readInto(ref.getPointer(), Short.BYTES);
		return JnaUtil.ushort(ref);
	}

	default int readInt() throws CException {
		IntByReference ref = new IntByReference();
		readInto(ref.getPointer(), Integer.BYTES);
		return ref.getValue();
	}

}
