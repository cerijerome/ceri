package ceri.ent.server;

import ceri.common.property.Parser;
import ceri.common.text.Regex;
import jakarta.servlet.http.HttpServletRequest;

public class Request {
	private static final String USER_AGENT_HEADER = "User-Agent";
	private static final String XFF_HEADER = "X-Forwarded-For";
	public final HttpServletRequest http;

	public Request(HttpServletRequest http) {
		this.http = http;
	}

	public String userAgent() {
		return http.getHeader(USER_AGENT_HEADER);
	}

	public String remoteAddress() {
		String ipAddress = http.getHeader(XFF_HEADER);
		ipAddress = Regex.Split.COMMA.stream(ipAddress).next();
		if (ipAddress != null) return ipAddress;
		return http.getRemoteAddr();
	}

	public String param(String name) {
		return http.getParameter(name);
	}

	public Parser.String parse(String paramName) {
		return Parser.string(param(paramName));
	}
}
