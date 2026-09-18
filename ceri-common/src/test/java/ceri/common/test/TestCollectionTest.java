package ceri.common.test;

import java.util.List;
import java.util.Spliterator;
import org.junit.Test;
import ceri.common.collect.Lists;
import ceri.common.stream.Stream;

public class TestCollectionTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(TestCollection.class);
	}

	@Test
	public void shouldDelegateIterableCalls() {
		Assert.ordered(TestCollection.iterableOf(1, 2, 3), 1, 2, 3);
		Assert.ordered(TestCollection.iterable(List.of(1, 2, 3).iterator()), 1, 2, 3);
	}

	@Test
	public void shouldGenerateIterableErrors() {
		var i = TestCollection.iterableOf(1, 2, 3).iterator();
		i.next.set(null, new IllegalStateException("test"));
		Assert.equal(i.next(), 1);
		Assert.thrown(IllegalStateException.class, i::next);
	}

	@Test
	public void shouldDelegateIteratorCalls() {
		Assert.iterator(TestCollection.iteratorOf(1, 2, 3), 1, 2, 3);
		var list = Lists.ofAll("a", "b");
		var iter = TestCollection.iterator(list);
		Assert.equal(iter.next(), "a");
		iter.remove();
		Assert.ordered(list, "b");
	}

	@Test
	public void shouldDelegateSpliteratorCalls() {
		Assert.stream(Stream.from(TestCollection.spliteratorOf(1, 2, 3)), 1, 2, 3);
		var src = List.of(1, 2, 3).spliterator();
		var split = TestCollection.spliterator(src);
		Assert.equal(split.characteristics(), src.characteristics());
		Assert.equal(split.estimateSize(), src.estimateSize());
		Assert.equal(splitList(split), splitList(List.of(1, 2, 3).spliterator()));
	}

	@Test
	public void shouldSplitSpliterator() {
		Assert.equal(TestCollection.spliteratorOf().trySplit(), null);
		Assert.equal(splitList(TestCollection.spliteratorOf(1, 2, 3)),
			splitList(List.of(1, 2, 3).spliterator()));
	}

	@Test
	public void shouldGenerateSpliteratorErrors() {
		var s = TestCollection.spliteratorOf(1, 2, 3);
		s.tryAdvance.set(null, new IllegalStateException("test"));
		Assert.equal(s.tryAdvance(i -> Assert.equal(i, 1)), true);
		Assert.thrown(IllegalStateException.class, () -> s.tryAdvance(_ -> {}));
	}

	private static <T> List<T> splitList(Spliterator<T> spliterator) {
		return Stream.from(spliterator.trySplit()).toList();
	}
}
