package ceri.common.io;

import ceri.common.data.TypeTranscoder;
import ceri.common.util.Basics;

/**
 * General mode for device construction or state.
 */
public enum DeviceMode {
	/** Use a disabled or no-op device / device state is disabled. */
	disabled(0),
	/** Create the standard device / device state is enabled. */
	enabled(1),
	/** Use a test device or emulator / device is in test mode. */
	test(2);

	public static final TypeTranscoder<DeviceMode> xcoder =
		TypeTranscoder.of(t -> t.value, DeviceMode.class);
	public final int value;

	public static boolean enabled(DeviceMode mode) {
		return mode != null && mode.enabled();
	}

	public static boolean disabled(DeviceMode mode) {
		return mode == null || mode.disabled();
	}

	public static DeviceMode from(Boolean isEnabled) {
		return Basics.ternary(isEnabled, enabled, disabled, test);
	}

	private DeviceMode(int value) {
		this.value = value;
	}

	public boolean enabled() {
		return this == enabled;
	}

	public boolean disabled() {
		return this == disabled;
	}
}
