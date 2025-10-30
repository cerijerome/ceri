package ceri.common.data;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import ceri.common.array.Array;
import ceri.common.collect.Enums;
import ceri.common.collect.Immutable;
import ceri.common.collect.Iterables;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.except.Exceptions;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.text.Format;
import ceri.common.text.Strings;
import ceri.common.util.Basics;

/**
 * Type transcoders.
 */
public class Xcoder {
	private Xcoder() {}

	/**
	 * Encapsulates decoded types and any remainder.
	 */
	public static record Rem<T>(Set<T> types, long diff) {
		@SafeVarargs
		public static <T> Rem<T> of(long diff, T... types) {
			if (Array.at(types, 0) == null) return new Rem<>(Immutable.set(), diff);
			return new Rem<>(Immutable.setOfAll(Sets::link, types), diff);
		}

		/**
		 * Returns the first type, or null if empty.
		 */
		public T first() {
			return Iterables.first(types);
		}

		/**
		 * Returns the difference as an int.
		 */
		public int diffInt() {
			return (int) diff();
		}

		/**
		 * Returns true if no types and remainder is 0.
		 */
		public boolean isEmpty() {
			return types().isEmpty() && isExact();
		}

		/**
		 * Returns true if no remainder.
		 */
		public boolean isExact() {
			return diff() == 0L;
		}

		@Override
		public String toString() {
			return types() + "+" + Format.udecHex(diff());
		}
	}

	public static <T extends Enum<T>> Xcoder.Type<T> type(Class<T> cls) {
		return type(cls, Enums.valueAccessor(cls)::apply);
	}

	public static <T extends Enum<T>> Xcoder.Type<T> type(Class<T> cls,
		Functions.ToLongFunction<T> valueFn) {
		return type(Enums.of(cls), valueFn);
	}

	public static <T extends Enum<T>> Xcoder.Type<T> type(Iterable<T> ts,
		Functions.ToLongFunction<T> valueFn) {
		return new Xcoder.Type<>(
			Maps.convert(Maps.Put.first, Maps::link, valueFn::applyAsLong, t -> t, ts));
	}

	public static <T extends Enum<T>> Xcoder.Types<T> types(Class<T> cls) {
		return types(cls, Enums.valueAccessor(cls)::apply);
	}

	public static <T extends Enum<T>> Xcoder.Types<T> types(Class<T> cls,
		Functions.ToLongFunction<T> valueFn) {
		return types(Enums.of(cls), valueFn);
	}

	public static <T extends Enum<T>> Xcoder.Types<T> types(Iterable<? extends T> ts,
		Functions.ToLongFunction<T> valueFn) {
		return new Xcoder.Types<>(
			Maps.convert(Maps.Put.first, Maps::link, valueFn::applyAsLong, t -> t, ts));
	}

	/**
	 * A single type transcoder.
	 */
	public static class Type<T> {
		private final Class<?> typeCls;
		private final Map<T, Long> values;
		private final Map<Long, T> types;

		protected Type(Map<Long, T> types) {
			this.types = Immutable.wrap(types);
			values = Immutable.invertMap(Maps::link, types);
			typeCls = typeClass(types);
		}

		/**
		 * Provides all mapped types.
		 */
		public Set<T> all() {
			return values.keySet();
		}

		/**
		 * Provides a mask of all mapped type values.
		 */
		public long mask() {
			return encodeAll(all());
		}

		/**
		 * Provides a mask of all mapped type values, limited to int.
		 */
		public int maskInt() {
			return (int) mask();
		}

		/**
		 * Provides the type value, or 0 if unmapped.
		 */
		public long encode(T t) {
			return values.getOrDefault(t, 0L);
		}

		/**
		 * Provides the type value, or 0 if unmapped, limited to int.
		 */
		public int encodeInt(T t) {
			return (int) encode(t);
		}

		/**
		 * Provides the type with the exact value, or null.
		 */
		public T decode(long value) {
			return types.get(value);
		}

		/**
		 * Provides the type with the exact unsigned value, or null.
		 */
		public T decode(int value) {
			return decode(Maths.uint(value));
		}

