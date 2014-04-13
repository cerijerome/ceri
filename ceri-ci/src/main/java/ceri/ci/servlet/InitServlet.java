package ceri.ci.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.MasterMold;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;

@WebServlet(loadOnStartup = 1, urlPatterns = { "" })
public class InitServlet extends HttpServlet {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -5357736371272028380L;
	private MasterMold masterMold;

	@Override
	public void init() throws ServletException {
		try {
			logger.info("Creating MasterMold");
			masterMold = new MasterMold();
			add(masterMold.alertService());
			add(masterMold.webService());
			add(masterMold.proxy());
			// Fake events -----------------
			BuildEvent ev0 = new BuildEvent("bolt", "smoke", Event.broken("cjerome"));
			//BuildEvent ev1 = new BuildEvent("bolt", "smoke", Event.fixed("cjerome"));
			BuildEvent ev2 = new BuildEvent("bolt", "integration", Event.broken("shuochen","tantony"));
			BuildEvent ev3 = new BuildEvent("bolt", "regression", Event.broken("sseamon","tantony"));
			BuildEvent ev4 = new BuildEvent("mweb", "regression", Event.broken("punpal"));
			masterMold.alertService().process(ev0, ev2, ev3, ev4);
		} catch (Exception e) {
			throw new ServletException("Failed to initialize", e);
		}
	}

	@Override
	public void destroy() {
		logger.info("Shutting down");
		try {
			remove(masterMold.proxy());
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
