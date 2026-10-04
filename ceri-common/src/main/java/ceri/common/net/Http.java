package ceri.common.net;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.except.Exceptions;
import ceri.common.function.Closeables;
import ceri.common.function.Functions;
import ceri.common.math.Maths;

public class Http {

	private Http() {}

	/**
	 * Select headers not defined in core java.
	 */
	public static class Headers {
		// Requests and responses
		public static final String CACHE_CONTROL = "Cache-Control";
		public static final String CONTENT_LENGTH = "Content-Length";
		public static final String CONTENT_TYPE = "Content-Type";
		public static final String DATE = "Date";
		public static final String PRAGMA = "Pragma";
		public static final String VIA = "Via";
		public static final String WARNING = "Warning";
		// Requests
		public static final String ACCEPT = "Accept";
		public static final String ACCEPT_CHARSET = "Accept-Charset";
		public static final String ACCEPT_ENCODING = "Accept-Encoding";
		public static final String ACCEPT_LANGUAGE = "Accept-Language";
		public static final String AUTHORIZATION = "Authorization";
		public static final String CONNECTION = "Connection";
		public static final String COOKIE = "Cookie";
		public static final String EXPECT = "Expect";
		public static final String FROM = "From";
		public static final String HOST = "Host";
		public static final String IF_MATCH = "If-Match";
		public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
		public static final String IF_NONE_MATCH = "If-None-Match";
		public static final String IF_RANGE = "If-Range";
		public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
		public static final String LAST_EVENT_ID = "Last-Event-ID";
		public static final String MAX_FORWARDS = "Max-Forwards";
		public static final String ORIGIN = "Origin";
		public static final String PROXY_AUTHORIZATION = "Proxy-Authorization";
		public static final String RANGE = "Range";
		public static final String REFERER = "Referer";
		public static final String TE = "TE";
		public static final String UPGRADE = "Upgrade";
		public static final String USER_AGENT = "User-Agent";
		// Responses
		public static final String ACCEPT_RANGES = "Accept-Ranges";
		public static final String AGE = "Age";
		public static final String ALLOW = "Allow";
		public static final String CONTENT_DISPOSITION = "Content-Disposition";
		public static final String CONTENT_ENCODING = "Content-Encoding";
		public static final String CONTENT_LANGUAGE = "Content-Language";
		public static final String CONTENT_LOCATION = "Content-Location";
		public static final String CONTENT_MD5 = "Content-MD5";
		public static final String CONTENT_RANGE = "Content-Range";
		public static final String ETAG = "ETag";
		public static final String EXPIRES = "Expires";
		public static final String LAST_MODIFIED = "Last-Modified";
		public static final String LINK = "Link";
		public static final String LOCATION = "Location";
		public static final String PROXY_AUTHENTICATE = "Proxy-Authenticate";
		public static final String RETRY_AFTER = "Retry-After";
		public static final String SERVER = "Server";
		public static final String SET_COOKIE = "Set-Cookie";
		public static final String SET_COOKIE2 = "Set-Cookie2";
		public static final String TRAILER = "Trailer";
		public static final String TRANSFER_ENCODING = "Transfer-Encoding";
		public static final String VARY = "Vary";
		public static final String WWW_AUTHENTICATE = "WWW-Authenticate";

		private Headers() {}
	}

	/**
	 * Interface for a simple url downloader.
	 */
	public interface Download {
		/**
		 * Returns bytes from a get request.
		 */
		default byte[] bytes(String url) throws IOException {
			return bytes(Http.request(url).build());
		}

		/**
		 * Returns bytes from a request.
		 */
		default byte[] bytes(HttpRequest request) throws IOException {
			return execute(request, HttpResponse.BodyHandlers.ofByteArray());
		}

		/**
		 * Returns a string from a get request.
		 */
		default String string(String url) throws IOException {
			return string(url, StandardCharsets.UTF_8);
		}

		/**
		 * Returns a string from a get request.
		 */
		default String string(String url, Charset charset) throws IOException {
			return string(Http.request(url).build(), charset);
		}

		/**
		 * Returns a string from a request.
		 */
		default String string(HttpRequest request) throws IOException {
			return string(request, StandardCharsets.UTF_8);
		}

		/**
		 * Returns a string from a request.
		 */
		default String string(HttpRequest request, Charset charset) throws IOException {
			return execute(request, HttpResponse.BodyHandlers.ofString(charset));
		}

		<T> T execute(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws IOException;

		/**
		 * Extends the interface for closeable resources.
		 */
		interface Closeable extends Download, Functions.Closeable {}
	}

	/**
	 * A downloader using the java http client.
	 */
	private static class Downloader implements Download.Closeable {
		private static final int OK_STATUS_MIN = 200;
		private static final int OK_STATUS_MAX = 299;
		private static final Downloader DEFAULT =
			new Downloader(HttpClient.newHttpClient(), Config.DEFAULT);
		private final HttpClient client;
		private final Config config;

		public record Config(int attempts) {
			public static final Config DEFAULT = new Config(3);
		}

		private Downloader(HttpClient client, Config config) {
			this.client = client;
			this.config = config;
		}

		@Override
		public <T> T execute(HttpRequest request, BodyHandler<T> handler) throws IOException {
			IOException ex = null;
			for (int i = config.attempts(); i > 0; i--) {
				try {
					var response = client.send(request, handler);
					var code = response.statusCode();
					if (Maths.within(code, OK_STATUS_MIN, OK_STATUS_MAX)) return response.body();
					throw new IOException("Unexpected response code: " + code);
				} catch (InterruptedException e) {
					throw new RuntimeInterruptedException(e);
				} catch (IOException e) {
					if (ex == null) ex = e;
				}
			}
			throw Exceptions.io(ex, "%s failed after %d attempts", request.uri(),
				config.attempts());
		}

		@Override
		public void close() {
			Closeables.close(client);
		}
	}

	/**
	 * Returns the default downloader.
	 */
	public static Download downloader() {
		return Downloader.DEFAULT;
	}

	/**
	 * Creates a downloader.
	 */
	public static Download.Closeable downloader(HttpClient client, Downloader.Config config) {
		return new Downloader(client, config);
	}

	/**
	 * Starts building a request from a url string.
	 */
	public static HttpRequest.Builder request(String url) {
		return HttpRequest.newBuilder().uri(URI.create(url));
	}
}
