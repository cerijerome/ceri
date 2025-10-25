package ceri.common.io;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertExists;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class FileTrackerBehavior {
	private FileTestHelper helper;

	@After
	public void after() {
		helper = TestUtil.close(helper);
	}

	@Test
	public void shouldCreateDirectories() throws IOException {
		initFiles();
		var tracker = new FileTracker();
		assertExists(helper.path("x"), false);
		tracker.dir(helper.path("x/x/x"));
		assertExists(helper.path("x/x/x"), true);
		tracker.dir(helper.path("x/y/z"));
		assertExists(helper.path("x/y/z"), true);
		Paths.deleteAll(helper.path("x"));
	}

	@Test
	public void shouldCreateFilePathButNotFile() throws IOException {
		initFiles();
		var tracker = new FileTracker();
		assertExists(helper.path("x"), false);
		tracker.file(helper.path("x/x/x.txt"));
		assertExists(helper.path("x/x"), true);
		assertExists(helper.path("x/x/x.txt"), false);
		tracker.file(helper.path("x/y/z.txt"));
		assertExists(helper.path("x/y"), true);
		assertExists(helper.path("x/y/z.txt"), false);
		Paths.deleteAll(helper.path("x"));
	}

	@Test
	public void shouldDeleteCreatedDirsOnly() throws IOException {
		initFiles();
		var tracker = new FileTracker();
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
		initFiles();
		var tracker = new FileTracker();
		tracker.dir(helper.path("z/z"));
		Files.delete(helper.path("z/z"));
		var captor = Captor.ofBi();
		tracker.delete(captor::accept);
		assertEquals(captor.first.values.size(), 1);
		assertEquals(captor.second.values.size(), 1);
	}

	private void initFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bbb")
			.file("c.txt", "ccc").build();
	}
}
