package ceri.common.comparator;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;

public class ComparatorsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Comparators.class);
	}

	@Test
	public void testTransform() {
		Comparator<String> c = Comparators.transform(Comparators.INTEGER, String::length);
		assertThat(c.compare("001", "2") > 0, is(true));
	}

	@Test
	public void testSequence() {
		Comparator<String> comparator =
			Comparators.sequence(Comparators.nonNullComparator(), Comparators.STRING);
		List<String> list = ArrayUtil.asList(null, "2", "1", null);
		Collections.sort(list, comparator);
		assertIterable(list, null, null, "1", "2");
	}

	@Test
	public void testGroupComparator() {
		Comparator<Integer> comparator = Comparators.group(Comparators.INTEGER, 3, 4, 5);
		assertThat(comparator.compare(1, 2), is(-1));
		assertThat(comparator.compare(1, 6), is(-1));
		assertThat(comparator.compare(6, 1), is(1));
		assertThat(comparator.compare(7, 6), is(1));
		assertThat(comparator.compare(1, 3), is(1));
		assertThat(comparator.compare(7, 3), is(1));
		assertThat(comparator.compare(3, 2), is(-1));
		assertThat(comparator.compare(3, 5), is(-1));
		assertThat(comparator.compare(5, 4), is(1));
	}

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
		Comparator<String> comparator = Comparators.comparable();
		assertThat(comparator.compare(null, null), is(0));
		assertThat(comparator.compare("A", null) > 0, is(true));
		assertThat(comparator.compare(null, "A") < 0, is(true));
		assertThat(comparator.compare("A", "B") < 0, is(true));
		assertThat(comparator.compare("A", "A"), is(0));
		assertThat(comparator.compare("B", "A") > 0, is(true));
	}

	@Test
	public void testByString() {
		Comparator<String> comparator = Comparators.string();
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
		assertThat(Comparators.nonNull(null).compare("A", "B"), is(0));
		assertThat(Comparators.nonNullComparator().compare("A", "B"), is(0));
		assertThat(Comparators.nonNullComparator().compare("A", null) > 0, is(true));
		assertThat(Comparators.nonNullComparator().compare(null, "A") < 0, is(true));
		assertThat(Comparators.nonNullComparator().compare(null, null), is(0));
	}

	@Test
	public void testReverse() {
		Comparator<Integer> comparator = Comparators.<Integer>comparable();
		Comparator<Integer> reverseComparator = Comparators.reverse(comparator);
		assertThat(reverseComparator.compare(0, 0), is(-comparator.compare(0, 0)));
		assertThat(reverseComparator.compare(0, 1), is(-comparator.compare(0, 1)));
		assertThat(reverseComparator.compare(1, 0), is(-comparator.compare(1, 0)));
		assertThat(reverseComparator.compare(1, 1), is(-comparator.compare(1, 1)));
	}

}
