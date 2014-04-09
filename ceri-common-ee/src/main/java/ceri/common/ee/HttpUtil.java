package ceri.common.ee;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.DefaultHttpRequestFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.CollectionUtil;

public class HttpUtil {
	private static final Logger logger = LogManager.getLogger();

	private HttpUtil() {}

	public static String httpGetString(CloseableHttpClient client, String url)
		throws IOException {
		HttpGet httpGet = new HttpGet(url);
		try (CloseableHttpResponse response = client.execute(httpGet)) {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) throw new IOException(statusLine
				.toString());
			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			EntityUtils.consume(entity);
			return content;
		}
	}

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
