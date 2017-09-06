package ceri.ent.web;

import java.io.IOException;
import org.apache.http.HttpEntity;
import org.apache.http.client.fluent.Request;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Simple url requester. For more complex requests use httpclient.
 */
public class UrlRequester {
	private static final Logger logger = LogManager.getLogger();
	public static final UrlRequester DEFAULT = new UrlRequester(3);
	private final int retries;
	
	public UrlRequester(int retries) {
		this.retries = retries;
	}

	public byte[] getBytes(String url) throws IOException {
		return EntityUtils.toByteArray(getEntity(url));
	}

	public String getString(String url) throws IOException {
		return RequestUtil.toString(getEntity(url));
	}

	private HttpEntity getEntity(String url) throws IOException {
		IOException ex = null;
		for (int i = retries; i >= 0; i--) {
			try {
				return RequestUtil.contentEntity(Request.Get(url));
			} catch (IOException e) {
				logger.catching(Level.WARN, e);
				ex = e;
			}
		}
		throw new IOException("Failed calling " + url + " after " + (retries + 1) + " attempts", ex);
	}

}
