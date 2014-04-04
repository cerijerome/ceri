package ceri.common.ee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.message.BasicHeader;
import ceri.common.collection.CollectionUtil;

public class HttpUtil {

	private HttpUtil() {}

	public static Collection<Header> headers(HttpServletRequest request) {
		Collection<Header> headers = new ArrayList<>();
		for (String name : CollectionUtil.iterable(request.getHeaderNames()))
			for (String value : CollectionUtil.iterable(request.getHeaders(name)))
				headers.add(new BasicHeader(name, value));
		return headers;
	}

	public static String requestUri(HttpServletRequest request) {
		String path = request.getRequestURI();
		String query = request.getQueryString();
		return query == null ? path : path + '?' + query;
	}

	public static HttpRequest convert(HttpServletRequest request) throws IOException {
		try {
			HttpRequest httpRequest =
				DefaultHttpRequestFactory.INSTANCE.newHttpRequest(request.getMethod(), request
					.getRequestURI());
			for (String name : CollectionUtil.iterable(request.getHeaderNames()))
				for (String value : CollectionUtil.iterable(request.getHeaders(name)))
					httpRequest.addHeader(name, value);
			return httpRequest;
		} catch (MethodNotSupportedException e) {
			throw new IOException(e);
		}
	}

}
