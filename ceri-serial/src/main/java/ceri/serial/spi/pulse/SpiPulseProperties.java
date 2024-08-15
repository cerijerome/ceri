package ceri.serial.spi.pulse;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.TypedProperties;
import ceri.common.util.Ref;

public class SpiPulseProperties extends Ref<TypedProperties> {
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
		super(TypedProperties.from(properties, groups));
	}

	public SpiPulseConfig config() {
		SpiPulseConfig.Builder b = SpiPulseConfig.builder(size());
		safeAccept(cycle(), b::cycle);
		safeAccept(delayMicros(), b::delayMicros);
		safeAccept(resetDelayMs(), b::resetDelayMs);
		return b.build();
	}

	private int size() {
		return ref.intValue(SIZE_KEY);
	}

	private Integer delayMicros() {
		return ref.intValue(DELAY_MICROS_KEY);
	}

	private Integer resetDelayMs() {
		return ref.intValue(RESET_DELAY_MS_KEY);
	}

	private PulseCycle cycle() {
		PulseCycle.Std std = cycleStd();
		if (std != null) return std.cycle;
		PulseCycle.Type type = cycleType();
		if (type == null) return null;
		return PulseCycle.of(type, cycleBits(), cycleOffset(), cycleT0Bits(), cycleT1Bits());
	}

	private PulseCycle.Std cycleStd() {
		return ref.enumValue(PulseCycle.Std.class, CYCLE_KEY, STD_KEY);
	}

	private PulseCycle.Type cycleType() {
		return ref.enumValue(PulseCycle.Type.class, CYCLE_KEY, TYPE_KEY);
	}

	private int cycleBits() {
		return ref.intValue(CYCLE_BITS_DEF, CYCLE_KEY, BITS_KEY);
	}

	private int cycleOffset() {
		return ref.intValue(CYCLE_OFFSET_DEF, CYCLE_KEY, OFFSET_KEY);
	}

	private int cycleT0Bits() {
		return ref.intValue(CYCLE_T0_BITS_DEF, CYCLE_KEY, T0_KEY, BITS_KEY);
	}

	private int cycleT1Bits() {
		return ref.intValue(CYCLE_T1_BITS_DEF, CYCLE_KEY, T1_KEY, BITS_KEY);
	}
}
