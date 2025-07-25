package ceri.ent.server;

import static ceri.common.util.BasicUtil.unchecked;
import org.eclipse.jetty.server.handler.ContextHandler;
import jakarta.servlet.GenericServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

public abstract class ServiceServlet<T> extends HttpServlet {
	private static final long serialVersionUID = 2677745336504034559L;
	private final Class<T> cls;
	private T service;

	protected ServiceServlet(Class<T> cls) {
		this.cls = cls;
	}

	public static <T> void setService(ContextHandler context, T service) {
		String attributeName = service.getClass().getName();
		context.setAttribute(attributeName, service);
	}

	public static <T> T getService(GenericServlet servlet, Class<T> cls) {
		String attributeName = cls.getName();
		return unchecked(servlet.getServletContext().getAttribute(attributeName));
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
