package ceri.ci.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.MasterMold;

@WebServlet(loadOnStartup = 1, urlPatterns = { "" })
public class InitServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -5357736371272028380L;
	private MasterMold masterMold;

	@Override
	public void init() throws ServletException {
		System.out.println("Creating MasterMold");
		try {
			logger.info("Creating MasterMold");
			masterMold = new MasterMold();
			add(masterMold.alertService());
			add(masterMold.webService());
		} catch (Exception e) {
			throw new ServletException("Failed to initialize", e);
		}
	}

	@Override
	public void destroy() {
		try {
			remove(masterMold.webService());
			remove(masterMold.alertService());
			masterMold.close();
		} catch (Exception e) {
			logger.catching(Level.WARN, e);
		}
	}

	public static <T> T get(ServletContext context, Class<T> cls) {
		return cls.cast(context.getAttribute(cls.getName()));
	}

	private void add(Object obj) {
		getServletContext().setAttribute(obj.getClass().getName(), obj);
	}

	private void remove(Object obj) {
		getServletContext().removeAttribute(obj.getClass().getName());
	}

}
