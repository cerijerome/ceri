package ceri.common.zip;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertDir;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.io.ByteBufferStream;
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;

public class ZipUtilTest {
	private static FileTestHelper helper = null;
	private static File srcDir = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder().file("src/a/a/a.txt", "aaa").file("src/b/b.txt", "bbb").file(
				"src/c.txt", "ccc").build();
		srcDir = helper.file("src");
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test(expected = RuntimeException.class)
	public void testMisbehavingInputStream() throws IOException {
		File unzipDir = new File(helper.root, "unzip");
		try (InputStream in = new InputStream() {
			@Override
			public int read() throws IOException {
				throw new RuntimeException();
			}
		}) {
			ZipUtil.unzip(in, unzipDir);
		}
	}

	@Test
	public void testUnzipDirs() throws IOException {
		// File contains dirs [ a/a/a, b/b, c ]
		File unzipDir = new File(helper.root, "unzip");
		ZipUtil.unzip(IoUtil.getResource(getClass(), "dirs.zip"), unzipDir);
		Collection<String> dirs = IoUtil.getFilenames(unzipDir);
		assertCollection(dirs, "a", "a/a", "a/a/a", "b", "b/b", "c");
	}

	@Test
	public void testZipWithStream() throws IOException {
		File unzipDir = helper.file("unzip");
		try (ByteBufferStream stream = new ByteBufferStream()) {
			ZipUtil.zip(srcDir, stream);
			ZipUtil.unzip(stream.asInputStream(), unzipDir);
			assertDir(unzipDir, srcDir);
			IoUtil.deleteAll(unzipDir);
		}
	}

	@Test
	public void testZipToFile() throws IOException {
		File zipFile = helper.file("test.zip");
		File unzipDir = helper.file("unzip");
		ZipUtil.zip(srcDir, zipFile);
		ZipUtil.unzip(zipFile, unzipDir);
		assertDir(unzipDir, srcDir);
		zipFile.delete();
		IoUtil.deleteAll(unzipDir);
	}

	@Test
	public void testZipWithSubDirectories() throws IOException {
		// Create files a..e/a..e/x with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("zip");
		for (char i = 'a'; i < 'f'; i++)
			for (char j = 'a'; j < 'f'; j++)
				builder.file(i + "/" + j + "/x", "abcdefghijklmnopqrstuvwxyz");
		try (FileTestHelper zipHelper = builder.build()) {
			File unzipDir = new File(helper.root, "unzip");
			byte[] zipData = ZipUtil.zipAsBytes(zipHelper.root);
			ZipUtil.unzip(zipData, unzipDir);
			assertDir(unzipDir, zipHelper.root);
			IoUtil.deleteAll(unzipDir);
		}
	}

	@Test
	public void testZipWithLargeNumberOfFiles() throws IOException {
		// Create files z100..299 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("zip");
		for (int i = 100; i < 300; i++)
			builder.file("z" + i, "abcdefghijklmnopqrstuvwxyz");
		try (FileTestHelper zipHelper = builder.build()) {
			File unzipDir = new File(helper.root, "unzip");
			byte[] zipData = ZipUtil.zipAsBytes(zipHelper.root);
			ZipUtil.unzip(zipData, unzipDir);
			assertDir(unzipDir, zipHelper.root);
			IoUtil.deleteAll(unzipDir);
		}
	}

	@Test
	public void testZipWithChecksum() throws IOException {
		// Create files z100..299 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("zip");
		for (int i = 100; i < 250; i++)
			builder.file(i + "/z" + i, "abcdefghijklmnopqrstuvwxyz");
		try (FileTestHelper zipHelper = builder.build();
			ByteBufferStream stream = new ByteBufferStream()) {
			File unzipDir = new File(helper.root, "unzip");
			Checksum zipChecksum = new Adler32();
			ZipUtil.zip(zipHelper.root, stream, zipChecksum, 100);
			Checksum unzipChecksum = new Adler32();
			ZipUtil.unzip(stream.asInputStream(), unzipDir, unzipChecksum, 100);

			assertDir(unzipDir, zipHelper.root);
			// Checksum fails depending on buffer boundaries, unzip seems to leave unread data.
			assertThat(unzipChecksum.getValue(), is(zipChecksum.getValue()));
			IoUtil.deleteAll(unzipDir);
		}
	}

}
