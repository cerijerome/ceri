package ceri.common.io;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestCollection;
import ceri.common.test.Testing;

public class PathListBehavior {
	private static final Excepts.Predicate<IOException, Object> nullFilter = null;
	private FileTestHelper helper;

	private static class TestDirStream<T> implements DirectoryStream<T> {
		public final TestCollection.Iterator<T> iterator;

		@SafeVarargs
		public static <T> TestDirStream<T> of(T... ts) {
			return new TestDirStream<>(Arrays.asList(ts));
		}

		public TestDirStream(Iterable<T> iterable) {
			this.iterator = TestCollection.iterator(iterable);
		}

		@Override
		public Iterator<T> iterator() {
			return iterator;
		}

		@Override
		public void close() {}
	}

	@After
	public void after() {
		helper = Testing.close(helper);
	}

	@Test
	public void testStreamExceptions() {
		try (var dirs = TestDirStream.of("a", "b", "c")) {
			var ioe = new IOException("test");
			dirs.iterator.next.set(new IllegalStateException("test"),
				new DirectoryIteratorException(ioe));
			Assert.thrown(IllegalStateException.class, () -> PathList.stream(dirs).toList());
			Assert.thrown(e -> Assert.same(e, ioe), () -> PathList.stream(dirs).toList());
		}
	}

	@Test
	public void shouldAllowNullDir() throws IOException {
		Assert.ordered(PathList.of(null).list());
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
		Assert.paths(PathList.all(helper.root).relative().list(), "a", "a/a", "a/a/a.txt", "b",
			"b/b.txt", "c.h", "d");
		Assert.paths(PathList.all(helper.root).relative().relative().list(), "a", "a/a",
			"a/a/a.txt", "b", "b/b.txt", "c.h", "d");
		Assert.paths(PathList.all(helper.root).files().relative().list(), "a/a/a.txt", "b/b.txt",
			"c.h");
		Assert.paths(PathList.all(helper.root).relative().filter("glob:*.*").list(), "c.h");
		Assert.paths(PathList.all(helper.root).relative().files().list()); // no file match
		Assert.paths(PathList.of(helper.root).relative().list(), "a", "b", "c.h", "d");
		Assert.paths(PathList.of(helper.root).relative().relative().list(), "a", "b", "c.h", "d");
	}

	@Test
	public void shouldSortPaths() throws IOException {
		initFiles();
		Assert.ordered(PathList.all(helper.root).files().relative().sort().strings(), "a/a/a.txt",
			"b/b.txt", "c.h");
		Assert.ordered(PathList.all(helper.root).files().sort().names(), "a.txt", "b.txt", "c.h");
		Assert.ordered(PathList.of(helper.root).sort().names(), "a", "b", "c.h", "d");
	}

	@Test
	public void shouldStreamPaths() throws IOException {
		initFiles();
		Assert.stream(PathList.all(helper.root).files().sort().stream().map(Paths::name), "a.txt",
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
