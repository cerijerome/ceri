package ceri.common.collection;

import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.function.Consumer;
import ceri.common.time.TimeSupplier;

/**
 * A queue that stores unique entries with a time.
 */
public class TimeQueue<T> {
	public final TimeSupplier timeSupplier;
	private final Set<T> set = new HashSet<>();
	private final PriorityQueue<Item<T>> queue = new PriorityQueue<>();

	private static record Item<T>(T t, long time) implements Comparable<Item<T>> {
		@Override
		public int compareTo(Item<T> o) {
			return Long.compare(time(), o.time());
		}
	}

	/**
	 * Creates an instance using the given time supplier to generate time stamps.
	 */
	public static <T> TimeQueue<T> of(TimeSupplier supplier) {
		return new TimeQueue<>(supplier);
	}

	private TimeQueue(TimeSupplier timeSupplier) {
		this.timeSupplier = timeSupplier;
	}

	/**
	 * Returns true if the queue contains the item.
	 */
	public boolean contains(T t) {
		return set.contains(t);
	}

	/**
	 * Adds an item, if not currently present, at current time. Returns true if added, false if the
	 * queue already contains the item.
	 */
	public boolean add(T t) {
		return add(t, time());
	}

	/**
	 * Adds an item, if not currently present, at given time. Returns true if added, false if the
	 * queue already contains the item.
	 */
	public boolean add(T t, long time) {
		if (contains(t)) return false;
		queue.add(new Item<>(t, time));
		set.add(t);
		return true;
	}

	/**
	 * Adds an item, if not currently present, at given time offset from now. Returns true if added,
	 * false if the queue already contains the item.
	 */
	public boolean addOffset(T t, long offset) {
		return add(t, time() + offset);
	}

	/**
	 * Adds or updates an item at current time. Returns true if added, false if the queue already
	 * contains the item.
	 */
	public boolean set(T t) {
		return set(t, time());
	}

	/**
	 * Adds or updates an item at given time. Returns true if added, false if the queue already
	 * contains the item.
	 */
	public boolean set(T t, long time) {
		boolean added = false;
		if (set.contains(t)) remove(t);
		else added = true;
		add(t, time);
		return added;
	}

	/**
	 * Adds or updates an item at given time offset from now. Returns true if added, false if the
	 * queue already contains the item.
	 */
	public boolean setOffset(T t, long offset) {
		return set(t, time() + offset);
	}

	/**
	 * Removes the given item. Returns false if not present.
	 */
	public boolean remove(T t) {
		if (!set.contains(t)) return false;
		queue.removeIf(e -> Objects.equals(e.t(), t));
		set.remove(t);
		return true;
	}

	/**
	 * Removes the head of the queue.
	 */
	public T remove() {
		Item<T> item = queue.poll();
		if (item == null) return null;
		set.remove(item.t());
		return item.t();
	}

	/**
	 * Clears all items.
	 */
	public void clear() {
		queue.clear();
		set.clear();
	}

	/**
	 * Returns true if the queue is empty.
	 */
	public boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Provides the current time, using the assigned time supplier.
	 */
	public long time() {
		return timeSupplier.time();
	}

	/**
	 * Provides the time offset from current time, using the assigned time supplier.
	 */
	public long time(long offset) {
		return time() + offset;
	}

	/**
	 * Provides the time of the head of the queue. Returns 0 if the queue is empty.
	 */
	public long nextTime() {
		Item<T> head = queue.peek();
		return head == null ? 0 : head.time();
	}

	/**
	 * Provides the duration from now to the time of the head of the queue. Returns 0 if the queue
	 * is empty.
	 */
	public long nextDelay() {
		return nextDelay(time());
	}

	/**
	 * Provides the duration from given time to the time of the head of the queue. Returns 0 if the
	 * queue is empty.
	 */
	public long nextDelay(long time) {
		Item<T> head = queue.peek();
		return head == null ? 0 : Math.max(0L, head.time() - time);
	}

	/**
	 * Removes and returns the head of the queue if current time >= item time. Returns null if
	 * empty, or the time is not yet reached.
	 */
	public T next() {
		return next(time());
	}

	/**
	 * Removes and returns the head of the queue if given time >= item time. Returns null if empty,
	 * or the time is not yet reached.
	 */
	public T next(long time) {
		Item<T> head = queue.peek();
		if (head == null || head.time() > time) return null;
		queue.remove();
		set.remove(head.t());
		return head.t();
	}

	/**
	 * Applies the consumer to items with times <= current time. Items are removed as they are
	 * consumed. Returns the number of consumed items.
	 */
	public int forEachNext(Consumer<T> consumer) {
		return forEachNext(time(), consumer);
	}

	/**
	 * Applies the consumer to items with times <= current time, up to given count. Items are
	 * removed as they are consumed. Returns the number of consumed items.
	 */
	public int forEachNext(Consumer<T> consumer, int max) {
		return forEachNext(time(), consumer, max);
	}

	/**
	 * Applies the consumer to items with times <= given time. Items are removed as they are
	 * consumed. Returns the number of consumed items.
	 */
	public int forEachNext(long time, Consumer<T> consumer) {
		return forEachNext(time, consumer, Integer.MAX_VALUE);
	}

	/**
	 * Applies the consumer to items with times <= given time, up to given count. Items are removed
	 * as they are consumed. Returns the number of consumed items.
	 */
	public int forEachNext(long time, Consumer<T> consumer, int max) {
		for (int i = 0; i < max; i++) {
			T next = next(time);
			if (next == null) return i;
			consumer.accept(next);
		}
		return max;
	}

}
