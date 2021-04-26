package ceri.ent.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.util.EntityUtils;

/**
 * Utility methods for fluent http client requests.
 */
public class RequestUtil {

	private RequestUtil() {}

	public static Request request(String method, String url) {
		return switch (method) {
			case HttpGet.METHOD_NAME -> Request.Get(url);
			case HttpPost.METHOD_NAME -> Request.Post(url);
			case HttpPut.METHOD_NAME -> Request.Put(url);
			case HttpDelete.METHOD_NAME -> Request.Delete(url);
			default -> throw new IllegalArgumentException("Unsupported method: " + method);
		};
	}

	public static byte[] contentBytes(Request request) throws IOException {
		return EntityUtils.toByteArray(contentEntity(request));
	}

	public static String content(Request request) throws IOException {
		return toString(contentEntity(request));
	}

	public static String toString(HttpEntity entity) throws IOException {
		return EntityUtils.toString(entity, StandardCharsets.UTF_8);
	}

	public static HttpEntity contentEntity(Request request) throws IOException {
		Response response = request.execute();
		HttpResponse httpResponse = response.returnResponse();
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		if (statusCode != HttpStatus.SC_OK) throw new IOException( //
			"Unexpected http response: " + statusCode);
		return httpResponse.getEntity();
	}

}
