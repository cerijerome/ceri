package ceri.ent.server.web;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import ceri.ent.server.JettyServer;
import ceri.ent.server.JettyUtil;
import ceri.ent.server.ServiceServlet;
import ceri.ent.server.ShutdownServlet;
import ceri.ent.server.ShutdownSync;

/**
 * Creates the servlets and starts the server.
 */
public class JspTestServer extends JettyServer {
	private static final Logger logger = LogManager.getLogger();
	private static final String JSP_TEST_PATH = "/jsp-test";

	public static void main(String[] args) throws IOException {
		ShutdownSync shutdown = new ShutdownSync();
		JspTestService service = new JspTestService("test");
		try (JspTestServer server = new JspTestServer(service, shutdown, 8080)) {
			server.start();
			shutdown.await();
		}
	}
	
	public JspTestServer(JspTestService service, ShutdownSync shutdown, int port) {
		super(createServer(service, shutdown, port));
	}

	@Override
	public void start() throws IOException {
		super.start();
		logger.info("jsp-test: {}", url(JSP_TEST_PATH));
	}

	public static void init(WebAppContext webapp, JspTestService service, ShutdownSync shutdown) {
		ServiceServlet.setService(webapp, service);
		webapp.addServlet(JspTestServlet.class, JSP_TEST_PATH);
		ShutdownServlet.init(webapp, shutdown);
	}

	private static Server createServer(JspTestService service, ShutdownSync shutdown, int port) {
		WebAppContext webapp = JettyUtil.createWebApp();
		JettyUtil.setResourceBase(webapp, JspTestServer.class);
		JettyUtil.initForJsp(webapp, JspTestServer.class);
		init(webapp, service, shutdown);
		return JettyUtil.createServer(webapp, port);
	}

}
