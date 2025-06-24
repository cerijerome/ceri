package ceri.common.util;

import java.util.Comparator;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
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
		static Predicate<ByInt> by(IntPredicate predicate) {
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
		static Predicate<ByLong> by(LongPredicate predicate) {
			return Predicates.testingLong(ByLong::id, predicate);
		}
	}
}
