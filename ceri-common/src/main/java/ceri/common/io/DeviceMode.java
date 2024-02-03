package ceri.common.io;

/**
 * General mode for device construction or state.
 */
public enum DeviceMode {
	/** Create the standard device / device state is enabled. */
	enabled,
	/** Use a disabled or no-op device / device state is disabled. */
	disabled,
	/** Use a test device or emulator / device is in test mode. */
	test;
}
