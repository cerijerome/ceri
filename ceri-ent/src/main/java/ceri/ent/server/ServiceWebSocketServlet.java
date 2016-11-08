package ceri.ent.server;

import javax.servlet.ServletException;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import ceri.common.util.BasicUtil;

public abstract class ServiceWebSocketServlet<T> extends WebSocketServlet {
	private static final long serialVersionUID = 2677745336504034559L;
	private final Class<T> cls;
	private T service;

	protected ServiceWebSocketServlet(Class<T> cls) {
		this.cls = cls;
	}

	@Override
	public void init() throws ServletException {
		service = ServiceServlet.getService(this, cls);
		if (service == null) throw new ServletException(cls + " has not been set");
		super.init();
	}

	@Override
	public void configure(WebSocketServletFactory factory) {
		factory.setCreator((req, resp) -> createWebSocket(req, resp, service()));
		configure(factory, service());
	}

	protected abstract WebSocketListener createWebSocket(ServletUpgradeRequest req,
		ServletUpgradeResponse resp, T service);

	protected void configure(WebSocketServletFactory factory, T service) {
		BasicUtil.unused(factory, service);
	}

	private T service() {
		return service;
	}

}
