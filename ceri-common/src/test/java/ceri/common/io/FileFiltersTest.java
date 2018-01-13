package ceri.common.io;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class FileFiltersTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper = FileTestHelper.builder() //
			.file("a/a/a.txt", "a").file("b/b.txt", "bb").file("c.txt", "ccc").build();
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(FileFilters.class);
	}

	@Test
	public void testByExtension() throws IOException {
		File[] files = helper.root.listFiles(FileFilters.byExtension());
		assertCollection(files);
		files = helper.root.listFiles(FileFilters.byExtension("txt", "jpg"));
		assertCollection(files, helper.files("c.txt"));
		files = helper.root.listFiles(FileFilters.byExtension("jpg"));
		assertCollection(files);
		try (FileTestHelper helper = FileTestHelper.builder().file(".txt", "").build()) {
			files = helper.root.listFiles(FileFilters.byExtension("txt"));
			assertCollection(files);
		}
	}

	@Test
	public void testNullFilter() {
		File[] files = helper.root.listFiles(FileFilters.NULL);
		assertThat(files.length, is(0));
		List<File> list = IoUtil.getFiles(helper.root, FileFilters.NULL);
		assertTrue(list.isEmpty());
	}

	@Test
	public void testAllFilter() {
		File[] files = helper.root.listFiles(FileFilters.ALL);
		assertCollection(files, helper.files("a", "b", "c.txt"));
		List<File> list = IoUtil.getFiles(helper.root, FileFilters.ALL);
		assertCollection(list, helper.files("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt"));
	}

	@Test
	public void testDirFilter() {
		File[] files = helper.root.listFiles(FileFilters.DIR);
		assertCollection(files, helper.files("a", "b"));
		List<File> list = IoUtil.getFiles(helper.root, FileFilters.DIR);
		assertCollection(list, helper.files("a", "a/a", "b"));
	}

	@Test
	public void testFileFilter() {
		File[] files = helper.root.listFiles(FileFilters.FILE);
		assertCollection(files, helper.files("c.txt"));
		List<File> list = IoUtil.getFiles(helper.root, FileFilters.FILE);
		assertCollection(list, helper.files("a/a/a.txt", "b/b.txt", "c.txt"));
	}

	@Test
	public void testReverseFilter() {
		FileFilter filter = pathname -> {
			return pathname.getPath().endsWith(File.separatorChar + "a");
		};
		List<File> list = IoUtil.getFiles(helper.root, filter);
		assertCollection(list, helper.files("a", "a/a"));

		filter = FileFilters.reverse(filter);
		list = IoUtil.getFiles(helper.root, filter);
		assertCollection(list, helper.files("a/a/a.txt", "b", "b/b.txt", "c.txt"));
	}

	@Test
	public void testModifiedSince() {
		File[] files = helper.root.listFiles(FileFilters.byModifiedSince(0));
		assertCollection(files, helper.files("a", "b", "c.txt"));
		files = helper.root.listFiles(FileFilters.byModifiedSince(Long.MAX_VALUE));
		assertCollection(files, helper.files());
	}

	@Test
	public void testMaxLength() {
		List<File> files = IoUtil.getFiles(helper.root, FileFilters.byMaxLength(2));
		assertCollection(files, helper.fileList("a/a/a.txt", "b/b.txt"));
	}

	@Test
	public void testOr() {
		FileFilter filter =
			FileFilters.or(RegexFilenameFilter.create("a"), RegexFilenameFilter.create("c.*"));
		List<File> files = IoUtil.getFiles(helper.root, filter);
		assertThat(files, is(helper.fileList("a", "a/a", "c.txt")));
	}

	@Test
	public void testAnd() {
		FileFilter filter = FileFilters.and(FileFilters.FILE, RegexFilenameFilter.create("[ac].*"));
		List<File> files = IoUtil.getFiles(helper.root, filter);
		assertThat(files, is(helper.fileList("a/a/a.txt", "c.txt")));
	}

}
