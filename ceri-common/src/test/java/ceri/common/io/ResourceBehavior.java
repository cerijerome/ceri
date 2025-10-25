package ceri.common.io;

import static ceri.common.test.Assert.assertArray;
import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertUnordered;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;
import ceri.common.test.Assert;

public class ResourceBehavior {
	private static final String PROPERTIES = ResourceBehavior.class.getSimpleName() + ".properties";

	@Test
	public void shouldProvideClassUrl() {
		assertEquals(Resource.url(null), null);
		var url = Resource.url(String.class);
		assertEquals(url.getPath().endsWith("/java/lang/String.class"), true);
		url = Resource.url(Test.class);
		assertEquals(url.getPath().endsWith("/org/junit/Test.class"), true);
		url = Resource.url(Resource.class);
		assertEquals(url.getPath().endsWith("/ceri/common/io/Resource.class"), true);
	}

	@Test
	public void shouldReadBytes() throws IOException {
		byte[] content = Resource.bytes(getClass(), PROPERTIES);
		assertArray(content, 'a', '=', 'b');
		content = Resource.bytes(String.class, "String.class"); // module
		assertEquals(content.length > 0, true);
		content = Resource.bytes(Test.class, "Test.class"); // jar
		assertEquals(content.length > 0, true);
	}

	@Test
	public void shouldReadString() throws IOException {
		var s = Resource.string(getClass(), PROPERTIES);
		assertEquals(s, "a=b");
	}

	@Test
	public void shouldProvideResourceRelativeToRoot() throws IOException {
		try (var r = Resource.root(String.class)) {
			var names = PathList.of(r.path()).names();
			assertEquals(names.contains("java"), true);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAccessFile() throws IOException {
		assertEquals(Resource.of(null), null);
		try (var r = Resource.of(getClass(), "res", "test")) {
			assertUnordered(PathList.of(r.path()).names(), "A.txt", "BB.txt", "CCC.txt");
			assertEquals(Files.readString(r.resolve("A.txt")), "aaa");
		}
		try (var r = Resource.of(getClass(), "res", "test", "BB.txt")) {
			assertEquals(r.string(), "bb");
		}
	}

	@Test
	public void shouldAccessJar() throws IOException {
		Assert.thrown(() -> Resource.of(Test.class, "\0"));
		try (var r = Resource.of(Test.class, "runner")) {
			var names = PathList.of(r.path()).names();
			assertEquals(names.contains("Runner.class"), true);
		}
	}

	@Test
	public void shouldAccessModule() throws IOException {
		try (var r = Resource.of(String.class, "ref")) {
			var names = PathList.of(r.path()).names();
			assertEquals(names.contains("Finalizer.class"), true);
		}
	}

	@Test
	public void shouldProvideString() throws IOException {
		try (var r = Resource.of(getClass(), "res", "test")) {
			assertEquals(r.toString(), r.path().toString());
		}
	}
}
