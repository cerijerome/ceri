package ceri.ci.ecg.sjc;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.admin.AdminServlet;

@WebServlet(loadOnStartup = 1, urlPatterns = { "" })
public class InitServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -5357736371272028380L;
	private MainContainer container;

	@Override
	public void init() throws ServletException {
		try {
			logger.info("Initialization started");
			container = new MainContainer();
			ServletContext context = getServletContext();
			ceri.ci.web.WebServlet.set(context, container.web());
			AdminServlet.set(context, container.master.alert());
			logger.info("Initialization complete");
		} catch (Exception e) {
			throw new ServletException("Failed to initialize", e);
		}
	}

	@Override
	public void destroy() {
		logger.info("Shutdown started");
		try {
			container.close();
		} catch (Exception e) {
			logger.catching(Level.WARN, e);
		}
		logger.info("Shutdown complete");
	}

}
