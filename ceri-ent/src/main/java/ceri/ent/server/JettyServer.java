package ceri.ent.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import ceri.common.net.NetUtil;
import ceri.common.text.RegexUtil;
import ceri.common.util.BasicUtil;

/**
 * Base wrapper class for managing a jetty server.
 */
public class JettyServer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern PROTOCOL_NAME_REGEX = Pattern.compile("^(\\w+)");
	private static final String LOCALHOST = "localhost";
	private final Server server;
	protected final String rootUrl;

	public JettyServer(Server server) {
		this.server = server;
		rootUrl = rootUrl();
	}

	public void start() throws IOException {
		try {
			server.start();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public void stop() throws IOException {
		try {
			server.stop();
		} catch (IOException e) {
			throw e;
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	public void waitForServer() throws InterruptedException {
		server.join();
	}

	@Override
	public void close() throws IOException {
		stop();
		try {
			waitForServer();
		} catch (InterruptedException e) {
			logger.catching(Level.WARN, e);
		}
	}

	public String url(String path) {
		if (rootUrl == null) return null;
		if (path == null) path = "";
		if (rootUrl.endsWith("/") && path.startsWith("/")) path = path.substring(1);
		return rootUrl + path;
	}

	@SuppressWarnings("resource")
	private String rootUrl() {
		WebAppContext context = context();
		ServerConnector connector = connector();
		String protocol = protocol(connector);
		if (context == null || protocol == null) return null;
		return String.format("%s://%s:%d%s", protocol, host(), connector.getPort(),
			context.getContextPath());
	}

	private String host() {
		try {
			InetAddress address = NetUtil.localAddress();
			if (address != null) return address.getHostAddress();
		} catch (SocketException e) {
			// no IP found
		}
		return LOCALHOST;
	}

	private String protocol(ServerConnector connector) {
		if (connector == null) return null;
		List<String> protocols = connector.getProtocols();
		if (protocols.isEmpty()) return null;
		return RegexUtil.find(PROTOCOL_NAME_REGEX, protocols.get(0));
	}

	private ServerConnector connector() {
		Connector[] connectors = server.getConnectors();
		if (connectors == null || connectors.length == 0) return null;
		return BasicUtil.castOrNull(ServerConnector.class, connectors[0]);
	}

	private WebAppContext context() {
		return BasicUtil.castOrNull(WebAppContext.class, server.getHandler());
	}

}
