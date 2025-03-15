package ceri.common.util;

/**
 * Simple interface for a object with enabled state.
 */
public interface Enablable {

	boolean enabled();

	static boolean enabled(Enablable enablable) {
		return enablable != null && enablable.enabled();
	}

	static <T> T conditional(Enablable enablable, T enabled, T disabled) {
		return enabled(enablable) ? enabled : disabled;
	}
}
