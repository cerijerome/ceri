package ceri.ci.proxy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.http.HttpStatus;
import ceri.common.collection.ImmutableUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.log.LogUtil;

public class MultiProxy {
	private static final Logger logger = LogManager.getLogger();
	private final ExecutorService executor;
	private final Collection<String> targets;

	public MultiProxy(int threads, String... targets) {
		this(threads, Arrays.asList(targets));
	}

	public MultiProxy(int threads, Collection<String> targets) {
		this.targets = ImmutableUtil.copyAsList(targets);
		executor = Executors.newFixedThreadPool(threads);
	}

	public void proxy(String uri) throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			Collection<Future<?>> futures = new ArrayList<>();
			for (String target : targets) {
				futures.add(execute(() -> {
					proxy(client, target, uri);
				}));
			}
			for (Future<?> future : futures)
				awaitFuture(future);
		}
	}

	private void proxy(CloseableHttpClient client, String target, String uri) {
		HttpGet httpGet = new HttpGet("http://" + target + "/" + uri);
		logger.debug("Request start: {}", httpGet.getURI());
		try (CloseableHttpResponse response = client.execute(httpGet)) {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.OK_200) throw new IOException(statusLine
				.toString());
			HttpEntity entity = response.getEntity();
			logger.debug("Response: {}", LogUtil.toString(() -> EntityUtils.toString(entity)));
			EntityUtils.consume(entity);
		} catch (IOException e) {
			logger.catching(e);
		}
		logger.debug("Request complete: {}", httpGet.getURI());
	}

	private Future<?> execute(final Runnable runnable) {
		if (executor.isShutdown()) throw new RuntimeInterruptedException("Executor is shut down");
		return executor.submit(() -> {
			logger.debug("Thread started");
			try {
				runnable.run();
			} catch (RuntimeInterruptedException e) {
				logger.info("Thread interrupted");
			} catch (RuntimeException e) {
				logger.catching(e);
			}
			logger.debug("Thread complete");
		});
	}

	private void awaitFuture(Future<?> future) {
		if (future == null) return;
		try {
			future.get();
		} catch (InterruptedException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeInterruptedException(e);
		} catch (ExecutionException e) {
			logger.throwing(Level.DEBUG, e);
			throw new RuntimeException(e.getCause());
		}
	}

}
