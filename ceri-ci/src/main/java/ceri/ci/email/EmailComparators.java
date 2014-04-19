package ceri.ci.email;

import java.util.Comparator;
import ceri.common.comparator.BaseComparator;
import ceri.common.comparator.Comparators;

/**
 * Comparators for sorting email objects.
 */
public class EmailComparators {
	public static final Comparator<Email> SENT_DATE = new BaseComparator<Email>() {
		@Override
		protected int compareNonNull(Email o1, Email o2) {
			return Comparators.LONG.compare(o1.sentDateMs, o2.sentDateMs);
		}
	};

	private EmailComparators() {}

}
