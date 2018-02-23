package ceri.common.data;

import java.util.BitSet;

public class OctetBitSet extends BitSet {
	private static final long serialVersionUID = -4005476760615651068L;

	public static OctetBitSet create() {
		return of(0);
	}

	public static OctetBitSet of(int value) {
		return of((byte) value);
	}
	
	public static OctetBitSet of(byte value) {
		OctetBitSet bitSet = new OctetBitSet();
		for (int i = 0; i < Byte.SIZE; i++)
			bitSet.set(i, ByteUtil.bit(value, i));
		return bitSet;
	}

	public byte value() {
		return toByteArray()[0];
	}

	private OctetBitSet() {
		super(Byte.SIZE);
	}

}
