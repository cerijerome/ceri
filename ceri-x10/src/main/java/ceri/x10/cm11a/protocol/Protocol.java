package ceri.x10.cm11a.protocol;

import ceri.common.data.Xcoder;

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

	public static final Xcoder.Type<Protocol> xcoder = Xcoder.type(Protocol.class);
	public final int value;

	public static Protocol from(int value) {
		return xcoder.decodeValid(value, Protocol.class.getSimpleName());
	}

	Protocol(int value) {
		this.value = value;
	}
}
