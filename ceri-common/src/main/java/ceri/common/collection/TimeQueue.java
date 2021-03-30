package ceri.common.collection;

import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.LongSupplier;
import ceri.common.concurrent.ConcurrentUtil;

/**
 * A queue that stores entries with a time.
 */
public class TimeQueue<T> {
	private final LongSupplier timeSupplier;
	public final TimeUnit unit;
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

	public static <T> TimeQueue<T> millis() {
		return new TimeQueue<>(System::currentTimeMillis, TimeUnit.MILLISECONDS);
	}

	public static <T> TimeQueue<T> micros() {
		return new TimeQueue<>(ConcurrentUtil::microTime, TimeUnit.MICROSECONDS);
	}

	public static <T> TimeQueue<T> nanos() {
		return new TimeQueue<>(System::nanoTime, TimeUnit.NANOSECONDS);
	}

	private TimeQueue(LongSupplier timeSupplier, TimeUnit unit) {
		this.timeSupplier = timeSupplier;
		this.unit = unit;
	}

	public void add(T t) {
		add(t, time());
	}

	public void add(T t, long time) {
		queue.add(new Item<>(t, time));
	}

	public void addOffset(T t, long offset) {
		add(t, time() + offset);
	}

	public T remove() {
		Item<T> item = queue.poll();
		return item == null ? null : item.t;
	}
	
	public void clear() {
		queue.clear();
	}
	
	public boolean isEmpty() {
		return queue.isEmpty();
	}
	
	public long time() {
		return timeSupplier.getAsLong();
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
