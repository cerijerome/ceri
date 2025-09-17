package ceri.ent.server;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.webapp.WebAppContext;
import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ShutdownServlet extends ServiceServlet<ShutdownSync> {
	private static final Logger logger = LogManager.getLogger();
	private static final String PATH_DEF = "/shutdown";

	public ShutdownServlet() {
		super(ShutdownSync.class);
	}

	public static void init(WebAppContext webapp, ShutdownSync shutdown) {
		init(webapp, shutdown, PATH_DEF);
	}

	public static void init(WebAppContext webapp, ShutdownSync shutdown, String path) {
		ServiceServlet.setService(webapp, shutdown);
		webapp.addServlet(ShutdownServlet.class, path);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletUtil.log(logger, request);
		service().signal();
		ServletUtil.setSuccessText(response);
	}

	public static void main(String[] args) {
		System.out.println(Servlet.class.isAssignableFrom(ShutdownServlet.class));
	}
}
