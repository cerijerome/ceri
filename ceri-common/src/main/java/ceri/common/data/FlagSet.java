package ceri.common.data;

import static ceri.common.collection.StreamUtil.bitwiseOr;
import static ceri.common.collection.StreamUtil.toSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.ToIntFunction;
import ceri.common.collection.ImmutableUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.HashCoder;

/**
 * Also see TypeTranscoder/FieldTranscoder
 */
public class FlagSet<T> {
	private static final FlagSet<?> ZERO = new FlagSet<>(Collections.emptySet(), 0);
	public final Set<T> flags;
	public final int value;

	public static <T extends Enum<T>> FlagSet<T> from(int value, ToIntFunction<T> bitFn,
		Class<T> cls) {
		if (value == 0) return FlagSet.zero();
		return from(value, bitFn, BasicUtil.enums(cls));
	}

	@SafeVarargs
	public static <T> FlagSet<T> from(int value, ToIntFunction<T> bitFn, T...flags) {
		if (value == 0) return FlagSet.zero();
		return from(value, bitFn, Arrays.asList(flags));
	}

	public static <T> FlagSet<T> from(int value, ToIntFunction<T> bitFn, Collection<T> flags) {
		if (value == 0) return FlagSet.zero();
		return of(bitFn, toSet(flags.stream().filter(t -> (mask(t, bitFn) & value) != 0)));
	}

	@SafeVarargs
	public static <T> FlagSet<T> of(ToIntFunction<T> bitFn, T... flags) {
		return of(bitFn, Arrays.asList(flags));
	}

	public static <T> FlagSet<T> of(ToIntFunction<T> bitFn, Collection<T> flags) {
		int value = bitwiseOr(flags.stream().mapToInt(t -> mask(t, bitFn)));
		return new FlagSet<>(flags, value);
	}

	public static <T> FlagSet<T> zero() {
		return BasicUtil.uncheckedCast(ZERO);
	}

	private FlagSet(Collection<T> flags, int value) {
		this.flags = ImmutableUtil.copyAsSet(flags);
		this.value = value;
	}

	@SuppressWarnings("unchecked")
	public boolean has(T... flags) {
		if (flags.length == 1) return this.flags.contains(flags[0]);
		return has(Arrays.asList(flags));
	}

	public boolean has(Collection<T> flags) {
		return this.flags.containsAll(flags);
	}

	@Override
	public int hashCode() {
		return HashCoder.hash(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof FlagSet<?>)) return false;
		FlagSet<?> other = (FlagSet<?>) obj;
		if (value != other.value) return false;
		return true;
	}

	@Override
	public String toString() {
		return flags.toString();
	}

	private static <T> int mask(T t, ToIntFunction<T> bitFn) {
		return (int) ByteUtil.maskOfBit(true, bitFn.applyAsInt(t));
	}
	
}
