package ceri.ent.server.web;

import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ent.server.ServiceServlet;
import ceri.ent.server.ServletUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JspTestServlet extends ServiceServlet<JspTestService> {
	private static final Logger logger = LogManager.getLogger();
	private static final long serialVersionUID = -2571110065755253481L;
	private static final String JSP = "jsp/jsp-test.jsp";

	public JspTestServlet() {
		super(JspTestService.class);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		ServletUtil.log(logger, request);
		ServletUtil.dispatchJsp(request, response, JSP, null);
	}

}
