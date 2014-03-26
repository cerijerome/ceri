package ceri.ci.servlet;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ceri.ci.alert.AlertService;
import ceri.common.util.StringUtil;


/**
 * Handles the following requests:
 * 
 * <pre>
 * /
 * /?clear
 * /?delete
 * /build
 * /build?clear
 * /build?delete
 * /build/job
 * /build/job?clear
 * /build/job?delete
 * /build/job?fixed&names=...
 * /build/job?broken&names=...
 * </pre>
 */
@javax.servlet.annotation.WebServlet(name = "alertServlet", urlPatterns = { "/alert/*" })
public class AlertServlet extends HttpServlet {
	private static final long serialVersionUID = 749914795926651883L;
	private static final String SUCCESS = "SUCCESS";
	private static final String ERROR = "ERROR";
	private static final Pattern PATH_SPLIT = Pattern.compile("/+([^/]+)");
	private static final String NAMES = "names";
	private AlertService service;

	@Override
	public void init() {
		service = InitServlet.get(getServletContext(), AlertService.class);
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException {
		String responseContent;
		try {
			AlertParams params = readParams(request);
			Object obj = execute(params);
			responseContent = successResponse(obj);
		} catch (Exception e) {
			responseContent = errorResponse(e);
		}
		response.getWriter().write(responseContent);
	}

	private Object execute(AlertParams params) {
		switch (params.action) {
		case read:
			break;
		case delete:
			service.delete(params.build, params.job);
			break;
		case clear:
			service.clear(params.build, params.job);
			break;
		case broken:
			service.broken(params.build, params.job, params.names);
			break;
		case fixed:
			service.fixed(params.build, params.job, params.names);
			break;
		}
		if (params.build == null) return service.builds();
		if (params.job == null) return service.build(params.build);
		return service.job(params.build, params.job);
	}

	private String errorResponse(Exception e) {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			out.println(ERROR);
			e.printStackTrace(out);
		}
		return b.toString();
	}

	private String successResponse(Object obj) {
		StringBuilder b = new StringBuilder();
		try (PrintStream out = StringUtil.asPrintStream(b)) {
			out.println(SUCCESS);
			out.println(obj);
		}
		return b.toString();
	}

	private AlertParams readParams(HttpServletRequest request) {
		String path = request.getServletPath();
		String build = null;
		String job = null;
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) pathInfo = "";
		Matcher m = PATH_SPLIT.matcher(pathInfo);
		if (m.find()) build = m.group(1);
		if (m.find()) job = m.group(1);
		System.out.println(path);
		System.out.println(build);
		System.out.println(job);
		switch (actionFromRequest(request)) {
		case read:
			return AlertParams.read(path, build, job);
		case clear:
			return AlertParams.clear(path, build, job);
		case delete:
			return AlertParams.delete(path, build, job);
		case fixed:
			return AlertParams.fixed(path, build, job, names(request));
		case broken:
			return AlertParams.broken(path, build, job, names(request));
		}
		throw new AssertionError("Should not happen");
	}

	private AlertAction actionFromRequest(HttpServletRequest request) {
		for (AlertAction action : AlertAction.values())
			if (request.getParameter(action.name()) != null) return action;
		return AlertAction.read;
	}

	private Collection<String> names(HttpServletRequest request) {
		String[] values = request.getParameterValues(NAMES);
		if (values == null) return Collections.emptySet();
		Collection<String> names = new LinkedHashSet<>();
		for (String value : values)
			names.addAll(StringUtil.commaSplit(value));
		return names;
	}

}
