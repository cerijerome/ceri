package ceri.ent.server;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.apache.jsp.JettyJasperInitializer;
import org.eclipse.jetty.plus.annotation.ContainerInitializer;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceCollection;
import org.eclipse.jetty.webapp.WebAppContext;
import ceri.common.util.BasicUtil;

public class JettyUtil {
	private static final Pattern PACKAGE_SEPARATOR_REGEX = Pattern.compile("\\.");
	private static final String CONTAINER_INCLUDE_JAR_PATTERN_ATTRIBUTE =
		"org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern";
	private static final String TAGLIBS_JAR_PATTERN = ".*/.*taglibs.*\\.jar$";
	private static final String CONTAINER_INITIALIZERS_ATTRIBUTE =
		"org.eclipse.jetty.containerInitializers";
	private static final List<ContainerInitializer> JSP_INITIALIZERS = jspInitializers();

	private JettyUtil() {}

	public static void setResourceBase(ContextHandler context, Class<?>...classes) {
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
		context.setAttribute(CONTAINER_INCLUDE_JAR_PATTERN_ATTRIBUTE, TAGLIBS_JAR_PATTERN);
		context.setAttribute(CONTAINER_INITIALIZERS_ATTRIBUTE, JSP_INITIALIZERS);
		context.addBean(new ServletContainerInitializersStarter(context), true);
	}

	public static void setService(ContextHandler context, Object service) {
		String attributeName = service.getClass().getName();
		context.setAttribute(attributeName, service);
	}

	public static <T> T getService(GenericServlet servlet, Class<T> cls) throws ServletException {
		String attributeName = cls.getName();
		T t = BasicUtil.uncheckedCast(servlet.getServletContext().getAttribute(attributeName));
		if (t == null) throw new ServletException(attributeName + " has not been set");
		return t;
	}

	private static List<ContainerInitializer> jspInitializers() {
		JettyJasperInitializer sci = new JettyJasperInitializer();
		ContainerInitializer initializer = new ContainerInitializer(sci, null);
		return Collections.singletonList(initializer);
	}

}
