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

	public static class Status {
		// Informational
		public static final int CONTINUE = 100;
		public static final int SWITCHING_PROTOCOLS = 101;
		public static final int EARLY_HINTS = 103;
		// Successful
		public static final int OK = 200;
		public static final int CREATED = 201;
		public static final int ACCEPTED = 202;
		public static final int NON_AUTHORITATIVE_INFORMATION = 203;
		public static final int NO_CONTENT = 204;
		public static final int RESET_CONTENT = 205;
		public static final int PARTIAL_CONTENT = 206;
		public static final int MULTI_STATUS = 207;
		public static final int ALREADY_REPORTED = 208;
		public static final int IM_USED = 226;
		// Redirection
		public static final int MULTIPLE_CHOICES = 300;
		public static final int MOVED_PERMANENTLY = 301;
		public static final int FOUND = 302;
		public static final int SEE_OTHER = 303;
		public static final int NOT_MODIFIED = 304;
		public static final int TEMPORARY_REDIRECT = 307;
		public static final int PERMANENT_REDIRECT = 308;
		// Client error
		public static final int BAD_REQUEST = 400;
		public static final int UNAUTHORIZED = 401;
		public static final int PAYMENT_REQUIRED = 402;
		public static final int FORBIDDEN = 403;
		public static final int NOT_FOUND = 404;
		public static final int METHOD_NOT_ALLOWED = 405;
		public static final int NOT_ACCEPTABLE = 406;
		public static final int PROXY_AUTHENTICATION_REQUIRED = 407;
		public static final int REQUEST_TIMEOUT = 408;
		public static final int CONFLICT = 409;
		public static final int GONE = 410;
		public static final int LENGTH_REQUIRED = 411;
		public static final int PRECONDITION_FAILED = 412;
		public static final int CONTENT_TOO_LARGE = 413;
		public static final int URI_TOO_LONG = 414;
		public static final int UNSUPPORTED_MEDIA_TYPE = 415;
		public static final int RANGE_NOT_SATISFIABLE = 416;
		public static final int EXPECTATION_FAILED = 417;
		public static final int IM_A_TEAPOT = 418;
		public static final int MISDIRECTED_REQUEST = 421;
		public static final int UNPROCESSABLE_CONTENT = 422;
		public static final int LOCKED = 423;
		public static final int FAILED_DEPENDENCY = 424;
		public static final int TOO_EARLY = 425;
		public static final int UPGRADE_REQUIRED = 426;
		public static final int PRECONDITION_REQUIRED = 428;
		public static final int TOO_MANY_REQUESTS = 429;
		public static final int REQUEST_HEADER_FIELDS_TOO_LARGE = 431;
		public static final int UNAVAILABLE_FOR_LEGAL_REASONS = 451;
		// Server error
		public static final int INTERNAL_SERVER_ERROR = 500;
		public static final int NOT_IMPLEMENTED = 501;
		public static final int BAD_GATEWAY = 502;
		public static final int SERVICE_UNAVAILABLE = 503;
		public static final int GATEWAY_TIMEOUT = 504;
		public static final int HTTP_VERSION_NOT_SUPPORTED = 505;
		public static final int VARIANT_ALSO_NEGOTIATES = 506;
		public static final int INSUFFICIENT_STORAGE = 507;
		public static final int LOOP_DETECTED = 508;
		public static final int NOT_EXTENDED = 510;
		public static final int NETWORK_AUTHENTICATION_REQUIRED = 511;

		private Status() {}

		public enum Group {
			none(0, 0),
			informational(100, 199),
			successful(200, 299),
			redirection(300, 399),
			clientError(400, 499),
			serverError(500, 599);

			public final int min;
			public final int max;

			private Group(int min, int max) {
				this.min = min;
				this.max = max;
			}

			public boolean known() {
				return this != none;
			}

			public boolean has(int code) {
				return Maths.within(code, min, max);
			}
		}

		public static Group group(int code) {
			return switch (code / 100) {
				case 1 -> Group.informational;
				case 2 -> Group.successful;
				case 3 -> Group.redirection;
				case 4 -> Group.clientError;
				case 5 -> Group.serverError;
				default -> Group.none;
			};
		}

		public static boolean success(int code) {
			return Group.successful.has(code);
		}
	}

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
		public static final String X_FORWARDED_FOR = "X-Forwarded-For";
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
	@FunctionalInterface
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
			return send(request, HttpResponse.BodyHandlers.ofByteArray());
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
			return send(request, HttpResponse.BodyHandlers.ofString(charset));
		}

		<T> T send(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws IOException;

		/**
		 * Extends the interface for closeable resources.
		 */
		interface Closeable extends Download, Functions.Closeable {}
	}

	/**
	 * A downloader using the java http client.
	 */
	public static class Downloader implements Download.Closeable {
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
		public <T> T send(HttpRequest request, BodyHandler<T> handler) throws IOException {
			IOException ex = null;
			for (int i = config.attempts(); i > 0; i--) {
				try {
					var response = client.send(request, handler);
					var code = response.statusCode();
					if (Status.success(code)) return response.body();
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
			if (this != DEFAULT) Closeables.close(client);
		}
	}

	/**
	 * Returns the default downloader.
	 */
	public static Download downloader() {
		return Downloader.DEFAULT;
	}

	/**
	 * Creates a downloader. The downloader closes the client when closed.
	 */
	public static Downloader downloader(HttpClient client) {
		return downloader(client, Downloader.Config.DEFAULT);
	}

	/**
	 * Creates a downloader. The downloader closes the client when closed.
	 */
	public static Downloader downloader(HttpClient client, Downloader.Config config) {
		return new Downloader(client, config);
	}

	/**
	 * Starts building a request from a url string.
	 */
	public static HttpRequest.Builder request(String url) {
		return HttpRequest.newBuilder().uri(URI.create(url));
	}
}
