package ceri.common.io;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertFile;
import static ceri.common.test.AssertUtil.assertHelperPaths;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPath;
import static ceri.common.test.AssertUtil.assertPaths;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.firstEnvironmentVariableName;
import static ceri.common.test.TestUtil.inputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.collection.WrappedStream;
import ceri.common.data.ByteProvider;
import ceri.common.io.IoStreamUtil.Read;
import ceri.common.test.AssertUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestUtil;
import ceri.common.util.SystemVars;

public class IoUtilTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder(IoUtil.systemTempDir()).file("a/a/a.txt", "aaa")
			.file("b/b.txt", "bbb").file("c.txt", "ccc").dir("d").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(IoUtil.class);
	}

	@Test
	public void testIoExceptionf() {
		assertEquals(IoUtil.ioExceptionf("%s", "test").getMessage(), "test");
		assertEquals(IoUtil.ioExceptionf(new Throwable(), "%s", "test").getMessage(), "test");
	}

	@Test
	public void testExecIo() throws IOException {
		IoUtil.IO_ADAPTER.run(() -> {});
		assertThrown(RuntimeException.class,
			() -> IoUtil.IO_ADAPTER.run(() -> Integer.valueOf(null)));
		assertThrown(IOException.class, () -> IoUtil.IO_ADAPTER.run(() -> {
			throw new Exception();
		}));
		assertThrown(IOException.class, () -> IoUtil.IO_ADAPTER.run(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testCallableIo() throws IOException {
		IoUtil.IO_ADAPTER.get(() -> "a");
		assertThrown(RuntimeException.class,
			() -> IoUtil.IO_ADAPTER.get(() -> Integer.valueOf(null)));
		assertThrown(IOException.class, () -> IoUtil.IO_ADAPTER.get(() -> {
			throw new Exception();
		}));
		assertThrown(IOException.class, () -> IoUtil.IO_ADAPTER.get(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testRuntimeIo() {
		IoUtil.RUNTIME_IO_ADAPTER.get(() -> "a");
		assertThrown(RuntimeException.class,
			() -> IoUtil.RUNTIME_IO_ADAPTER.get(() -> Integer.valueOf(null)));
		assertThrown(RuntimeIoException.class, () -> IoUtil.RUNTIME_IO_ADAPTER.get(() -> {
			throw new Exception();
		}));
		assertThrown(RuntimeIoException.class, () -> IoUtil.RUNTIME_IO_ADAPTER.get(() -> {
			throw new IOException();
		}));
		assertThrown(RuntimeIoException.class, () -> IoUtil.RUNTIME_IO_ADAPTER.get(() -> {
			throw new RuntimeIoException("");
		}));
	}

	@Test
	public void testClear() throws IOException {
		assertClearBuffer(new byte[0]);
		assertClearBuffer(new byte[100]);
		assertClearBuffer(new byte[32 * 1024]);
	}

	private void assertClearBuffer(byte[] buffer) throws IOException {
		InputStream in = new ByteArrayInputStream(buffer);
		assertEquals(IoUtil.clear(in), (long) buffer.length);
		assertEquals(in.available(), 0);
	}

	@Test
	public void testNullPrintStream() throws IOException {
		try (PrintStream out = IoUtil.nullPrintStream()) {
			out.write(-1);
			out.write(new byte[1000]);
			out.write(new byte[1000], 1, 999);
		}
	}

	@Test
	public void testSystemTempDir() {
		assertPath(IoUtil.systemTempDir(), SystemVars.sys("java.io.tmpdir"));
	}

	@Test
	public void testUserHome() {
		assertPath(IoUtil.userHome(), SystemVars.sys("user.home"));
		assertPath(IoUtil.userHome("test"), SystemVars.sys("user.home") + "/test");
	}

	@Test
	public void testUserDir() {
		assertPath(IoUtil.userDir(), SystemVars.sys("user.dir"));
		assertPath(IoUtil.userDir("test"), SystemVars.sys("user.dir") + "/test");
	}

	@Test
	public void testSystemPropertyPath() {
		assertNull(IoUtil.systemPropertyPath("?"));
	}

	@Test
	public void testEnvironmentPath() {
		assertNull(IoUtil.environmentPath("?"));
		String name = firstEnvironmentVariableName();
		assertPath(IoUtil.environmentPath(name), SystemVars.env(name));
	}

	@Test
	public void testPathVariable() {
		assertEquals(IoUtil.pathVariable(), "");
		assertEquals(IoUtil.pathVariable("a/b"), "a/b");
		assertEquals(IoUtil.pathVariable("a/b", "c/d"), "a/b" + File.pathSeparator + "c/d");
	}

	@Test
	public void testVariablePaths() {
		assertCollection(IoUtil.variablePaths(null));
		assertCollection(IoUtil.variablePaths(""));
		assertCollection(IoUtil.variablePaths(File.pathSeparator));
		assertCollection(IoUtil.variablePaths(" " + File.pathSeparator + " a"), "a");
		assertCollection(IoUtil.variablePaths("a/b" + File.pathSeparator + "a/b"), "a/b");
		assertCollection(IoUtil.variablePaths(" a/b" + File.pathSeparator + "a/b /c"), "a/b",
			"a/b /c");
	}

	@Test
	public void testExtend() throws IOException {
		try (ResourcePath rp = ResourcePath.of(String.class)) {
			assertNull(IoUtil.extend(null, "test"));
			assertPath(IoUtil.extend(rp.path()), rp.path().toString());
			assertPath(IoUtil.extend(rp.path(), "ref", "Finalizer.class"),
				rp.path() + "/ref/Finalizer.class");
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testRoot() {
		assertNull(IoUtil.root(null));
		assertPath(IoUtil.root(FileSystems.getDefault()), "/");
		TestFileSystem fs = TestFileSystem.of();
		assertNull(IoUtil.root(fs));
	}

	@Test
	public void testIsRoot() {
		assertFalse(IoUtil.isRoot(null));
		assertFalse(IoUtil.isRoot(Path.of("")));
		assertFalse(IoUtil.isRoot(Path.of("test")));
		assertFalse(IoUtil.isRoot(Path.of("/test")));
		assertTrue(IoUtil.isRoot(Path.of("/")));
	}

	@Test
	public void testChangeName() {
		assertNull(IoUtil.changeName(null, "test"));
		assertPath(IoUtil.changeName(Path.of(""), ""), "");
		assertPath(IoUtil.changeName(Path.of(""), "hello"), "hello");
		assertPath(IoUtil.changeName(Path.of("test"), "hello"), "hello");
		assertPath(IoUtil.changeName(Path.of("test/"), "hello"), "hello"); // path = "test"
		assertPath(IoUtil.changeName(Path.of("/"), "hello"), "/hello");
		assertPath(IoUtil.changeName(Path.of("/test"), "hello"), "/hello");
		assertPath(IoUtil.changeName(Path.of("/test/test"), "hello"), "/test/hello");
	}

	@Test
	public void testName() {
		assertNull(IoUtil.name(null, 0));
		assertNull(IoUtil.name(Path.of("/a/b/c/d"), -1));
		assertNull(IoUtil.name(Path.of("/a/b/c/d"), 4));
		assertEquals(IoUtil.name(Path.of("/a/b/c/d"), 0), "a");
		assertEquals(IoUtil.name(Path.of("/a/b/c/d"), 3), "d");
		assertNull(IoUtil.name(Path.of("/"), 0));
		assertEquals(IoUtil.name(Path.of(""), 0), "");
	}

	@Test
	public void testSubpath() {
		assertNull(IoUtil.subpath(null, 0));
		assertPath(IoUtil.subpath(Path.of("/"), 0), "");
		assertPath(IoUtil.subpath(Path.of(""), 0), "");
		assertThrown(() -> IoUtil.subpath(Path.of(""), 1));
		assertPath(IoUtil.subpath(Path.of("/a/b/c"), 0), "a/b/c");
		assertPath(IoUtil.subpath(Path.of("/a/b/c"), 2), "c");
		assertPath(IoUtil.subpath(Path.of("/a/b/c"), 3), "");
		assertThrown(() -> IoUtil.subpath(Path.of("/a/b/c"), 4));

		assertNull(IoUtil.subpath(null, 0, 0));
		assertPath(IoUtil.subpath(Path.of("a"), 0, 1), "a");
		assertPath(IoUtil.subpath(Path.of("/a/b/c/d"), 1, 3), "b/c");
		assertPath(IoUtil.subpath(Path.of("/a/b/c"), 0, 0), "");
		assertPath(IoUtil.subpath(Path.of("a/b/c"), 0, 0), "");
	}

	@Test
	public void testShorten() {
		assertNull(IoUtil.shorten(null, 0));
		assertPath(IoUtil.shorten(Path.of("a"), 0), "a");
		assertPath(IoUtil.shorten(Path.of("/"), 0), "/");
		assertThrown(() -> IoUtil.shorten(Path.of("/"), 1));
		assertPath(IoUtil.shorten(Path.of("/a/b/c"), 0), "/a/b/c");
		assertPath(IoUtil.shorten(Path.of("/a/b/c"), 2), "/a");
		assertPath(IoUtil.shorten(Path.of("/a/b/c"), 3), "/");
		assertPath(IoUtil.shorten(Path.of("a/b/c"), 0), "a/b/c");
		assertPath(IoUtil.shorten(Path.of("a/b/c"), 2), "a");
		assertPath(IoUtil.shorten(Path.of("a/b/c"), 3), "");
	}

	@Test
	public void testFilename() {
		assertNull(IoUtil.fileName(null));
		assertEquals(IoUtil.fileName(Path.of("/")), "");
		assertEquals(IoUtil.fileName(Path.of("")), "");
		assertEquals(IoUtil.fileName(Path.of("/a")), "a");
		assertEquals(IoUtil.fileName(Path.of("/a/b")), "b");
		assertEquals(IoUtil.fileName(Path.of("a")), "a");
		assertEquals(IoUtil.fileName(Path.of("a/b")), "b");
	}

	@Test
	public void testFileNameWithoutExt() {
		assertNull(IoUtil.fileNameWithoutExt(null));
		assertEquals(IoUtil.fileNameWithoutExt(Path.of("/")), "");
		assertEquals(IoUtil.fileNameWithoutExt(Path.of("")), "");
		assertEquals(IoUtil.fileNameWithoutExt(Path.of(".file")), ".file");
		assertEquals(IoUtil.fileNameWithoutExt(Path.of("/a")), "a");
		assertEquals(IoUtil.fileNameWithoutExt(Path.of("/a.txt")), "a");
		assertEquals(IoUtil.fileNameWithoutExt(Path.of("a.b.c")), "a.b");
	}

	@Test
	public void testExtension() {
		assertNull(IoUtil.extension(null));
		assertEquals(IoUtil.extension(Path.of("/")), "");
		assertEquals(IoUtil.extension(Path.of("")), "");
		assertEquals(IoUtil.extension(Path.of(".file")), "");
		assertEquals(IoUtil.extension(Path.of("/a")), "");
		assertEquals(IoUtil.extension(Path.of("/a.txt")), "txt");
		assertEquals(IoUtil.extension(Path.of("a.b.c")), "c");
	}

	@Test
	public void testCreateTempDir() throws IOException {
		Path tempDir = IoUtil.createTempDir(helper.root);
		assertTrue(Files.exists(tempDir));
		assertTrue(Files.isDirectory(tempDir));
		Files.delete(tempDir);
		try {
			tempDir = IoUtil.createTempDir(null);
			assertNull(tempDir.getParent());
		} finally {
			Files.delete(tempDir);
		}
	}

	@Test
	public void testDeleteAll() throws IOException {
		try (FileTestHelper deleteHelper = FileTestHelper.builder(helper.root).file("x/x/x.txt", "")
			.dir("y/y").file("z.txt", "").build()) {
			assertFalse(IoUtil.deleteAll(null));
			assertFalse(IoUtil.deleteAll(Path.of("XXX/XXX/XXX/XXX")));
			assertThrown(() -> IoUtil.deleteAll(Path.of("/XXX/XXX/XXX")));
			assertFalse(IoUtil.deleteAll(deleteHelper.path("z.txt")));
			assertTrue(Files.exists(deleteHelper.path("x/x/x.txt")));
			assertTrue(Files.exists(deleteHelper.path("y/y")));
			assertTrue(Files.exists(deleteHelper.path("z.txt")));
			IoUtil.deleteAll(deleteHelper.root);
			assertFalse(Files.exists(deleteHelper.root));
		}
	}

	@Test
	public void testDeleteEmptyDirs() throws IOException {
		try (FileTestHelper deleteHelper =
			FileTestHelper.builder(helper.root).dir("x/x/x").file("y/y.txt", "").dir("z").build()) {
			assertTrue(Files.exists(deleteHelper.path("x/x/x")));
			assertTrue(Files.exists(deleteHelper.path("y/y.txt")));
			assertTrue(Files.exists(deleteHelper.path("z")));
			IoUtil.deleteEmptyDirs(deleteHelper.root);
			assertFalse(Files.exists(deleteHelper.path("x/x/x")));
			assertTrue(Files.exists(deleteHelper.path("y/y.txt")));
			assertFalse(Files.exists(deleteHelper.path("z")));
		}
	}

	@Test
	public void testIsEmptyDir() throws IOException {
		assertFalse(IoUtil.isEmptyDir(null));
		assertFalse(IoUtil.isEmptyDir(helper.path("a")));
		assertFalse(IoUtil.isEmptyDir(helper.path("a/a/a.txt")));
		assertTrue(IoUtil.isEmptyDir(helper.path("d")));
	}

	@SuppressWarnings("resource")
	@Test
	public void testExecOrClose() throws IOException {
		TestInputStream in = TestInputStream.of();
		assertEquals(IoUtil.execOrClose(in, InputStream::close), in);
		in.close.error.setFrom(IOX);
		assertThrown(() -> IoUtil.execOrClose(in, InputStream::close));
	}

	@Test
	public void testClose() {
		final StringReader in = new StringReader("0123456789");
		assertFalse(IoUtil.close(null));
		assertTrue(IoUtil.close(in));
		assertThrown(IOException.class, in::read);
	}

	@Test
	public void testCloseException() {
		@SuppressWarnings("resource")
		Closeable closeable = () -> {
			throw new IOException();
		};
		assertFalse(IoUtil.close(closeable));
	}

	@Test
	public void testPollString() throws IOException {
		try (InputStream in = TestUtil.inputStream("test")) {
			String s = IoUtil.pollString(in);
			assertEquals(s, "test");
		}
	}

	@Test
	public void testAvailableChar() throws IOException {
		try (SystemIo sys = SystemIo.of()) {
			sys.in(TestUtil.inputStream("test"));
			assertEquals(IoUtil.availableChar(), 't');
			assertEquals(IoUtil.availableChar(), 'e');
			assertEquals(IoUtil.availableChar(), 's');
			assertEquals(IoUtil.availableChar(), 't');
			sys.in().close();
			assertEquals(IoUtil.availableChar(), '\0');
		}
		try (TestInputStream in = TestInputStream.of()) {
			in.available.error.setFrom(IOX);
			assertEquals(IoUtil.availableChar(in), '\0');
		}
	}

	@Test
	public void testAvailableString() throws IOException {
		assertNull(IoUtil.availableString(null));
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			assertEquals(IoUtil.availableString(in), "test");
			assertEquals(IoUtil.availableString(in), "");
		}
	}

	@Test
	public void testAvailableBytes() throws IOException {
		assertNull(IoUtil.availableBytes(null));
		try (InputStream in = new ByteArrayInputStream(ArrayUtil.bytes(0, 1, 2, 3, 4))) {
			assertEquals(IoUtil.availableBytes(in), ByteProvider.of(0, 1, 2, 3, 4));
			assertEquals(IoUtil.availableBytes(in), ByteProvider.empty());
		}
		try (InputStream in = IoStreamUtil.in((buf, off, len) -> 0, () -> 3)) {
			assertEquals(IoUtil.availableBytes(in), ByteProvider.empty());
		}
	}

	@Test
	public void testReadBytes() throws IOException {
		assertEquals(IoUtil.readBytes(null, null), 0);
		try (InputStream in = new ByteArrayInputStream(ArrayUtil.bytes(0, 1, 2, 3, 4))) {
			assertEquals(IoUtil.readBytes(in, null), 0);
			byte[] buffer = new byte[4];
			IoUtil.readBytes(in, buffer);
			assertArray(buffer, 0, 1, 2, 3);
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testReadNext() throws IOException {
		assertNull(IoUtil.readNext(null));
		try (var in = TestInputStream.of()) {
			in.to.writeBytes(1, 2, 3);
			assertArray(IoUtil.readNext(in), 1, 2, 3);
			in.to.writeBytes(4);
			assertArray(IoUtil.readNext(in), 4);
			in.to.writeBytes(5);
			in.read.autoResponses(-1);
			assertArray(IoUtil.readNext(in));
		}
	}

	@SuppressWarnings("resource")
	@Test
	public void testReadNextString() throws IOException {
		assertNull(IoUtil.readNext(null));
		try (var in = TestInputStream.of()) {
			in.to.writeBytes('a', 'b', 'c');
			assertEquals(IoUtil.readNextString(in), "abc");
			in.to.writeBytes('d');
			assertEquals(IoUtil.readNextString(in), "d");
			in.to.writeBytes(5);
			in.read.autoResponses(-1);
			assertEquals(IoUtil.readNextString(in), "");
		}
	}

	@Test
	public void testPollForData() throws IOException {
		int[] available = { 0 };
		try (var in = IoStreamUtil.in((Read) null, () -> available[0])) {
			assertThrown(IoTimeoutException.class, () -> IoUtil.pollForData(in, 1, 1, 1));
			available[0] = 3;
			assertEquals(IoUtil.pollForData(in, 1, 0, 1), 3);
		}
	}

	@Test
	public void testPathToUnix() {
		assertEquals(IoUtil.pathToUnix(Path.of("a", "b", "c")), "a/b/c");
		assertEquals(IoUtil.pathToUnix(Path.of("")), "");
	}

	@Test
	public void testUnixToPath() {
		String path = "a" + File.separatorChar + "b" + File.separatorChar + "c";
		assertEquals(IoUtil.unixToPath("a/b/c"), path);
	}

	@Test
	public void testConvertPath() {
		assertEquals(IoUtil.convertPath("a\\b\\c", '\\', '/'), "a/b/c");
	}

	@SuppressWarnings("resource")
	@Test
	public void testWalkRelative() throws IOException {
		assertStreamPaths(IoUtil.walkRelative(helper.root), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
		assertStreamPaths(IoUtil.walkRelative(helper.root, "glob:**/{a,b}"), "a", "a/a", "b");
		assertStreamPaths(IoUtil.walkRelative(helper.root, (String) null), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
	}

	@SuppressWarnings("resource")
	@Test
	public void testWalk() throws IOException {
		assertStreamHelperPaths(IoUtil.walk(helper.root), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
		assertStreamHelperPaths(IoUtil.walk(helper.root, "glob:**/{a,b}"), "a", "a/a", "b");
		assertStreamHelperPaths(IoUtil.walk(helper.root, (String) null), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
	}

	@Test
	public void testPathsRelative() throws IOException {
		assertPaths(IoUtil.pathsRelative(helper.root), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
		assertPaths(IoUtil.pathsRelative(helper.root, "glob:**/{a,b}"), "a", "a/a", "b");
		assertPaths(IoUtil.pathsRelative(helper.root, (String) null), //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
	}

	@Test
	public void testPaths() throws IOException {
		assertHelperPaths(IoUtil.paths(helper.root), helper, //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
		assertHelperPaths(IoUtil.paths(helper.root, "glob:**/{a,b}"), helper, "a", "a/a", "b");
		assertHelperPaths(IoUtil.paths(helper.root, (String) null), helper, //
			"", "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt", "d");
	}

	@Test
	public void testPathsCollect() throws IOException {
		assertCollection(IoUtil.pathsCollect(helper.root, //
			path -> helper.root.equals(path) ? null : path.getFileName().toString().charAt(0)), //
			'a', 'a', 'a', 'b', 'b', 'c', 'd');
	}

	@Test
	public void testListNames() throws IOException {
		assertCollection(IoUtil.listNames(helper.root), "a", "b", "c.txt", "d");
		assertCollection(IoUtil.listNames(helper.path("a")), "a");
		assertCollection(IoUtil.listNames(helper.path("a/a")), "a.txt");
		assertCollection(IoUtil.listNames(helper.path("d")));
		assertCollection(IoUtil.listNames(helper.root, //
			path -> path.getFileName().toString().length() == 1), "a", "b", "d");
		assertCollection(IoUtil.listNames(helper.root, "regex:a|.*\\.txt"), "a", "c.txt");
		assertCollection(IoUtil.listNames(helper.root, (String) null), "a", "b", "c.txt", "d");
	}

	@Test
	public void testList() throws IOException {
		assertHelperPaths(IoUtil.list(helper.root), helper, "a", "b", "c.txt", "d");
		assertHelperPaths(IoUtil.list(helper.path("a")), helper, "a/a");
		assertHelperPaths(IoUtil.list(helper.path("a/a")), helper, "a/a/a.txt");
		assertHelperPaths(IoUtil.list(helper.path("d")), helper);
		assertHelperPaths(IoUtil.list(helper.root, //
			path -> path.getFileName().toString().length() == 1), helper, "a", "b", "d");
		assertHelperPaths(IoUtil.list(helper.root, "glob:?"), helper, "a", "b", "d");
		assertHelperPaths(IoUtil.list(helper.root, (String) null), helper, "a", "b", "c.txt", "d");
	}

	@Test
	public void testListCollect() throws IOException {
		try (var stream = Files.newDirectoryStream(helper.root)) {
			assertCollection(IoUtil.listCollect(stream, //
				path -> path.getFileName().toString().charAt(0)), 'a', 'b', 'c', 'd');
		}
	}

	@Test
	public void testDirStreamForEach() {
		assertThrown(() -> IoUtil.dirStreamForEach(Files.newDirectoryStream(helper.root), path -> {
			throw new IOException();
		}));
	}

	@Test
	public void testReadString() throws IOException {
		InputStream in = inputStream("abc\0");
		assertEquals(IoUtil.readString(in), "abc\0");
	}

	@Test
	public void testLines() {
		InputStream in = inputStream("line0\n\nline2\nend");
		assertStream(IoUtil.lines(in), "line0", "", "line2", "end");
	}

	@Test
	public void testCopyFile() throws IOException {
		try {
			Path fromFile = helper.path("a/a/a.txt");
			Path toFile = helper.path("x/x/x.txt");
			IoUtil.copyFile(fromFile, toFile);
			assertFile(fromFile, toFile);
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopyBadFile() throws IOException {
		try {
			TestPath badFile = TestPath.of();
			Path toFile2 = helper.path("x/y/z.txt");
			assertThrown(() -> IoUtil.copyFile(badFile, toFile2));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopy() throws IOException {
		try {
			Path fromFile = helper.path("a/a/a.txt");
			Path toFile = helper.path("x/x/x.txt");
			try (InputStream in = Files.newInputStream(fromFile)) {
				long n = IoUtil.copy(in, toFile);
				assertEquals(n, 3L);
				assertFile(fromFile, toFile);
			}
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopyWithBadInput() throws IOException {
		try {
			@SuppressWarnings("resource")
			InputStream badIn = IoStreamUtil.in(AssertUtil::throwIo);
			Path toFile2 = helper.path("x/y/z.txt");
			assertThrown(() -> IoUtil.copy(badIn, toFile2));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testWrite() throws IOException {
		try {
			ByteProvider ba = ByteProvider.of('a', 'b', 'c');
			Path toFile = helper.path("x/x/x.txt");
			IoUtil.write(toFile, ba);
			assertFile(toFile, 'a', 'b', 'c');
			ByteProvider badArray = badProvider(3);
			Path toFile2 = helper.path("x/y/z.txt");
			assertThrown(() -> IoUtil.write(toFile2, badArray));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			IoUtil.deleteAll(helper.path("x"));
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

	@Test
	public void testClassUrl() {
		assertNull(IoUtil.classUrl(null));
		URL url = IoUtil.classUrl(String.class);
		assertTrue(url.getPath().endsWith("/java/lang/String.class"));
		url = IoUtil.classUrl(Test.class);
		assertTrue(url.getPath().endsWith("/org/junit/Test.class"));
		url = IoUtil.classUrl(IoUtil.class);
		assertTrue(url.getPath().endsWith("/ceri/common/io/IoUtil.class"));
	}

	@Test
	public void testResource() throws IOException {
		byte[] content = IoUtil.resource(getClass(), getClass().getSimpleName() + ".properties");
		assertArray(content, 'a', '=', 'b');
		content = IoUtil.resource(String.class, "String.class"); // module
		assertTrue(content.length > 0);
		content = IoUtil.resource(Test.class, "Test.class"); // jar
		assertTrue(content.length > 0);
	}

	@Test
	public void testResourceString() throws IOException {
		String s = IoUtil.resourceString(getClass(), getClass().getSimpleName() + ".properties");
		assertEquals(s, "a=b");
	}

	private void assertStreamHelperPaths(WrappedStream<IOException, Path> actual, String... paths)
		throws IOException {
		assertHelperPaths(actual.collect(Collectors.toList()), helper, paths);
	}

	private void assertStreamPaths(WrappedStream<IOException, Path> actual, String... paths)
		throws IOException {
		assertPaths(actual.collect(Collectors.toList()), paths);
	}

}
