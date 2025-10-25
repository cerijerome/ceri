package ceri.common.net;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;

public class UrlsTest {

	@Test
	public void testEncode() {
		assertEquals(Urls.encode(null), "");
		assertEquals(Urls.encode(""), "");
		assertEquals(Urls.encode("a b&c"), "a+b%26c");
	}

	@Test
	public void testDecode() {
		assertEquals(Urls.decode(null), "");
		assertEquals(Urls.decode(""), "");
		assertEquals(Urls.decode("a+b%26c"), "a b&c");
	}
}
