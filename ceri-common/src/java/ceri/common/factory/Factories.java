package ceri.common.factory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import ceri.common.collection.ArrayUtil;
import ceri.common.util.BasicUtil;

public class Factories {

	private Factories() {}

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

	private static final Factory<Object, Object> ASSIGN = new Factory<Object, Object>() {
		@Override
		public Object create(Object from) {
			return from;
		}
	};

	public static <T> Factory<T, T> assign() {
		return BasicUtil.uncheckedCast(ASSIGN);
	}
	
	public static <T, F> Factory<T, F> nul(final Factory<T, F> factory) {
		return new Factory<T, F>() {
			@Override
			public T create(F from) {
				if (from == null) return null;
				return factory.create(from);
			}
		};
	}
		
	public static <T, F> Factory<T, F> threadSafe(
		final Factory<? extends T, ? super F> constructor) {
		return new Factory<T, F>() {
			@Override
			public synchronized T create(F from) {
				return constructor.create(from);
			}
		};
	}

	public static <T, F> Factory<T[], F[]> array(final Factory<T, F> constructor,
		final Class<T> toClass) {
		return new Factory.Base<T[], F[]>() {
			@Override
			protected T[] createNonNull(F[] from) {
				T[] toArray = ArrayUtil.create(toClass, from.length);
				for (int i = 0; i < from.length; i++)
					toArray[i] = Factories.create(constructor, from[i]);
				return toArray;
			}
		};
	}

	public static <T, F> Factory<List<T>, Iterable<F>>
		list(final Factory<T, F> constructor) {
		return new Factory.Base<List<T>, Iterable<F>>() {
			@Override
			protected List<T> createNonNull(Iterable<F> from) {
				List<T> list = new ArrayList<>();
				for (F f : from) {
					T t = Factories.create(constructor, f);
					list.add(t);
				}
				return list;
			}
		};
	}

	public static <T, F> Factory<Set<T>, Iterable<F>> set(final Factory<T, F> constructor) {
		return new Factory.Base<Set<T>, Iterable<F>>() {
			@Override
			protected Set<T> createNonNull(Iterable<F> from) {
				Set<T> set = new HashSet<>();
				for (F f : from) {
					T t = Factories.create(constructor, f);
					set.add(t);
				}
				return set;
			}
		};
	}

}
