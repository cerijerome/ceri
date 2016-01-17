package ceri.ent.server;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;

public class JettyUtil {
	private static final Pattern PACKAGE_SEPARATOR_REGEX = Pattern.compile("\\.");
	private static final String CONTAINER_INCLUDE_JAR_PATTERN_ATTRIBUTE =
		"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern";
	private static final String TAGLIBS_JAR_PATTERN = ".*/.*taglibs.*\\.jar$";
	private static final String CONTAINER_INITIALIZERS_ATTRIBUTE =
		"org.eclipse.jetty.containerInitializers";
	private static final List<ContainerInitializer> JSP_INITIALIZERS = jspInitializers();
	private static final String TMP_DIR = "tmp";
	private static final String CONTEXT_PATH_DEF = "/";

	private JettyUtil() {}

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

	@SuppressWarnings("resource")
	public static void setResourceBase(ContextHandler context, Collection<Class<?>> classes) {
		Resource[] resources = classes.stream().map(JettyUtil::resource).toArray(Resource[]::new);
		context.setBaseResource(new ResourceCollection(resources));
	}

	private static Resource resource(Class<?> cls) {
		String packageName = cls.getPackage().getName();
		String path = PACKAGE_SEPARATOR_REGEX.matcher(packageName).replaceAll("/");
		return Resource.newResource(cls.getClassLoader().getResource(path));
	}

	public static void initForJsp(WebAppContext context) {
		initForJsp(context, (File) null);
	}

	public static void initForJsp(WebAppContext context, Class<?> jspTmpDirClass) {
		String jspTmpDir = TMP_DIR + "/" + jspTmpDirClass.getPackage().getName();
		initForJsp(context, jspTmpDir);
	}

	public static void initForJsp(WebAppContext context, String jspTmpDir) {
		initForJsp(context, new File(jspTmpDir));
	}

	public static void initForJsp(WebAppContext context, File jspTmpDir) {
		context.setAttribute(CONTAINER_INCLUDE_JAR_PATTERN_ATTRIBUTE, TAGLIBS_JAR_PATTERN);
		context.setAttribute(CONTAINER_INITIALIZERS_ATTRIBUTE, JSP_INITIALIZERS);
		context.addBean(new ServletContainerInitializersStarter(context), true);
		if (jspTmpDir != null) context.setTempDirectory(jspTmpDir);
	}

	private static List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		return Collections.singletonList(initializer);
	}

}
