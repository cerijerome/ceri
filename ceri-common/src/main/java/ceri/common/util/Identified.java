package ceri.common.util;

import java.util.Comparator;
import ceri.common.function.Excepts.IntPredicate;
import ceri.common.function.Excepts.LongPredicate;
import ceri.common.function.Excepts.Predicate;
import ceri.common.function.Predicates;

/**
 * Indicates that an object provides an id.
 */
public class Identified {
	private Identified() {}

	public interface ByInt extends Comparable<ByInt> {
		Comparator<ByInt> COMPARATOR = Comparator.comparingInt(ByInt::id);

		/**
		 * Provides an id. By default this is the identity hash.
		 */
		default int id() {
			return id(this, null);
		}

		@Override
		default int compareTo(ByInt other) {
			return COMPARATOR.compare(this, other);
		}

		/**
		 * Provides a default id if the given id is null.
		 */
		static int id(Object obj, Integer id) {
			if (id != null) return id;
			return System.identityHashCode(obj);
		}

		/**
		 * A predicate by id.
		 */
		static <E extends Exception> Predicate<E, ByInt> by(IntPredicate<E> predicate) {
			return Predicates.testingInt(ByInt::id, predicate);
		}
	}

	public interface ByLong extends Comparable<ByLong> {
		Comparator<ByLong> COMPARATOR = Comparator.comparingLong(ByLong::id);

		/**
		 * Provides an id. By default this is the identity hash.
		 */
		default long id() {
			return id(this, null);
		}

		@Override
		default int compareTo(ByLong other) {
			return COMPARATOR.compare(this, other);
		}

		/**
		 * Provides a default id if the given id is null.
		 */
		static long id(Object obj, Long id) {
			if (id != null) return id;
			return System.identityHashCode(obj);
		}

		/**
		 * A predicate by id.
		 */
		static <E extends Exception> Predicate<E, ByLong> by(LongPredicate<E> predicate) {
			return Predicates.testingLong(ByLong::id, predicate);
		}
	}
}
