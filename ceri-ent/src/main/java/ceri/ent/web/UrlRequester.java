package ceri.ent.web;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;

public class UrlRequester {
	private static final Logger logger = LogManager.getLogger();
	public static final UrlRequester DEFAULT = new UrlRequester(3);
	private final int retries;
	
	public UrlRequester(int retries) {
		this.retries = retries;
	}

	public byte[] get(String url) throws IOException {
		IOException ex = null;
		for (int i = retries; i >= 0; i--) {
			try {
				Response response = Request.Get(url).execute();
				HttpResponse httpResponse = response.returnResponse();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) throw new IOException(
					"Unexpected http response: " + statusCode);
				try (InputStream in = httpResponse.getEntity().getContent()) {
					return IoUtil.getContent(in, 0);
				}
			} catch (IOException e) {
				logger.catching(Level.WARN, e);
				ex = e;
			}
		}
		throw new IOException("Failed calling " + url + " after " + (retries + 1) + " attempts", ex);
	}

}
