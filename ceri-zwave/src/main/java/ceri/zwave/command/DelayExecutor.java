package ceri.zwave.command;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

/**
 * Makes sure a delay occurs before sending a command. Thread safe.
 */
public class DelayExecutor implements Executor {
	private static final Logger logger = LogManager.getLogger();
	private static final int DELAY_DEF = 300;
	private static final int RETRIES_DEF = 2;
	private static final int RETRY_DELAY_DEF = 100;
	private final int delay;
	private final Lock lock = new ReentrantLock();
	private final int retries;
	private final int retryDelay;
	private long lastRun;

	public DelayExecutor() {
		this(DELAY_DEF);
	}
	
	public DelayExecutor(int delay) {
		this(delay, RETRIES_DEF, RETRY_DELAY_DEF);
	}

	public DelayExecutor(int delay, int retries, int retryDelay) {
		this.delay = delay;
		if (retries < 0) throw new IllegalArgumentException("retries must be >= 0: " + retries);
		this.retries = retries;
		this.retryDelay = retryDelay;
		resetDelay();
	}

	@Override
	public String execute(String url) throws IOException {
		lock.lock();
		try {
			return doExecute(url);
		} finally {
			lock.unlock();
		}
	}

	private String doExecute(String url) throws IOException {
		logger.info("Executing {}", url);
		waitForDelay();
		logger.debug("Waited {}ms", System.currentTimeMillis() - lastRun);
		String content = requestContent(url);
		resetDelay();
		return content;
	}

	private String requestContent(String url) throws IOException {
		IOException ex = null;
		for (int i = retries; i >= 0; i--) {
			try {
				Response response = Request.Get(url).execute();
				HttpResponse httpResponse = response.returnResponse();
				int statusCode = httpResponse.getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) throw new IOException(
					"Unexpected http response: " + statusCode);
				return IoUtil.getContentString(httpResponse.getEntity().getContent(), 0);
			} catch (IOException e) {
				logger.catching(Level.WARN, e);
				ex = e;
				if (i > 0) BasicUtil.delay(retryDelay);
			}
		}
		throw new IOException("Failed calling " + url + " after " + (retries + 1) + " attempts", ex);
	}
	
	private void waitForDelay() {
		long t = System.currentTimeMillis() - lastRun;
		if (t >= delay) return;
		BasicUtil.delay(delay - (int) t);
	}

	private void resetDelay() {
		lastRun = System.currentTimeMillis();
	}

}
