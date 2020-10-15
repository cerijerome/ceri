package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertExists;
import static ceri.common.test.TestUtil.assertIterable;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.collection.ArrayUtil;

public class FileTestHelperBehavior {

	@Test
	public void shouldFailForNonChildPaths() {
		assertThrown(() -> FileTestHelper.builder().dir("/abc"));
		assertThrown(() -> FileTestHelper.builder().dir("a/b/../../../c"));
	}

	@Test
	public void shouldTidyUpOnBuildFailure() {
		Path badPath = Mockito.mock(Path.class);
		when(badPath.normalize()).thenReturn(badPath);
		FileTestHelper.Builder b = FileTestHelper.builder().root("a").dir("b").file(badPath, "aaa");
		assertThrown(() -> b.build());
		assertExists(Path.of("a"), false);
	}

	@Test
	public void shouldBuildFromGivenPath() throws IOException {
		try (FileTestHelper root = FileTestHelper.builder().build()) {
			try (FileTestHelper unix = FileTestHelper.builder(root.root).root("a/b")
				.file("a/a/a.txt", "aaa").filef("bbb", "a/a/b.%s", "txt") //
				.dir("a/b").dirf("a/%s/%s", "b", "c").build()) {
				assertThat(unix.root, is(root.path("a/b")));
			}
		}
	}

	@Test
	public void shouldCreateTextFiles() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder().dirf("%s/%s", "a", "b")
			.filef("abc", "%s/%s", "a", "test.txt").build()) {
			assertThat(Files.readString(files.path("a/test.txt")), is("abc"));
		}
	}

	@Test
	public void shouldCreateBinaryFiles() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder()
			.filef(ArrayUtil.bytes(1, 2, 3, 4, 5), "a/%s", "data").build()) {
			assertArray(Files.readAllBytes(files.path("a/data")), 1, 2, 3, 4, 5);
		}
	}

	@Test
	public void shouldProvideFiles() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder().dir("a/b/c").build()) {
			assertThat(files.pathf("%s/%s", "a", "b"), is(files.path("a/b")));
			assertArray(files.paths("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
				files.path("a/b/c"));
			assertIterable(files.pathList("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
				files.path("a/b/c"));
		}
	}

	@Test
	public void shouldProvidePaths() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder().dir("a/b/c").build()) {
			assertThat(files.path("a/b/c"), is(files.root.resolve("a/b/c")));
			assertThat(files.pathf("%s/%s", "a", "b"), is(files.path("a/b")));
			assertArray(files.paths("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
				files.path("a/b/c"));
			assertIterable(files.pathList("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
				files.path("a/b/c"));
		}
	}

}
