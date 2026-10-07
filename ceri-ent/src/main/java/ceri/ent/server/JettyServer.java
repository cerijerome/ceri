package ceri.ent.server;

import java.io.IOException;
import java.io.Writer;
import java.net.SocketException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tomcat.util.descriptor.DigesterFactory;
import org.eclipse.jetty.ee10.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ErrorHandler;
import org.eclipse.jetty.ee10.webapp.MetaInfConfiguration;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import ceri.common.collect.Lists;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Excepts;
import ceri.common.net.Net;
import ceri.common.reflect.Reflect;
import ceri.common.stream.Stream;
import ceri.common.text.Regex;
import ceri.common.util.SystemVars;
import ceri.log.util.Logs;
import jakarta.servlet.Servlet;

/**
 * Base wrapper class for managing a jetty server.
 */
public class JettyServer implements AutoCloseable {
	private static final Logger logger = LogManager.getLogger();
	// Check dependency jar names still match this pattern
	private static final String JSTL_JAR_PATTERN = ".*/jakarta.*\\.jstl.*\\.jar$";
	private static final String DIGESTER_FACTORY_VALIDATING =
		SystemVars.name(DigesterFactory.class, "validating");
	private static final Pattern PACKAGE_SEPARATOR_REGEX = Pattern.compile("\\.");
	private static final Pattern PROTOCOL_NAME_REGEX = Pattern.compile("^(\\w+)");
	private static final String ROOT_PATH = "/";
	private static final String LOCALHOST = "localhost";
	private final Config config;
	private final ResourceFactory.Closeable resources;
	private final WebAppContext app;
	private final Server server;
	private final String rootUrl;

	static {
		// Remove noisy logging here rather than every log4 config
		var config = Logs.config();
		config.max("org.eclipse.jetty", Level.INFO);
		config.max("org.apache.tomcat", Level.INFO);
		config.max("org.apache.jasper", Level.INFO);
		config.max(DigesterFactory.class, Level.ERROR);
	}

	private static class MinimalErrorHandler extends ErrorHandler {
		@Override
		protected void writeErrorHtmlBody(Request request, Writer writer, int code, String message,
			Throwable cause) throws IOException {
			var uri = request.getHttpURI().asString();
			writeErrorHtmlMessage(request, writer, code, message, cause, uri);
			if (isShowStacks()) writeErrorHtmlStacks(request, writer);
		}
	}

	private record Config(int port, boolean showServletUrls) {}

	public static class Builder {
		private static final String DIR_ALLOWED = DefaultServlet.CONTEXT_INIT + "dirAllowed";
		private static final String TMP_DIR = "tmp";
		private final ResourceFactory.Closeable resources;
		private final WebAppContext app = new WebAppContext();
		private int port = 8080;
		private boolean showServletUrls = true;

		private Builder(ResourceFactory.Closeable resources) {
			// Disable XML validation and namespace awareness
			app.setAttribute(MetaInfConfiguration.WEBINF_JAR_PATTERN, ".*");
			// Force the internal DigesterFactory/MetaData processor to skip validation
			app.setConfigurationDiscovered(true);
			System.setProperty(DIGESTER_FACTORY_VALIDATING, "false");
			this.resources = resources;
		}

		/**
		 * Set the server path.
		 */
		public Builder path(String path) {
			app().setContextPath(path);
			return this;
		}

		/**
		 * Set the server port.
		 */
		public Builder port(int port) {
			this.port = port;
			return this;
		}

		/**
		 * Sets base resource dirs from class packages.
		 */
		public Builder base(Class<?>... classes) {
			return base(Arrays.asList(classes));
		}

		/**
		 * Sets base resource dirs from class packages.
		 */
		public Builder base(Iterable<Class<?>> classes) {
			app().setBaseResource(JettyServer.resources(resources(), classes));
			return this;
		}

		/**
		 * Don't show the powered-by jetty line in error pages.
		 */
		public Builder minimalError() {
			app().setErrorHandler(new MinimalErrorHandler());
			return this;
		}

		/**
		 * Enable or disable directory listing.
		 */
		public Builder noDirs() {
			app().setInitParameter(DIR_ALLOWED, String.valueOf(false));
			return this;
		}

		/**
		 * Don't write servlet urls to log on startup.
		 */
		public Builder hideServletUrls() {
			showServletUrls = false;
			return this;
		}

		/**
		 * Enables JSP using default temp dir.
		 */
		public Builder jsp() {
			initForJsp(app(), (Path) null);
			return this;
		}

		/**
		 * Enables JSP using given class package-based temp dir.
		 */
		public Builder jsp(Class<?> jspTmpDirClass) {
			var jspTmpDir = TMP_DIR + "/" + jspTmpDirClass.getPackage().getName();
			return jsp(jspTmpDir);
		}

		/**
		 * Enables JSP using given temp dir.
		 */
		public Builder jsp(String jspTmpDir) {
			initForJsp(app(), Path.of(jspTmpDir));
			return this;
		}

		/**
		 * Adds a servlet.
		 */
		public Builder servlet(Class<? extends Servlet> servlet, String pathSpec) {
			app().addServlet(servlet, pathSpec);
			return this;
		}

		/**
		 * Sets an attribute.
		 */
		public Builder attribute(String name, Object value) {
			app().setAttribute(name, value);
			return this;
		}

