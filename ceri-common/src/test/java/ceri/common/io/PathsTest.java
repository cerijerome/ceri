package ceri.common.io;

import static ceri.common.test.Assert.assertEquals;
import static ceri.common.test.Assert.assertFalse;
import static ceri.common.test.Assert.assertFile;
import static ceri.common.test.Assert.assertPath;
import static ceri.common.test.Assert.assertPrivateConstructor;
import static ceri.common.test.Assert.assertString;
import static ceri.common.test.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import org.junit.After;
import org.junit.Test;
import ceri.common.data.ByteProvider;
import ceri.common.function.Excepts;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;
import ceri.common.util.SystemVars;

public class PathsTest {
	private FileTestHelper helper;
	private FileTestHelper deleter;

	@After
	public void after() {
		deleter = TestUtil.close(deleter);
		helper = TestUtil.close(helper);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Paths.class);
		assertPrivateConstructor(Paths.Filter.class);
	}

	@Test
	public void testFilters() throws IOException {
		initFiles();
		assertPathListOf(p -> p.filter(Paths.Filter.file()), "c.h");
		assertPathListAll(p -> p.filter(Paths.Filter.file()), "a/a/a.txt", "b/b.txt", "c.h");
		assertPathListOf(p -> p.filter(Paths.Filter.dir()), "a", "b", "d");
		assertPathListAll(p -> p.filter(Paths.Filter.dir()), "a", "a/a", "b", "d");
		assertPathListAll(
			p -> p.filter(Paths.Filter.namePath(path -> path.toString().endsWith(".txt"))),
			"a/a/a.txt", "b/b.txt");
		assertPathListAll(p -> p.filter(Paths.Filter.name(s -> s.endsWith(".txt"))), "a/a/a.txt",
			"b/b.txt");
		assertPathListAll(p -> p.filter(Paths.Filter.ext("txt"::equals)), "a/a/a.txt", "b/b.txt");
		var now = Instant.now();
		assertPathListOf(p -> p.filter(Paths.Filter.lastModified(d -> d.isBefore(now))), "a", "b",
			"c.h", "d");
		assertPathListOf(p -> p.filter(Paths.Filter.lastModified(d -> d.isAfter(now))));
		assertPathListOf(p -> p.filter(Paths.Filter.size(s -> s == 0)));
		assertPathListOf(p -> p.filter(Paths.Filter.size(s -> s < 2)), "c.h");
	}

	@SuppressWarnings("resource")
	@Test
	public void testFs() {
		Assert.same(Paths.fs(null), FileSystems.getDefault());
	}

	@SuppressWarnings("resource")
	@Test
	public void testRoot() {
		assertPath(Paths.root(null), null);
		assertPath(Paths.root(FileSystems.getDefault()), "/");
		assertPath(Paths.root(TestFileSystem.of()), null);
	}

	@Test
	public void testIsRoot() {
		assertEquals(Paths.isRoot(null), false);
		assertEquals(Paths.isRoot(Path.of("")), false);
		assertEquals(Paths.isRoot(Path.of("test")), false);
		assertEquals(Paths.isRoot(Path.of("/test")), false);
		assertEquals(Paths.isRoot(Path.of("/")), true);
	}

	@Test
	public void testIsEmpty() {
		assertEquals(Paths.isEmpty(null), true);
		assertEquals(Paths.isEmpty(Path.of("")), false);
		assertEquals(Paths.isEmpty(Path.of("").getRoot()), true);
	}

	@Test
	public void testPatterns() {
		assertString(Paths.glob(null), "glob:");
		assertString(Paths.glob("**/*.*"), "glob:**/*.*");
		assertString(Paths.regex(null), "regex:");
		assertString(Paths.regex(".*/[^/]+*\\.[^/]+"), "regex:.*/[^/]+*\\.[^/]+");
	}

	@Test
	public void testName() {
		assertString(Paths.name(null), null);
		assertString(Paths.name(Path.of("/")), "");
		assertString(Paths.name(Path.of("")), "");
		assertString(Paths.name(Path.of("/a")), "a");
		assertString(Paths.name(Path.of("/a/b")), "b");
		assertString(Paths.name(Path.of("a")), "a");
		assertString(Paths.name(Path.of("a/b")), "b");
	}

	@Test
	public void testNameWithIndex() {
		assertString(Paths.name(null, 0), null);
		assertString(Paths.name(Path.of("/a/b/c/d"), -5), null);
		assertString(Paths.name(Path.of("/a/b/c/d"), -4), "a");
		assertString(Paths.name(Path.of("/a/b/c/d"), -1), "d");
		assertString(Paths.name(Path.of("/a/b/c/d"), 4), null);
		assertString(Paths.name(Path.of("/a/b/c/d"), 0), "a");
		assertString(Paths.name(Path.of("/a/b/c/d"), 3), "d");
		assertString(Paths.name(Path.of("/"), 0), null);
		assertString(Paths.name(Path.of(""), 0), "");
	}

	@Test
	public void testNameWithoutExt() {
		assertString(Paths.nameWithoutExt((Path) null), null);
		assertString(Paths.nameWithoutExt(Path.of("/")), "");
		assertString(Paths.nameWithoutExt(Path.of("")), "");
		assertString(Paths.nameWithoutExt(Path.of(".file")), ".file");
		assertString(Paths.nameWithoutExt(Path.of("/a")), "a");
		assertString(Paths.nameWithoutExt(Path.of("/a.txt")), "a");
		assertString(Paths.nameWithoutExt(Path.of("a.b.c")), "a.b");
	}

	@Test
	public void testExt() {
		assertString(Paths.ext((Path) null), null);
		assertString(Paths.ext(Path.of("/")), "");
		assertString(Paths.ext(Path.of("")), "");
		assertString(Paths.ext(Path.of(".file")), "");
		assertString(Paths.ext(Path.of("/a")), "");
		assertString(Paths.ext(Path.of("/a.txt")), "txt");
		assertString(Paths.ext(Path.of("a.b.c")), "c");
	}

	@Test
	public void testLastModified() throws IOException {
		assertEquals(Paths.lastModified(null), null);
	}

	@Test
	public void testSub() {
		assertPath(Paths.sub(null, 0), null);
		assertPath(Paths.sub(Path.of("/"), 0), "");
		assertPath(Paths.sub(Path.of(""), 0), "");
		Assert.thrown(() -> Paths.sub(Path.of(""), 1));
		assertPath(Paths.sub(Path.of("/a/b/c"), 0), "a/b/c");
		assertPath(Paths.sub(Path.of("/a/b/c"), 2), "c");
		assertPath(Paths.sub(Path.of("/a/b/c"), 3), "");
		Assert.thrown(() -> Paths.sub(Path.of("/a/b/c"), 4));
		assertPath(Paths.sub(null, 0, 0), null);
		assertPath(Paths.sub(Path.of("a"), 0, 1), "a");
		assertPath(Paths.sub(Path.of("/a/b/c/d"), 1, 3), "b/c");
		assertPath(Paths.sub(Path.of("/a/b/c"), 0, 0), "");
		assertPath(Paths.sub(Path.of("a/b/c"), 0, 0), "");
	}

	@Test
	public void testExtend() throws IOException {
		try (var r = Resource.of(String.class)) {
			assertPath(Paths.extend(null, "test"), null);
			assertPath(Paths.extend(r.path()), r.path().toString());
			assertPath(Paths.extend(r.path(), "ref", "Finalizer.class"),
				r.path() + "/ref/Finalizer.class");
		}
	}

	@Test
	public void testNewPath() {
		assertEquals(Paths.newPath(null, null), null);
	}

	@Test
	public void testChangeName() {
		assertPath(Paths.changeName(null, "test"), null);
		assertPath(Paths.changeName(Path.of(""), ""), "");
		assertPath(Paths.changeName(Path.of(""), "hello"), "hello");
		assertPath(Paths.changeName(Path.of("test"), "hello"), "hello");
		assertPath(Paths.changeName(Path.of("test/"), "hello"), "hello"); // path = "test"
		assertPath(Paths.changeName(Path.of("/"), "hello"), "/hello");
		assertPath(Paths.changeName(Path.of("/test"), "hello"), "/hello");
		assertPath(Paths.changeName(Path.of("/test/test"), "hello"), "/test/hello");
	}

	@Test
	public void testChangeNameFunction() {
		assertPath(Paths.changeName(Path.of("/test"), f -> f + "0"), "/test0");
		assertPath(Paths.changeName(Path.of("/test"), _ -> null), "/test");
	}

	@Test
	public void testShorten() {
		assertPath(Paths.shorten(null, 0), null);
		assertPath(Paths.shorten(Path.of("a"), 0), "a");
		assertPath(Paths.shorten(Path.of("/"), 0), "/");
		Assert.thrown(() -> Paths.shorten(Path.of("/"), 1));
		assertPath(Paths.shorten(Path.of("/a/b/c"), 0), "/a/b/c");
		assertPath(Paths.shorten(Path.of("/a/b/c"), 2), "/a");
		assertPath(Paths.shorten(Path.of("/a/b/c"), 3), "/");
		assertPath(Paths.shorten(Path.of("a/b/c"), 0), "a/b/c");
		assertPath(Paths.shorten(Path.of("a/b/c"), 2), "a");
		assertPath(Paths.shorten(Path.of("a/b/c"), 3), "");
	}

	@Test
	public void testCreateTempDir() throws IOException {
		helper = FileTestHelper.builder(SystemVars.tempDir()).build();
		var tempDir = Paths.createTempDir(helper.root);
		assertEquals(Files.exists(tempDir), true);
		assertEquals(Files.isDirectory(tempDir), true);
		Files.delete(tempDir);
		try {
			tempDir = Paths.createTempDir(null);
			assertPath(tempDir.getParent(), null);
		} finally {
			Files.delete(tempDir);
		}
	}

	@Test
	public void testDeleteAll() throws IOException {
		initFiles();
		deleter = FileTestHelper.builder(helper.root).file("x/x/x.txt", "").dir("y/y")
			.file("z.txt", "").build();
		assertEquals(Paths.deleteAll(null), false);
		assertEquals(Paths.deleteAll(Path.of("XXX/XXX/XXX/XXX")), false);
		Assert.thrown(() -> Paths.deleteAll(Path.of("/XXX/XXX/XXX")));
		assertEquals(Paths.deleteAll(deleter.path("z.txt")), false);
		assertEquals(Files.exists(deleter.path("x/x/x.txt")), true);
		assertEquals(Files.exists(deleter.path("y/y")), true);
		assertEquals(Files.exists(deleter.path("z.txt")), true);
		Paths.deleteAll(deleter.root);
		assertEquals(Files.exists(deleter.root), false);
	}

	@Test
	public void testDeleteEmptyDirs() throws IOException {
		initFiles();
		Paths.deleteEmptyDirs(null);
		deleter =
			FileTestHelper.builder(helper.root).dir("x/x/x").file("y/y.txt", "").dir("z").build();
		assertEquals(Files.exists(deleter.path("x/x/x")), true);
		assertEquals(Files.exists(deleter.path("y/y.txt")), true);
		assertEquals(Files.exists(deleter.path("z")), true);
		Paths.deleteEmptyDirs(deleter.root);
		assertEquals(Files.exists(deleter.path("x/x/x")), false);
		assertEquals(Files.exists(deleter.path("y/y.txt")), true);
		assertEquals(Files.exists(deleter.path("z")), false);
	}

	@Test
	public void testIsEmptyDir() throws IOException {
		initFiles();
		assertFalse(Paths.isEmptyDir(null));
		assertFalse(Paths.isEmptyDir(helper.path("a")));
		assertFalse(Paths.isEmptyDir(helper.path("a/a/a.txt")));
		assertTrue(Paths.isEmptyDir(helper.path("d")));
	}

	@Test
	public void testToUnix() {
		assertEquals(Paths.toUnix((String) null), null);
		assertEquals(Paths.toUnix((Path) null), null);
		assertEquals(Paths.toUnix(Path.of("a", "b", "c")), "a/b/c");
		assertEquals(Paths.toUnix(Path.of("")), "");
	}

	@Test
	public void testFromUnix() {
		assertEquals(Paths.fromUnix(null), null);
		assertEquals(Paths.fromUnix("a/b/c"), String.format("a%sb%1$sc", File.separatorChar));
	}

	@Test
	public void testConvertPath() {
		assertEquals(Paths.convert("a\\b\\c", '\\', '/'), "a/b/c");
	}

	@Test
	public void testCopyFile() throws IOException {
		initFiles();
		try {
			var fromFile = helper.path("a/a/a.txt");
			var toFile = helper.path("x/x/x.txt");
			Paths.copyFile(null, toFile); // ignored
			Paths.copyFile(fromFile, null); // ignored
			Paths.copyFile(fromFile, toFile);
			assertFile(fromFile, toFile);
		} finally {
			Paths.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopyBadFile() throws IOException {
		initFiles();
		try {
			var badFile = TestPath.of();
			var toFile2 = helper.path("x/y/z.txt");
			Assert.thrown(() -> Paths.copyFile(badFile, toFile2));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			Paths.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopy() throws IOException {
		initFiles();
		try {
			var fromFile = helper.path("a/a/a.txt");
			var toFile = helper.path("x/x/x.txt");
			try (var in = Files.newInputStream(fromFile)) {
				assertEquals(Paths.copy(null, toFile), 0L);
				assertEquals(Paths.copy(in, null), 0L);
				assertEquals(Paths.copy(in, toFile), 3L);
				assertFile(fromFile, toFile);
			}
		} finally {
			Paths.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopyWithBadInput() throws IOException {
		initFiles();
		try {
			@SuppressWarnings("resource")
			var badIn = IoStream.in(Assert::throwIo);
			var toFile2 = helper.path("x/y/z.txt");
			Assert.thrown(() -> Paths.copy(badIn, toFile2));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			Paths.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testWrite() throws IOException {
		initFiles();
		try {
			var ba = ByteProvider.of('a', 'b', 'c');
			var toFile = helper.path("x/x/x.txt");
			assertEquals(Paths.write(null, ba), 0);
			assertEquals(Paths.write(toFile, null), 0);
			assertEquals(Paths.write(toFile, ba), 3);
			assertFile(toFile, 'a', 'b', 'c');
			var badArray = badProvider(3);
			var toFile2 = helper.path("x/y/z.txt");
			Assert.thrown(() -> Paths.write(toFile2, badArray));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			Paths.deleteAll(helper.path("x"));
		}
	}

	private static ByteProvider badProvider(int length) {
		return new ByteProvider() {
			@Override
			public int writeTo(int index, OutputStream out, int length) throws IOException {
				throw new IOException("generated");
			}

			@Override
			public byte getByte(int index) {
				return 0;
			}

			@Override
			public int length() {
				return length;
			}
		};
	}

	private void initFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bb")
			.file("c.h", "c").dir("d").build();
	}

	private void assertPathListOf(Excepts.Consumer<IOException, PathList> action, String... paths)
		throws IOException {
		var pathFinder = PathList.of(helper.root);
		action.accept(pathFinder);
		helper.assertPaths(pathFinder.list(), paths);
	}

	private void assertPathListAll(Excepts.Consumer<IOException, PathList> action, String... paths)
		throws IOException {
		var pathFinder = PathList.all(helper.root);
		action.accept(pathFinder);
		helper.assertPaths(pathFinder.list(), paths);
	}
}
