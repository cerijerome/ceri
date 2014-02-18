package ceri.x10.cm11a.protocol;


public enum Protocol {
	OK(0x00),
	READY(0x55),
	DATA_POLL(0x5a),
	STATUS(0x8b),
	TIME(0x9b),
	TIME_POLL(0xa5),
	PC_READY(0xc3),
	RING_DISABLE(0xdb),
	RING_ENABLE(0xeb);

	public final byte value;

	private Protocol(int value) {
		this.value = (byte) value;
	}

	public static Protocol fromValue(int value) {
		byte b = (byte) value;
		for (Protocol p : Protocol.values())
			if (p.value == b) return p;
		throw new IllegalArgumentException("No such protocol byte: 0x" +
			Integer.toHexString(value & 0xff));
	}

}
