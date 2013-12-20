package ceri.zwave.command;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.apache.http.HttpStatus;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import ceri.common.util.BasicUtil;

/**
 * Makes sure a delay occurs before sending a command. Thread safe.
 */
public class DelayExecutor implements Executor {
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

	public String doExecute(String url) throws IOException {
		System.out.println(url);
		waitForDelay();
		System.out.println(System.currentTimeMillis() - lastRun);
		Content response = requestContent(url);
		resetDelay();
		String content = response.asString();
		return content;
	}

	private Content requestContent(String url) throws IOException {
		IOException ex = null;
		for (int i = retries; i >= 0; i--) {
			try {
				Response response = Request.Get(url).execute();
				int statusCode = response.returnResponse().getStatusLine().getStatusCode();
				if (statusCode != HttpStatus.SC_OK) throw new IOException(
					"Unexpected http response: " + statusCode);
				return response.returnContent();
			} catch (IOException e) {
				e.printStackTrace();
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
