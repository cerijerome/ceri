package ceri.common.io;

import ceri.common.data.Xcoder;
import ceri.common.util.Basics;

/**
 * Types and utilities for general device support.
 */
public class Device {

	private Device() {}
	
	/**
	 * General mode for device construction or state.
	 */
	public enum Mode {
		/** Use a disabled or no-op device / device state is disabled. */
		disabled(0),
		/** Create the standard device / device state is enabled. */
		enabled(1),
		/** Use a test device or emulator / device is in test mode. */
		test(2);

		public static final Xcoder.Type<Mode> xcoder = Xcoder.type(Mode.class);
		public final int value;

		public static boolean enabled(Mode mode) {
			return mode != null && mode.enabled();
		}

		public static boolean disabled(Mode mode) {
			return mode == null || mode.disabled();
		}

		public static Mode from(Boolean isEnabled) {
			return Basics.ternary(isEnabled, enabled, disabled, test);
		}

		private Mode(int value) {
			this.value = value;
		}

		public boolean enabled() {
			return this == enabled;
		}

		public boolean disabled() {
			return this == disabled;
		}
	}

	/**
	 * Event type for a connection/device state change.
	 */
	public enum Change {
		none(0),
		fixed(1),
		broken(2);

		public static final Xcoder.Type<Change> xcoder = Xcoder.type(Change.class);
		public final int value;

		public static Change from(Boolean isFixed) {
			return Basics.ternary(isFixed, fixed, broken, none);
		}

		private Change(int value) {
			this.value = value;
		}
	}
}
