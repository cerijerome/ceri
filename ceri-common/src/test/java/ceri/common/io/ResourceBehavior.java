package ceri.common.io;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

public class ResourceBehavior {
	private static final String PROPERTIES = ResourceBehavior.class.getSimpleName() + ".properties";

	@Test
	public void shouldProvideClassUrl() {
		assertNull(Resource.url(null));
		var url = Resource.url(String.class);
		assertTrue(url.getPath().endsWith("/java/lang/String.class"));
		url = Resource.url(Test.class);
		assertTrue(url.getPath().endsWith("/org/junit/Test.class"));
		url = Resource.url(Resource.class);
		assertTrue(url.getPath().endsWith("/ceri/common/io/Resource.class"));
	}

	@Test
	public void shouldReadBytes() throws IOException {
		byte[] content = Resource.bytes(getClass(), PROPERTIES);
		assertArray(content, 'a', '=', 'b');
		content = Resource.bytes(String.class, "String.class"); // module
		assertTrue(content.length > 0);
		content = Resource.bytes(Test.class, "Test.class"); // jar
		assertTrue(content.length > 0);
	}

	@Test
	public void shouldReadString() throws IOException {
		var s = Resource.string(getClass(), PROPERTIES);
		assertEquals(s, "a=b");
	}

	@Test
	public void shouldProvideResourceRelativeToRoot() throws IOException {
		try (var r = Resource.root(String.class)) {
			var names = Paths.listNames(r.path());
			assertTrue(names.contains("java"));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAccessFile() throws IOException {
		assertNull(Resource.of(null));
		try (var r = Resource.of(getClass(), "res", "test")) {
			assertUnordered(Paths.listNames(r.path()), "A.txt", "BB.txt", "CCC.txt");
			assertEquals(Files.readString(r.resolve("A.txt")), "aaa");
		}
		try (var r = Resource.of(getClass(), "res", "test", "BB.txt")) {
			assertEquals(r.string(), "bb");
		}
	}

	@Test
	public void shouldAccessJar() throws IOException {
		assertThrown(() -> Resource.of(Test.class, "\0"));
		try (var r = Resource.of(Test.class, "runner")) {
			var names = Paths.listNames(r.path());
			assertTrue(names.contains("Runner.class"));
		}
	}

	@Test
	public void shouldAccessModule() throws IOException {
		try (var r = Resource.of(String.class, "ref")) {
			var names = Paths.listNames(r.path());
			assertTrue(names.contains("Finalizer.class"));
		}
	}

	@Test
	public void shouldProvideString() throws IOException {
		try (var r = Resource.of(getClass(), "res", "test")) {
			assertEquals(r.toString(), r.path().toString());
		}
	}
}
