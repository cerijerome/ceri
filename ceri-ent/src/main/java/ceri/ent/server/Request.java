package ceri.ent.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;
import ceri.common.text.StringUtil;
import ceri.common.util.PrimitiveUtil;

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
		if (ipAddress != null) ipAddress = StringUtil.commaSplit(ipAddress).get(0);
		if (ipAddress != null) return ipAddress;
		return http.getRemoteAddr();
	}

	public String stringValue(String name) {
		return http.getParameter(name);
	}

	public <T> T value(String name, Function<String, T> fromStringFn) {
		String value = http.getParameter(name);
		if (value == null) return null;
		return fromStringFn.apply(value);
	}

	public <T> T value(String name, T def, Function<String, T> fromStringFn) {
		try {
			return value(name, fromStringFn);
		} catch (RuntimeException e) {
			return def;
		}
	}

	public <T> List<T> commaValues(String name, Function<String, T> fromStringFn) {
		List<String> values = commaValues(name);
		if (values.isEmpty()) return Collections.emptyList();
		List<T> ts = new ArrayList<>();
		for (String value : values) {
			T t = fromStringFn.apply(value);
			if (t != null) ts.add(t);
		}
		return ts;
	}

	public List<String> commaValues(String name) {
		String value = http.getParameter(name);
		if (value == null) return Collections.emptyList();
		return StringUtil.commaSplit(value);
	}

	public Double doubleValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Double) null);
	}

	public Float floatValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Float) null);
	}

	public Long longValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Long) null);
	}

	public Integer intValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Integer) null);
	}

	public Short shortValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Short) null);
	}

	public Byte byteValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Byte) null);
	}

	public Character charValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Character) null);
	}

	public Boolean booleanValue(String name) {
		String value = http.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Boolean) null);
	}

}
