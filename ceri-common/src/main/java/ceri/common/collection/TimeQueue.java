package ceri.common.collection;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import ceri.common.time.TimeSupplier;

/**
 * A queue that stores unique entries with a time.
 */
public class TimeQueue<T> {
	public final TimeSupplier timeSupplier;
	private final Set<T> set = new HashSet<>();
	private final PriorityQueue<Item<T>> queue = new PriorityQueue<>();

	private static class Item<T> implements Comparable<Item<T>> {
		private final T t;
		private final long time;

		private Item(T t, long time) {
			this.t = t;
			this.time = time;
		}

		@Override
		public int compareTo(Item<T> o) {
			return Long.compare(time, o.time);
		}
	}

	public static <T> TimeQueue<T> of(TimeUnit unit) {
		return of(TimeSupplier.from(unit));
	}

	public static <T> TimeQueue<T> of(TimeSupplier supplier) {
		return new TimeQueue<>(supplier);
	}

	private TimeQueue(TimeSupplier timeSupplier) {
		this.timeSupplier = timeSupplier;
	}

	public boolean add(T t) {
		return add(t, time());
	}

	public boolean add(T t, long time) {
		if (set.contains(t)) return false;
		queue.add(new Item<>(t, time));
		set.add(t);
		return true;
	}

	public void addOffset(T t, long offset) {
		add(t, time() + offset);
	}

	public T remove() {
		Item<T> item = queue.poll();
		if (item == null) return null;
		set.remove(item.t);
		return item.t;
	}

	public void clear() {
		queue.clear();
		set.clear();
	}

	public boolean isEmpty() {
		return queue.isEmpty();
	}

	public long time() {
		return timeSupplier.time();
	}

	public long nextTime() {
		Item<T> head = queue.peek();
		return head == null ? 0 : head.time;
	}

	public long nextDelay() {
		return nextDelay(time());
	}

	public long nextDelay(long time) {
		Item<T> head = queue.peek();
		return head == null ? 0 : Math.max(0L, head.time - time);
	}

	public T next() {
		return next(time());
	}

	public T next(long time) {
		Item<T> head = queue.peek();
		if (head == null || head.time > time) return null;
		queue.remove();
		set.remove(head.t);
		return head.t;
	}

	public int forEachNext(Consumer<T> consumer) {
		return forEachNext(time(), consumer);
	}

	public int forEachNext(Consumer<T> consumer, int max) {
		return forEachNext(time(), consumer, max);
	}

	public int forEachNext(long time, Consumer<T> consumer) {
		return forEachNext(time, consumer, Integer.MAX_VALUE);
	}

	public int forEachNext(long time, Consumer<T> consumer, int max) {
		for (int i = 0; i < max; i++) {
			T next = next(time);
			if (next == null) return i;
			consumer.accept(next);
		}
		return max;
	}

}
