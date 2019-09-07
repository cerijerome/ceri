package ceri.common.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.BasicUtil;

/**
 * Factory utility methods.
 */
public class Factories {

	private Factories() {}

	/**
	 * Converts a given object using given factory. Any exception is wrapped as a runtime
	 * FactoryException.
	 */
	public static <T, F> T create(Factory<? extends T, ? super F> constructor, F from)
		throws FactoryException {
		try {
			return constructor.create(from);
		} catch (FactoryException e) {
			throw e;
		} catch (RuntimeException e) {
			throw new FactoryException(e);
		}
	}

	private static final Factory<Object, Object> ASSIGN = from -> from;

	/**
	 * A pass-through factory that returns the original object.
	 */
	public static <T> Factory<T, T> assign() {
		return BasicUtil.uncheckedCast(ASSIGN);
	}

	/**
	 * A factory wrapper that returns null from null and delegates the rest. Useful for factories
	 * that don't want to handle nulls.
	 */
	public static <T, F> Factory<T, F> nul(final Factory<T, F> factory) {
		return from -> from == null ? null : factory.create(from);
	}

	/**
	 * A wrapper that synchronizes over itself.
	 */
	public static <T, F> Factory<T, F>
		threadSafe(final Factory<? extends T, ? super F> constructor) {
		return new Factory<>() {
			@Override
			public synchronized T create(F from) {
				return constructor.create(from);
			}
		};
	}

	/**
	 * A wrapper that applies a factory to an array. The original factory exceptions are thrown
	 * unmodified.
	 */
	public static <T, F> Factory<T[], F[]> array(final Factory<T, F> constructor,
		final Class<T> toClass) {
		return new Factory.Base<>() {
			@Override
			protected T[] createNonNull(F[] from) {
				T[] toArray = ArrayUtil.create(toClass, from.length);
				for (int i = 0; i < from.length; i++)
					toArray[i] = constructor.create(from[i]);
				return toArray;
			}
		};
	}

	/**
	 * A wrapper that applies a factory to a list. The return type is ArrayList. The original
	 * factory exceptions are thrown unmodified.
	 */
	public static <T, F> Factory<List<T>, Iterable<F>> list(final Factory<T, F> constructor) {
		return new Factory.Base<>() {
			@Override
			protected List<T> createNonNull(Iterable<F> from) {
				List<T> list = new ArrayList<>();
				for (F f : from) {
					T t = constructor.create(f);
					list.add(t);
				}
				return list;
			}
		};
	}

	/**
	 * A wrapper that applies a factory to a set. The return type is HashSet. The original factory
	 * exceptions are thrown unmodified.
	 */
	public static <T, F> Factory<Set<T>, Iterable<F>> set(final Factory<T, F> constructor) {
		return new Factory.Base<>() {
			@Override
			protected Set<T> createNonNull(Iterable<F> from) {
				Set<T> set = new HashSet<>();
				for (F f : from) {
					T t = constructor.create(f);
					set.add(t);
				}
				return set;
			}
		};
	}

}
