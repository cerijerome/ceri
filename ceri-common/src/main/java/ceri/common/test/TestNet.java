package ceri.common.test;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import ceri.common.array.Array;
import ceri.common.collect.Lists;
import ceri.common.collect.Maps;
import ceri.common.concurrent.Concurrent;
import ceri.common.except.ExceptionAdapter;
import ceri.common.net.Http;
import ceri.common.reflect.Reflect;

/**
 * Network-based test support.
 */
public class TestNet {

	private TestNet() {}

	/**
	 * A test http client.
	 */
	public static class HttpClient extends java.net.http.HttpClient {
		public CookieHandler cookieHandler = null;
		public Duration connectTimeout = null;
		public Redirect redirect = Redirect.NEVER;
		public Version version = Version.HTTP_2;
		public final CallSync.Function<Send<?>, HttpResponse<?>> send =
			CallSync.function(null, httpResponse(null));

		public record Send<T>(HttpRequest request, HttpResponse.BodyHandler<T> bodyHandler,
			HttpResponse.PushPromiseHandler<T> ppHandler) {
			public HttpResponse<?> response(Object body) {
				return response(Http.Status.OK, body);
			}

			public HttpResponse<?> response(int statusCode, Object body) {
				return new HttpResponse<>().statusCode(statusCode).body(body).request(request());
			}
		}

		protected HttpClient() {}

		@Override
		public Optional<CookieHandler> cookieHandler() {
			return Optional.ofNullable(cookieHandler);
		}

		@Override
		public Optional<Duration> connectTimeout() {
			return Optional.ofNullable(connectTimeout);
		}

		@Override
		public Redirect followRedirects() {
			return redirect;
		}

		@Override
		public Optional<ProxySelector> proxy() {
			return Optional.empty();
		}

		@Override
		public SSLContext sslContext() {
			return ExceptionAdapter.runtime.get(SSLContext::getDefault);
		}

		@Override
		public SSLParameters sslParameters() {
			return sslContext().getDefaultSSLParameters();
		}

		@Override
		public Optional<Authenticator> authenticator() {
			return Optional.empty();
		}

		@Override
		public Version version() {
			return version;
		}

		@Override
		public Optional<Executor> executor() {
			return Optional.empty();
		}

		@Override
		public <T> HttpResponse<T> send(HttpRequest request,
			HttpResponse.BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
			return send(request, bodyHandler, null);
		}

		@Override
		public <T> CompletableFuture<java.net.http.HttpResponse<T>> sendAsync(HttpRequest request,
			HttpResponse.BodyHandler<T> bodyHandler) {
			return sendAsync(request, bodyHandler, null);
		}

		@Override
		public <T> CompletableFuture<java.net.http.HttpResponse<T>> sendAsync(HttpRequest request,
			HttpResponse.BodyHandler<T> bodyHandler, HttpResponse.PushPromiseHandler<T> ppHandler) {
			return Concurrent.completableFuture(() -> send(request, bodyHandler, ppHandler));
		}

		protected <T> HttpResponse<T> send(HttpRequest request,
			HttpResponse.BodyHandler<T> bodyHandler, HttpResponse.PushPromiseHandler<T> ppHandler)
			throws IOException, InterruptedException {
			var send = new Send<>(request, bodyHandler, ppHandler);
			var response = this.send.applyWithInterrupt(send, ExceptionAdapter.io);
			return Reflect.unchecked(response);
		}
	}

	/**
	 * A test http response.
	 */
	public static class HttpResponse<T> implements java.net.http.HttpResponse<T> {
		private int statusCode = Http.Status.OK;
		public final Map<String, List<String>> headers = Maps.of();
		public HttpClient.Version version = HttpClient.Version.HTTP_2;
		private T body = null;
		private HttpRequest request = null;
		private URI uri = null;

		public HttpResponse<T> header(String name, String... values) {
			headers.put(name, Lists.ofAll(values));
			return this;
		}

		public HttpResponse<T> statusCode(int statusCode) {
			this.statusCode = statusCode;
			return this;
		}

		public HttpResponse<T> body(T body) {
			this.body = body;
			return this;
		}

		public HttpResponse<T> request(HttpRequest request) {
			this.request = request;
			return uri(request.uri());
		}

		public HttpResponse<T> uri(URI uri) {
			this.uri = uri;
			return this;
		}

		@Override
		public int statusCode() {
			return statusCode;
		}

		@Override
		public HttpRequest request() {
			return request;
		}

		@Override
		public Optional<java.net.http.HttpResponse<T>> previousResponse() {
			return Optional.empty();
		}

		@Override
		public HttpHeaders headers() {
			return HttpHeaders.of(headers, (_, _) -> true);
		}

		@Override
		public T body() {
			return body;
		}

		@Override
		public Optional<SSLSession> sslSession() {
			return Optional.empty();
		}

		@Override
		public URI uri() {
			return uri;
		}

		@Override
		public HttpClient.Version version() {
			return version;
		}
	}

	public static HttpClient httpClient() {
		return new HttpClient();
	}

	public static <T> HttpResponse<T> httpResponse(int statusCode) {
		return new HttpResponse<T>().statusCode(statusCode);
	}

	public static <T> HttpResponse<T> httpResponse(T body) {
		return new HttpResponse<T>().body(body);
	}

	public static HttpResponse<byte[]> httpResponseBytes(int... bytes) {
		return httpResponse(Array.BYTE.of(bytes));
	}
}
