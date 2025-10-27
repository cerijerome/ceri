package ceri.common.net;

import org.junit.Test;
import ceri.common.test.Assert;

public class UrlsTest {

	@Test
	public void testEncode() {
		Assert.equal(Urls.encode(null), "");
		Assert.equal(Urls.encode(""), "");
		Assert.equal(Urls.encode("a b&c"), "a+b%26c");
	}

	@Test
	public void testDecode() {
		Assert.equal(Urls.decode(null), "");
		Assert.equal(Urls.decode(""), "");
		Assert.equal(Urls.decode("a+b%26c"), "a b&c");
	}
}
