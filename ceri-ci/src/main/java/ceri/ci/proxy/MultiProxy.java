package ceri.ci.proxy;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.common.HttpUtil;
import ceri.ci.common.LoggingExecutor;
import ceri.common.collection.ImmutableUtil;

public class MultiProxy implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private final LoggingExecutor executor;
	private final Collection<String> targets;

	public static void main(String[] args) throws Exception {
		try (MultiProxy proxy = new MultiProxy(10, Arrays.asList("www.bbc.com", "www.yahoo.com"))) {
			proxy.proxy("news");
		}
	}

	public MultiProxy(int threads, String... targets) {
		this(threads, Arrays.asList(targets));
	}

	public MultiProxy(int threads, Collection<String> targets) {
		this.targets = ImmutableUtil.copyAsList(targets);
		executor = new LoggingExecutor(Executors.newFixedThreadPool(threads));
	}

	@Override
	public void close() throws IOException {
		executor.close();
	}

	public void proxy(String uri) throws IOException {
		try (CloseableHttpClient client = HttpClients.createDefault()) {
			for (String target : targets) {
				executor.execute(() -> {
					proxy(client, target, uri);
				});
			}
			executor.awaitCompletion();
		}
	}

	private void proxy(CloseableHttpClient client, String target, String uri) {
		String url = "http://" + target + "/" + uri;
		logger.debug("Request start: {}", url);
		try {
			HttpUtil.httpGetString(client, url);
		} catch (IOException e) {
			logger.catching(e);
		}
		logger.debug("Request complete: {}", url);
	}

}
