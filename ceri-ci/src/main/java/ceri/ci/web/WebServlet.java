package ceri.ci.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ceri.ci.build.Builds;

public class WebServlet extends HttpServlet {
	private static final String WEB_ATTRIBUTE = WebAlerter.class.getName();
	private static final long serialVersionUID = -8414876117635334465L;
	private static final String MODEL = "model";
	private static final String JSP = "/WEB-INF/jsp/Web.jsp";
	private WebAlerter service;

	public static void set(ServletContext context, WebAlerter web) {
		context.setAttribute(WEB_ATTRIBUTE, web);
	}

	@Override
	public void init() throws ServletException {
		service = (WebAlerter)getServletContext().getAttribute(WEB_ATTRIBUTE);
		if (service == null) throw new ServletException(WEB_ATTRIBUTE + " has not been set");
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		WebParams params = WebParams.createFromRequest(request);
		WebModel model = createModel(params);
		request.setAttribute(MODEL, model);
		RequestDispatcher dispatcher = request.getRequestDispatcher(JSP);
		dispatcher.forward(request, response);
	}

	private WebModel createModel(WebParams params) {
		Builds builds = service.builds();
		return new WebModel(params, builds);
	}
	
}