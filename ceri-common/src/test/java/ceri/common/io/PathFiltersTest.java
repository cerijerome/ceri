package ceri.common.io;

import static ceri.common.test.AssertUtil.assertHelperPaths;
import static ceri.common.test.AssertUtil.assertPaths;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.function.Filters;
import ceri.common.test.FileTestHelper;
import ceri.common.text.Regex;

public class PathFiltersTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "a").file("b/b.txt", "bb")
			.file("c.txt", "ccc").build();
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
	public void testDirFilter() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.dir()), helper, "a", "b");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.dir()), helper, "", "a", "a/a",
			"b");
		assertHelperPaths(IoUtil.paths(helper.root, Filters.not(PathFilters.dir())), helper,
			"a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testFileFilter() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.file()), helper, "c.txt");
		assertHelperPaths(IoUtil.paths(helper.root, PathFilters.file()), helper, "a/a/a.txt",
			"b/b.txt", "c.txt");
		assertHelperPaths(IoUtil.paths(helper.root, Filters.not(PathFilters.file())), helper, "",
			"a", "a/a", "b");
	}

	@Test
	public void testByUnixPath() throws IOException {
		assertHelperPaths(
			IoUtil.paths(helper.root, PathFilters.byUnixPath(Regex.Filter.find("/a/"))), helper,
			"a/a", "a/a/a.txt");
	}

	@Test
	public void testByFileNamePath() throws IOException {
		assertHelperPaths(
			IoUtil.paths(helper.root,
				PathFilters.byFileNamePath(path -> path.toString().endsWith(".txt"))),
			helper, "a/a/a.txt", "b/b.txt", "c.txt");
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
		assertPaths(IoUtil.list(helper.root, PathFilters.byExtension()));
		assertHelperPaths(IoUtil.list(helper.root, PathFilters.byExtension("txt", "jpg")), helper,
			"c.txt");
		assertPaths(IoUtil.paths(helper.root, PathFilters.byExtension("jpg")));
		try (FileTestHelper helper = FileTestHelper.builder().file(".txt", "").build()) {
			assertPaths(IoUtil.list(helper.root, PathFilters.byExtension("txt")));
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
		assertHelperPaths(
			IoUtil.paths(helper.root, Filters.and(PathFilters.file(), PathFilters.byMinSize(2))),
			helper, "b/b.txt", "c.txt");
	}

}
