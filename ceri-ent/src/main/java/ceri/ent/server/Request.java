package ceri.ent.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import ceri.common.factory.Factory;
import ceri.common.util.PrimitiveUtil;
import ceri.common.util.StringUtil;

public class Request {
	private final HttpServletRequest request;

	public Request(HttpServletRequest request) {
		this.request = request;
	}

	public String stringValue(String name) {
		return request.getParameter(name);
	}
	
	public <T> T value(String name, Factory<T, String> factory) {
		String value = request.getParameter(name);
		if (value == null) return null;
		return factory.create(value);
	}
	
	public <T> T value(String name, T def, Factory<T, String> factory) {
		try {
			return value(name, factory);
		} catch (RuntimeException e) {
			return def;
		}
	}
	
	public <T> List<T> commaValues(String name, Factory<T, String> factory) {
		List<String> values = commaValues(name);
		if (values.isEmpty()) return Collections.emptyList();
		List<T> ts = new ArrayList<>();
		for (String value : values) {
			T t = factory.create(value);
			if (t != null) ts.add(t);
		}
		return ts;
	}
	
	public List<String> commaValues(String name) {
		String value = request.getParameter(name);
		if (value == null) return Collections.emptyList();
		return StringUtil.commaSplit(value);
	}
	
	public Double doubleValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Double) null);
	}

	public Float floatValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Float) null);
	}

	public Long longValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Long) null);
	}

	public Integer intValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Integer) null);
	}

	public Short shortValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Short) null);
	}

	public Byte byteValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Byte) null);
	}

	public Character charValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Character) null);
	}

	public Boolean booleanValue(String name) {
		String value = request.getParameter(name);
		return PrimitiveUtil.valueOf(value, (Boolean) null);
	}

}
