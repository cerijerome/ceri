package ceri.ent.server;

import java.io.IOException;
import java.io.Writer;
import org.eclipse.jetty.server.handler.ErrorHandler;
import jakarta.servlet.http.HttpServletRequest;

public class MinimalErrorHandler extends ErrorHandler {

	@Override
	protected void writeErrorPageBody(HttpServletRequest request, Writer writer, int code,
		String message, boolean showStacks) throws IOException {
		String uri = request.getRequestURI();
		writeErrorPageMessage(request, writer, code, message, uri);
		if (showStacks) writeErrorPageStacks(request, writer);
	}

}
