package ceri.common.comparator;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import java.util.Comparator;
import org.junit.Test;

public class ReverseComparatorBehavior {

	@Test
	public void shouldReverseComparatorResults() {
		Comparator<Integer> comparator = Comparators.<Integer>byComparable();
		Comparator<Integer> reverseComparator = ReverseComparator.create(comparator);
		assertThat(reverseComparator.compare(0, 1), is(-comparator.compare(0, 1)));
		assertThat(reverseComparator.compare(1, 0), is(-comparator.compare(1, 0)));
		assertThat(reverseComparator.compare(1, 1), is(-comparator.compare(1, 1)));
	}

}
