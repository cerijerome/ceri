package ceri.common.test;

import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import javax.net.ssl.SSLContext;
import org.junit.After;
import org.junit.Test;
import ceri.common.net.Http;

public class TestNetTest {
	private TestNet.HttpClient client;

	@After
	public void after() {
		client = Testing.close(client);
	}

	@Test
	public void testHttpClientProperties() throws NoSuchAlgorithmException {
		client = TestNet.httpClient();
		client.connectTimeout = Duration.ZERO;
		Assert.optional(client.cookieHandler(), null);
		Assert.optional(client.connectTimeout(), Duration.ZERO);
		Assert.equal(client.followRedirects(), HttpClient.Redirect.NEVER);
		Assert.optional(client.proxy(), null);
		Assert.equal(client.sslContext(), SSLContext.getDefault());
		Assert.equal(client.sslParameters().getMaximumPacketSize(),
			SSLContext.getDefault().getDefaultSSLParameters().getMaximumPacketSize());
		Assert.optional(client.authenticator(), null);
		Assert.equal(client.version(), HttpClient.Version.HTTP_2);
		Assert.optional(client.executor(), null);
	}

	@Test
	public void testHttpClientSendAsync() throws InterruptedException, ExecutionException {
		client = TestNet.httpClient();
		client.send.autoResponse(s -> s.response(123));
		var response = client.sendAsync(Http.request("http://test").build(), //
			HttpResponse.BodyHandlers.ofString()).get();
		Assert.string(response.uri(), "http://test");
		Assert.string(response.body(), "123");
	}

	@Test
	public void testHttpResponse() {
		var request = Http.request("http://test").build();
		var response = TestNet.httpResponse("abc").statusCode(Http.Status.BAD_GATEWAY)
			.header(Http.Headers.LINK, "link1", "link2").request(request);
		Assert.equal(response.request(), request);
		Assert.string(response.uri(), "http://test");
		Assert.optional(response.previousResponse(), null);
		Assert.ordered(response.headers().allValues(Http.Headers.LINK), "link1", "link2");
		Assert.optional(response.sslSession(), null);
		Assert.equal(response.version(), HttpClient.Version.HTTP_2);
	}
}
