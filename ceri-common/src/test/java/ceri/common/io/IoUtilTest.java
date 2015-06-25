package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.matchesRegex;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.regex.Pattern;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class IoUtilTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder(IoUtil.systemTempDir()).file("a/a/a.txt", "aaa").file("b/b.txt",
				"bbb").file("c.txt", "ccc").build();
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
		IoUtil.close(in);
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

		try (FileInputStream in = new FileInputStream(toFile)) {
			byte[] b = new byte[5];
			assertThat(in.read(b), is(3));
			assertThat(b, is(new byte[] { 'a', 'a', 'a', 0, 0 }));
		}
		IoUtil.deleteAll(helper.file("x"));
	}

	@Test
	public void testCreateTempDir() {
		File tempDir = IoUtil.createTempDir(new File("."));
		assertTrue(tempDir.exists());
		assertTrue(tempDir.isDirectory());
		tempDir.delete();
	}

	@Test
	public void testDeleteAll() throws IOException {
		try (FileTestHelper deleteHelper =
			FileTestHelper.builder(helper.root).file("x/x/x.txt", "").dir("y/y").file("z.txt", "")
				.build()) {
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
	public void testGetChar() {
		assertThat(IoUtil.getChar(), is('\0'));
	}

	@Test
	public void testGetContent() throws IOException {
		assertThat(IoUtil.getContent(helper.file("a/a/a.txt")), is(new byte[] { 'a', 'a', 'a' }));
		try (InputStream in = new FileInputStream(helper.file("b/b.txt"))) {
			assertThat(IoUtil.getContent(in, 0), is(new byte[] { 'b', 'b', 'b' }));
		}
		assertThat(IoUtil.getContentString(helper.file("a/a/a.txt")), is("aaa"));
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
	}

	@Test
	public void testGetFiles() {
		List<File> files = IoUtil.getFiles(helper.root);
		assertCollection(files, helper.fileList("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt"));
		files = IoUtil.getFiles(helper.root, RegexFilenameFilter.create(".*[^\\.txt]"));
		assertCollection(files, helper.fileList("a", "a/a", "b"));
	}

	@Test
	public void testGetRelativeUnixPath() throws IOException {
		String relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/d/e/f"));
		assertThat(IoUtil.unixPath(relative), is("../../../d/e/f"));
		relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/a/b/c/d/e"));
		assertThat(IoUtil.unixPath(relative), is("d/e"));
		relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/a/x/y"));
		assertThat(IoUtil.unixPath(relative), is("../../x/y"));
	}

	@Test
	public void testListResourcesFromFile() throws Exception {
		List<String> resources = IoUtil.listResources(getClass(), "res/test", null);
		assertCollection(resources, "A.txt", "BB.txt", "CCC.txt");
	}

	@Test
	public void testListResourcesFromJar() throws Exception {
		List<String> resources = IoUtil.listResources(BigDecimal.class);
		assertTrue(resources.contains("BigDecimal.class"));
		assertTrue(resources.contains("BigInteger.class"));
		resources = IoUtil.listResources(BigDecimal.class, "", Pattern.compile("BigDecimal\\..*"));
		assertCollection(resources, "BigDecimal.class");
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
		assertThat(path, matchesRegex("jar:file:.*" + String.class.getPackage().getName() + "/"));
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
		try {
			IoUtil.getResource(getClass(), "test");
			fail();
		} catch (MissingResourceException e) {}
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
		IoUtil.setContentString(file, "xyz");
		assertThat(IoUtil.getContentString(file), is("xyz"));
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
