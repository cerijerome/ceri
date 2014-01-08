package ceri.common.factory;

/**
 * Interface for a class that will construct an object of type T from an object
 * of type F. The same constructor can be used for subtypes of T and supertypes
 * of F.
 */
public interface Factory<T, F> {
	T create(F from);

	static abstract class Base<T, F> implements Factory<T, F> {

		@Override
		public T create(F from) {
			if (from == null) return null;
			return createNonNull(from);
		}

		protected abstract T createNonNull(F from);
	}

}