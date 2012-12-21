package ceri.common.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class FileTrackerBehavior {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bbb").file("c.txt",
				"ccc").build();
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}
	
	@Test
	public void shouldCreateDirectories() {
		FileTracker tracker = new FileTracker();
		assertFalse(helper.file("x").exists());
		tracker.dir(helper.file("x/x/x"));
		assertTrue(helper.file("x/x/x").exists());
		tracker.dir(helper.file("x/y/z"));
		assertTrue(helper.file("x/y/z").exists());
		IoUtil.deleteAll(helper.file("x"));
	}

	@Test
	public void shouldCreateFilePathButNotFile() {
		FileTracker tracker = new FileTracker();
		assertFalse(helper.file("x").exists());
		tracker.file(helper.file("x/x/x.txt"));
		assertTrue(helper.file("x/x").exists());
		assertFalse(helper.file("x/x/x.txt").exists());
		tracker.file(helper.file("x/y/z.txt"));
		assertTrue(helper.file("x/y").exists());
		assertFalse(helper.file("x/y/z.txt").exists());
		IoUtil.deleteAll(helper.file("x"));
	}
	
	@Test
	public void shouldDeleteCreatedDirsOnly() {
		FileTracker tracker = new FileTracker();
		tracker.dir(helper.file("a/x/y/z"));
		assertTrue(helper.file("a/x/y/z").exists());
		tracker.dir(helper.file("x/y/z"));
		assertTrue(helper.file("x/y/z").exists());
		assertTrue(helper.file("a/a/a.txt").exists());
		tracker.dir(helper.file("a/a"));
		assertTrue(helper.file("b/b.txt").exists());
		tracker.file(helper.file("b/b.txt"));
		tracker.delete();
		assertFalse(helper.file("a/x").exists());
		assertFalse(helper.file("x").exists());
		assertTrue(helper.file("a/a/a.txt").exists());
		assertTrue(helper.file("b/b.txt").exists());
	}

}
