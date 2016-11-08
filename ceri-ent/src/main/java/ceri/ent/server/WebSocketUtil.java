package ceri.ent.server;

import java.net.SocketTimeoutException;
import org.eclipse.jetty.websocket.api.StatusCode;

public class WebSocketUtil {

	private WebSocketUtil() {}

	public static boolean isTrivialClose(int closeCode) {
		return closeCode == StatusCode.NORMAL || closeCode == StatusCode.SHUTDOWN;
	}
	
	public static boolean isTrivialError(Throwable t) {
		if (t instanceof SocketTimeoutException) return true;
		return false;
	}
	
}
