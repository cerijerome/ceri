package ceri.serial.mcp;

import ceri.common.data.TypeTranscoder;

public enum Mcp3008Channel implements Mcp3008Input {
	CH0(0),
	CH1(1),
	CH2(2),
	CH3(3),
	CH4(4),
	CH5(5),
	CH6(6),
	CH7(7);

	public static final int COUNT = 8;
	private static final int SINGLE_ENDED_MASK = 0x80;
	private static final int CHANNEL_MASK = 0x70;
	private static final int SHIFT_BITS = 4;
	private static final TypeTranscoder<Mcp3008Channel> xcoder =
		TypeTranscoder.of(t -> t.channel, Mcp3008Channel.class);
	public final int channel;
	private final byte value;

	public static Mcp3008Channel fromChannel(int channel) {
		return xcoder.decode(channel);
	}

	public static Mcp3008Channel fromValue(int value) {
		return fromChannel((value & CHANNEL_MASK) >>> SHIFT_BITS);
	}

	private Mcp3008Channel(int channel) {
		this.channel = channel;
		value = (byte) (SINGLE_ENDED_MASK | (channel << SHIFT_BITS));
	}

	@Override
	public byte encode() {
		return value;
	}

}
