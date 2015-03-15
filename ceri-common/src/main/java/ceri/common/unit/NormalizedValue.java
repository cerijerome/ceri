package ceri.common.unit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import ceri.common.comparator.Comparators;
import ceri.common.util.BasicUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;
import ceri.common.util.ToStringHelper;

/**
 * Encapsulates an immutable normalized value of integral numbers of units.
 */
public class NormalizedValue<T extends Unit> {
	private static final Comparator<Unit> REVERSE_COMPARATOR = Comparators
		.nonNull((lhs, rhs) -> -Long.compare(lhs.units(), rhs.units()));
	public final Map<T, Long> values;
	public final long value;
	private final int hashCode;

	public static class Builder<T extends Unit> {
		Collection<T> units;
		long value = 0;

		Builder(Collection<T> units) {
			this.units = units;
		}

		/**
		 * Adds a value.
		 */
		public Builder<T> value(long value) {
			this.value += value;
			return this;
		}

		/**
		 * Adds a value of the given unit.
		 */
		public Builder<T> value(long value, T unit) {
			this.value += value * unit.units();
			return this;
		}

		/**
		 * Builds the normalized value.
		 */
		public NormalizedValue<T> build() {
			return NormalizedValue.create(value, units);
		}

	}

	private NormalizedValue(long value, Collection<T> units) {
		values = Collections.unmodifiableMap(toUnits(value, units));
		this.value = value;
		hashCode = HashCoder.hash(values, value);
	}

	/**
	 * Creates a builder for a normalized value using given unit enum types.
	 */
	public static <T extends Enum<T> & Unit> Builder<T> builder(Class<T> cls) {
		return new Builder<>(Arrays.asList(cls.getEnumConstants()));
	}

	/**
	 * Creates a builder for a normalized value using given units.
	 */
	public static <T extends Unit> Builder<T> builder(Collection<T> units) {
		return new Builder<>(units);
	}

	/**
	 * Creates a builder for a normalized value using given units.
	 */
	@SafeVarargs
	public static <T extends Unit> Builder<T> builder(T... units) {
		return new Builder<>(Arrays.asList(units));
	}

	/**
	 * Normalizes a value with given unit enum types.
	 */
	public static <T extends Enum<T> & Unit> NormalizedValue<T> create(long value, Class<T> cls) {
		Collection<T> units = Arrays.asList(cls.getEnumConstants());
		return new NormalizedValue<>(value, units);
	}

	/**
	 * Normalizes a value with given units.
	 */
	public static <T extends Unit> NormalizedValue<T> create(long value, Collection<T> units) {
		return new NormalizedValue<>(value, units);
	}

	/**
	 * Normalizes a value with given units.
	 */
	@SafeVarargs
	public static <T extends Unit> NormalizedValue<T> create(long value, T... units) {
		return new NormalizedValue<>(value, Arrays.asList(units));
	}

	/**
	 * Returns the normalized count of the given unit.
	 */
	public long value(T unit) {
		Long value = values.get(unit);
		if (value == null) return 0;
		return value.longValue();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof NormalizedValue<?>)) return false;
		NormalizedValue<?> n = (NormalizedValue<?>) obj;
		if (value != n.value) return false;
		return EqualsUtil.equals(values, n.values);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, value, values).toString();
	}

	private Comparator<T> reverseComparator() {
		return BasicUtil.uncheckedCast(REVERSE_COMPARATOR);
	}

	private final List<T> sort(Collection<T> units) {
		List<T> list = new ArrayList<>(units);
		Collections.sort(list, reverseComparator());
		return list;
	}

	private final Map<T, Long> toUnits(long value, Collection<T> units) {
		Map<T, Long> map = new TreeMap<>();
		for (T unit : sort(units)) {
			long count = value / unit.units();
			if (count > 0) map.put(unit, count);
			value = value % unit.units();
		}
		return map;
	}

}
