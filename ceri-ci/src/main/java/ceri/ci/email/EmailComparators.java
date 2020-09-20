package ceri.ci.email;

import java.util.Comparator;
import ceri.common.comparator.Comparators;

/**
 * Comparators for sorting email objects.
 */
public class EmailComparators {
	public static final Comparator<Email> SENT_DATE =
		Comparators.nonNull((lhs, rhs) -> Comparators.LONG.compare(lhs.sentDateMs, rhs.sentDateMs));

	private EmailComparators() {}

}
