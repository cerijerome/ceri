package ceri.ent.server;

import ceri.common.reflect.Reflect;
import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

@SuppressWarnings("serial")
public abstract class ServiceServlet<T> extends HttpServlet {
	private final Class<T> cls;
	private T service;

	public static <T> T service(GenericServlet servlet, Class<T> cls) {
		var attributeName = cls.getName();
		return Reflect.unchecked(servlet.getServletContext().getAttribute(attributeName));
	}

	public static <T> T requireService(GenericServlet servlet, Class<T> cls)
		throws ServletException {
		var service = service(servlet, cls);
		if (service != null) return service;
		throw new ServletException(cls + " has not been set");
	}

	protected ServiceServlet(Class<T> cls) {
		this.cls = cls;
	}

	@Override
	public void init() throws ServletException {
		service = requireService(this, cls);
	}

	protected T service() {
		return service;
	}
}
