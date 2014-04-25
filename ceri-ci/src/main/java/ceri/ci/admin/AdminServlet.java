package ceri.ci.admin;

import java.io.IOException;
import java.io.PrintStream;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ceri.ci.alert.AlertService;
import ceri.common.util.StringUtil;

/**
 * Handles the following requests:
 * 
 * <pre>
 * /
 * /?purge
 * /?clear
 * /?delete
 * /?process&events=...
 * /{build}
 * /{build}?clear
 * /{build}?delete
 * /{build}/{job}
 * /{build}/{job}?clear
 * /{build}/{job}?delete
 * </pre>
 */
@WebServlet(name = "adminServlet", urlPatterns = { "/admin/*" })
public class AdminServlet extends HttpServlet {
	private static final String ALERT_ATTRIBUTE = AlertService.class.getName();
	private static final long serialVersionUID = 749914795926651883L;
	private static final String ERROR = "error";
	private AlertService service;

	public static void set(ServletContext context, AlertService alert) {
		context.setAttribute(ALERT_ATTRIBUTE, alert);
	}

	@Override
	public void init() throws ServletException {
		service = (AlertService)getServletContext().getAttribute(ALERT_ATTRIBUTE);
		if (service == null) throw new ServletException(ALERT_ATTRIBUTE + " has not been set");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String responseContent;
		try {
			Params params = new Params(request);
			Command command = CommandFactory.create(params);
			responseContent = command.execute(service);
		} catch (Exception e) {
			responseContent = errorResponse(e);
		}
		response.getWriter().write(responseContent);
	}

	private String errorResponse(Exception e) {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			out.println(ERROR);
			e.printStackTrace(out);
		}
		return b.toString();
	}

}
