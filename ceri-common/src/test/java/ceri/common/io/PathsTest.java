package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertPath;
import static ceri.common.test.AssertUtil.assertPaths;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.AssertUtil.assertUnordered;
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
import ceri.common.test.AssertUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;
import ceri.common.util.SystemVars;

public class PathsTest {
	private FileTestHelper files;
	private FileTestHelper deleter;

	@After
	public void after() {
		deleter = TestUtil.close(deleter);
		files = TestUtil.close(files);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Paths.class);
		assertPrivateConstructor(Paths.Filter.class);
	}

	@Test
	public void testFilters() throws IOException {
		initFiles();
		files.assertPaths(Paths.list(files.root, Paths.Filter.file()), "c.h");
		files.assertPaths(Paths.paths(files.root, Paths.Filter.file()), "a/a/a.txt", "b/b.txt",
			"c.h");
		files.assertPaths(Paths.list(files.root, Paths.Filter.dir()), "a", "b", "d");
		files.assertPaths(Paths.paths(files.root, Paths.Filter.dir()), "", "a", "a/a", "b", "d");
		files.assertPaths(
			Paths.paths(files.root,
				Paths.Filter.namePath(path -> path.toString().endsWith(".txt"))),
			"a/a/a.txt", "b/b.txt");
		files.assertPaths(Paths.paths(files.root, Paths.Filter.name(s -> s.endsWith(".txt"))),
			"a/a/a.txt", "b/b.txt");
		files.assertPaths(Paths.paths(files.root, Paths.Filter.ext(s -> "txt".equals(s))),
			"a/a/a.txt", "b/b.txt");
		var now = Instant.now();
		files.assertPaths(Paths.list(files.root, Paths.Filter.lastModified(d -> d.isBefore(now))),
			"a", "b", "c.h", "d");
		files.assertPaths(Paths.list(files.root, Paths.Filter.lastModified(d -> d.isAfter(now))));
		files.assertPaths(Paths.paths(files.root, Paths.Filter.size(s -> s == 0)));
		files.assertPaths(Paths.paths(files.root, Paths.Filter.size(s -> s < 2)), "c.h");
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
		assertThrown(() -> Paths.sub(Path.of(""), 1));
		assertPath(Paths.sub(Path.of("/a/b/c"), 0), "a/b/c");
		assertPath(Paths.sub(Path.of("/a/b/c"), 2), "c");
		assertPath(Paths.sub(Path.of("/a/b/c"), 3), "");
		assertThrown(() -> Paths.sub(Path.of("/a/b/c"), 4));
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
		assertThrown(() -> Paths.shorten(Path.of("/"), 1));
		assertPath(Paths.shorten(Path.of("/a/b/c"), 0), "/a/b/c");
		assertPath(Paths.shorten(Path.of("/a/b/c"), 2), "/a");
		assertPath(Paths.shorten(Path.of("/a/b/c"), 3), "/");
		assertPath(Paths.shorten(Path.of("a/b/c"), 0), "a/b/c");
		assertPath(Paths.shorten(Path.of("a/b/c"), 2), "a");
		assertPath(Paths.shorten(Path.of("a/b/c"), 3), "");
	}

	@Test
	public void testCreateTempDir() throws IOException {
		files = FileTestHelper.builder(SystemVars.tempDir()).build();
		var tempDir = Paths.createTempDir(files.root);
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
		deleter = FileTestHelper.builder(files.root).file("x/x/x.txt", "").dir("y/y")
			.file("z.txt", "").build();
		assertEquals(Paths.deleteAll(null), false);
		assertEquals(Paths.deleteAll(Path.of("XXX/XXX/XXX/XXX")), false);
		assertThrown(() -> Paths.deleteAll(Path.of("/XXX/XXX/XXX")));
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
			FileTestHelper.builder(files.root).dir("x/x/x").file("y/y.txt", "").dir("z").build();
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
		assertFalse(Paths.isEmptyDir(files.path("a")));
		assertFalse(Paths.isEmptyDir(files.path("a/a/a.txt")));
		assertTrue(Paths.isEmptyDir(files.path("d")));
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
	public void testPathsRelative() throws IOException {
		initFiles();
		assertPaths(Paths.pathsRelative(null));
		assertPaths(Paths.pathsRelative(files.root), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.h", "d");
		assertPaths(Paths.pathsRelative(files.root, "glob:**/{a,b}"), "a", "a/a", "b");
		assertPaths(Paths.pathsRelative(files.root, (String) null), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.h", "d");
	}

	@Test
	public void testPaths() throws IOException {
		initFiles();
		files.assertPaths(Paths.paths(files.root), "", "a", "a/a", "a/a/a.txt", "b", "b/b.txt",
			"c.h", "d");
		files.assertPaths(Paths.paths(files.root, "glob:**/{a,b}"), "a", "a/a", "b");
		files.assertPaths(Paths.paths(files.root, (String) null), "", "a", "a/a", "a/a/a.txt", "b",
			"b/b.txt", "c.h", "d");
	}

	@Test
	public void testPathsCollect() throws IOException {
		initFiles();
		assertUnordered(Paths.pathsCollect(null, _ -> null));
		assertUnordered(Paths.pathsCollect(files.root, null));
		assertUnordered(Paths.pathsCollect(files.root, //
			path -> files.root.equals(path) ? null : path.getFileName().toString().charAt(0)), //
			'a', 'a', 'a', 'b', 'b', 'c', 'd');
	}

	@Test
	public void testListNames() throws IOException {
		initFiles();
		assertUnordered(Paths.listNames(null));
		assertUnordered(Paths.listNames(files.root), "a", "b", "c.h", "d");
		assertUnordered(Paths.listNames(files.path("a")), "a");
		assertUnordered(Paths.listNames(files.path("a/a")), "a.txt");
		assertUnordered(Paths.listNames(files.path("d")));
		assertUnordered(
			Paths.listNames(files.root, path -> path.getFileName().toString().length() == 1), "a",
			"b", "d");
		assertUnordered(Paths.listNames(files.root, "regex:a|.*\\.h"), "a", "c.h");
		assertUnordered(Paths.listNames(files.root, (String) null), "a", "b", "c.h", "d");
	}

	@Test
	public void testList() throws IOException {
		initFiles();
		files.assertPaths(Paths.list(null));
		files.assertPaths(Paths.list(files.root), "a", "b", "c.h", "d");
		files.assertPaths(Paths.list(files.path("a")), "a/a");
		files.assertPaths(Paths.list(files.path("a/a")), "a/a/a.txt");
		files.assertPaths(Paths.list(files.path("d")));
		files.assertPaths(
			Paths.list(files.root, path -> path.getFileName().toString().length() == 1), "a", "b",
			"d");
		files.assertPaths(Paths.list(files.root, "glob:?"), "a", "b", "d");
		files.assertPaths(Paths.list(files.root, (String) null), "a", "b", "c.h", "d");
	}

	@Test
	public void testListCollect() throws IOException {
		initFiles();
		assertUnordered(Paths.listCollect(null, _ -> null));
		assertUnordered(Paths.listCollect(files.root, null));
		assertUnordered(Paths.listCollect(files.root, //
			path -> path.getFileName().toString().charAt(0)), 'a', 'b', 'c', 'd');
	}

	@Test
	public void testCopyFile() throws IOException {
		initFiles();
		try {
			var fromFile = files.path("a/a/a.txt");
			var toFile = files.path("x/x/x.txt");
			Paths.copyFile(null, toFile); // ignored
			Paths.copyFile(fromFile, null); // ignored
			Paths.copyFile(fromFile, toFile);
			assertFile(fromFile, toFile);
		} finally {
			Paths.deleteAll(files.path("x"));
		}
	}

	@Test
	public void testCopyBadFile() throws IOException {
		initFiles();
		try {
			var badFile = TestPath.of();
			var toFile2 = files.path("x/y/z.txt");
			assertThrown(() -> Paths.copyFile(badFile, toFile2));
			assertFalse(Files.exists(files.path("x/y")));
		} finally {
			Paths.deleteAll(files.path("x"));
		}
	}

	@Test
	public void testCopy() throws IOException {
		initFiles();
		try {
			var fromFile = files.path("a/a/a.txt");
			var toFile = files.path("x/x/x.txt");
			try (var in = Files.newInputStream(fromFile)) {
				assertEquals(Paths.copy(null, toFile), 0L);
				assertEquals(Paths.copy(in, null), 0L);
				assertEquals(Paths.copy(in, toFile), 3L);
				assertFile(fromFile, toFile);
			}
		} finally {
			Paths.deleteAll(files.path("x"));
		}
	}

	@Test
	public void testCopyWithBadInput() throws IOException {
		initFiles();
		try {
			@SuppressWarnings("resource")
			var badIn = IoStream.in(AssertUtil::throwIo);
			var toFile2 = files.path("x/y/z.txt");
			assertThrown(() -> Paths.copy(badIn, toFile2));
			assertFalse(Files.exists(files.path("x/y")));
		} finally {
			Paths.deleteAll(files.path("x"));
		}
	}

	@Test
	public void testWrite() throws IOException {
		initFiles();
		try {
			var ba = ByteProvider.of('a', 'b', 'c');
			var toFile = files.path("x/x/x.txt");
			assertEquals(Paths.write(null, ba), 0);
			assertEquals(Paths.write(toFile, null), 0);
			assertEquals(Paths.write(toFile, ba), 3);
			assertFile(toFile, 'a', 'b', 'c');
			var badArray = badProvider(3);
			var toFile2 = files.path("x/y/z.txt");
			assertThrown(() -> Paths.write(toFile2, badArray));
			assertFalse(Files.exists(files.path("x/y")));
		} finally {
			Paths.deleteAll(files.path("x"));
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
		files = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bb")
			.file("c.h", "c").dir("d").build();
	}
}
