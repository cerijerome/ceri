package ceri.ent.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;

/**
 * Utility methods for fluent http client requests.
 */
public class RequestUtil {

	private RequestUtil() {}

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