		/**
		 * Provides a type with the exact value, or default.
		 */
		public T decode(long value, T def) {
			return Basics.def(decode(value), def);
		}

		/**
		 * Provides a type with the exact unsigned value, or default.
		 */
		public T decode(int value, T def) {
			return decode(Maths.uint(value), def);
		}

		/**
		 * Provides a type with the exact value, or throws an exception.
		 */
		public T decodeValid(long value) {
			return decodeValid(value, "");
		}

		/**
		 * Provides a type with the exact unsigned value, or throws an exception.
		 */
		public T decodeValid(int value) {
			return decodeValid(Maths.uint(value));
		}

		/**
		 * Provides a type with the exact value, or throws an exception.
		 */
		public T decodeValid(long value, String format, Object... args) {
			T t = decode(value);
			if (t != null) return t;
			throw Exceptions.illegalArg("%s decoding failed for %s",
				Xcoder.name(typeCls, format, args), Format.UDEC_HEX.uint(value));
		}

		/**
		 * Provides a type with the exact unsigned value, or throws an exception.
		 */
		public T decodeValid(int value, String format, Object... args) {
			return decodeValid(Maths.uint(value), format, args);
		}

		/**
		 * Provides a type with bits within the value (or null), and the remainder.
		 */
		public Rem<T> decodeRem(long value) {
			var t = decode(value);
			if (t != null) return Rem.of(0L, t);
			for (var entry : values.entrySet()) {
				var v = entry.getValue();
				if (v != 0L && (v & value) == v) return Rem.of(value & ~v, entry.getKey());
			}
			return Rem.of(value, decode(0));
		}

		/**
		 * Provides a type with unsigned bits within the value (or null), and the remainder.
		 */
		public Rem<T> decodeRem(int value) {
			return decodeRem(Maths.uint(value));
		}

		/**
		 * Returns true if a type has the exact value.
		 */
		public boolean isValid(long value) {
			return value == 0 || types.containsKey(value);
		}

		/**
		 * Returns true if a type has the exact unsigned value.
		 */
		public boolean isValid(int value) {
			return isValid(Maths.uint(value));
		}

		/**
		 * Returns true if the type is mapped to the value.
		 */
		public boolean is(long value, T t) {
			return Objects.equals(types.get(value), t);
		}

		/**
		 * Returns true if the type is mapped to the unsigned value.
		 */
		public boolean is(int value, T t) {
			return is(Maths.uint(value), t);
		}

		/**
		 * Returns true if any of the types are mapped to the value.
		 */
		@SafeVarargs
		public final boolean isAny(long value, T... ts) {
			return isAny(value, Lists.wrap(ts));
		}

		/**
		 * Returns true if any of the types are mapped to the unsigned value.
		 */
		@SafeVarargs
		public final boolean isAny(int value, T... ts) {
			return isAny(Maths.uint(value), ts);
		}

		/**
		 * Returns true if any of the types are mapped to the value.
		 */
		public boolean isAny(long value, Iterable<? extends T> ts) {
			for (T t : ts)
				if (is(value, t)) return true;
			return false;
		}

		/**
		 * Returns true if any of the types are mapped to the unsigned value.
		 */
		public boolean isAny(int value, Iterable<? extends T> ts) {
			return isAny(Maths.uint(value), ts);
		}

		/**
		 * Returns true if the type has all bits within the value.
		 */
		public boolean has(long value, T t) {
			var mask = values.get(t);
			return mask != null && (value & mask) == mask;
		}

		/**
		 * Returns true if the type has all bits within the unsigned value.
		 */
		public boolean has(int value, T t) {
			return has(Maths.uint(value), t);
		}

		/**
		 * Returns true if any of the types have all bits within the value.
		 */
		@SafeVarargs
		public final boolean hasAny(long value, T... ts) {
			return hasAny(value, Lists.wrap(ts));
		}

		/**
		 * Returns true if any of the types have all bits within the unsigned value.
		 */
		@SafeVarargs
		public final boolean hasAny(int value, T... ts) {
			return hasAny(Maths.uint(value), ts);
		}

