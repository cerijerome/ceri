package ceri.common.io;

import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;
import ceri.common.test.Assert;

public class ResourceBehavior {
	private static final String PROPERTIES = ResourceBehavior.class.getSimpleName() + ".properties";

	@Test
	public void shouldProvideClassUrl() {
		Assert.equal(Resource.url(null), null);
		var url = Resource.url(String.class);
		Assert.equal(url.getPath().endsWith("/java/lang/String.class"), true);
		url = Resource.url(Test.class);
		Assert.equal(url.getPath().endsWith("/org/junit/Test.class"), true);
		url = Resource.url(Resource.class);
		Assert.equal(url.getPath().endsWith("/ceri/common/io/Resource.class"), true);
	}

	@Test
	public void shouldReadBytes() throws IOException {
		byte[] content = Resource.bytes(getClass(), PROPERTIES);
		Assert.array(content, 'a', '=', 'b');
		content = Resource.bytes(String.class, "String.class"); // module
		Assert.equal(content.length > 0, true);
		content = Resource.bytes(Test.class, "Test.class"); // jar
		Assert.equal(content.length > 0, true);
	}

	@Test
	public void shouldReadString() throws IOException {
		var s = Resource.string(getClass(), PROPERTIES);
		Assert.equal(s, "a=b");
	}

	@Test
	public void shouldProvideResourceRelativeToRoot() throws IOException {
		try (var r = Resource.root(String.class)) {
			var names = PathList.of(r.path()).names();
			Assert.equal(names.contains("java"), true);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void shouldAccessFile() throws IOException {
		Assert.equal(Resource.of(null), null);
		try (var r = Resource.of(getClass(), "res", "test")) {
			Assert.unordered(PathList.of(r.path()).names(), "A.txt", "BB.txt", "CCC.txt");
			Assert.equal(Files.readString(r.resolve("A.txt")), "aaa");
		}
		try (var r = Resource.of(getClass(), "res", "test", "BB.txt")) {
			Assert.equal(r.string(), "bb");
		}
	}

	@Test
	public void shouldAccessJar() throws IOException {
		Assert.thrown(() -> Resource.of(Test.class, "\0"));
		try (var r = Resource.of(Test.class, "runner")) {
			var names = PathList.of(r.path()).names();
			Assert.equal(names.contains("Runner.class"), true);
		}
	}

	@Test
	public void shouldAccessModule() throws IOException {
		try (var r = Resource.of(String.class, "ref")) {
			var names = PathList.of(r.path()).names();
			Assert.equal(names.contains("Finalizer.class"), true);
		}
	}

	@Test
	public void shouldProvideString() throws IOException {
		try (var r = Resource.of(getClass(), "res", "test")) {
			Assert.equal(r.toString(), r.path().toString());
		}
	}
}
