package ceri.common.data;

import java.util.BitSet;

public class OctetBitSet extends BitSet {
	private static final long serialVersionUID = -4005476760615651068L;

	public static OctetBitSet create() {
		return new OctetBitSet();
	}

	public static OctetBitSet create(byte value) {
		OctetBitSet bitSet = new OctetBitSet();
		for (int i = 0; i < ByteUtil.BITS_PER_BYTE; i++)
			bitSet.set(i, ByteUtil.bit(value, i));
		return bitSet;
	}

	public byte value() {
		return toByteArray()[0];
	}

	private OctetBitSet() {
		super(ByteUtil.BITS_PER_BYTE);
	}

}
