package ceri.ent.server;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ShutdownServlet extends ServiceServlet<ShutdownSync> {
	private static final Logger logger = LogManager.getLogger();
	private static final String PATH_DEF = "/shutdown";

	public static JettyServer.Builder init(JettyServer.Builder builder, ShutdownSync shutdown) {
		return init(builder, shutdown, PATH_DEF);
	}

	public static JettyServer.Builder init(JettyServer.Builder builder, ShutdownSync shutdown,
		String path) {
		return builder.service(shutdown).servlet(ShutdownServlet.class, path);
	}

	public ShutdownServlet() {
		super(ShutdownSync.class);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ServletUtil.log(logger, request);
		service().signal();
		ServletUtil.setSuccessText(response);
	}
}
