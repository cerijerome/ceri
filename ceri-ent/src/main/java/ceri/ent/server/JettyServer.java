package ceri.ent.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import ceri.common.except.ExceptionAdapter;
import ceri.common.net.NetUtil;
import ceri.common.reflect.Reflect;
import ceri.common.text.Regex;
import ceri.log.util.LogUtil;

/**
 * Base wrapper class for managing a jetty server.
 */
public class JettyServer implements AutoCloseable {
	private static final Pattern PROTOCOL_NAME_REGEX = Pattern.compile("^(\\w+)");
	private static final String LOCALHOST = "localhost";
	private final Server server;
	protected final String rootUrl;

	public JettyServer(Server server) {
		this.server = server;
		rootUrl = rootUrl();
	}

	public void start() throws IOException {
		ExceptionAdapter.io.run(server::start);
	}

	public void stop() throws IOException {
		ExceptionAdapter.io.run(server::stop);
	}

	public void waitForServer() throws InterruptedException {
		server.join();
	}

	@Override
	public void close() throws IOException {
		stop();
		LogUtil.close(this::waitForServer);
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
		return Regex.findGroup(PROTOCOL_NAME_REGEX, protocols.get(0), 1);
	}

	private ServerConnector connector() {
		Connector[] connectors = server.getConnectors();
		if (connectors == null || connectors.length == 0) return null;
		return Reflect.castOrNull(ServerConnector.class, connectors[0]);
	}

	private WebAppContext context() {
		return Reflect.castOrNull(WebAppContext.class, server.getHandler());
	}
}
