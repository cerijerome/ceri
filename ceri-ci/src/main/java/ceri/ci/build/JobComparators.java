package ceri.ci.build;

import java.util.Comparator;
import ceri.common.comparator.BaseComparator;
import ceri.common.comparator.Comparators;

public class JobComparators {
	public static final Comparator<Job> NAME = new BaseComparator<Job>() {
		@Override
		protected int compareNonNull(Job o1, Job o2) {
			return Comparators.STRING.compare(o1.name, o2.name);
		}
	};

	private JobComparators() {}
	
}
