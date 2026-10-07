package ceri.ent.server;

import java.io.IOException;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import ceri.common.net.Http;
import ceri.common.property.Parser;
import ceri.common.text.Regex;
import ceri.common.text.Strings;
import ceri.log.util.Logs;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Servlets {
	private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	private static final String MIME_TYPE_JSON = "application/json";
	private static final String SUCCESS_MESSAGE_DEF = "Success";
	private static final String MODEL = "model";

	private Servlets() {}

	/**
	 * A request wrapper for accessing servlet request properties.
	 */
	public record Request(HttpServletRequest request) {

		public void log(Logger logger) {
			log(logger, Level.INFO);
		}

		public void log(Logger logger, Level level) {
			if (!logger.isEnabled(level)) return;
			logger.log(level, "Request from {}: {} {}", remoteAddress(), request().getServletPath(),
				Strings.compact(String.valueOf(request().getParameterMap())));
		}

		public String userAgent() {
			return request().getHeader(Http.Headers.USER_AGENT);
		}

		public String remoteAddress() {
			var ipAddress = request().getHeader(Http.Headers.X_FORWARDED_FOR);
			ipAddress = Regex.Split.COMMA.stream(ipAddress).next();
			if (ipAddress != null) return ipAddress;
			return request().getRemoteAddr();
		}

		public String param(String name) {
			return request().getParameter(name);
		}

		public Parser.String parse(String paramName) {
			return Parser.string(param(paramName));
		}
		
		public void dispatchJsp(HttpServletResponse response,
			String jspPath, Object model) throws ServletException, IOException {
			if (model != null) request().setAttribute(MODEL, model);
			var dispatcher = request().getRequestDispatcher(jspPath);
			dispatcher.forward(request(), response);
		}
	}

	/**
	 * A response wrapper for setting properties.
	 */
	public record Response(HttpServletResponse response) {

		public void success() throws IOException {
			text(SUCCESS_MESSAGE_DEF);
		}

		public void text(String text) throws IOException {
			response().setContentType(MIME_TYPE_TEXT_PLAIN);
			write(text);
		}

		public void json(String json) throws IOException {
			response().setContentType(MIME_TYPE_JSON);
			write(json);
		}

		public void error(Throwable t) throws IOException {
			error(t == null ? "" : Strings.safe(t.getMessage()));
		}

		public void error(String message) throws IOException {
			response().sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message);
		}

		@SuppressWarnings("resource")
		private void write(String text) throws IOException {
			response().getWriter().write(text);
		}
	}

	public static void log(Logger logger, ceri.ent.server.Request request) {
		log(logger, Level.INFO, request);
	}

	public static void log(Logger logger, HttpServletRequest request) {
		log(logger, Level.INFO, request);
	}

	public static void log(Logger logger, Level level, HttpServletRequest request) {
		log(logger, level, new ceri.ent.server.Request(request));
	}

	public static void log(Logger logger, Level level, ceri.ent.server.Request request) {
		logger.log(level, "Request from {}: {} {}", Logs.toString(request::remoteAddress),
			request.http.getServletPath(), Logs.compact(request.http.getParameterMap()));
	}

	public static void dispatchJsp(HttpServletRequest request, HttpServletResponse response,
		String jspPath, Object model) throws ServletException, IOException {
		if (model != null) request.setAttribute(MODEL, model);
		var dispatcher = request.getRequestDispatcher(jspPath);
		dispatcher.forward(request, response);
	}

	public static void setSuccessText(HttpServletResponse response) throws IOException {
		setSuccessText(response, SUCCESS_MESSAGE_DEF);
	}

	@SuppressWarnings("resource")
	public static void setSuccessText(HttpServletResponse response, String message)
		throws IOException {
		response.setContentType(MIME_TYPE_TEXT_PLAIN);
		response.getWriter().write(message);
	}

	@SuppressWarnings("resource")
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

	private static String message(Throwable t) {
		return t == null ? "" : Strings.safe(t.getMessage());
	}
}
