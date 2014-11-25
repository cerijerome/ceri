package ceri.common.comparator;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

public class BaseComparatorBehavior {
	private static BaseComparator<String> comparator = new BaseComparator<String>() {
		@Override
		protected int compareNonNull(String s1, String s2) {
			return Integer.MAX_VALUE;
		}
	};
	
	@Test
	public void shouldTreatNullsAsEqual() {
		assertThat(comparator.compare(null, null), is(0));
	}

	@Test
	public void shouldTreatNullAsInferior() {
		assertThat(comparator.compare(null, "") < 0, is(true));
		assertThat(comparator.compare("", null) > 0, is(true));
	}

	@Test
	public void shouldReturnZeroForEqualObjects() {
		assertThat(comparator.compare("", ""), is(0));
		assertThat(comparator.compare("", new String("")), is(0));
	}

	@Test
	public void shouldDelegateForNonNullAndNonEqualObjectsOnly() {
		assertThat(comparator.compare("1", "2"), is(Integer.MAX_VALUE));
	}

}
