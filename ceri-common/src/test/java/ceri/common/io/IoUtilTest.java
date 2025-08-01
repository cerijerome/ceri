package ceri.common.io;

import static ceri.common.io.IoUtil.EOL_BYTES;
import static ceri.common.test.AssertUtil.assertArray;
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
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.TestUtil.firstEnvironmentVariableName;
import static ceri.common.test.TestUtil.inputStream;
import static ceri.common.text.StringUtil.EOL;
import static java.nio.charset.StandardCharsets.US_ASCII;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.data.ByteProvider;
import ceri.common.exception.Exceptions;
import ceri.common.function.Excepts;
import ceri.common.io.IoStreamUtil.Read;
import ceri.common.test.AssertUtil;
import ceri.common.test.CallSync;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestInputStream;
import ceri.common.test.TestUtil;
import ceri.common.text.StringUtil;
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
	public void testEolBytes() {
		assertEquals(IoUtil.eolBytes(null), EOL_BYTES);
		assertEquals(IoUtil.eolBytes(Charset.defaultCharset()), EOL_BYTES);
		assertArray(IoUtil.eolBytes(US_ASCII), EOL.getBytes(US_ASCII));
	}

	@Test
	public void testIoExceptionf() {
		assertEquals(Exceptions.io("%s", "test").getMessage(), "test");
		assertEquals(Exceptions.io(new Throwable(), "%s", "test").getMessage(), "test");
	}

	@Test
	public void testClearReader() throws IOException {
		assertEquals(IoUtil.clear(new StringReader(StringUtil.repeat('x', 0x41))), 0x41L);
	}

	@Test
	public void testClearInputStream() throws IOException {
		assertClearBuffer(new byte[0]);
		assertClearBuffer(new byte[100]);
		assertClearBuffer(new byte[32 * 1024]);
	}

	private void assertClearBuffer(byte[] buffer) throws IOException {
		var in = new ByteArrayInputStream(buffer);
		assertEquals(IoUtil.clear(in), (long) buffer.length);
		assertEquals(in.available(), 0);
	}

	@Test
	public void testNullPrintStream() throws IOException {
		try (var out = IoUtil.nullPrintStream()) {
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
		var name = firstEnvironmentVariableName();
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
		assertUnordered(IoUtil.variablePaths(null));
		assertUnordered(IoUtil.variablePaths(""));
		assertUnordered(IoUtil.variablePaths(File.pathSeparator));
		assertUnordered(IoUtil.variablePaths(" " + File.pathSeparator + " a"), "a");
		assertUnordered(IoUtil.variablePaths("a/b" + File.pathSeparator + "a/b"), "a/b");
		assertUnordered(IoUtil.variablePaths(" a/b" + File.pathSeparator + "a/b /c"), "a/b",
			"a/b /c");
	}

	@Test
	public void testExtend() throws IOException {
		try (var rp = ResourcePath.of(String.class)) {
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
		var fs = TestFileSystem.of();
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
	public void testChangeNameFunction() {
		assertPath(IoUtil.changeName(Path.of("/test"), f -> f + "0"), "/test0");
		assertPath(IoUtil.changeName(Path.of("/test"), _ -> null), "/test");
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
		assertNull(IoUtil.filename(null));
		assertEquals(IoUtil.filename(Path.of("/")), "");
		assertEquals(IoUtil.filename(Path.of("")), "");
		assertEquals(IoUtil.filename(Path.of("/a")), "a");
		assertEquals(IoUtil.filename(Path.of("/a/b")), "b");
		assertEquals(IoUtil.filename(Path.of("a")), "a");
		assertEquals(IoUtil.filename(Path.of("a/b")), "b");
	}

	@Test
	public void testFileNameWithoutExt() {
		assertNull(IoUtil.filenameWithoutExt((Path) null));
		assertEquals(IoUtil.filenameWithoutExt(Path.of("/")), "");
		assertEquals(IoUtil.filenameWithoutExt(Path.of("")), "");
		assertEquals(IoUtil.filenameWithoutExt(Path.of(".file")), ".file");
		assertEquals(IoUtil.filenameWithoutExt(Path.of("/a")), "a");
		assertEquals(IoUtil.filenameWithoutExt(Path.of("/a.txt")), "a");
		assertEquals(IoUtil.filenameWithoutExt(Path.of("a.b.c")), "a.b");
	}

	@Test
	public void testExtension() {
		assertNull(IoUtil.extension((Path) null));
		assertEquals(IoUtil.extension(Path.of("/")), "");
		assertEquals(IoUtil.extension(Path.of("")), "");
		assertEquals(IoUtil.extension(Path.of(".file")), "");
		assertEquals(IoUtil.extension(Path.of("/a")), "");
		assertEquals(IoUtil.extension(Path.of("/a.txt")), "txt");
		assertEquals(IoUtil.extension(Path.of("a.b.c")), "c");
	}

	@Test
	public void testCreateTempDir() throws IOException {
		var tempDir = IoUtil.createTempDir(helper.root);
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
		try (var deleteHelper = FileTestHelper.builder(helper.root).file("x/x/x.txt", "").dir("y/y")
			.file("z.txt", "").build()) {
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

	@Test
	public void testPollString() throws IOException {
		try (var in = TestUtil.inputStream("test")) {
			var s = IoUtil.pollString(in);
			assertEquals(s, "test");
		}
	}

	@Test
	public void testAvailableChar() throws IOException {
		try (var sys = SystemIo.of()) {
			sys.in(TestUtil.inputStream("test"));
			assertEquals(IoUtil.availableChar(), 't');
			assertEquals(IoUtil.availableChar(), 'e');
			assertEquals(IoUtil.availableChar(), 's');
			assertEquals(IoUtil.availableChar(), 't');
			sys.in().close();
			assertEquals(IoUtil.availableChar(), '\0');
		}
		try (var in = TestInputStream.of()) {
			in.available.error.setFrom(IOX);
			assertEquals(IoUtil.availableChar(in), '\0');
		}
	}

	@Test
	public void testAvailableString() throws IOException {
		assertNull(IoUtil.availableString(null));
		try (var in = new ByteArrayInputStream("test".getBytes())) {
			assertEquals(IoUtil.availableString(in), "test");
			assertEquals(IoUtil.availableString(in), "");
		}
	}

	@Test
	public void testAvailableLine() throws IOException {
		assertNull(IoUtil.availableLine(null));
		var s = "te" + EOL + EOL + "s" + EOL + "t";
		try (var in = new ByteArrayInputStream(s.getBytes())) {
			assertEquals(IoUtil.availableLine(in), "te" + EOL);
			assertEquals(IoUtil.availableLine(in), EOL);
			assertEquals(IoUtil.availableLine(in), "s" + EOL);
			assertEquals(IoUtil.availableLine(in), "t");
		}
	}

	@Test
	public void testAvailableBytes() throws IOException {
		assertNull(IoUtil.availableBytes(null));
		try (var in = new ByteArrayInputStream(ArrayUtil.bytes.of(0, 1, 2, 3, 4))) {
			assertEquals(IoUtil.availableBytes(in), ByteProvider.of(0, 1, 2, 3, 4));
			assertEquals(IoUtil.availableBytes(in), ByteProvider.empty());
		}
		try (var in = IoStreamUtil.in((_, _, _) -> 0, () -> 3)) {
			assertEquals(IoUtil.availableBytes(in), ByteProvider.empty());
		}
	}

	@Test
	public void testAvailableBytesWithPredicate() throws IOException {
		assertNull(IoUtil.availableBytes(null, null));
		Excepts.ObjIntPredicate<RuntimeException, byte[]> p = (b, n) -> b[n - 1] == -1;
		try (var in = new ByteArrayInputStream(ArrayUtil.bytes.of(0, 1, -1, -1, 2, 3))) {
			assertEquals(IoUtil.availableBytes(in, p), ByteProvider.of(0, 1, -1));
			assertEquals(IoUtil.availableBytes(in, p), ByteProvider.of(-1));
			assertEquals(IoUtil.availableBytes(in, p), ByteProvider.of(2, 3));
			assertEquals(IoUtil.availableBytes(in, p), ByteProvider.empty());
		}
		try (var in = IoStreamUtil.in((_, _, _) -> 0, () -> 3)) {
			assertEquals(IoUtil.availableBytes(in, null), ByteProvider.of(0, 0, 0));
		}
		try (var in = IoStreamUtil.in((_, _, _) -> -1, () -> 3)) {
			assertEquals(IoUtil.availableBytes(in, null), ByteProvider.empty());
		}
	}

	@Test
	public void testReadBytes() throws IOException {
		assertEquals(IoUtil.readBytes(null, null), 0);
		try (var in = new ByteArrayInputStream(ArrayUtil.bytes.of(0, 1, 2, 3, 4))) {
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
			assertThrown(IoExceptions.Timeout.class, () -> IoUtil.pollForData(in, 1, 1, 1));
			available[0] = 3;
			assertEquals(IoUtil.pollForData(in, 1, 0, 1), 3);
		}
	}

	@Test
	public void testPipe() throws IOException {
		var in = TestUtil.inputStream(1, 2, 3, 4, 5);
		var out = new ByteArrayOutputStream();
		IoUtil.pipe(in, out);
		assertArray(out.toByteArray(), 1, 2, 3, 4, 5);
	}

	@Test
	public void testPipeWithDelay() throws IOException {
		var read = CallSync.supplier(1, 0, 3, -1);
		try (var in = IoStreamUtil.in((_, _, _) -> read.get())) {
			var out = new ByteArrayOutputStream();
			IoUtil.pipe(in, out, new byte[3], 0);
			assertArray(out.toByteArray(), 0, 0, 0, 0);
		}
	}

	@Test
	public void testPathToUnix() {
		assertEquals(IoUtil.pathToUnix(Path.of("a", "b", "c")), "a/b/c");
		assertEquals(IoUtil.pathToUnix(Path.of("")), "");
	}

	@Test
	public void testUnixToPath() {
		var path = "a" + File.separatorChar + "b" + File.separatorChar + "c";
		assertEquals(IoUtil.unixToPath("a/b/c"), path);
	}

	@Test
	public void testConvertPath() {
		assertEquals(IoUtil.convertPath("a\\b\\c", '\\', '/'), "a/b/c");
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
		assertUnordered(IoUtil.pathsCollect(helper.root, //
			path -> helper.root.equals(path) ? null : path.getFileName().toString().charAt(0)), //
			'a', 'a', 'a', 'b', 'b', 'c', 'd');
	}

	@Test
	public void testListNames() throws IOException {
		assertUnordered(IoUtil.listNames(helper.root), "a", "b", "c.txt", "d");
		assertUnordered(IoUtil.listNames(helper.path("a")), "a");
		assertUnordered(IoUtil.listNames(helper.path("a/a")), "a.txt");
		assertUnordered(IoUtil.listNames(helper.path("d")));
		assertUnordered(
			IoUtil.listNames(helper.root, path -> path.getFileName().toString().length() == 1), "a",
			"b", "d");
		assertUnordered(IoUtil.listNames(helper.root, "regex:a|.*\\.txt"), "a", "c.txt");
		assertUnordered(IoUtil.listNames(helper.root, (String) null), "a", "b", "c.txt", "d");
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
		assertUnordered(IoUtil.listCollect(helper.root, //
			path -> path.getFileName().toString().charAt(0)), 'a', 'b', 'c', 'd');
	}

	@Test
	public void testReadString() throws IOException {
		var in = inputStream("abc\0");
		assertEquals(IoUtil.readString(in), "abc\0");
	}

	@Test
	public void testLines() throws IOException {
		var in = inputStream("line0\n\nline2\nend");
		assertStream(IoUtil.lines(in), "line0", "", "line2", "end");
	}

	@Test
	public void testCopyFile() throws IOException {
		try {
			var fromFile = helper.path("a/a/a.txt");
			var toFile = helper.path("x/x/x.txt");
			IoUtil.copyFile(fromFile, toFile);
			assertFile(fromFile, toFile);
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopyBadFile() throws IOException {
		try {
			var badFile = TestPath.of();
			var toFile2 = helper.path("x/y/z.txt");
			assertThrown(() -> IoUtil.copyFile(badFile, toFile2));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testCopy() throws IOException {
		try {
			var fromFile = helper.path("a/a/a.txt");
			var toFile = helper.path("x/x/x.txt");
			try (var in = Files.newInputStream(fromFile)) {
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
			var badIn = IoStreamUtil.in(AssertUtil::throwIo);
			var toFile2 = helper.path("x/y/z.txt");
			assertThrown(() -> IoUtil.copy(badIn, toFile2));
			assertFalse(Files.exists(helper.path("x/y")));
		} finally {
			IoUtil.deleteAll(helper.path("x"));
		}
	}

	@Test
	public void testWrite() throws IOException {
		try {
			var ba = ByteProvider.of('a', 'b', 'c');
			var toFile = helper.path("x/x/x.txt");
			IoUtil.write(toFile, ba);
			assertFile(toFile, 'a', 'b', 'c');
			var badArray = badProvider(3);
			var toFile2 = helper.path("x/y/z.txt");
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
		var url = IoUtil.classUrl(String.class);
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
		var s = IoUtil.resourceString(getClass(), getClass().getSimpleName() + ".properties");
		assertEquals(s, "a=b");
	}
}
