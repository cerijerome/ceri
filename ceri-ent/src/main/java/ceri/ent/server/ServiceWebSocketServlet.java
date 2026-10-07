package ceri.ent.server;

import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeRequest;
import org.eclipse.jetty.ee10.websocket.server.JettyServerUpgradeResponse;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServlet;
import org.eclipse.jetty.ee10.websocket.server.JettyWebSocketServletFactory;
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
		service = ServiceServlet.requireService(this, cls);
		super.init();
	}

	@Override
	public void configure(JettyWebSocketServletFactory factory) {
		factory.setCreator((req, resp) -> createWebSocket(req, resp, service()));
		configure(factory, service());
	}

	protected abstract Object createWebSocket(JettyServerUpgradeRequest req,
		JettyServerUpgradeResponse resp, T service);

	@SuppressWarnings("unused")
	protected void configure(JettyWebSocketServletFactory factory, T service) {}

	private T service() {
		return service;
	}
}
