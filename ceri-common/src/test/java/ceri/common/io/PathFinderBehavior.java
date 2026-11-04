package ceri.common.io;

import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.Iterator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestCollection;
import ceri.common.test.Testing;

public class PathFinderBehavior {
	private static final Functions.Predicate<Object> nullFilter = null;
	private static final Excepts.Predicate<IOException, Object> nullFilterEx = null;
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

	@AfterEach
	public void after() {
		helper = Testing.close(helper);
	}

	@Test
	public void testStreamExceptions() {
		try (var dirs = TestDirStream.of("a", "b", "c")) {
			var ioe = new IOException("test");
			dirs.iterator.next.set(new IllegalStateException("test"),
				new DirectoryIteratorException(ioe));
			Assert.thrown(IllegalStateException.class, () -> PathFinder.stream(dirs).toList());
			Assert.thrown(e -> Assert.same(e, ioe), () -> PathFinder.stream(dirs).toList());
		}
	}

	@Test
	public void shouldNotFailForFileAsRoot() throws IOException {
		helper = FileTestHelper.builder().file("a.txt", "aaa").build();
		assertPaths(PathFinder.of(helper.path("a.txt")));
		assertPaths(PathFinder.all(helper.path("a.txt")));
	}

	@Test
	public void shouldAllowNullDir() throws IOException {
		Assert.ordered(PathFinder.of(null).list());
	}

	@Test
	public void shouldListPaths() throws IOException {
		initFiles();
		assertPaths(PathFinder.of(helper.root), "a", "b", "c.h", "d");
		assertPaths(PathFinder.all(helper.root), "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.h",
			"d");
	}

	@Test
	public void shouldFilterByPath() throws IOException {
		initFiles();
		assertPaths(PathFinder.all(helper.root).files(), "a/a/a.txt", "b/b.txt", "c.h");
		assertPaths(PathFinder.all(helper.root).filter("glob:*.txt"));
		assertPaths(PathFinder.all(helper.root).filter("glob:**/*.txt"), "a/a/a.txt", "b/b.txt");
		assertPaths(PathFinder.all(helper.root).filter("regex:[^/]*\\.txt"));
		assertPaths(PathFinder.all(helper.root).filter("regex:.*/[^/]*\\.txt"), "a/a/a.txt",
			"b/b.txt");
		assertPaths(PathFinder.of(helper.root).filter(nullFilter));
		assertPaths(PathFinder.of(helper.root).filterEx(nullFilterEx));
	}

	@Test
	public void shouldFilterByName() throws IOException {
		initFiles();
		assertPaths(PathFinder.all(helper.root).files(), "a/a/a.txt", "b/b.txt", "c.h");
		assertPaths(PathFinder.all(helper.root).nameFilter("glob:*.txt"), "a/a/a.txt", "b/b.txt");
		assertPaths(PathFinder.all(helper.root).nameFilter("glob:**/*.txt"));
		assertPaths(PathFinder.all(helper.root).nameFilter("regex:[^/]*\\.txt"), "a/a/a.txt",
			"b/b.txt");
		assertPaths(PathFinder.all(helper.root).nameFilter("regex:.*/[^/]*\\.txt"));
		assertPaths(PathFinder.all(helper.root).nameFilter(s -> s.endsWith("txt")), "a/a/a.txt",
			"b/b.txt");
		assertPaths(PathFinder.of(helper.root).nameFilter(nullFilter));
		assertPaths(PathFinder.of(helper.root).nameFilterEx(nullFilterEx));
	}

	@Test
	public void shouldConvertToRelativePaths() throws IOException {
		initFiles();
		Assert.paths(PathFinder.all(helper.root).relative().list(), "a", "a/a", "a/a/a.txt", "b",
			"b/b.txt", "c.h", "d");
		Assert.paths(PathFinder.all(helper.root).relative().relative().list(), "a", "a/a",
			"a/a/a.txt", "b", "b/b.txt", "c.h", "d");
		Assert.paths(PathFinder.all(helper.root).files().relative().list(), "a/a/a.txt", "b/b.txt",
			"c.h");
		Assert.paths(PathFinder.all(helper.root).relative().filter("glob:*.*").list(), "c.h");
		Assert.paths(PathFinder.all(helper.root).relative().files().list()); // no file match
		Assert.paths(PathFinder.of(helper.root).relative().list(), "a", "b", "c.h", "d");
		Assert.paths(PathFinder.of(helper.root).relative().relative().list(), "a", "b", "c.h", "d");
	}

	@Test
	public void shouldSortPaths() throws IOException {
		initFiles();
		Assert.ordered(PathFinder.all(helper.root).files().relative().sort().strings(), "a/a/a.txt",
			"b/b.txt", "c.h");
		Assert.ordered(PathFinder.all(helper.root).files().sort().names(), "a.txt", "b.txt", "c.h");
		Assert.ordered(PathFinder.of(helper.root).sort().names(), "a", "b", "c.h", "d");
	}

	@Test
	public void shouldStreamPaths() throws IOException {
		initFiles();
		Assert.stream(PathFinder.all(helper.root).files().sort().stream().map(Paths::name), "a.txt",
			"b.txt", "c.h");
	}

	private void initFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bb")
			.file("c.h", "c").dir("d").build();
	}

	private void assertPaths(PathFinder pathList, String... paths) throws IOException {
		helper.assertPaths(pathList.list(), paths);
	}
}
