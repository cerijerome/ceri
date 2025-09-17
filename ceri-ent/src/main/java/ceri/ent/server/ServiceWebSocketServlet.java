package ceri.ent.server;

import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.websocket.server.JettyWebSocketServletFactory;
import ceri.common.util.Basics;
import jakarta.servlet.ServletException;

@SuppressWarnings("serial")
public abstract class ServiceWebSocketServlet<T> extends JettyWebSocketServlet {
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
	public void configure(JettyWebSocketServletFactory factory) {
		factory.setCreator((req, resp) -> createWebSocket(req, resp, service()));
		configure(factory, service());
	}

	protected abstract WebSocketListener createWebSocket(JettyServerUpgradeRequest req,
		JettyServerUpgradeResponse resp, T service);

	protected void configure(JettyWebSocketServletFactory factory, T service) {
		Basics.unused(factory, service);
	}

	private T service() {
		return service;
	}
}
