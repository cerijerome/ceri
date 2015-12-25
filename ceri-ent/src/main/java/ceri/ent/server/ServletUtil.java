package ceri.ent.server;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class ServletUtil {
	private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";
	private static final String SUCCESS_MESSAGE_DEF = "Success";

	private ServletUtil() {}

	public static void setSuccessText(HttpServletResponse response) throws IOException {
		setSuccessText(response, SUCCESS_MESSAGE_DEF);
	}

	public static void setSuccessText(HttpServletResponse response, String message)
		throws IOException {
		response.setContentType(MIME_TYPE_TEXT_PLAIN);
		response.getWriter().write(message);
	}

}
