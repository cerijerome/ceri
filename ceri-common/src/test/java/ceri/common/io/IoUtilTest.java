package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEnum;
import static ceri.common.test.TestUtil.matchesRegex;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.List;
import java.util.MissingResourceException;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.collection.ImmutableByteArray;
import ceri.common.data.ByteUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class IoUtilTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper = FileTestHelper.builder(IoUtil.systemTempDir()) //
			.file("a/a/a.txt", "aaa").file("b/b.txt", "bbb").file("c.txt", "ccc").build();
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(IoUtil.class);
	}

	@Test
	public void test() {
		exerciseEnum(DeviceMode.class);
	}

	@Test
	public void testClear() throws IOException {
		assertClearBuffer(new byte[0]);
		assertClearBuffer(new byte[100]);
		assertClearBuffer(new byte[32 * 1024]);
		assertClearBuffer(new byte[65 * 1024]);
		try (InputStream in = Mockito.mock(InputStream.class)) {
			when(in.available()).thenReturn(1);
			when(in.read(any())).thenReturn(0);
			assertThat(IoUtil.clear(in), is(0L));
		}
	}

	private void assertClearBuffer(byte[] buffer) throws IOException {
		InputStream in = new ByteArrayInputStream(buffer);
		assertThat(IoUtil.clear(in), is((long) buffer.length));
		assertThat(in.available(), is(0));
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
	public void testClassUrl() {
		URL url = IoUtil.classUrl(String.class);
		assertTrue(url.getPath().endsWith("/java/lang/String.class"));
	}

	@Test
	public void testCheckIoInterrupted() throws InterruptedIOException {
		IoUtil.checkIoInterrupted();
		Thread.currentThread().interrupt();
		assertThrown(InterruptedIOException.class, IoUtil::checkIoInterrupted);
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
	public void testCopyFile() throws IOException {
		File toFile = helper.file("x/x/x.txt");
		IoUtil.copyFile(helper.file("a/a/a.txt"), toFile);
		assertFile(helper.file("a/a/a.txt"), toFile);
		IoUtil.deleteAll(helper.file("x"));
		File badFile = mock(File.class);
		assertThrown(RuntimeException.class,
			() -> IoUtil.copyFile(helper.file("a/a/a.txt"), badFile));
	}

	@Test
	public void testCreateTempDir() {
		File tempDir = IoUtil.createTempDir(helper.root);
		assertTrue(tempDir.exists());
		assertTrue(tempDir.isDirectory());
		tempDir.delete();
		// Should fail - generating same dir each iteration
		TestUtil.assertThrown(() -> IoUtil.createTempDir(helper.root, file -> helper.file("a")));
		// Should fail - parent path is an existing file
		TestUtil
			.assertThrown(() -> IoUtil.createTempDir(helper.root, file -> helper.file("c.txt/c")));
	}

	@Test
	public void testDeleteAll() throws IOException {
		try (FileTestHelper deleteHelper = FileTestHelper.builder(helper.root).file("x/x/x.txt", "")
			.dir("y/y").file("z.txt", "").build()) {
			assertTrue(deleteHelper.file("x/x/x.txt").exists());
			assertTrue(deleteHelper.file("y/y").exists());
			assertTrue(deleteHelper.file("z.txt").exists());
			IoUtil.deleteAll(deleteHelper.root);
			assertFalse(deleteHelper.root.exists());
		}
	}

	@Test
	public void testDeleteEmptyDirs() throws IOException {
		try (FileTestHelper deleteHelper =
			FileTestHelper.builder(helper.root).dir("x/x/x").file("y/y.txt", "").dir("z").build()) {
			assertTrue(deleteHelper.file("x/x/x").exists());
			assertTrue(deleteHelper.file("y/y.txt").exists());
			assertTrue(deleteHelper.file("z").exists());
			IoUtil.deleteEmptyDirs(deleteHelper.root);
			assertFalse(deleteHelper.file("x/x/x").exists());
			assertTrue(deleteHelper.file("y/y.txt").exists());
			assertFalse(deleteHelper.file("z").exists());
		}
	}

	@Test
	public void testAvailableChar() throws IOException {
		try (SystemIo sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			assertThat(IoUtil.availableChar(), is('t'));
			assertThat(IoUtil.availableChar(), is('e'));
			assertThat(IoUtil.availableChar(), is('s'));
			assertThat(IoUtil.availableChar(), is('t'));
			sys.in().close();
			assertThat(IoUtil.availableChar(), is('\0'));
		}
		try (InputStream in = Mockito.mock(InputStream.class)) {
			doThrow(new IOException()).when(in).available();
			assertThat(IoUtil.availableChar(in), is('\0'));
		}
	}

	@Test
	public void testPollString() throws IOException {
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			String s = IoUtil.pollString(in);
			assertThat(s, is("test"));
		}
	}

	@Test
	public void testAvailableString() throws IOException {
		assertNull(IoUtil.availableString(null));
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			assertThat(IoUtil.availableString(in), is("test"));
			assertThat(IoUtil.availableString(in), is(""));
		}
	}

	@Test
	public void testAvailableBytes() throws IOException {
		assertNull(IoUtil.availableBytes(null));
		try (InputStream in = new ByteArrayInputStream(ByteUtil.bytes(0, 1, 2, 3, 4))) {
			assertThat(IoUtil.availableBytes(in), is(ImmutableByteArray.wrap(0, 1, 2, 3, 4)));
			assertThat(IoUtil.availableBytes(in), is(ImmutableByteArray.EMPTY));
		}
		try (InputStream in = Mockito.mock(InputStream.class)) {
			when(in.available()).thenReturn(5);
			when(in.read(any())).thenReturn(0);
			assertThat(IoUtil.availableBytes(in), is(ImmutableByteArray.EMPTY));
		}
	}

	@Test
	public void testReadBytes() throws IOException {
		assertThat(IoUtil.readBytes(null, null), is(0));
		try (InputStream in = new ByteArrayInputStream(ByteUtil.bytes(0, 1, 2, 3, 4))) {
			assertThat(IoUtil.readBytes(in, null), is(0));
			byte[] buffer = new byte[4];
			IoUtil.readBytes(in, buffer);
			assertArray(buffer, 0, 1, 2, 3);
		}
	}

	@Test
	public void testPollForData() throws IOException {
		try (InputStream in = Mockito.mock(InputStream.class)) {
			assertThrown(IoTimeoutException.class, () -> IoUtil.pollForData(in, 1, 1, 1));
			when(in.available()).thenReturn(0).thenReturn(2);
			assertThat(IoUtil.pollForData(in, 1, 0, 1), is(2));
		}
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
		IoUtil.IO_ADAPTER.call(() -> "a");
		assertThrown(RuntimeException.class,
			() -> IoUtil.IO_ADAPTER.call(() -> Integer.valueOf(null)));
		assertThrown(IOException.class, () -> IoUtil.IO_ADAPTER.call(() -> {
			throw new Exception();
		}));
		assertThrown(IOException.class, () -> IoUtil.IO_ADAPTER.call(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testUnixPath() {
		assertThat(IoUtil.unixPath(new File("a/b/c")), is("a/b/c"));
		assertThat(IoUtil.unixPath("a\\b\\c", '\\', Pattern.compile("\\\\")), is("a/b/c"));
	}

	@Test
	public void testReadString() throws IOException {
		assertThat(IoUtil.readBytes(helper.file("a/a/a.txt")), is(new byte[] { 'a', 'a', 'a' }));
		assertThat(IoUtil.readString(helper.file("a/a/a.txt")), is("aaa"));
		assertThat(IoUtil.readString(helper.file("a/a/a.txt").getAbsolutePath()), is("aaa"));
		try (InputStream in = new FileInputStream(helper.file("b/b.txt"))) {
			assertThat(IoUtil.readString(in), is("bbb"));
		}
	}

	@Test
	public void testFilenames() {
		List<String> filenames = IoUtil.filenames(helper.root);
		assertCollection(filenames, "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt");
		filenames = IoUtil.filenames(helper.root, RegexFilenameFilter.create(".*\\.txt"));
		assertCollection(filenames, "a/a/a.txt", "b/b.txt", "c.txt");
		assertCollection(IoUtil.filenames(new File("")));
	}

	@Test
	public void testFiles() {
		List<File> files = IoUtil.files(helper.root);
		assertCollection(files, helper.fileList("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt"));
		files = IoUtil.files(helper.root, RegexFilenameFilter.create(".*[^\\.txt]"));
		assertCollection(files, helper.fileList("a", "a/a", "b"));
		assertCollection(IoUtil.files(new File("")));
	}

	@Test
	public void testRelativePath() throws IOException {
		String relative = IoUtil.relativePath(new File("/a/b/c"), new File("/d/e/f"));
		assertThat(IoUtil.unixPath(relative), is("../../../d/e/f"));
		relative = IoUtil.relativePath(new File("/a/b/c"), new File("/a/b/c/d/e"));
		assertThat(IoUtil.unixPath(relative), is("d/e"));
		relative = IoUtil.relativePath(new File("/a/b/c"), new File("/a/x/y"));
		assertThat(IoUtil.unixPath(relative), is("../../x/y"));
		File dir = mock(File.class);
		when(dir.getCanonicalFile()).thenReturn(null);
		relative = IoUtil.relativePath(dir, new File("/a/b/c"));
		assertThat(IoUtil.unixPath(relative), is("/a/b/c"));
	}

	@Test
	public void testListResourcesFromFile() throws Exception {
		List<String> resources = IoUtil.listResources(getClass(), "res/test", null);
		assertCollection(resources, "A.txt", "BB.txt", "CCC.txt");
	}

	@Test
	public void testListResourcesFromModule() throws Exception {
		List<String> resources = IoUtil.listResources(String.class);
		assertTrue(resources.contains("String.class"));
		assertTrue(resources.contains("Object.class"));
		resources = IoUtil.listResources(String.class, "ref", Pattern.compile("Cleaner\\..*"));
		assertCollection(resources, "Cleaner.class");
	}

	@Test
	public void testListResourcesFromJar() throws Exception {
		List<String> resources = ResourceLister.of(Test.class).list();
		assertTrue(resources.contains("Test.class"));
		resources = ResourceLister.of(Test.class, "runner", "Run.*").list();
		assertCollection(resources, "Runner.class", "RunWith.class");
	}

	@Test
	public void testResourceFile() {
		File f = IoUtil.resourceFile(getClass(), getClass().getSimpleName() + ".properties");
		assertTrue(f.exists());
		TestUtil.assertThrown(() -> IoUtil.resourceFile(getClass(), "missing"));
	}

	@Test
	public void testResourcePath() {
		String path = IoUtil.resourcePath(getClass());
		assertThat(path, matchesRegex("file:.*" + getClass().getPackage().getName() + "/"));
		path = IoUtil.resourcePath(String.class);
		assertThat(path, matchesRegex("jrt:.*" + String.class.getPackage().getName() + "/"));
	}

	@Test
	public void testResource() throws IOException {
		byte[] bytes = IoUtil.resource(getClass(), getClass().getSimpleName() + ".properties");
		assertThat(bytes, is(new byte[] { 'a', '=', 'b' }));
		bytes = IoUtil.classResource(getClass(), "properties");
		assertThat(bytes, is(new byte[] { 'a', '=', 'b' }));
		String s = IoUtil.resourceString(getClass(), getClass().getSimpleName() + ".properties");
		assertThat(s, is("a=b"));
		s = IoUtil.classResourceAsString(getClass(), "properties");
		assertThat(s, is("a=b"));
		assertThrown(MissingResourceException.class, () -> IoUtil.resource(getClass(), "test"));
		TestUtil.assertThrown(() -> IoUtil.resource(getClass(), null));
	}

	@Test
	public void testJoinPaths() {
		assertThat(IoUtil.joinPaths(null, null), is(""));
		assertThat(IoUtil.joinPaths("", null), is(""));
		assertThat(IoUtil.joinPaths(null, ""), is(""));
		assertThat(IoUtil.joinPaths("", ""), is(""));
		assertThat(IoUtil.joinPaths("/a/b/c/", "/d/e/"), is("/a/b/c/d/e/"));
		assertThat(IoUtil.joinPaths("/a/b/c", "d/e"), is("/a/b/c/d/e"));
		assertThat(IoUtil.joinPaths("/a/b/c/", ""), is("/a/b/c/"));
		assertThat(IoUtil.joinPaths("", "/a/b/c/"), is("/a/b/c/"));
	}

	@Test
	public void testSetContent() throws IOException {
		File file = helper.file("x/x/x.txt");
		file.getParentFile().mkdirs();
		IoUtil.write(file, "abc".getBytes());
		assertThat(IoUtil.readString(file), is("abc"));
		IoUtil.write(file, ImmutableByteArray.wrap("abc".getBytes()));
		assertThat(IoUtil.readString(file), is("abc"));
		IoUtil.writeString(file, "xyz");
		assertThat(IoUtil.readString(file), is("xyz"));
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			IoUtil.copy(in, file);
		}
		assertThat(IoUtil.readString(file), is("test"));
		IoUtil.deleteAll(helper.file("x"));
	}

	@Test
	public void testTransferContent() throws IOException {
		String s = "0123456789abcdefghijklmnopqrstuvwxyz\u1fff\2fff\3fff\4fff";
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes(UTF_8));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IoUtil.transferBytes(in, out, 1);
		assertThat(new String(out.toByteArray(), UTF_8), is(s));
		in.reset();
		out.reset();
		IoUtil.transferBytes(in, out, 1000000);
		assertThat(new String(out.toByteArray(), UTF_8), is(s));
		in.reset();
		out.reset();
		IoUtil.transferBytes(in, out, 0);
		assertThat(new String(out.toByteArray(), UTF_8), is(s));
		in.reset();
		IoUtil.transferBytes(in, null, 0);
	}

}
