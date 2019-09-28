package ceri.common.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.net.URL;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class ResourceListerBehavior {

	@Test
	public void shouldFindClassResource() throws IOException {
		ResourceLister r = ResourceLister.of(getClass(), "");
		assertTrue(r.list().contains(getClass().getSimpleName() + ".class"));
	}

	@Test
	public void shouldFindResourceInAbsolutePath() throws IOException {
		ResourceLister r = ResourceLister.of(getClass(), "/");
		// r.list().forEach(System.out::println);
		assertFalse(r.list().contains(getClass().getSimpleName() + ".class"));
	}

	@Test
	public void shouldReturnEmptyForNullUrl() throws IOException {
		assertTrue(ResourceLister.list(null, "", null).isEmpty());
	}

	@Test
	public void shouldProcessJrtUrl() throws IOException {
		URL url = new URL("jrt:/test");
		assertTrue(ResourceLister.list(url, "", null).isEmpty());
	}

	@Test
	public void shouldFailOnUnsupportedProtocol() throws IOException {
		URL url = new URL("http://test");
		TestUtil.assertThrown(() -> ResourceLister.list(url, "", null));
	}

	// @Test
	// public void shouldFailOnInvalidUri() throws IOException {
	// URL url = new URL("jar:http://hi");
	// assertException(FileSystemNotFoundException.class, () -> ResourceLister.list(url, "", ALL));
	// }

}
