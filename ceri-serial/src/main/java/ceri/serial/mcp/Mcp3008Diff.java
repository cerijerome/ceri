package ceri.serial.mcp;

import static ceri.common.collection.StreamUtil.stream;
import static ceri.serial.mcp.Mcp3008Channel.CH0;
import static ceri.serial.mcp.Mcp3008Channel.CH1;
import static ceri.serial.mcp.Mcp3008Channel.CH2;
import static ceri.serial.mcp.Mcp3008Channel.CH3;
import static ceri.serial.mcp.Mcp3008Channel.CH4;
import static ceri.serial.mcp.Mcp3008Channel.CH5;
import static ceri.serial.mcp.Mcp3008Channel.CH6;
import static ceri.serial.mcp.Mcp3008Channel.CH7;

public enum Mcp3008Diff implements Mcp3008Input {
	CH0_CH1(CH0, CH1),
	CH1_CH0(CH1, CH0),
	CH2_CH3(CH2, CH3),
	CH3_CH2(CH3, CH2),
	CH4_CH5(CH4, CH5),
	CH5_CH4(CH5, CH4),
	CH6_CH7(CH6, CH7),
	CH7_CH6(CH7, CH6);

	private static final int SHIFT_BITS = 4;
	public final Mcp3008Channel plus;
	public final Mcp3008Channel minus;
	private final byte value;

	public static Mcp3008Diff fromValue(int value) {
		return fromPlus(Mcp3008Channel.fromValue(value));
	}

	public static Mcp3008Diff fromPlus(Mcp3008Channel plus) {
		return stream(Mcp3008Diff.class).filter(d -> d.plus == plus).findFirst().orElse(null);
	}

	public static Mcp3008Diff fromMinus(Mcp3008Channel minus) {
		return stream(Mcp3008Diff.class).filter(d -> d.minus == minus).findFirst().orElse(null);
	}

	private Mcp3008Diff(Mcp3008Channel plus, Mcp3008Channel minus) {
		this.plus = plus;
		this.minus = minus;
		value = (byte) (plus.channel << SHIFT_BITS);
	}

	@Override
	public byte encode() {
		return value;
	}
	
}
