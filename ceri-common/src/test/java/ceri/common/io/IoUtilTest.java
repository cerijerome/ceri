package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertFile;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.matchesRegex;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
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
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
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
	public void testClear() throws IOException {
		assertClearBuffer(new byte[0]);
		assertClearBuffer(new byte[100]);
		assertClearBuffer(new byte[32 * 1024]);
		assertClearBuffer(new byte[65 * 1024]);
		try (InputStream in = Mockito.mock(InputStream.class)) {
			when(in.available()).thenReturn(1);
			when(in.read(any())).thenReturn(0);
			assertThat(IoUtil.clear(in), is(0));
		}
	}

	private void assertClearBuffer(byte[] buffer) throws IOException {
		InputStream in = new ByteArrayInputStream(buffer);
		assertThat(IoUtil.clear(in), is(buffer.length));
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
	public void testNullOutputStream() throws IOException {
		try (OutputStream out = IoUtil.nullOutputStream()) {
			out.write(-1);
			out.write(new byte[1000]);
			out.write(new byte[1000], 1, 999);
		}
	}

	@Test
	public void testGetClassUrl() {
		URL url = IoUtil.getClassUrl(String.class);
		assertTrue(url.getPath().endsWith("/java/lang/String.class"));
	}

	@Test
	public void testCheckIoInterrupted() throws InterruptedIOException {
		IoUtil.checkIoInterrupted();
		Thread.currentThread().interrupt();
		assertException(InterruptedIOException.class, () -> IoUtil.checkIoInterrupted());
	}

	@Test
	public void testClose() {
		final StringReader in = new StringReader("0123456789");
		assertFalse(IoUtil.close(null));
		assertTrue(IoUtil.close(in));
		assertException(IOException.class, () -> in.read());
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
		assertException(RuntimeException.class,
			() -> IoUtil.copyFile(helper.file("a/a/a.txt"), badFile));
	}

	@Test
	public void testCreateTempDir() {
		File tempDir = IoUtil.createTempDir(helper.root);
		assertTrue(tempDir.exists());
		assertTrue(tempDir.isDirectory());
		tempDir.delete();
		// Should fail - generating same dir each iteration
		assertException(() -> IoUtil.createTempDir(helper.root, file -> helper.file("a")));
		// Should fail - parent path is an existing file
		assertException(() -> IoUtil.createTempDir(helper.root, file -> helper.file("c.txt/c")));
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
	public void testGetChar() throws IOException {
		try (SystemIo sys = SystemIo.of()) {
			sys.in(new ByteArrayInputStream("test".getBytes()));
			assertThat(IoUtil.getChar(), is('t'));
			assertThat(IoUtil.getChar(), is('e'));
			assertThat(IoUtil.getChar(), is('s'));
			assertThat(IoUtil.getChar(), is('t'));
			sys.in().close();
			assertThat(IoUtil.getChar(), is('\0'));
		}
		try (InputStream in = Mockito.mock(InputStream.class)) {
			doThrow(new IOException()).when(in).available();
			assertThat(IoUtil.getChar(in), is('\0'));
		}
	}

	@Test
	public void testReadString() throws IOException {
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			String s = IoUtil.readString(in);
			assertThat(s, is("test"));
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
	public void testWaitForData() throws IOException {
		try (InputStream in = Mockito.mock(InputStream.class)) {
			assertException(IoTimeoutException.class, () -> IoUtil.waitForData(in, 1, 1, 1));
			when(in.available()).thenReturn(0).thenReturn(2);
			assertThat(IoUtil.waitForData(in, 1, 0, 1), is(2));
		}
	}

	@Test
	public void testExecIo() throws IOException {
		IoUtil.execIo(() -> {});
		assertException(RuntimeException.class, () -> IoUtil.execIo(() -> Integer.valueOf(null)));
		assertException(IOException.class, () -> IoUtil.execIo(() -> {
			throw new Exception();
		}));
		assertException(IOException.class, () -> IoUtil.execIo(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testCallableIo() throws IOException {
		IoUtil.callableIo(() -> "a");
		assertException(RuntimeException.class,
			() -> IoUtil.callableIo(() -> Integer.valueOf(null)));
		assertException(IOException.class, () -> IoUtil.callableIo(() -> {
			throw new Exception();
		}));
		assertException(IOException.class, () -> IoUtil.callableIo(() -> {
			throw new IOException();
		}));
	}

	@Test
	public void testUnixPath() {
		assertThat(IoUtil.unixPath(new File("a/b/c")), is("a/b/c"));
		assertThat(IoUtil.unixPath("a\\b\\c", '\\', Pattern.compile("\\\\")), is("a/b/c"));
	}

	@Test
	public void testGetContent() throws IOException {
		assertThat(IoUtil.getContent(helper.file("a/a/a.txt")), is(new byte[] { 'a', 'a', 'a' }));
		try (InputStream in = new FileInputStream(helper.file("b/b.txt"))) {
			assertThat(IoUtil.getContent(in, 0), is(new byte[] { 'b', 'b', 'b' }));
		}
		assertThat(IoUtil.getContentString(helper.file("a/a/a.txt")), is("aaa"));
		assertThat(IoUtil.getContentString(helper.file("a/a/a.txt").getAbsolutePath()), is("aaa"));
		try (InputStream in = new FileInputStream(helper.file("b/b.txt"))) {
			assertThat(IoUtil.getContentString(in, 0), is("bbb"));
		}
	}

	@Test
	public void testGetFilenames() {
		List<String> filenames = IoUtil.getFilenames(helper.root);
		assertCollection(filenames, "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt");
		filenames = IoUtil.getFilenames(helper.root, RegexFilenameFilter.create(".*\\.txt"));
		assertCollection(filenames, "a/a/a.txt", "b/b.txt", "c.txt");
		assertCollection(IoUtil.getFilenames(new File("")));
	}

	@Test
	public void testGetFiles() {
		List<File> files = IoUtil.getFiles(helper.root);
		assertCollection(files, helper.fileList("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt"));
		files = IoUtil.getFiles(helper.root, RegexFilenameFilter.create(".*[^\\.txt]"));
		assertCollection(files, helper.fileList("a", "a/a", "b"));
		assertCollection(IoUtil.getFiles(new File("")));
	}

	@Test
	public void testGetRelativePath() throws IOException {
		String relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/d/e/f"));
		assertThat(IoUtil.unixPath(relative), is("../../../d/e/f"));
		relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/a/b/c/d/e"));
		assertThat(IoUtil.unixPath(relative), is("d/e"));
		relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/a/x/y"));
		assertThat(IoUtil.unixPath(relative), is("../../x/y"));
		File dir = mock(File.class);
		when(dir.getCanonicalFile()).thenReturn(null);
		relative = IoUtil.getRelativePath(dir, new File("/a/b/c"));
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
	public void testGetResourceFile() {
		File f = IoUtil.getResourceFile(getClass(), getClass().getSimpleName() + ".properties");
		assertTrue(f.exists());
		assertException(() -> IoUtil.getResourceFile(getClass(), "missing"));
	}

	@Test
	public void testGetResourcePath() {
		String path = IoUtil.getResourcePath(getClass());
		assertThat(path, matchesRegex("file:.*" + getClass().getPackage().getName() + "/"));
		path = IoUtil.getResourcePath(String.class);
		assertThat(path, matchesRegex("jrt:.*" + String.class.getPackage().getName() + "/"));
	}

	@Test
	public void testGetResource() throws IOException {
		byte[] bytes = IoUtil.getResource(getClass(), getClass().getSimpleName() + ".properties");
		assertThat(bytes, is(new byte[] { 'a', '=', 'b' }));
		bytes = IoUtil.getClassResource(getClass(), "properties");
		assertThat(bytes, is(new byte[] { 'a', '=', 'b' }));
		String s = IoUtil.getResourceString(getClass(), getClass().getSimpleName() + ".properties");
		assertThat(s, is("a=b"));
		s = IoUtil.getClassResourceAsString(getClass(), "properties");
		assertThat(s, is("a=b"));
		assertException(MissingResourceException.class,
			() -> IoUtil.getResource(getClass(), "test"));
		assertException(() -> IoUtil.getResource(getClass(), null));
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
		IoUtil.setContent(file, "abc".getBytes());
		assertThat(IoUtil.getContentString(file), is("abc"));
		IoUtil.setContent(file, ImmutableByteArray.wrap("abc".getBytes()));
		assertThat(IoUtil.getContentString(file), is("abc"));
		IoUtil.setContentString(file, "xyz");
		assertThat(IoUtil.getContentString(file), is("xyz"));
		try (InputStream in = new ByteArrayInputStream("test".getBytes())) {
			IoUtil.setContent(file, in);
		}
		assertThat(IoUtil.getContentString(file), is("test"));
		IoUtil.deleteAll(helper.file("x"));
	}

	@Test
	public void testTransferContent() throws IOException {
		String s = "0123456789abcdefghijklmnopqrstuvwxyz\u1fff\2fff\3fff\4fff";
		ByteArrayInputStream in = new ByteArrayInputStream(s.getBytes("UTF8"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		IoUtil.transferContent(in, out, 1);
		assertThat(new String(out.toByteArray(), "UTF8"), is(s));
		in.reset();
		out.reset();
		IoUtil.transferContent(in, out, 1000000);
		assertThat(new String(out.toByteArray(), "UTF8"), is(s));
		in.reset();
		out.reset();
		IoUtil.transferContent(in, out, 0);
		assertThat(new String(out.toByteArray(), "UTF8"), is(s));
		in.reset();
		IoUtil.transferContent(in, null, 0);
	}

	@Test
	public void testFillBufferExceptions() {
		final byte[] inBuffer = new byte[200];
		final byte[] outBuffer = new byte[256];
		final ByteArrayInputStream in = new ByteArrayInputStream(inBuffer);
		assertException(() -> IoUtil.fillBuffer(in, outBuffer, 255, 2));
		assertException(() -> IoUtil.fillBuffer(in, outBuffer, -1, 2));
	}

	@Test
	public void testFillBuffer() throws IOException {
		byte[] inBuffer = new byte[200];
		byte[] outBuffer = new byte[256];
		for (int i = 0; i < inBuffer.length; i++)
			inBuffer[i] = (byte) i;
		ByteArrayInputStream in = new ByteArrayInputStream(inBuffer);
		int count = IoUtil.fillBuffer(in, outBuffer);
		assertThat(count, is(200));
		assertArray(Arrays.copyOf(outBuffer, 200), inBuffer);
		in.reset();
		outBuffer = new byte[256];
		count = IoUtil.fillBuffer(in, outBuffer, 0, 150);
		assertThat(count, is(150));
		count = IoUtil.fillBuffer(in, outBuffer, 150, 100);
		assertThat(count, is(50));
		count = IoUtil.fillBuffer(in, outBuffer, 200, 50);
		assertThat(count, is(0));
		assertArray(Arrays.copyOf(outBuffer, 200), inBuffer);
	}

}
