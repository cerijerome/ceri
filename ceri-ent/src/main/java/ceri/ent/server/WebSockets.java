package ceri.ent.server;

import java.net.SocketTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Callback;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketOpen;
import ceri.common.function.Functions;

public class WebSockets {
	private static final Functions.Runnable NOOP = () -> {};

	private WebSockets() {}

	/**
	 * Provides core functionality for a websocket.
	 */
	public static class Adapter {
		private static final Logger logger = LogManager.getLogger();
		protected static final Callback errorLog = errorCallback(logger::catching);
		private Session session = null;

		/**
		 * Called when the socket is opened.
		 */
		@OnWebSocketOpen
		public void onWebSocketOpen(Session session) {
			this.session = session;
			onOpen();
		}

		/**
		 * Called on a socket error.
		 */
		@OnWebSocketError
		public void onWebSocketError(Throwable cause) {
			if (!isTrivialError(cause)) logger.catching(cause);
		}

		/**
		 * Called when a socket is closed.
		 */
		@OnWebSocketClose
		public void onWebSocketClose(int code, String reason) {
			onClose(code, reason);
			session = null;
			if (!isTrivialClose(code)) logger.warn("Closing socket: {} ({})", reason, code);
		}

		protected void onOpen() {}

		@SuppressWarnings("unused")
		protected void onClose(int code, String reason) {}

		/**
		 * Returns the current session, which may be null.
		 */
		protected Session session() {
			return session;
		}

		/**
		 * Attempts to send the given text. Returns false if the text or session is null.
		 */
		protected boolean sendText(String text) {
			if (text == null) return false;
			if (session == null) return false;
			session.sendText(text, errorLog);
			return true;
		}
	}

	public static Callback errorCallback(Functions.Consumer<Throwable> handler) {
		return Callback.from(NOOP, handler);
	}

	public static boolean isTrivialClose(int closeCode) {
		return closeCode == StatusCode.NORMAL || closeCode == StatusCode.SHUTDOWN;
	}

	public static boolean isTrivialError(Throwable t) {
		if (t instanceof SocketTimeoutException) return true;
		return false;
	}
}
