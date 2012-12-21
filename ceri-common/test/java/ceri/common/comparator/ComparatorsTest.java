package ceri.common.comparator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Comparator;
import org.junit.Test;

public class ComparatorsTest {
	
	@Test
	public void testPrimitiveComparator() {
		assertThat(Comparators.BOOLEAN.compare(null, null), is(0));
		assertThat(Comparators.BOOLEAN.compare(null, true) < 0, is(true));
		assertThat(Comparators.BOOLEAN.compare(null, false) < 0, is(true));
		assertThat(Comparators.BOOLEAN.compare(true, null) > 0, is(true));
		assertThat(Comparators.BOOLEAN.compare(false, null) > 0, is(true));
		assertThat(Comparators.BOOLEAN.compare(false, true) < 0, is(true));
		assertThat(Comparators.BOOLEAN.compare(true, false) > 0, is(true));
	}

	@Test
	public void testByComparable() {
		Comparator<String> comparator = Comparators.byComparable();
		assertThat(comparator.compare(null, null), is(0));
		assertThat(comparator.compare("A", null) > 0, is(true));
		assertThat(comparator.compare(null, "A") < 0, is(true));
		assertThat(comparator.compare("A", "B") < 0, is(true));
		assertThat(comparator.compare("A", "A"), is(0));
		assertThat(comparator.compare("B", "A") > 0, is(true));
	}
	
	@Test
	public void testByString() {
		Comparator<String> comparator = Comparators.byString();
		assertThat(comparator.compare(null, null), is(0));
		assertThat(comparator.compare("A", null) > 0, is(true));
		assertThat(comparator.compare(null, "A") < 0, is(true));
		assertThat(comparator.compare("A", "B") < 0, is(true));
		assertThat(comparator.compare("A", "A"), is(0));
		assertThat(comparator.compare("B", "A") > 0, is(true));
	}

	@Test
	public void testNullComparator() {
		assertThat(Comparators.nullComparator().compare("A", "B"), is(0));
		assertThat(Comparators.nullComparator().compare("A", null), is(0));
		assertThat(Comparators.nullComparator().compare(null, null), is(0));
	}

	@Test
	public void testNonNullComparator() {
		assertThat(Comparators.nonNullComparator().compare("A", "B"), is(0));
		assertThat(Comparators.nonNullComparator().compare("A", null) > 0, is(true));
		assertThat(Comparators.nonNullComparator().compare(null, "A") < 0, is(true));
		assertThat(Comparators.nonNullComparator().compare(null, null), is(0));
	}

}
