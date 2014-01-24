package ceri.ci.build;

import java.util.Comparator;
import ceri.common.comparator.BaseComparator;
import ceri.common.comparator.EnumComparators;

public class EventComparators {
	public static final Comparator<Event> TIMESTAMP = new BaseComparator<Event>() {
		@Override
		protected int compareNonNull(Event o1, Event o2) {
			return o1.compareTo(o2);
		}
	};
	
	public static final Comparator<Event> TYPE = new BaseComparator<Event>() {
		@Override
		protected int compareNonNull(Event o1, Event o2) {
			return EnumComparators.<Event.Type>ordinal().compare(o1.type, o2.type);
		}
	};

	private EventComparators() {}

}
