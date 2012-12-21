package ceri.aws.util;

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

import ceri.common.io.FilenameIterator;
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class TarringInputStreamBehavior {
	private static final String contentL = TestUtil.randomString(100000);
	private static final String contentS = TestUtil.randomString(1000);
	private static FileTestHelper helper = null;
	private static File srcDir = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder().file("src/a.txt", contentL).file("src/b/b.txt", contentS)
				.file("src/c/c/c.txt", contentL).file("src/d/d/d/d.txt", contentS).file(
					"src/e/e/e/e/e.txt", contentL).build();
		srcDir = helper.file("src");
	}

	@AfterClass
	public static void deleteTempFiles() {
		helper.close();
	}

	@Test
	public void shouldTarToStreamConsistentWithUntar() throws IOException {
		File untarDir = helper.file("untar");
		TarringInputStream in = new TarringInputStream(srcDir, none);
		TarUtil.untar(in, untarDir, none);
		assertDir(untarDir, srcDir);
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void shouldTarToFileConsistentWithUntar() throws IOException {
		File tarFile = helper.file("test.tar");
		File untarDir = helper.file("untar");
		TarringInputStream in = new TarringInputStream(srcDir, none);
		IoUtil.setContent(tarFile, in);
		TarUtil.untar(tarFile, untarDir);
		assertDir(untarDir, srcDir);
		tarFile.delete();
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void shouldTarSubDirectoriesConsistentWithUntar() throws IOException {
		// Create files a..e/a..e/x
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("tar");
		for (char i = 'a'; i < 'f'; i++)
			for (char j = 'a'; j < 'f'; j++)
				builder.file(i + "/" + j + "/x", contentS);
		File untarDir = new File(helper.root, "untar");
		try (FileTestHelper tarHelper = builder.build()) {
			TarringInputStream in = new TarringInputStream(tarHelper.root, gzip);
			TarUtil.untar(in, untarDir, gzip);
			assertDir(untarDir, tarHelper.root);
		}
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void testTarWithLargeNumberOfFiles() throws IOException {
		// Create files z100..299
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("tar");
		for (int i = 100; i < 300; i++)
			builder.file("z" + i, contentS);
		File untarDir = new File(helper.root, "untar");
		try (FileTestHelper tarHelper = builder.build()) {
			TarringInputStream in = new TarringInputStream(tarHelper.root, gzip);
			TarUtil.untar(in, untarDir, gzip);
			assertDir(untarDir, tarHelper.root);
		}
		IoUtil.deleteAll(untarDir);
	}

	@Test
	public void testTarWithChecksum() throws IOException {
		// Create files z100..199 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("tar");
		for (int i = 100; i < 200; i++)
			builder.file(i + "/z" + i, contentS);
		File untarDir = new File(helper.root, "untar");
		try (FileTestHelper tarHelper = builder.build()) {
			Checksum tarChecksum = new Adler32();
			FilenameIterator fileIterator = new FilenameIterator(tarHelper.root);
			TarringInputStream in = new TarringInputStream(fileIterator, gzip, tarChecksum, 100, 100);
			Checksum untarChecksum = new Adler32();
			TarUtil.untar(in, untarDir, gzip, untarChecksum, 128);
	
			assertDir(untarDir, tarHelper.root);
			// Checksum fails depending on buffer boundaries, untar seems to leave unread data.
			assertThat(untarChecksum.getValue(), is(tarChecksum.getValue()));
		}
		IoUtil.deleteAll(untarDir);
	}

}
