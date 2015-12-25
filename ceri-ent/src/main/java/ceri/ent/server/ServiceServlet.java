package ceri.ent.server;

import static ceri.common.util.BasicUtil.uncheckedCast;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import org.eclipse.jetty.server.handler.ContextHandler;

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
		return uncheckedCast(servlet.getServletContext().getAttribute(attributeName));
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
