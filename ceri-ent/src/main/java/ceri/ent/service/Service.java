package ceri.ent.service;

import java.io.IOException;
import org.apache.logging.log4j.Logger;
import ceri.common.util.Capability;

/**
 * A simple service that returns a value based on key.
 */
public interface Service<K, V> extends Capability.Name {
	/**
	 * Retrieves the value for the given key.
	 */
	V retrieve(K k) throws Service.Exception;

	/**
	 * An exception specific to services.
	 */
	@SuppressWarnings("serial")
	class Exception extends IOException {
		/**
		 * Exception with message and no cause.
		 */
		public Exception(String message) {
			super(message);
		}

		/**
		 * Exception with message and cause.
		 */
		public Exception(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Wraps a given cause.
		 */
		public Exception(Throwable cause) {
			super(cause);
		}
	}

	/**
	 * Interface for control of a persistent service.
	 */
	interface Persistable extends Capability.Name {
		/**
		 * Loads service data.
		 */
		void load() throws IOException;
		/**
		 * Saves service data.
		 */
		void save() throws IOException;
		
		/**
		 * Saves the service, logging any I/O exception.
		 */
		static boolean save(Logger logger, Service.Persistable persistable) {
			try {
				persistable.save();
				return true;
			} catch (IOException e) {
				logger.catching(e);
				return false;
			}
		}
	}

	/**
	 * Interface for service storage.
	 */
	interface Store<T> {
		/**
		 * Loads data.
		 */
		T load() throws IOException;

		/**
		 * Saves given data.
		 */
		void save(T t) throws IOException;
	}
}
