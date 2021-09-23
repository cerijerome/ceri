package ceri.ci.admin;

import static ceri.common.test.AssertUtil.assertThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.io.PrintWriter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.alert.AlertService;
import ceri.ci.build.Builds;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AdminServletBehavior {
	@Mock
	private HttpServletRequest request;
	@Mock
	private HttpServletResponse response;
	@Mock
	private ServletContext context;
	@Mock
	private ServletConfig config;
	@Mock
	private AlertService service;
	@Mock
	private PrintWriter writer;
	private AdminServlet servlet;

	@SuppressWarnings("resource")
	@Before
	public void init() throws Exception {
		MockitoAnnotations.openMocks(this).close();
		servlet = new AdminServlet();
		when(config.getServletContext()).thenReturn(context);
		when(context.getAttribute("ceri.ci.alert.AlertService")).thenReturn(service);
		when(request.getParameter("view")).thenReturn("");
		when(service.builds()).thenReturn(new Builds());
		when(response.getWriter()).thenReturn(writer);
	}

	@SuppressWarnings("resource")
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
		assertThrown(ServletException.class, () -> servlet.init(config));
	}

	@Test
	public void shouldInitializeFromContext() throws ServletException {
		servlet.init(config);
	}

}
