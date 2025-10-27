package ceri.common.test;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Closeables;
import ceri.common.io.TestPath;

public class FileTestHelperBehavior {
	private FileTestHelper files;

	@After
	public void after() {
		Closeables.close(files);
		files = null;
	}

	@Test
	public void shouldFailForNonChildPaths() {
		Assert.thrown(() -> FileTestHelper.builder().dir("/abc"));
		Assert.thrown(() -> FileTestHelper.builder().dir("a/b/../../../c"));
	}

	@Test
	public void shouldTidyUpOnBuildFailure() {
		var badPath = TestPath.of();
		var b = FileTestHelper.builder().root("a").dir("b").file(badPath, "aaa");
		Assert.thrown(() -> b.build());
		Assert.exists(Path.of("a"), false);
	}

	@Test
	public void shouldBuildFromGivenPath() throws IOException {
		files = FileTestHelper.builder().build();
		try (var sub = FileTestHelper.builder(files.root).root("r").file("a/a/a.txt", "aaa")
			.filef("bbb", "a/a/b.%s", "txt").dir("a/b").dirf("a/%s/%s", "b", "c").build()) {
			Assert.equal(sub.root, files.path("r"));
		}
	}

	@Test
	public void shouldFailIfRootExists() throws IOException {
		files = FileTestHelper.builder().dir("r").build();
		Assert.thrown(FileAlreadyExistsException.class,
			() -> FileTestHelper.builder(files.root).root("r").build());
	}

	@Test
	public void shouldCreateTextFiles() throws IOException {
		files = FileTestHelper.builder().dirf("%s/%s", "a", "b")
			.filef("abc", "%s/%s", "a", "test.txt").build();
		Assert.equal(files.readString("a/test.txt"), "abc");
	}

	@Test
	public void shouldCreateBinaryFiles() throws IOException {
		files = FileTestHelper.builder().filef(ArrayUtil.bytes.of(1, 2, 3, 4, 5), "a/%s", "data")
			.build();
		Assert.array(files.read("a/data"), 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldProvideFiles() throws IOException {
		files = FileTestHelper.builder().dir("a/b/c").build();
		Assert.equal(files.path("%s/%s", "a", "b"), files.path("a/b"));
		Assert.array(files.paths("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
			files.path("a/b/c"));
		Assert.ordered(files.pathList("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
			files.path("a/b/c"));
	}

	@Test
	public void shouldProvidePaths() throws IOException {
		files = FileTestHelper.builder().dir("a/b/c").build();
		Assert.equal(files.path("a/b/c"), files.root.resolve("a/b/c"));
		Assert.equal(files.path("%s/%s", "a", "b"), files.path("a/b"));
		Assert.array(files.paths("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
			files.path("a/b/c"));
		Assert.ordered(files.pathList("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
			files.path("a/b/c"));
	}
}
