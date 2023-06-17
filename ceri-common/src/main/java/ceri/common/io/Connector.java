package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.event.Listenable;
import ceri.common.reflect.ReflectUtil;

/**
 * A general hardware connector that provides input and output data streams.
 */
public interface Connector extends Closeable {

	/**
	 * Provides the connector name. By default this is the simple class name.
	 */
	default String name() {
		return ReflectUtil.name(getClass());
	}

	/**
	 * The hardware input stream.
	 */
	InputStream in();

	/**
	 * The hardware output stream.
	 */
	OutputStream out();

	/**
	 * A connector that is state-aware, with state change notifications.
	 */
	interface Fixable extends Connector, Listenable.Indirect<StateChange> {
		
		/**
		 * Notify the device that it is broken. For when the device itself cannot determine it is
		 * broken. Does nothing by default.
		 */
		default void broken() {}

		/**
		 * Open the connector.
		 */
		void open() throws IOException;		
	}
	
	/**
	 * A no-op connector implementation.
	 */
	class Null implements Connector.Fixable {
		
		@Override
		public Listenable<StateChange> listeners() {
			return Listenable.ofNull();
		}
		
		@Override
		public void open() throws IOException {}

		@Override
		public InputStream in() {
			return IoStreamUtil.nullIn;
		}

		@Override
		public OutputStream out() {
			return IoStreamUtil.nullOut;
		}

		@Override
		public void close() {}
	}
}
