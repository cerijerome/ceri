package ceri.common.io;

import static ceri.common.test.TestUtil.assertExists;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.Capturer;
import ceri.common.test.FileTestHelper;

public class FileTrackerBehavior {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bbb")
			.file("c.txt", "ccc").build();
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test
	public void shouldCreateDirectories() throws IOException {
		FileTracker tracker = new FileTracker();
		assertExists(helper.path("x"), false);
		tracker.dir(helper.path("x/x/x"));
		assertExists(helper.path("x/x/x"), true);
		tracker.dir(helper.path("x/y/z"));
		assertExists(helper.path("x/y/z"), true);
		IoUtil.deleteAll(helper.path("x"));
	}

	@Test
	public void shouldCreateFilePathButNotFile() throws IOException {
		FileTracker tracker = new FileTracker();
		assertExists(helper.path("x"), false);
		tracker.file(helper.path("x/x/x.txt"));
		assertExists(helper.path("x/x"), true);
		assertExists(helper.path("x/x/x.txt"), false);
		tracker.file(helper.path("x/y/z.txt"));
		assertExists(helper.path("x/y"), true);
		assertExists(helper.path("x/y/z.txt"), false);
		IoUtil.deleteAll(helper.path("x"));
	}

	@Test
	public void shouldDeleteCreatedDirsOnly() throws IOException {
		FileTracker tracker = new FileTracker();
		tracker.dir(helper.path("a/x/y/z"));
		assertExists(helper.path("a/x/y/z"), true);
		tracker.dir(helper.path("x/y/z"));
		assertExists(helper.path("x/y/z"), true);
		assertExists(helper.path("a/a/a.txt"), true);
		tracker.dir(helper.path("a/a"));
		assertExists(helper.path("b/b.txt"), true);
		tracker.file(helper.path("b/b.txt"));
		tracker.delete();
		assertExists(helper.path("a/x"), false);
		assertExists(helper.path("x"), false);
		assertExists(helper.path("a/a/a.txt"), true);
		assertExists(helper.path("b/b.txt"), true);
	}

	@Test
	public void shouldNotifyDeletinFailures() throws IOException {
		FileTracker tracker = new FileTracker();
		tracker.dir(helper.path("z/z"));
		Files.delete(helper.path("z/z"));
		Capturer.Bi<IOException, Path> capturer = Capturer.ofBi();
		tracker.delete(capturer::accept);
		assertThat(capturer.first.values.size(), is(1));
		assertThat(capturer.second.values.size(), is(1));
	}

}
