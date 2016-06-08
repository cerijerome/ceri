package ceri.ent.server;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import ceri.log.util.LogUtil;

public class ServletUtil {
	private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	private static final String MIME_TYPE_JSON = "application/json";
	private static final String SUCCESS_MESSAGE_DEF = "Success";
	private static final String MODEL = "model";

	private ServletUtil() {}

	public static void log(Logger logger, Request request) {
		log(logger, Level.INFO, request);
	}

	public static void log(Logger logger, HttpServletRequest request) {
		log(logger, Level.INFO, request);
	}

	public static void log(Logger logger, Level level, HttpServletRequest request) {
		log(logger, level, new Request(request));
	}

	public static void log(Logger logger, Level level, Request request) {
		logger.log(level, "Request from {}: {}", LogUtil.toString(request::remoteAddress),
			request.http.getParameterMap());
	}

	public static void dispatchJsp(HttpServletRequest request, HttpServletResponse response,
		String jspPath, Object model) throws ServletException, IOException {
		if (model != null) request.setAttribute(MODEL, model);
		RequestDispatcher dispatcher = request.getRequestDispatcher(jspPath);
		dispatcher.forward(request, response);
	}

	public static void setSuccessText(HttpServletResponse response) throws IOException {
		setSuccessText(response, SUCCESS_MESSAGE_DEF);
	}

	public static void setSuccessText(HttpServletResponse response, String message)
		throws IOException {
		response.setContentType(MIME_TYPE_TEXT_PLAIN);
		response.getWriter().write(message);
	}

	public static void setJsonResponse(HttpServletResponse response, String json)
		throws IOException {
		response.setContentType(MIME_TYPE_JSON);
		response.getWriter().write(json);
	}

	public static void setErrorResponse(HttpServletResponse response, Exception e)
		throws IOException {
		setErrorResponse(response, message(e));
	}

	public static void setErrorResponse(HttpServletResponse response, String message)
		throws IOException {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
	}

	private static String message(Exception e) {
		if (e == null) return "";
		String message = e.getMessage();
		if (message == null) return "";
		return message;
	}

}
