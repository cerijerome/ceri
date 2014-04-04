package ceri.ci.servlet;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ceri.ci.build.Builds;
import ceri.ci.web.WebAlerter;

@javax.servlet.annotation.WebServlet(name = "webServlet", urlPatterns = { "/web/*" })
public class WebServlet extends HttpServlet {
	private static final long serialVersionUID = -8414876117635334465L;
	private static final String MODEL = "model";
	private static final String JSP = "/WEB-INF/jsp/Web.jsp";
	private WebAlerter service;

	@Override
	public void init() {
		service = InitServlet.get(getServletContext(), WebAlerter.class);
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