package ceri.log.io;

import ceri.common.io.Connector;
import ceri.common.net.HostPort;

/**
 * A fixable socket connector interface.
 */
public interface SocketConnector extends Connector.Fixable {
	
	/**
	 * Provides the socket host and port.
	 */
	HostPort hostPort();

	/**
	 * A no-op socket connector instance.
	 */
	SocketConnector NULL = new Null();

	/**
	 * A no-op socket connector implementation.
	 */
	class Null extends Connector.Null implements SocketConnector {
		@Override
		public HostPort hostPort() {
			return HostPort.NULL;
		}
	}
}
