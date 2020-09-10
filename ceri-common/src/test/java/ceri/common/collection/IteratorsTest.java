package ceri.common.collection;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;
import ceri.common.test.Capturer;

public class IteratorsTest {

	@Test
	public void testIndexed() {
		Iterator<String> iter = Iterators.indexed(3, i -> String.valueOf(i));
		assertThat(iter.next(), is("0"));
		assertThat(iter.next(), is("1"));
		assertThat(iter.next(), is("2"));
		assertThrown(() -> iter.next());
	}

	@Test
	public void testReverseList() {
		List<String> list = add(new ArrayList<>(), "1", "2", "3");
		Iterator<String> iter = Iterators.reverseList(list);
		assertThat(iter.next(), is("3"));
		assertThat(iter.next(), is("2"));
		iter.remove();
		assertThat(iter.hasNext(), is(true));
		assertThat(iter.next(), is("1"));
		assertThat(iter.hasNext(), is(false));
		assertThrown(() -> iter.next());
		assertIterable(list, "1", "3");
	}

	@Test
	public void testForEachEnumeration() {
		Properties props = new Properties();
		props.put("A", 1);
		props.put("B", 2);
		props.put("C", 3);
		Set<String> set = new HashSet<>();
		for (Object obj : Iterators.forEach(props.propertyNames()))
			set.add((String) obj);
		assertCollection(set, "A", "B", "C");
	}

	@Test
	public void testForEachReversedList() {
		Capturer<String> captor = Capturer.of();
		for (String s : Iterators.forEachReversedList(add(new ArrayList<>(), "1", "2", "3")))
			captor.accept(s);
		captor.verify("3", "2", "1");
	}

	@Test
	public void testForEachReversedQueue() {
		Capturer<String> captor = Capturer.of();
		for (String s : Iterators.forEachReversedQueue(add(new ArrayDeque<>(), "1", "2", "3")))
			captor.accept(s);
		captor.verify("3", "2", "1");
	}

	@Test
	public void testForEachReversedSet() {
		Capturer<String> captor = Capturer.of();
		for (String s : Iterators.forEachReversedSet(add(new TreeSet<>(), "1", "2", "3")))
			captor.accept(s);
		captor.verify("3", "2", "1");
	}

	@Test
	public void testSpliteratorTryAdvance() {
		Capturer<Object> capturer = Capturer.of();
		Iterators.spliterator(null, 1, 0).tryAdvance(capturer);
		capturer.verify();
	}

	@Test
	public void testSpliteratorNext() {
		Capturer<Object> capturer = Capturer.of();
		Iterators.spliterator(null, null, 1, 0).tryAdvance(capturer);
		capturer.verify();
		Iterators.spliterator(() -> false, null, 1, 0).tryAdvance(capturer);
		capturer.verify();
		Iterators.spliterator(() -> true, null, 1, 0).tryAdvance(capturer);
		capturer.verify();
		Iterators.spliterator(() -> true, () -> "test", 1, 0).tryAdvance(capturer);
		capturer.verify("test");
	}

	/**
	 * Creates a mutable list.
	 */
	@SafeVarargs
	private static <T, C extends Collection<? super T>> C add(C c, T... ts) {
		Collections.addAll(c, ts);
		return c;
	}

}