		/**
		 * Returns true if any of the types have all bits within the value.
		 */
		public boolean hasAny(long value, Iterable<? extends T> ts) {
			for (T t : ts)
				if (has(value, t)) return true;
			return false;
		}

		/**
		 * Returns true if any of the types have all bits within the unsigned value.
		 */
		public boolean hasAny(int value, Iterable<? extends T> ts) {
			return hasAny(Maths.uint(value), ts);
		}

		/**
		 * Creates the remainder instance. Provides sub-classes an opportunity to modify the decode
		 * set.
		 */
		protected Rem<T> rem(Set<T> types, long diff) {
			return new Rem<>(Immutable.wrap(types), diff);
		}

		private long encodeAll(Iterable<? extends T> ts) {
			if (ts == null) return 0;
			long value = 0;
			for (T t : ts)
				value |= encode(t);
			return value;
		}
	}

	/**
	 * A multiple type transcoder.
	 */
	public static class Types<T> extends Type<T> {

		protected Types(Map<Long, T> types) {
			super(types);
		}

		/**
		 * Combines mapped type bits with the value.
		 */
		@SafeVarargs
		public final long add(long value, T... ts) {
			return value | encode(ts);
		}

		/**
		 * Combines mapped type bits with the value.
		 */
		public long add(long value, Iterable<? extends T> ts) {
			return value | encode(ts);
		}

		/**
		 * Combines mapped type bits with the value.
		 */
		@SafeVarargs
		public final int add(int value, T... ts) {
			return value | encodeInt(ts);
		}

		/**
		 * Combines mapped type bits with the value.
		 */
		public int add(int value, Iterable<? extends T> ts) {
			return value | encodeInt(ts);
		}

		/**
		 * Removes mapped type bits from the value.
		 */
		@SafeVarargs
		public final long remove(long value, T... ts) {
			return value & ~encode(ts);
		}

		/**
		 * Removes mapped type bits from the value.
		 */
		public long remove(long value, Iterable<T> ts) {
			return value & ~encode(ts);
		}

		/**
		 * Removes mapped type bits from the value.
		 */
		@SafeVarargs
		public final int remove(int value, T... ts) {
			return value & ~encodeInt(ts);
		}

		/**
		 * Removes mapped type bits from the value.
		 */
		public int remove(int value, Iterable<T> ts) {
			return value & ~encodeInt(ts);
		}

		/**
		 * Provides the bit-combined type values, with 0 for each unmapped type.
		 */
		@SafeVarargs
		public final long encode(T... ts) {
			return encode(Lists.wrap(ts));
		}

		/**
		 * Provides the bit-combined type values, with 0 for each unmapped type.
		 */
		public long encode(Iterable<? extends T> ts) {
			return super.encodeAll(ts);
		}

		/**
		 * Provides the bit-combined type values, with 0 for each unmapped type, limited to int.
		 */
		@SafeVarargs
		public final int encodeInt(T... ts) {
			return encodeInt(Lists.wrap(ts));
		}

		/**
		 * Provides the bit-combined type values, with 0 for each unmapped type, limited to int.
		 */
		public int encodeInt(Iterable<? extends T> ts) {
			return (int) super.encodeAll(ts);
		}

		/**
		 * Provides a set of types with non-overlapping bits within the value.
		 */
		public Set<T> decodeAll(long value) {
			return decodeAllRem(value).types();
		}

		/**
		 * Provides a set of types with non-overlapping bits within the unsigned value.
		 */
		public Set<T> decodeAll(int value) {
			return decodeAll(Maths.uint(value));
		}

		/**
		 * Provides a set of types with non-overlapping bits within the value. Throws an exception
		 * if there is a remainder.
		 */
		public Set<T> decodeAllValid(long value) {
			return decodeAllValid(value, "");
		}

		/**
		 * Provides a set of types with non-overlapping bits within the unsigned value. Throws an
		 * exception if there is a remainder.
		 */
		public Set<T> decodeAllValid(int value) {
			return decodeAllValid(Maths.uint(value));
		}

