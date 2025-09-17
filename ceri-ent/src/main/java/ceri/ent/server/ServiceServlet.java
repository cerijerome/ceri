package ceri.ent.server;

import org.eclipse.jetty.server.handler.ContextHandler;
import ceri.common.reflect.Reflect;
import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

@SuppressWarnings("serial")
public abstract class ServiceServlet<T> extends HttpServlet {
	private final Class<T> cls;
	private T service;

	protected ServiceServlet(Class<T> cls) {
		this.cls = cls;
	}

	public static <T> void setService(ContextHandler context, T service) {
		var attributeName = service.getClass().getName();
		context.setAttribute(attributeName, service);
	}

	public static <T> T getService(GenericServlet servlet, Class<T> cls) {
		var attributeName = cls.getName();
		return Reflect.unchecked(servlet.getServletContext().getAttribute(attributeName));
	}

	@Override
	public void init() throws ServletException {
		service = getService(this, cls);
		if (service == null) throw new ServletException(cls + " has not been set");
	}

	protected T service() {
		return service;
	}
}
