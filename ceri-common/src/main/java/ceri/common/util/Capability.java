package ceri.common.util;

import java.util.Arrays;
import java.util.Comparator;
import ceri.common.function.Excepts;
import ceri.common.function.Filters;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.text.Strings;

/**
 * Simple capability interfaces.
 */
public class Capability {
	private Capability() {}
	
	/**
	 * For objects with an integer id.
	 */
	public interface IntId extends Comparable<IntId> {
		Comparator<IntId> COMPARATOR = Comparator.comparingInt(IntId::id);

		/**
		 * Provides an id. By default this is the identity hash.
		 */
		default int id() {
			return id(this, null);
		}

		@Override
		default int compareTo(IntId other) {
			return COMPARATOR.compare(this, other);
		}

		/**
		 * Provides a default id if the given id is null.
		 */
		static int id(Object obj, Integer id) {
			if (id != null) return id.intValue();
			return System.identityHashCode(obj);
		}

		/**
		 * A predicate by id.
		 */
		static <E extends Exception> Excepts.Predicate<E, IntId>
			by(Excepts.IntPredicate<E> predicate) {
			return Filters.testingInt(IntId::id, predicate);
		}
	}

	/**
	 * For objects with a long id.
	 */
	public interface LongId extends Comparable<LongId> {
		Comparator<LongId> COMPARATOR = Comparator.comparingLong(LongId::id);

		/**
		 * Provides an id. By default this is the identity hash.
		 */
		default long id() {
			return id(this, null);
		}

		@Override
		default int compareTo(LongId other) {
			return COMPARATOR.compare(this, other);
		}

		/**
		 * Provides a default id if the given id is null.
		 */
		static long id(Object obj, Long id) {
			if (id != null) return id.longValue();
			return System.identityHashCode(obj);
		}

		/**
		 * A predicate by id.
		 */
		static <E extends Exception> Excepts.Predicate<E, LongId>
			by(Excepts.LongPredicate<E> predicate) {
			return Filters.testingLong(LongId::id, predicate);
		}
	}

	/**
	 * For objects that have a name.
	 */
	public interface Name {
		static Comparator<Name> COMPARATOR = Comparator.comparing(Name::name);

		/**
		 * Provides the instance name. By default this is the simple class name and system hash.
		 */
		default String name() {
			return name(this, null);
		}

		/**
		 * Provides a default name if the given name is null.
		 */
		static String name(Object obj, String name) {
			if (name != null) return name;
			return Reflect.nameHash(obj);
		}

		/**
		 * Provides name or null string.
		 */
		static String nameOf(Name named) {
			return named != null ? named.name() : Strings.NULL;
		}

		/**
		 * Predicate by name.
		 */
		static <E extends Exception, T extends Name> Excepts.Predicate<E, T>
			by(Excepts.Predicate<E, String> name) {
			return Filters.testing(Name::name, name);
		}
	}

	/**
	 * For objects that can be initialized.
	 */
	@FunctionalInterface
	public interface Init<E extends Exception> {

		/**
		 * Initializes the object.
		 */
		void init() throws E;

		/**
		 * Initializes the objects.
		 */
		@SafeVarargs
		static <E extends Exception> void init(Init<E>... inits) throws E {
			init(Arrays.asList(inits));
		}

		/**
		 * Initializes the objects.
		 */
		static <E extends Exception> void init(Iterable<Init<E>> inits) throws E {
			for (var init : inits)
				if (init != null) init.init();
		}
	}

	/**
	 * For objects that have empty state.
	 */
	@FunctionalInterface
	public interface IsEmpty {
		static Functions.Predicate<IsEmpty> NOT_BY = t -> !IsEmpty.isEmpty(t);

		/**
		 * Returns true if the object is empty.
		 */
		boolean isEmpty();

		/**
		 * Returns true if the object is null or empty.
		 */
		static boolean isEmpty(IsEmpty isEmpty) {
			return isEmpty == null || !isEmpty.isEmpty();
		}
	}
	
	/**
	 * For objects that have enabled state.
	 */
	@FunctionalInterface
	public interface Enabled {
		static Comparator<Enabled> COMPARATOR = Comparator.comparing(Enabled::enabledInt);
		static Functions.Predicate<Enabled> BY = t -> Enabled.enabled(t);

		/**
		 * Returns true if the object is enabled.
		 */
		boolean enabled();

		/**
		 * Returns true if the object is non-null and enabled.
		 */
		static boolean enabled(Enabled enablable) {
			return enablable != null && enablable.enabled();
		}

		static int enabledInt(Enabled enablable) {
			return enabled(enablable) ? 1 : 0;
		}

		static <T> T conditional(Enabled enablable, T enabled, T disabled) {
			return enabled(enablable) ? enabled : disabled;
		}
	}
}
