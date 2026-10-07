package ceri.ent.server.web;

import java.io.IOException;
import ceri.ent.server.JettyServer;
import ceri.ent.server.ShutdownServlet;
import ceri.ent.server.ShutdownSync;

/**
 * Creates the servlets and starts the server.
 */
public class JspTestServer {
	private static final String JSP_TEST_PATH = "/jsp-test";

	public static void main(String[] args) throws IOException {
		var shutdown = new ShutdownSync();
		var service = new JspTestService("test");
		try (var server = of(service, shutdown, 8080)) {
			server.start();
			shutdown.await();
		}
	}

	public static JettyServer of(JspTestService service, ShutdownSync shutdown, int port) {
		return JettyServer.build(b -> {
			b.port(port).base(JspTestServer.class).jsp(JspTestServer.class).service(service)
				.servlet(JspTestServlet.class, JSP_TEST_PATH);
			ShutdownServlet.init(b, shutdown);
		});
	}
}
