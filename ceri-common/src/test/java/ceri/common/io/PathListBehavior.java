package ceri.common.io;

import static ceri.common.test.Assert.assertOrdered;
import static ceri.common.test.Assert.assertPaths;
import static ceri.common.test.Assert.assertStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class PathListBehavior {
	private static final Excepts.Predicate<IOException, Object> nullFilter = null;
	private FileTestHelper helper;

	@After
	public void after() {
		helper = TestUtil.close(helper);
	}

	@Test
	public void shouldAllowNullDir() throws IOException {
		assertOrdered(PathList.of(null).list());
	}

	@Test
	public void shouldListPaths() throws IOException {
		initFiles();
		assertPathList(PathList.of(helper.root), "a", "b", "c.h", "d");
		assertPathList(PathList.all(helper.root), "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.h",
			"d");
	}

	@Test
	public void shouldFilterByPath() throws IOException {
		initFiles();
		assertPathList(PathList.all(helper.root).files(), "a/a/a.txt", "b/b.txt", "c.h");
		assertPathList(PathList.all(helper.root).filter("glob:*.txt"));
		assertPathList(PathList.all(helper.root).filter("glob:**/*.txt"), "a/a/a.txt", "b/b.txt");
		assertPathList(PathList.all(helper.root).filter("regex:[^/]*\\.txt"));
		assertPathList(PathList.all(helper.root).filter("regex:.*/[^/]*\\.txt"), "a/a/a.txt",
			"b/b.txt");
		assertPathList(PathList.of(helper.root).filter(nullFilter));
	}

	@Test
	public void shouldFilterByName() throws IOException {
		initFiles();
		assertPathList(PathList.all(helper.root).files(), "a/a/a.txt", "b/b.txt", "c.h");
		assertPathList(PathList.all(helper.root).nameFilter("glob:*.txt"), "a/a/a.txt", "b/b.txt");
		assertPathList(PathList.all(helper.root).nameFilter("glob:**/*.txt"));
		assertPathList(PathList.all(helper.root).nameFilter("regex:[^/]*\\.txt"), "a/a/a.txt",
			"b/b.txt");
		assertPathList(PathList.all(helper.root).nameFilter("regex:.*/[^/]*\\.txt"));
		assertPathList(PathList.all(helper.root).nameFilter(s -> s.endsWith("txt")), "a/a/a.txt",
			"b/b.txt");
		assertPathList(PathList.of(helper.root).nameFilter(nullFilter));
	}

	@Test
	public void shouldConvertToRelativePaths() throws IOException {
		initFiles();
		assertPaths(PathList.all(helper.root).relative().list(), "a", "a/a", "a/a/a.txt", "b",
			"b/b.txt", "c.h", "d");
		assertPaths(PathList.all(helper.root).relative().relative().list(), "a", "a/a", "a/a/a.txt",
			"b", "b/b.txt", "c.h", "d");
		assertPaths(PathList.all(helper.root).files().relative().list(), "a/a/a.txt", "b/b.txt",
			"c.h");
		assertPaths(PathList.all(helper.root).relative().filter("glob:*.*").list(), "c.h");
		assertPaths(PathList.all(helper.root).relative().files().list()); // no file match
		assertPaths(PathList.of(helper.root).relative().list(), "a", "b", "c.h", "d");
		assertPaths(PathList.of(helper.root).relative().relative().list(), "a", "b", "c.h", "d");
	}

	@Test
	public void shouldSortPaths() throws IOException {
		initFiles();
		assertOrdered(PathList.all(helper.root).files().relative().sort().strings(), "a/a/a.txt",
			"b/b.txt", "c.h");
		assertOrdered(PathList.all(helper.root).files().sort().names(), "a.txt", "b.txt", "c.h");
		assertOrdered(PathList.of(helper.root).sort().names(), "a", "b", "c.h", "d");
	}

	@Test
	public void shouldStreamPaths() throws IOException {
		initFiles();
		assertStream(PathList.all(helper.root).files().sort().stream().map(Paths::name), "a.txt",
			"b.txt", "c.h");
	}

	private void initFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bb")
			.file("c.h", "c").dir("d").build();
	}

	private void assertPathList(PathList pathList, String... paths) throws IOException {
		helper.assertPaths(pathList.list(), paths);
	}
}
