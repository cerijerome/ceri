package ceri.common.math;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import ceri.common.text.ToStringHelper;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

public class Bound<T extends Comparable<T>> {
	private static final Bound<?> UNBOUND = new Bound<>(null, null);
	public final T value;
	public final Type type;

	public static enum Type {
		inclusive,
		exclusive
	}

	public static <T extends Comparable<T>> Bound<T> unbound() {
		return BasicUtil.uncheckedCast(UNBOUND);
	}

	public static <T extends Comparable<T>> Bound<T> inclusive(T value) {
		validateNotNull(value);
		return of(value, Type.inclusive);
	}

	public static <T extends Comparable<T>> Bound<T> exclusive(T value) {
		return of(value, Type.exclusive);
	}

	public static <T extends Comparable<T>> Bound<T> of(T value, Type type) {
		if (value == null) return unbound();
		return new Bound<>(value, type);
	}

	private Bound(T value, Type type) {
		this.value = value;
		this.type = type;
	}

	public boolean isUnbound() {
		return value == null;
	}

	public boolean upperFor(T value) {
		if (value == null) return false;
		if (isUnbound()) return true;
		int compare = this.value.compareTo(value);
		return compare > 0 || (compare == 0 && type == Type.inclusive);
	}

	public boolean lowerFor(T value) {
		if (value == null) return false;
		if (isUnbound()) return true;
		int compare = this.value.compareTo(value);
		return compare < 0 || (compare == 0 && type == Type.inclusive);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(value, type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Bound)) return false;
		Bound<?> other = (Bound<?>) obj;
		if (!EqualsUtil.equals(value, other.value)) return false;
		if (!EqualsUtil.equals(type, other.type)) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, value, type).toString();
	}

}