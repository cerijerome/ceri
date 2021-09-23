package ceri.ci.admin;

import java.io.IOException;
import java.io.PrintStream;
import ceri.ci.alert.AlertService;
import ceri.common.text.StringUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
public class AdminServlet extends HttpServlet {
	public static final String ALERT_ATTRIBUTE = AlertService.class.getName();
	private static final long serialVersionUID = 749914795926651883L;
	private static final String JSON_CONTENT_TYPE = "application/json";
	private static final String TEXT_CONTENT_TYPE = "text/plain";
	private static final String SUCCESS = "success";
	private static final String ERROR = "error";
	private final Serializer serializer = new Serializer(true);
	private final CommandFactory commandFactory = new CommandFactory(serializer);
	private AlertService service;

	/**
	 * Servlet context attribute must be set to retrieve the service.
	 */
	@Override
	public void init() throws ServletException {
		service = (AlertService) getServletContext().getAttribute(ALERT_ATTRIBUTE);
		if (service == null) throw new ServletException(ALERT_ATTRIBUTE + " has not been set");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Response commandResponse = null;
		try {
			Params params = new Params(request, serializer);
			Command command = commandFactory.create(params);
			commandResponse = command.execute(service);
		} catch (Exception e) {
			commandResponse = Response.fail(e);
		}
		sendResponse(response, commandResponse);
	}

	@SuppressWarnings("resource")
	private void sendResponse(HttpServletResponse response, Response commandResponse)
		throws IOException {
		response.setContentType(contentType(commandResponse));
		String content = content(commandResponse);
		if (commandResponse.success) response.getWriter().write(content);
		else response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, content);
	}

	private String contentType(Response response) {
		if (!response.success) return TEXT_CONTENT_TYPE;
		if (response.content == null) return TEXT_CONTENT_TYPE;
		return JSON_CONTENT_TYPE;
	}

	private String content(Response response) {
		if (response.success) {
			if (response.content == null) return SUCCESS;
			return response.content;
		}
		if (response.exception != null) return toString(response.exception);
		if (response.content != null) return response.content;
		return ERROR;
	}

	private String toString(Exception e) {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			e.printStackTrace(out);
		}
		return b.toString();
	}
}
