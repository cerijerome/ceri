package ceri.common.io;

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
import ceri.common.test.Testing;
import ceri.common.util.SystemVars;

public class PathsTest {
	private FileTestHelper helper;
	private FileTestHelper deleter;

	@After
	public void after() {
		deleter = Testing.close(deleter);
		helper = Testing.close(helper);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Paths.class);
		Assert.privateConstructor(Paths.Filter.class);
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
		Assert.path(Paths.root(null), null);
		Assert.path(Paths.root(FileSystems.getDefault()), "/");
		Assert.path(Paths.root(TestFileSystem.of()), null);
	}

	@Test
	public void testIsRoot() {
		Assert.equal(Paths.isRoot(null), false);
		Assert.equal(Paths.isRoot(Path.of("")), false);
		Assert.equal(Paths.isRoot(Path.of("test")), false);
		Assert.equal(Paths.isRoot(Path.of("/test")), false);
		Assert.equal(Paths.isRoot(Path.of("/")), true);
	}

	@Test
	public void testIsEmpty() {
		Assert.equal(Paths.isEmpty(null), true);
		Assert.equal(Paths.isEmpty(Path.of("")), false);
		Assert.equal(Paths.isEmpty(Path.of("").getRoot()), true);
	}

	@Test
	public void testPatterns() {
		Assert.string(Paths.glob(null), "glob:");
		Assert.string(Paths.glob("**/*.*"), "glob:**/*.*");
		Assert.string(Paths.regex(null), "regex:");
		Assert.string(Paths.regex(".*/[^/]+*\\.[^/]+"), "regex:.*/[^/]+*\\.[^/]+");
	}

	@Test
	public void testName() {
		Assert.string(Paths.name(null), null);
		Assert.string(Paths.name(Path.of("/")), "");
		Assert.string(Paths.name(Path.of("")), "");
		Assert.string(Paths.name(Path.of("/a")), "a");
		Assert.string(Paths.name(Path.of("/a/b")), "b");
		Assert.string(Paths.name(Path.of("a")), "a");
		Assert.string(Paths.name(Path.of("a/b")), "b");
	}

	@Test
	public void testNameWithIndex() {
		Assert.string(Paths.name(null, 0), null);
		Assert.string(Paths.name(Path.of("/a/b/c/d"), -5), null);
		Assert.string(Paths.name(Path.of("/a/b/c/d"), -4), "a");
		Assert.string(Paths.name(Path.of("/a/b/c/d"), -1), "d");
		Assert.string(Paths.name(Path.of("/a/b/c/d"), 4), null);
		Assert.string(Paths.name(Path.of("/a/b/c/d"), 0), "a");
		Assert.string(Paths.name(Path.of("/a/b/c/d"), 3), "d");
		Assert.string(Paths.name(Path.of("/"), 0), null);
		Assert.string(Paths.name(Path.of(""), 0), "");
	}

	@Test
	public void testNameWithoutExt() {
		Assert.string(Paths.nameWithoutExt((Path) null), null);
		Assert.string(Paths.nameWithoutExt(Path.of("/")), "");
		Assert.string(Paths.nameWithoutExt(Path.of("")), "");
		Assert.string(Paths.nameWithoutExt(Path.of(".file")), ".file");
		Assert.string(Paths.nameWithoutExt(Path.of("/a")), "a");
		Assert.string(Paths.nameWithoutExt(Path.of("/a.txt")), "a");
		Assert.string(Paths.nameWithoutExt(Path.of("a.b.c")), "a.b");
	}

	@Test
	public void testExt() {
		Assert.string(Paths.ext((Path) null), null);
		Assert.string(Paths.ext(Path.of("/")), "");
		Assert.string(Paths.ext(Path.of("")), "");
		Assert.string(Paths.ext(Path.of(".file")), "");
		Assert.string(Paths.ext(Path.of("/a")), "");
		Assert.string(Paths.ext(Path.of("/a.txt")), "txt");
		Assert.string(Paths.ext(Path.of("a.b.c")), "c");
	}

	@Test
	public void testLastModified() throws IOException {
		Assert.equal(Paths.lastModified(null), null);
	}

	@Test
	public void testSub() {
		Assert.path(Paths.sub(null, 0), null);
		Assert.path(Paths.sub(Path.of("/"), 0), "");
		Assert.path(Paths.sub(Path.of(""), 0), "");
		Assert.thrown(() -> Paths.sub(Path.of(""), 1));
		Assert.path(Paths.sub(Path.of("/a/b/c"), 0), "a/b/c");
		Assert.path(Paths.sub(Path.of("/a/b/c"), 2), "c");
		Assert.path(Paths.sub(Path.of("/a/b/c"), 3), "");
		Assert.thrown(() -> Paths.sub(Path.of("/a/b/c"), 4));
		Assert.path(Paths.sub(null, 0, 0), null);
		Assert.path(Paths.sub(Path.of("a"), 0, 1), "a");
		Assert.path(Paths.sub(Path.of("/a/b/c/d"), 1, 3), "b/c");
		Assert.path(Paths.sub(Path.of("/a/b/c"), 0, 0), "");
		Assert.path(Paths.sub(Path.of("a/b/c"), 0, 0), "");
	}

	@Test
	public void testExtend() throws IOException {
		try (var r = Resource.of(String.class)) {
			Assert.path(Paths.extend(null, "test"), null);
			Assert.path(Paths.extend(r.path()), r.path().toString());
			Assert.path(Paths.extend(r.path(), "ref", "Finalizer.class"),
				r.path() + "/ref/Finalizer.class");
		}
	}

	@Test
	public void testNewPath() {
		Assert.equal(Paths.newPath(null, null), null);
	}

	@Test
	public void testChangeName() {
		Assert.path(Paths.changeName(null, "test"), null);
		Assert.path(Paths.changeName(Path.of(""), ""), "");
		Assert.path(Paths.changeName(Path.of(""), "hello"), "hello");
		Assert.path(Paths.changeName(Path.of("test"), "hello"), "hello");
		Assert.path(Paths.changeName(Path.of("test/"), "hello"), "hello"); // path = "test"
		Assert.path(Paths.changeName(Path.of("/"), "hello"), "/hello");
		Assert.path(Paths.changeName(Path.of("/test"), "hello"), "/hello");
		Assert.path(Paths.changeName(Path.of("/test/test"), "hello"), "/test/hello");
	}

	@Test
	public void testChangeNameFunction() {
		Assert.path(Paths.changeName(Path.of("/test"), f -> f + "0"), "/test0");
		Assert.path(Paths.changeName(Path.of("/test"), _ -> null), "/test");
	}

	@Test
	public void testShorten() {
		Assert.path(Paths.shorten(null, 0), null);
		Assert.path(Paths.shorten(Path.of("a"), 0), "a");
		Assert.path(Paths.shorten(Path.of("/"), 0), "/");
		Assert.thrown(() -> Paths.shorten(Path.of("/"), 1));
		Assert.path(Paths.shorten(Path.of("/a/b/c"), 0), "/a/b/c");
		Assert.path(Paths.shorten(Path.of("/a/b/c"), 2), "/a");
		Assert.path(Paths.shorten(Path.of("/a/b/c"), 3), "/");
		Assert.path(Paths.shorten(Path.of("a/b/c"), 0), "a/b/c");
		Assert.path(Paths.shorten(Path.of("a/b/c"), 2), "a");
		Assert.path(Paths.shorten(Path.of("a/b/c"), 3), "");
	}

	@Test
	public void testCreateTempDir() throws IOException {
		helper = FileTestHelper.builder(SystemVars.tempDir()).build();
		var tempDir = Paths.createTempDir(helper.root);
		Assert.equal(Files.exists(tempDir), true);
		Assert.equal(Files.isDirectory(tempDir), true);
		Files.delete(tempDir);
		try {
			tempDir = Paths.createTempDir(null);
			Assert.path(tempDir.getParent(), null);
		} finally {
			Files.delete(tempDir);
		}
	}

	@Test
	public void testDeleteAll() throws IOException {
		initFiles();
		deleter = FileTestHelper.builder(helper.root).file("x/x/x.txt", "").dir("y/y")
			.file("z.txt", "").build();
		Assert.equal(Paths.deleteAll(null), false);
		Assert.equal(Paths.deleteAll(Path.of("XXX/XXX/XXX/XXX")), false);
		Assert.thrown(() -> Paths.deleteAll(Path.of("/XXX/XXX/XXX")));
		Assert.equal(Paths.deleteAll(deleter.path("z.txt")), false);
		Assert.equal(Files.exists(deleter.path("x/x/x.txt")), true);
		Assert.equal(Files.exists(deleter.path("y/y")), true);
		Assert.equal(Files.exists(deleter.path("z.txt")), true);
		Paths.deleteAll(deleter.root);
		Assert.equal(Files.exists(deleter.root), false);
	}

	@Test
	public void testDeleteEmptyDirs() throws IOException {
		initFiles();
		Paths.deleteEmptyDirs(null);
		deleter =
			FileTestHelper.builder(helper.root).dir("x/x/x").file("y/y.txt", "").dir("z").build();
		Assert.equal(Files.exists(deleter.path("x/x/x")), true);
		Assert.equal(Files.exists(deleter.path("y/y.txt")), true);
		Assert.equal(Files.exists(deleter.path("z")), true);
		Paths.deleteEmptyDirs(deleter.root);
		Assert.equal(Files.exists(deleter.path("x/x/x")), false);
		Assert.equal(Files.exists(deleter.path("y/y.txt")), true);
		Assert.equal(Files.exists(deleter.path("z")), false);
	}

	@Test
	public void testIsEmptyDir() throws IOException {
		initFiles();
		Assert.no(Paths.isEmptyDir(null));
		Assert.no(Paths.isEmptyDir(helper.path("a")));
		Assert.no(Paths.isEmptyDir(helper.path("a/a/a.txt")));
		Assert.yes(Paths.isEmptyDir(helper.path("d")));
	}

	@Test
	public void testToUnix() {
		Assert.equal(Paths.toUnix((String) null), null);
		Assert.equal(Paths.toUnix((Path) null), null);
		Assert.equal(Paths.toUnix(Path.of("a", "b", "c")), "a/b/c");
		Assert.equal(Paths.toUnix(Path.of("")), "");
	}

	@Test
	public void testFromUnix() {
		Assert.equal(Paths.fromUnix(null), null);
		Assert.equal(Paths.fromUnix("a/b/c"), String.format("a%sb%1$sc", File.separatorChar));
	}

	@Test
	public void testConvertPath() {
		Assert.equal(Paths.convert("a\\b\\c", '\\', '/'), "a/b/c");
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
			Assert.file(fromFile, toFile);
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
			Assert.no(Files.exists(helper.path("x/y")));
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
				Assert.equal(Paths.copy(null, toFile), 0L);
				Assert.equal(Paths.copy(in, null), 0L);
				Assert.equal(Paths.copy(in, toFile), 3L);
				Assert.file(fromFile, toFile);
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
			Assert.no(Files.exists(helper.path("x/y")));
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
			Assert.equal(Paths.write(null, ba), 0);
			Assert.equal(Paths.write(toFile, null), 0);
			Assert.equal(Paths.write(toFile, ba), 3);
			Assert.file(toFile, 'a', 'b', 'c');
			var badArray = badProvider(3);
			var toFile2 = helper.path("x/y/z.txt");
			Assert.thrown(() -> Paths.write(toFile2, badArray));
			Assert.no(Files.exists(helper.path("x/y")));
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
