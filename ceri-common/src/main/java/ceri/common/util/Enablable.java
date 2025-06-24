package ceri.common.util;

import java.util.Comparator;
import java.util.function.Predicate;

/**
 * Simple interface for a object with enabled state.
 */
public interface Enablable {
	static Comparator<Enablable> COMPARATOR = Comparator.comparing(Enablable::enabledInt);
	static Predicate<Enablable> PREDICATE = t -> t.enabled();

	boolean enabled();

	default int enabledInt() {
		return enabled() ? 1 : 0;
	}

	static boolean enabled(Enablable enablable) {
		return enablable != null && enablable.enabled();
	}

	static <T> T conditional(Enablable enablable, T enabled, T disabled) {
		return enabled(enablable) ? enabled : disabled;
	}
}
