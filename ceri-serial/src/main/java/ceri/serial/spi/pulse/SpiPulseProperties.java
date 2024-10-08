package ceri.serial.spi.pulse;

import ceri.common.function.FunctionUtil;
import ceri.common.property.TypedProperties;

public class SpiPulseProperties extends TypedProperties.Ref {
	private static final String CYCLE_KEY = "cycle";
	private static final String STD_KEY = "std";
	private static final String TYPE_KEY = "type";
	private static final String BITS_KEY = "bits";
	private static final String OFFSET_KEY = "offset";
	private static final String T0_KEY = "t0";
	private static final String T1_KEY = "t1";
	private static final String SIZE_KEY = "size";
	private static final String DELAY_MICROS_KEY = "delay.micros";
	private static final String RESET_DELAY_MS_KEY = "recovery.delay.ms";
	private static final int CYCLE_BITS_DEF = 4;
	private static final int CYCLE_OFFSET_DEF = 0;
	private static final int CYCLE_T0_BITS_DEF = 1;
	private static final int CYCLE_T1_BITS_DEF = 2;

	public SpiPulseProperties(TypedProperties properties, String... groups) {
		super(properties, groups);
	}

	public SpiPulseConfig config() {
		int size = parse(SIZE_KEY).asInt().getValid();
		SpiPulseConfig.Builder b = SpiPulseConfig.builder(size);
		FunctionUtil.safeAccept(cycle(), b::cycle);
		parse(DELAY_MICROS_KEY).asInt().accept(b::delayMicros);
		parse(RESET_DELAY_MS_KEY).asInt().accept(b::resetDelayMs);
		return b.build();
	}

	private PulseCycle cycle() {
		var std = parse(CYCLE_KEY, STD_KEY).toEnum(PulseCycle.Std.class);
		if (std != null) return std.cycle;
		var type = parse(CYCLE_KEY, TYPE_KEY).toEnum(PulseCycle.Type.class);
		if (type == null) return null;
		int bits = parse(CYCLE_KEY, BITS_KEY).toInt(CYCLE_BITS_DEF);
		int offset = parse(CYCLE_KEY, OFFSET_KEY).toInt(CYCLE_OFFSET_DEF);
		int t0Bits = parse(CYCLE_KEY, T0_KEY, BITS_KEY).toInt(CYCLE_T0_BITS_DEF);
		int t1Bits = parse(CYCLE_KEY, T1_KEY, BITS_KEY).toInt(CYCLE_T1_BITS_DEF);
		return PulseCycle.of(type, bits, offset, t0Bits, t1Bits);
	}
}
