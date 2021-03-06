package ceri.serial.ftdi.util;

import static ceri.common.function.FunctionUtil.safeAccept;
import ceri.common.property.BaseProperties;
import ceri.serial.ftdi.FtdiBitMode;
import ceri.serial.ftdi.FtdiLineProperties;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_interface;
import ceri.serial.ftdi.jna.LibFtdi.ftdi_mpsse_mode;

public class SelfHealingFtdiProperties extends BaseProperties {
	private static final String FINDER_KEY = "finder";
	private static final String PORT_KEY = "port";
	private static final String INTERFACE_KEY = "interface";
	private static final String BIT_KEY = "bit";
	private static final String MODE_KEY = "mode";
	private static final String MASK_KEY = "mask";
	private static final String BAUD_RATE_KEY = "baud.rate";
	private static final String FIX_RETRY_DELAY_MS_KEY = "fix.retry.delay.ms";
	private static final String RECOVERY_DELAY_MS_KEY = "recovery.delay.ms";
	private static final String BITMODE_PREFIX = "BITMODE_";
	private static final String INTERFACE_PREFIX = "INTERFACE_";
	private final FtdiLineProperties port;

	public SelfHealingFtdiProperties(BaseProperties properties, String... groups) {
		super(properties, groups);
		port = new FtdiLineProperties(this, PORT_KEY);
	}

	public SelfHealingFtdiConfig config() {
		SelfHealingFtdiConfig.Builder b = SelfHealingFtdiConfig.builder().line(port.params());
		safeAccept(finder(), b::finder);
		safeAccept(iface(), b::iface);
		safeAccept(ftdiBitMode(), b::bitmode);
		safeAccept(portBaudRate(), b::baud);
		safeAccept(fixRetryDelayMs(), b::fixRetryDelayMs);
		safeAccept(recoveryDelayMs(), b::recoveryDelayMs);
		return b.build();
	}

	private String finder() {
		return value(FINDER_KEY);
	}

	private ftdi_interface iface() {
		String name = value(INTERFACE_KEY);
		if (name == null) return null;
		return ftdi_interface.valueOf(INTERFACE_PREFIX + name.toUpperCase());
	}

	private FtdiBitMode ftdiBitMode() {
		ftdi_mpsse_mode mode = bitMode();
		if (mode == null) return null;
		FtdiBitMode.Builder b = FtdiBitMode.builder(mode);
		safeAccept(bitMask(), b::mask);
		return b.build();
	}

	private ftdi_mpsse_mode bitMode() {
		String name = value(BIT_KEY, MODE_KEY);
		if (name == null) return null;
		return ftdi_mpsse_mode.valueOf(BITMODE_PREFIX + name.toUpperCase());
	}

	private Integer bitMask() {
		return intValue(BIT_KEY, MASK_KEY);
	}

	private Integer portBaudRate() {
		return intValue(PORT_KEY, BAUD_RATE_KEY);
	}

	private Integer fixRetryDelayMs() {
		return intValue(FIX_RETRY_DELAY_MS_KEY);
	}

	private Integer recoveryDelayMs() {
		return intValue(RECOVERY_DELAY_MS_KEY);
	}

}