		/**
		 * Sets a service object as an attribute using its class name.
		 */
		public Builder service(Object service) {
			if (service == null) return this;
			return attribute(service.getClass().getName(), service);
		}

		/**
		 * Provide direct access to web app context.
		 */
		public WebAppContext app() {
			return app;
		}

		/**
		 * Provide direct access to resource factory.
		 */
		public ResourceFactory resources() {
			return resources;
		}

		private JettyServer build() {
			return new JettyServer(resources, app, server(app, port),
				new Config(port, showServletUrls));
		}
	}

	/**
	 * Builds a jetty server by passing a builder to the consumer.
	 */
	@SuppressWarnings("resource")
	public static <E extends Exception> JettyServer build(Excepts.Consumer<E, Builder> consumer)
		throws E {
		var resources = ResourceFactory.closeable();
		try {
			var builder = new Builder(resources);
			consumer.accept(builder);
			return builder.build();
		} catch (Exception e) {
			Logs.close(resources);
			throw e;
		}
	}

	/**
	 * Starts a simple server with resource dirs based on class packages.
	 */
	public static JettyServer start(int port, Class<?>... classes) throws IOException {
		return start(port, Arrays.asList(classes));
	}

	/**
	 * Starts a simple server with resource dirs based on class packages.
	 */
	public static JettyServer start(int port, Iterable<Class<?>> classes) throws IOException {
		var server = build(b -> b.port(port).base(classes));
		try {
			server.start();
			return server;
		} catch (Exception e) {
			Logs.close(server);
			throw e;
		}
	}

	private JettyServer(ResourceFactory.Closeable resources, WebAppContext app, Server server,
		Config config) {
		this.config = config;
		this.resources = resources;
		this.app = app;
		this.server = server;
		rootUrl = rootUrl();
	}

	/**
	 * Returns the server port.
	 */
	public int port() {
		return config.port();
	}

	/**
	 * Starts the server.
	 */
	public void start() throws IOException {
		ExceptionAdapter.io.run(server::start);
		if (config.showServletUrls()) servletUrls().forEach(url -> logger.info("URL: {}", url));
	}

	/**
	 * Stops the server.
	 */
	public void stop() throws IOException {
		ExceptionAdapter.io.run(server::stop);
	}

	/**
	 * Waits until the server threads are closed.
	 */
	public void waitForServer() throws InterruptedException {
		server.join();
	}

	/**
	 * Constructs a url by appending the given path to the root.
	 */
	public String url(String path) {
		if (rootUrl == null) return null;
		if (path == null) path = "";
		if (rootUrl.endsWith("/") && path.startsWith("/")) path = path.substring(1);
		return rootUrl + path;
	}

	/**
	 * Finds the urls for registered servlets.
	 */
	public List<String> servletUrls() {
		return Stream.from(servletPaths(app)).map(this::url).toList();
	}

	@Override
	public void close() throws IOException {
		stop();
		Logs.close(this::waitForServer, resources);
	}

	// support

	@SuppressWarnings("resource")
	private String rootUrl() {
		var protocol = protocol(connector());
		if (protocol == null) return null;
		return String.format("%s://%s:%d%s", protocol, host(), port(), app.getContextPath());
	}

	private String host() {
		try {
			var address = Net.localAddress();
			if (address != null) return address.getHostAddress();
		} catch (SocketException e) {
			// no IP found
		}
		return LOCALHOST;
	}

	private String protocol(ServerConnector connector) {
		if (connector == null) return null;
		var protocols = connector.getProtocols();
		if (protocols.isEmpty()) return null;
		return Regex.findGroup(PROTOCOL_NAME_REGEX, protocols.get(0), 1);
	}

	private ServerConnector connector() {
		var connectors = server.getConnectors();
		if (connectors == null || connectors.length == 0) return null;
		return Reflect.castOrNull(ServerConnector.class, connectors[0]);
	}

	private static Resource resources(ResourceFactory resources, Iterable<Class<?>> classes) {
		var array = Stream.from(classes).map(c -> resource(resources, c)).toArray(Resource[]::new);
		return ResourceFactory.combine(array);
	}

	private static Resource resource(ResourceFactory resources, Class<?> cls) {
		var packageName = cls.getPackage().getName();
		var path = PACKAGE_SEPARATOR_REGEX.matcher(packageName).replaceAll("/");
		return resources.newResource(cls.getClassLoader().getResource(path));
	}

	private static void initForJsp(WebAppContext context, Path jspTmpDir) {
		context.setAttribute(MetaInfConfiguration.CONTAINER_JAR_PATTERN, JSTL_JAR_PATTERN);
		context.addServletContainerInitializer(new JettyJasperInitializer());
		if (jspTmpDir != null) context.setTempDirectory(jspTmpDir.toFile());
	}

	private static Server server(WebAppContext app, int port) {
		var server = new Server(port);
		server.setHandler(app);
		return server;
	}

	private static List<String> servletPaths(WebAppContext app) {
		var mappings = app.getServletHandler().getServletMappings();
		var paths = Lists.<String>of();
		for (var mapping : mappings)
			for (var path : mapping.getPathSpecs())
				if (path.charAt(0) != '*' && !ROOT_PATH.equals(path)) paths.add(path);
		if (paths.isEmpty()) paths.add(ROOT_PATH);
		return paths;
	}
}
