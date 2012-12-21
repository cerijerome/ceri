package ceri.aws.util;

import static ceri.aws.util.TarUtil.Compression.bzip2;
import static ceri.aws.util.TarUtil.Compression.gzip;
import static ceri.aws.util.TarUtil.Compression.none;
import static ceri.common.test.TestUtil.assertDir;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import java.util.zip.Adler32;
import java.util.zip.Checksum;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.io.ByteBufferStream;
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class TarUtilTest {
	private static final String contentL = TestUtil.randomString(100000);
	private static final String contentS = TestUtil.randomString(1000);
	private static FileTestHelper helper = null;
	private static File srcDir = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder().file("src/a/a/a.txt", contentS).file("src/b/b.txt", contentL)
				.file("src/c.txt", contentS).build();
		srcDir = helper.file("src");
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test
	public void testTarWithStream() throws IOException {
		File untarDir = helper.file("untar");
		ByteBufferStream stream = new ByteBufferStream();
		TarUtil.tar(srcDir, stream, bzip2);
		TarUtil.untar(stream.asInputStream(), untarDir, bzip2);
		assertDir(untarDir, srcDir);
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void testTarToFileWithCompression() throws IOException {
		File tarFile = helper.file("test.tar.gz");
		File untarDir = helper.file("untar");
		TarUtil.tar(srcDir, tarFile, gzip);
		TarUtil.untar(tarFile, untarDir);
		assertDir(untarDir, srcDir);
		tarFile.delete();
		IoUtil.deleteAll(untarDir);
	}

	@Test(expected = IOException.class)
	public void testUntarWithoutCompressionFailsForCompressedTar() throws IOException {
		byte[] tarData = TarUtil.tarAsBytes(srcDir, bzip2);
		TarUtil.untar(tarData, helper.root, none);
	}

	@Test(expected = IOException.class)
	public void testUntarWithCompressionFailsForUncompressedTar() throws IOException {
		byte[] tarData = TarUtil.tarAsBytes(srcDir, none);
		TarUtil.untar(tarData, helper.root, bzip2);
	}

	@Test
	public void testTarWithSubDirectories() throws IOException {
		// Create files a..e/a..e/x with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("tar");
		for (char i = 'a'; i < 'f'; i++)
			for (char j = 'a'; j < 'f'; j++)
				builder.file(i + "/" + j + "/x", contentS);
		File untarDir = new File(helper.root, "untar");
		try (FileTestHelper tarHelper = builder.build()) {
			byte[] tarData = TarUtil.tarAsBytes(tarHelper.root, none);
			TarUtil.untar(tarData, untarDir, none);
			assertDir(untarDir, tarHelper.root);
		}
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void testTarWithLargeNumberOfFiles() throws IOException {
		// Create files z100..299 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("tar");
		for (int i = 100; i < 300; i++)
			builder.file("z" + i, contentS);
		File untarDir = new File(helper.root, "untar");
		try (FileTestHelper tarHelper = builder.build()) {
			byte[] tarData = TarUtil.tarAsBytes(tarHelper.root, none);
			TarUtil.untar(tarData, untarDir, none);
			assertDir(untarDir, tarHelper.root);
		}
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void testTarWithChecksum() throws IOException {
		// Create files z100..299 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("tar");
		for (int i = 100; i < 200; i++)
			builder.file(i + "/z" + i, contentS);
		File untarDir = new File(helper.root, "untar");
		try (FileTestHelper tarHelper = builder.build()) {
			ByteBufferStream stream = new ByteBufferStream();
			Checksum tarChecksum = new Adler32();
			TarUtil.tar(tarHelper.root, stream, gzip, tarChecksum, 100);
			Checksum untarChecksum = new Adler32();
			TarUtil.untar(stream.asInputStream(), untarDir, gzip, untarChecksum, 100);
	
			assertDir(untarDir, tarHelper.root);
			assertThat(untarChecksum.getValue(), is(tarChecksum.getValue()));
		}
		IoUtil.deleteAll(untarDir);
	}

}
