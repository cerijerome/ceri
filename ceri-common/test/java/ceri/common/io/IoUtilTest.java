package ceri.common.io;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.isList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class IoUtilTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bbb").file("c.txt",
				"ccc").build();
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test(expected = InterruptedException.class)
	public void testCheckInterrupted() throws InterruptedException {
		Thread.currentThread().interrupt();
		IoUtil.checkInterrupted();
	}

	@Test(expected = InterruptedIOException.class)
	public void testCheckIoInterrupted() throws InterruptedIOException {
		Thread.currentThread().interrupt();
		IoUtil.checkIoInterrupted();
	}

	@Test(expected = IOException.class)
	public void testClose() throws IOException {
		StringReader in = new StringReader("0123456789");
		IoUtil.close(in);
		in.read();
	}

	@Test
	public void testCopyFile() throws IOException {
		File toFile = helper.file("x/x/x.txt");
		IoUtil.copyFile(helper.file("a/a/a.txt"), toFile);

		FileInputStream in = new FileInputStream(toFile);
		byte[] b = new byte[5];
		assertThat(in.read(b), is(3));
		assertThat(b, is(new byte[] { 'a', 'a', 'a', 0, 0 }));
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
		FileTestHelper deleteHelper =
			FileTestHelper.builder(helper.root).file("x/x/x.txt", "").dir("y/y").file("z.txt",
				"").build();
		assertTrue(deleteHelper.file("x/x/x.txt").exists());
		assertTrue(deleteHelper.file("y/y").exists());
		assertTrue(deleteHelper.file("z.txt").exists());
		IoUtil.deleteAll(deleteHelper.root);
		assertFalse(deleteHelper.root.exists());
	}

	@Test
	public void testDeleteEmptyDirs() throws IOException {
		FileTestHelper deleteHelper =
			FileTestHelper.builder(helper.root).dir("x/x/x").file("y/y.txt", "").dir("z")
				.build();
		assertTrue(deleteHelper.file("x/x/x").exists());
		assertTrue(deleteHelper.file("y/y.txt").exists());
		assertTrue(deleteHelper.file("z").exists());
		IoUtil.deleteEmptyDirs(deleteHelper.root);
		assertFalse(deleteHelper.file("x/x/x").exists());
		assertTrue(deleteHelper.file("y/y.txt").exists());
		assertFalse(deleteHelper.file("z").exists());
		deleteHelper.close();
	}

	@Test
	public void testGetAbsolutePath() {
		File file = new File(new File(new File("a"), "b"), "c");
		String path = IoUtil.getAbsolutePath(file);
		assertTrue(path.endsWith("/a/b/c"));
	}

	@Test
	public void testGetChar() throws IOException {
		assertThat(IoUtil.getChar(), is('\0'));
	}

	@Test
	public void testGetContent() throws IOException {
		assertThat(IoUtil.getContent(helper.file("a/a/a.txt")), is(new byte[] { 'a', 'a', 'a' }));
		InputStream in = new FileInputStream(helper.file("b/b.txt"));
		assertThat(IoUtil.getContent(in, 0), is(new byte[] { 'b', 'b', 'b' }));
		IoUtil.close(in);
		assertThat(IoUtil.getContentString(helper.file("a/a/a.txt")), is("aaa"));
		in = new FileInputStream(helper.file("b/b.txt"));
		assertThat(IoUtil.getContentString(in, 0), is("bbb"));
		IoUtil.close(in);
	}

	@Test
	public void testGetFilenames() {
		List<String> filenames = IoUtil.getFilenames(helper.root);
		assertThat(filenames, isList("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt"));
		filenames = IoUtil.getFilenames(helper.root, new RegexFilenameFilter(false, ".*\\.txt"));
		assertThat(filenames, isList("a/a/a.txt", "b/b.txt", "c.txt"));
	}

	@Test
	public void testGetFiles() {
		List<File> files = IoUtil.getFiles(helper.root);
		assertThat(files, is(helper.fileList("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt")));
		files = IoUtil.getFiles(helper.root, new RegexFilenameFilter(false, ".*[^\\.txt]"));
		assertThat(files, is(helper.fileList("a", "a/a", "b")));
	}

	@Test
	public void testGetRelativePath() {
		String relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/d/e/f"));
		assertThat(relative, is("/d/e/f"));
		relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/a/b/c/d/e"));
		assertThat(relative, is("d/e"));
		relative = IoUtil.getRelativePath(new File("/a/b/c"), new File("/a/x/y"));
		assertThat(relative, is("../../x/y"));
	}

	@Test
	public void testGetResource() throws IOException {
		byte[] bytes = IoUtil.getResource(getClass(), getClass().getSimpleName() + ".properties");
		assertThat(bytes, is(new byte[] { 'a', '=', 'b' }));
		String s = IoUtil.getResourceString(getClass(), getClass().getSimpleName() + ".properties");
		assertThat(s, is("a=b"));
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
	public void testFillBuffer() throws IOException {
		byte[] inBuffer = new byte[200];
		for (int i = 0; i < inBuffer.length; i++) inBuffer[i] = (byte)i;
		ByteArrayInputStream in = new ByteArrayInputStream(inBuffer);
		byte[] outBuffer = new byte[256];
		int count = IoUtil.fillBuffer(in, outBuffer);
		assertThat(count, is(200));
		assertArray(Arrays.copyOf(outBuffer, 200), inBuffer);
		in.reset();
		outBuffer = new byte[256];
		count = IoUtil.fillBuffer(in, outBuffer, 0, 150);
		assertThat(count, is(150));
		count = IoUtil.fillBuffer(in, outBuffer, 150, 100);
		assertThat(count, is(200));
		count = IoUtil.fillBuffer(in, outBuffer, 200, 50);
		assertThat(count, is(200)); 
		assertArray(Arrays.copyOf(outBuffer, 200), inBuffer);
	}
	
}
