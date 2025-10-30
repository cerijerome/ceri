package ceri.ent.server;

import static org.eclipse.jetty.servlet.DefaultServlet.CONTEXT_INIT;
import static org.eclipse.jetty.webapp.MetaInfConfiguration.CONTAINER_JAR_PATTERN;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import ceri.log.util.Logs;

public class JettyUtil {
	private static final Pattern PACKAGE_SEPARATOR_REGEX = Pattern.compile("\\.");
	private static final String TAGLIBS_JAR_PATTERN = ".*/.*taglibs.*\\.jar$";
	private static final String TMP_DIR = "tmp";
	private static final String CONTEXT_PATH_DEF = "/";
	private static final String DIR_ALLOWED = CONTEXT_INIT + "dirAllowed";

	private JettyUtil() {}

	public static JettyServer startSimplServer(int port, Class<?>... classes) throws IOException {
		return startSimplServer(port, Arrays.asList(classes));
	}

	public static JettyServer startSimplServer(int port, Collection<Class<?>> classes)
		throws IOException {
		WebAppContext webapp = JettyUtil.createWebApp();
		JettyUtil.setResourceBase(webapp, classes);
		JettyServer server = new JettyServer(createServer(webapp, port));
		server.start();
		return server;
	}

	public static Server createServer(WebAppContext webapp, int port) {
		Server server = new Server(port);
		server.setHandler(webapp);
		return server;
	}

	public static WebAppContext createWebApp() {
		return createWebApp(CONTEXT_PATH_DEF);
	}

	public static WebAppContext createWebApp(String contextPath) {
		WebAppContext webapp = new WebAppContext();
		webapp.setContextPath(contextPath);
		return webapp;
	}

	public static void setResourceBase(ContextHandler context, Class<?>... classes) {
		setResourceBase(context, Arrays.asList(classes));
	}

	@SuppressWarnings("resource") // collection is correctly closed if error during construction
	public static void setResourceBase(ContextHandler context, Collection<Class<?>> classes) {
		context.setBaseResource(resourceCollection(classes));
	}

	private static ResourceCollection resourceCollection(Collection<Class<?>> classes) {
		return new ResourceCollection(
			Logs.createArray(Resource[]::new, JettyUtil::resource, classes));
	}

	private static Resource resource(Class<?> cls) {
		String packageName = cls.getPackage().getName();
		String path = PACKAGE_SEPARATOR_REGEX.matcher(packageName).replaceAll("/");
		return Resource.newResource(cls.getClassLoader().getResource(path));
	}

	public static void disableDirectory(WebAppContext context) {
		context.setInitParameter(DIR_ALLOWED, String.valueOf(false));
	}

	/**
	 * Doesn't show the powered-by jetty line in error pages.
	 */
	public static void minimalErrorPages(WebAppContext context) {
		context.setErrorHandler(new MinimalErrorHandler());
	}

	public static void initForJsp(WebAppContext context) {
		initForJsp(context, (Path) null);
	}

	public static void initForJsp(WebAppContext context, Class<?> jspTmpDirClass) {
		String jspTmpDir = TMP_DIR + "/" + jspTmpDirClass.getPackage().getName();
		initForJsp(context, jspTmpDir);
	}

	public static void initForJsp(WebAppContext context, String jspTmpDir) {
		initForJsp(context, Path.of(jspTmpDir));
	}

	public static void initForJsp(WebAppContext context, Path jspTmpDir) {
		context.setAttribute(CONTAINER_JAR_PATTERN, TAGLIBS_JAR_PATTERN);
		context.addServletContainerInitializer(new JettyJasperInitializer());
		if (jspTmpDir != null) context.setTempDirectory(jspTmpDir.toFile());
	}
}
