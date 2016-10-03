package ceri.common.data;

import ceri.common.collection.ArrayUtil;
import ceri.common.collection.ImmutableByteArray;

public class Xor {
	private int value = 0;

	public byte value() {
		return (byte) value;
	}

	public Xor add(int... values) {
		return add(ByteUtil.bytes(values));
	}

	public Xor add(byte... bytes) {
		return add(bytes, 0);
	}

	public Xor add(byte[] bytes, int offset) {
		return add(bytes, offset, bytes.length - offset);
	}

	public Xor add(byte[] bytes, int offset, int length) {
		ArrayUtil.validateSlice(bytes.length, offset, length);
		for (int i = 0; i < length; i++)
			xor(bytes[offset + i]);
		return this;
	}

	public Xor add(ImmutableByteArray bytes) {
		return add(bytes, 0);
	}

	public Xor add(ImmutableByteArray bytes, int offset) {
		return add(bytes, offset, bytes.length - offset);
	}

	public Xor add(ImmutableByteArray bytes, int offset, int length) {
		ArrayUtil.validateSlice(bytes.length, offset, length);
		for (int i = 0; i < length; i++)
			xor(bytes.at(offset + i));
		return this;
	}

	private void xor(int b) {
		value = (value ^ (b & 0xff)) & 0xff;
	}

}
