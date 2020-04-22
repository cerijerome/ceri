package ceri.serial.jna;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.ShortByReference;
import ceri.common.collection.ArrayUtil;
import ceri.common.text.ToStringHelper;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

/**
 * Encapsulates a buffer, tracking its pointer and length in bytes.
 */
@Deprecated
public class Buffer {
	public final int size;
	public final Pointer p;

	public static Buffer of(ByteByReference ref) {
		return of(Byte.BYTES, ref.getPointer());
	}

	public static Buffer of(ShortByReference ref) {
		return of(Short.BYTES, ref.getPointer());
	}

	public static Buffer of(IntByReference ref) {
		return of(Integer.BYTES, ref.getPointer());
	}

	public static Buffer of(Memory memory) {
		return of(JnaUtil.size(memory), memory);
	}

	public static Buffer of(int size, Pointer p) {
		return new Buffer(size, p);
	}

	private Buffer(int size, Pointer p) {
		this.size = size;
		this.p = p;
	}

	public Buffer sub(int offset, int len) {
		ArrayUtil.validateSlice(size, offset, len);
		return of(len, p.getPointer(offset));
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(size, p);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Buffer)) return false;
		Buffer other = (Buffer) obj;
		if (size != other.size) return false;
		if (!EqualsUtil.equals(p, other.p)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, size, p).toString();
	}

}
