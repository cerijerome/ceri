package ceri.ci.admin;

import static ceri.common.test.TestUtil.assertException;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.alert.AlertService;
import ceri.ci.build.Builds;

public class AdminServletBehavior {
	@Mock private HttpServletRequest request;
	@Mock private HttpServletResponse response;
	@Mock private ServletContext context;
	@Mock private ServletConfig config;
	@Mock private AlertService service;
	@Mock private PrintWriter writer;
	private Builds builds;
	private AdminServlet servlet;

	@Before
	public void init() throws IOException {
		MockitoAnnotations.initMocks(this);
		servlet = new AdminServlet();
		when(config.getServletContext()).thenReturn(context);
		when(context.getAttribute("ceri.ci.alert.AlertService")).thenReturn(service);
		when(request.getParameter("view")).thenReturn("");
		builds = new Builds();
		when(service.builds()).thenReturn(builds);
		when(response.getWriter()).thenReturn(writer);
	}

	@Test
	public void shouldReturnSuccessfulResponse() throws IOException, ServletException {
		servlet.init(config);
		servlet.doGet(request, response);
		verify(response).setContentType("application/json");
		verify(writer).write("{\n  \"builds\": []\n}");
	}

	@Test
	public void shouldReturnErrorResponseForServiceFailure() throws IOException, ServletException {
		servlet.init(config);
		when(service.builds()).thenThrow(new RuntimeException());
		servlet.doGet(request, response);
		verify(response).setContentType("text/plain");
		verify(response).sendError(anyInt(), any());
	}

	@Test
	public void shouldFailInitializationIfContextNotSet() {
		when(context.getAttribute(any())).thenReturn(null);
		assertException(ServletException.class, () -> servlet.init(config));
	}

	@Test
	public void shouldInitializeFromContext() throws ServletException {
		servlet.init(config);
	}

}
