package ceri.ent.server;

import java.util.regex.Pattern;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import org.eclipse.jetty.server.handler.ContextHandler;
import ceri.common.util.BasicUtil;

public class ServletUtil {
	private static final Pattern PACKAGE_SEPARATOR_REGEX = Pattern.compile("\\.");
	private static final String TARGET_PREFIX = "target/classes/";

	public static void setResourceBase(ContextHandler context, Object obj) {
		String packageName = obj.getClass().getPackage().getName();
		String path = PACKAGE_SEPARATOR_REGEX.matcher(packageName).replaceAll("/");
		context.setResourceBase(TARGET_PREFIX + path);
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

}
