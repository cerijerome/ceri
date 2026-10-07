package ceri.common.net;

import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.TestNet;
import ceri.common.test.Testing;

public class HttpTest {
	private TestNet.HttpClient client = null;
	private Http.Downloader downloader = null;

	@After
	public void after() {
		downloader = Testing.close(downloader);
		client = Testing.close(client);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Http.class);
	}

	@Test
	public void testKnownStatusGroup() {
		Assert.equal(Http.Status.Group.none.known(), false);
		Assert.equal(Http.Status.Group.successful.known(), true);
	}

	@Test
	public void testGroupFromStatus() {
		Assert.equal(Http.Status.group(99), Http.Status.Group.none);
		Assert.equal(Http.Status.group(100), Http.Status.Group.informational);
		Assert.equal(Http.Status.group(199), Http.Status.Group.informational);
		Assert.equal(Http.Status.group(200), Http.Status.Group.successful);
		Assert.equal(Http.Status.group(299), Http.Status.Group.successful);
		Assert.equal(Http.Status.group(300), Http.Status.Group.redirection);
		Assert.equal(Http.Status.group(399), Http.Status.Group.redirection);
		Assert.equal(Http.Status.group(400), Http.Status.Group.clientError);
		Assert.equal(Http.Status.group(499), Http.Status.Group.clientError);
		Assert.equal(Http.Status.group(500), Http.Status.Group.serverError);
		Assert.equal(Http.Status.group(599), Http.Status.Group.serverError);
		Assert.equal(Http.Status.group(699), Http.Status.Group.none);
	}

	@Test
	public void testStatusSuccess() {
		Assert.equal(Http.Status.success(0), false);
		Assert.equal(Http.Status.success(299), true);
		Assert.equal(Http.Status.success(300), false);
	}

	@Test
	public void testDownloader() throws IOException {
		client = TestNet.httpClient();
		client.send.autoResponses(TestNet.httpResponseBytes(1, -1, 0),
			TestNet.httpResponse("test"));
		downloader = Http.downloader(client);
		Assert.array(downloader.bytes("http://test"), 1, -1, 0);
		Assert.equal(downloader.string("http://test"), "test");
		Assert.equal(downloader.string(Http.request("http://test").build()), "test");
	}

	@Test
	public void testDownloaderErrors() {
		client = TestNet.httpClient();
		client.send.autoResponses(TestNet.httpResponse(Http.Status.NOT_FOUND));
		client.send.error.setFrom(null, null, ErrorGen.INX);
		downloader = Http.downloader(client, new Http.Downloader.Config(2));
		Assert.io(() -> downloader.string("http://test"));
		Assert.thrown(RuntimeInterruptedException.class, () -> downloader.string("http://test"));
	}

	@Test
	public void testCloseDownloader() {
		((Functions.Closeable) Http.downloader()).close();
	}
}
