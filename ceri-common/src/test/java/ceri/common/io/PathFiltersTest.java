package ceri.common.io;

import static ceri.common.test.TestUtil.assertHelperPaths;
import static ceri.common.test.TestUtil.assertPaths;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.common.text.RegexUtil;

public class PathFiltersTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder() //
			.file("a/a/a.txt", "a").file("b/b.txt", "bb").file("c.txt", "ccc").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(PathFilters.class);
	}

	@Test
	public void testNoneFilter() throws IOException {
		assertPaths(IoUtil.list(helper.root, PathFilters.NONE::test));
		assertPaths(IoUtil.paths(helper.root, PathFilters.NONE::test));
	}

	@Test
	public void testAllFilter() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.ALL::test), helper, //
			"a", "b", "c.txt");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.ALL::test), helper, //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt");
	}

	@Test
	public void testDirFilter() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.DIR::test), helper, //
			"a", "b");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.DIR::test), helper, //
			"", "a", "a/a", "b");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.DIR.negate()::test), helper, //
			"a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testFileFilter() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.FILE::test), helper, "c.txt");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.FILE::test), helper, //
			"a/a/a.txt", "b/b.txt", "c.txt");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.FILE.negate()::test), helper, //
			"", "a", "a/a", "b");
	}

	@Test
	public void testByUnixPath() throws IOException {
		assertHelperPaths(
			IoUtil.paths(helper.root, PathFilters.byUnixPath(RegexUtil.finder("/a/"))::test),
			helper, "a/a", "a/a/a.txt");
	}

	@Test
	public void testByFileNamePath() throws IOException {
		assertHelperPaths(IoUtil.paths(helper.root, //
			PathFilters.byFileNamePath(path -> path.toString().endsWith(".txt"))), helper, //
			"a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testByFileName() throws IOException {
		assertHelperPaths(
			IoUtil.paths(helper.root, PathFilters.byFileName(s -> s.endsWith(".txt"))), helper,
			"a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testByIndex() throws IOException {
		assertPaths(IoUtil.pathsRelative(helper.root, // paths start at helper.root/...
			PathFilters.byIndex(2, s -> s.length() == 1)::test), "a/a", "a/a/a.txt");
	}

	@Test
	public void testByExtension() throws IOException {
		assertPaths(IoUtil.list(helper.root, PathFilters.byExtension()::test));
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.byExtension("txt", "jpg")::test),
			helper, "c.txt");
		assertPaths(IoUtil.paths(helper.root, PathFilters.byExtension("jpg")::test));
		try (FileTestHelper helper = FileTestHelper.builder().file(".txt", "").build()) {
			assertPaths(IoUtil.list(helper.root, PathFilters.byExtension("txt")::test));
		}
	}

	@Test
	public void testModifiedSince() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.byModifiedSince(0)), helper, "a",
			"b", "c.txt");
		assertPaths(IoUtil.list(helper.root, PathFilters.byModifiedSince(Long.MAX_VALUE)));
	}

	@Test
	public void testMaxSize() throws IOException {
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.byMaxSize(0)), helper);
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.byMaxSize(2)), helper, "a/a/a.txt",
			"b/b.txt");
	}

	@Test
	public void testMinSize() throws IOException {
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.byMinSize(Long.MAX_VALUE)), helper);
		assertHelperPaths(IoUtil.paths(helper.root, //
			PathFilters.adapt(PathFilters.FILE).and(PathFilters.byMinSize(2))), helper, //
			"b/b.txt", "c.txt");
	}

}
