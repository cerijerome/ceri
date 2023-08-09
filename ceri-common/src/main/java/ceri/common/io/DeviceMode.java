package ceri.common.io;

/**
 * General mode for device construction.
 */
public enum DeviceMode {
	/** Create the standard device. */
	enabled,
	/** Use a disabled or no-op device. */
	disabled,
	/** Use a test device or emulator. */
	test;
}
