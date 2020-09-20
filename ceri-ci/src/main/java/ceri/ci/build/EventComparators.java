package ceri.ci.build;

import java.util.Comparator;
import ceri.common.comparator.Comparators;
import ceri.common.comparator.EnumComparators;

public class EventComparators {
	public static final Comparator<Event> TIMESTAMP = Comparators.nonNull(Event::compareTo);
	public static final Comparator<Event> TYPE = Comparators
		.nonNull((lhs, rhs) -> EnumComparators.<Event.Type>ordinal().compare(lhs.type, rhs.type));

	private EventComparators() {}

}
