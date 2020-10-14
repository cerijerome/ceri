package ceri.common.comparator;

import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.junit.Test;

public class ComparatorsTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Comparators.class);
	}

	@Test
	public void testUnsignedComparators() {
		assertThat(Comparators.UINT.compare(0xffffffff, -1), is(0));
		assertThat(Comparators.UINT.compare(0xffffffff, 0), is(1));
		assertThat(Comparators.UINT.compare(0, 0xffffffff), is(-1));
		assertThat(Comparators.UINT.compare(0xffffffff, -2), is(1));
		assertThat(Comparators.UINT.compare(-2, 0xffffffff), is(-1));
		assertThat(Comparators.UINT.compare(0x7fffffff, 0x80000000), is(-1));
		assertThat(Comparators.UINT.compare(0x80000000, 0x7fffffff), is(1));
		assertThat(Comparators.ULONG.compare(0xffffffffffffffffL, -1L), is(0));
		assertThat(Comparators.ULONG.compare(0xffffffffffffffffL, 0L), is(1));
		assertThat(Comparators.ULONG.compare(0L, 0xffffffffffffffffL), is(-1));
		assertThat(Comparators.ULONG.compare(0xffffffffffffffffL, -2L), is(1));
		assertThat(Comparators.ULONG.compare(-2L, 0xffffffffffffffffL), is(-1));
		assertThat(Comparators.ULONG.compare(0x7fffffffffffffffL, 0x8000000000000000L), is(-1));
		assertThat(Comparators.ULONG.compare(0x8000000000000000L, 0x7fffffffffffffffL), is(1));
	}

	@Test
	public void testOrder() {
		Comparator<Integer> c = Comparators.order(100, 10, 1000);
		assertThat(c.compare(0, 0), is(0));
		assertThat(c.compare(100, 100), is(0));
		assertThat(c.compare(10, 100), is(1));
		assertThat(c.compare(1000, 100), is(1));
		assertThat(c.compare(10, 1000), is(-1));
		assertThat(c.compare(0, 100), is(1));
	}

	@Test
	public void testTransform() {
		Comparator<String> c = Comparators.transform(Comparators.INT, String::length);
		assertThat(c.compare(null, null) > 0, is(false));
		assertThat(c.compare("001", null) > 0, is(true));
		assertThat(c.compare(null, "2") > 0, is(false));
		assertThat(c.compare("001", "2") > 0, is(true));
	}

	@Test
	public void testSequence() {
		Comparator<String> comparator =
			Comparators.sequence(Comparators.nonNullComparator(), Comparators.STRING);
		List<String> list = Arrays.asList(null, "2", "1", null);
		list.sort(comparator);
		assertIterable(list, null, null, "1", "2");
	}

	@Test
	public void testGroupComparator() {
		Comparator<Integer> comparator = Comparators.group(Comparators.INT, 3, 4, 5);
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
		assertThat(Comparators.BOOL.compare(null, null), is(0));
		assertThat(Comparators.BOOL.compare(null, true) < 0, is(true));
		assertThat(Comparators.BOOL.compare(null, false) < 0, is(true));
		assertThat(Comparators.BOOL.compare(true, null) > 0, is(true));
		assertThat(Comparators.BOOL.compare(false, null) > 0, is(true));
		assertThat(Comparators.BOOL.compare(false, true) < 0, is(true));
		assertThat(Comparators.BOOL.compare(true, false) > 0, is(true));
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
		Comparator<Integer> comparator = Comparators.comparable();
		Comparator<Integer> reverseComparator = Comparators.reverse(comparator);
		assertThat(reverseComparator.compare(0, 0), is(-comparator.compare(0, 0)));
		assertThat(reverseComparator.compare(0, 1), is(-comparator.compare(0, 1)));
		assertThat(reverseComparator.compare(1, 0), is(-comparator.compare(1, 0)));
		assertThat(reverseComparator.compare(1, 1), is(-comparator.compare(1, 1)));
	}

}