		/**
		 * Provides a set of types with non-overlapping bits within the value. Throws an exception
		 * if there is a remainder.
		 */
		public Set<T> decodeAllValid(long value, String format, Object... args) {
			var rem = decodeAllRem(value);
			if (rem.diff() == 0) return rem.types();
			throw Exceptions.illegalArg("%s decoding failed for %s: %s",
				Xcoder.name(super.typeCls, format, args), Format.UDEC_HEX.uint(value), rem);
		}

		/**
		 * Provides a set of types with non-overlapping bits within the unsigned value. Throws an
		 * exception if there is a remainder.
		 */
		public Set<T> decodeAllValid(int value, String format, Object... args) {
			return decodeAllValid(Maths.uint(value), format, args);
		}

		/**
		 * Provides a set of types with non-overlapping bits within the value, and any remainder.
		 */
		public Rem<T> decodeAllRem(long value) {
			var set = Sets.<T>link();
			for (var entry : super.values.entrySet()) {
				var v = entry.getValue();
				if (v == 0L || (v & value) != v) continue;
				set.add(entry.getKey());
				value &= ~v;
				if (value == 0) break;
			}
			if (set.isEmpty()) {
				T zero = decode(0);
				if (zero != null) set.add(zero);
			}
			return rem(set, value);
		}

		/**
		 * Provides a set of types with non-overlapping bits within the unsigned value, and any
		 * remainder.
		 */
		public Rem<T> decodeAllRem(int value) {
			return decodeAllRem(Maths.uint(value));
		}

		/**
		 * Returns true if type bits are within the value without a remainder.
		 */
		@Override
		public boolean isValid(long value) {
			return super.isValid(value) || rem(value) == 0;
		}

		/**
		 * Returns true if type bits are within the unsigned value without a remainder.
		 */
		@Override
		public boolean isValid(int value) {
			return isValid(Maths.uint(value));
		}

		/**
		 * Returns true if the type bits fill the value without a remainder.
		 */
		@SafeVarargs
		public final boolean isAll(long value, T... ts) {
			return encode(ts) == value;
		}

		/**
		 * Returns true if the type bits fill the unsigned value without a remainder.
		 */
		@SafeVarargs
		public final boolean isAll(int value, T... ts) {
			return isAll(Maths.uint(value), ts);
		}

		/**
		 * Returns true if the type bits fill the value without a remainder.
		 */
		public boolean isAll(long value, Iterable<T> ts) {
			return encode(ts) == value;
		}

		/**
		 * Returns true if the type bits fill the unsigned value without a remainder.
		 */
		public boolean isAll(int value, Iterable<T> ts) {
			return isAll(Maths.uint(value), ts);
		}

		/**
		 * Returns true if the type bits fill the value with possible remainder.
		 */
		@SafeVarargs
		public final boolean hasAll(long value, T... ts) {
			long mask = encode(ts);
			return (value & mask) == mask;
		}

		/**
		 * Returns true if the type bits fill the unsigned value with possible remainder.
		 */
		@SafeVarargs
		public final boolean hasAll(int value, T... ts) {
			return hasAll(Maths.uint(value), ts);
		}

		/**
		 * Returns true if the type bits fill the value with possible remainder.
		 */
		public boolean hasAll(long value, Iterable<T> ts) {
			long mask = encode(ts);
			return (value & mask) == mask;
		}

		/**
		 * Returns true if the type bits fill the unsigned value with possible remainder.
		 */
		public boolean hasAll(int value, Iterable<T> ts) {
			return hasAll(Maths.uint(value), ts);
		}

		private long rem(long value) {
			for (var v : super.values.values()) {
				if ((v & value) != v) continue;
				value &= ~v;
				if (value == 0L) break;
			}
			return value;
		}
	}

	// support

	private static Class<?> typeClass(Map<Long, ?> types) {
		if (Maps.isEmpty(types)) return Object.class;
		return Iterables.first(types.values()).getClass();
	}

	private static String name(Class<?> typeCls, String format, Object... args) {
		if (Strings.isEmpty(format)) return typeCls.getSimpleName();
		return Strings.format(format, args);
	}
}
