package ceri.common.util;

import java.util.Objects;

/**
 * Holder for a value, and flag to indicate if the value is empty. Provides mutable and immutable
 * holders. Immutable holder is similar to Optional, but allows null values. Use OptionalInt,
 * OptionalLong, and OptionalDouble for equivalent functionality with primitives. A Volatile holder
 * is not considered empty, and always has a volatile (nullable) value.
 */
public abstract class Holder<T> {
	private static final Holder<Object> EMPTY = new Immutable<>(null, true);
	private static final String EMPTY_STRING = "empty";

	/**
	 * Creates an immutable non-empty value holder (value can be null).
	 */
	public static <T> Holder<T> of(T value) {
		return new Immutable<>(value, false);
	}

	/**
	 * Returns an immutable empty holder.
	 */
	public static <T> Holder<T> of() {
		return BasicUtil.uncheckedCast(EMPTY);
	}

	/**
	 * Creates a mutable value holder that is non-empty with given value, which may be null.
	 */
	public static <T> Mutable<T> mutable(T value) {
		return Holder.<T>mutable().set(value);
	}

	/**
	 * Creates a mutable value holder that is empty.
	 */
	public static <T> Mutable<T> mutable() {
		return new Mutable<>();
	}

	/**
	 * Creates a mutable volatile value holder.
	 */
	public static <T> Volatile<T> ofVolatile(T value) {
		return new Volatile<>(value);
	}

	public static boolean equals(Holder<?> lhs, Holder<?> rhs) {
		if (lhs == rhs) return true;
		if (lhs == null || rhs == null) return false;
		if (lhs.isEmpty() != rhs.isEmpty()) return false;
		if (!Objects.equals(lhs.value(), rhs.value())) return false;
		return true;
	}

	private static class Immutable<T> extends Holder<T> {
		private final T value;
		private final boolean empty;

		private Immutable(T value, boolean empty) {
			this.value = value;
			this.empty = empty;
		}

		@Override
		public T value() {
			return value;
		}

		@Override
		public boolean isEmpty() {
			return empty;
		}
	}

	public static class Mutable<T> extends Holder<T> {
		private T value = null;
		private boolean empty = true;

		private Mutable() {}

		@Override
		public T value() {
			return value;
		}

		@Override
		public boolean isEmpty() {
			return empty;
		}

		public Mutable<T> set(T value) {
			this.value = value;
			empty = false;
			return this;
		}

		public Mutable<T> clear() {
			value = null;
			empty = true;
			return this;
		}
	}

	public static class Volatile<T> extends Holder<T> {
		private volatile T value;

		private Volatile(T value) {
			this.value = value;
		}

		@Override
		public T value() {
			return value;
		}

		@Override
		public boolean isEmpty() {
			return false;
		}

		public Volatile<T> set(T value) {
			this.value = value;
			return this;
		}
	}

	public abstract boolean isEmpty();

	public abstract T value();

	public T value(T def) {
		return isEmpty() ? def : value();
	}

	public boolean nullValue() {
		return holds(null);
	}

	public boolean holds(T other) {
		if (isEmpty()) return false;
		return Objects.equals(value(), other);
	}

	public T verify() {
		if (isEmpty()) throw new IllegalStateException("Value is not present");
		return value();
	}

	@Override
	public int hashCode() {
		return Objects.hash(isEmpty(), value());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Holder)) return false;
		return Holder.equals(this, (Holder<?>) obj);
	}

	@Override
	public String toString() {
		if (isEmpty()) return EMPTY_STRING;
		return "[" + String.valueOf(value()) + "]";
	}

}
