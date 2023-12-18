package ceri.common.util;

/**
 * Indicates that an object provides an id.
 */
public class Id {

	private Id() {}

	public interface OfInt {
		/**
		 * Provides an id. By default this is the identity hash.
		 */
		default int id() {
			return id(this, null);
		}

		/**
		 * Provides a default id if the given id is null.
		 */
		static int id(Object obj, Integer id) {
			if (id != null) return id;
			return System.identityHashCode(obj);
		}
	}

	public interface OfLong {
		/**
		 * Provides an id. By default this is the identity hash.
		 */
		default long id() {
			return id(this, null);
		}

		/**
		 * Provides a default id if the given id is null.
		 */
		static long id(Object obj, Long id) {
			if (id != null) return id;
			return System.identityHashCode(obj);
		}
	}
}
