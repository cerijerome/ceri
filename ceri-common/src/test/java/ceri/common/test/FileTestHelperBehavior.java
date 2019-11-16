package ceri.common.test;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertIterable;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import org.junit.Test;
import ceri.common.io.IoUtil;

public class FileTestHelperBehavior {

	@Test
	public void shouldBuildFromGivenPath() throws IOException {
		try (FileTestHelper root = FileTestHelper.builder().dir("a/b").build()) {
			try (FileTestHelper files = FileTestHelper.builder(root.path("a/b")).build()) {
				assertThat(files.root.getParentFile(), is(root.file("a/b")));
			}
		}
	}

	@Test
	public void shouldCreateFiles() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder().dirf("%s/%s", "a", "b")
			.filef("abc", "%s/%s", "a", "test.txt").build()) {
			assertThat(IoUtil.readString(files.file("a/test.txt")), is("abc"));
		}
	}

	@Test
	public void shouldProvideFiles() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder().dir("a/b/c").build()) {
			assertThat(files.filef("%s/%s", "a", "b"), is(files.file("a/b")));
			assertArray(files.files("a", "a/b", "a/b/c"), files.file("a"), files.file("a/b"),
				files.file("a/b/c"));
			assertIterable(files.fileList("a", "a/b", "a/b/c"), files.file("a"), files.file("a/b"),
				files.file("a/b/c"));
		}
	}

	@Test
	public void shouldProvidePaths() throws IOException {
		try (FileTestHelper files = FileTestHelper.builder().dir("a/b/c").build()) {
			assertThat(files.path("a/b/c"), is(files.root.toPath().resolve("a/b/c")));
			assertThat(files.pathf("%s/%s", "a", "b"), is(files.path("a/b")));
			assertArray(files.paths("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
				files.path("a/b/c"));
			assertIterable(files.pathList("a", "a/b", "a/b/c"), files.path("a"), files.path("a/b"),
				files.path("a/b/c"));
		}
	}

}
