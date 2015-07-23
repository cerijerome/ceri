package ceri.common.zip;

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
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;

public class ZippingInputStreamBehavior {
	private static final boolean checksumIssueIsSolved = true;
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

	@Test
	public void shouldZipToStreamConsistentWithUnzip() throws IOException {
		File unzipDir = helper.file("unzip");
		try (ZippingInputStream in = new ZippingInputStream(srcDir)) {
			ZipUtil.unzip(in, unzipDir);
			assertDir(unzipDir, srcDir);
			IoUtil.deleteAll(unzipDir);
		}
	}
	
	@Test
	public void shouldZipToFileConsistentWithUnzip() throws IOException {
		File zipFile = helper.file("test.zip");
		File unzipDir = helper.file("unzip");
		try (ZippingInputStream in = new ZippingInputStream(srcDir)) {
			IoUtil.setContent(zipFile, in);
			ZipUtil.unzip(zipFile, unzipDir);
			assertDir(unzipDir, srcDir);
			zipFile.delete();
			IoUtil.deleteAll(unzipDir);
		}
	}
	
	@Test
	public void shouldZipSubDirectoriesConsistentWithUnzip() throws IOException {
		// Create files a..e/a..e/x with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("zip");
		for (char i = 'a'; i < 'f'; i++)
			for (char j = 'a'; j < 'f'; j++)
				builder.file(i + "/" + j + "/x", "abcdefghijklmnopqrstuvwxyz");
		try (
			FileTestHelper zipHelper = builder.build();
			ZippingInputStream in = new ZippingInputStream(zipHelper.root)
		) {
			File unzipDir = new File(helper.root, "unzip");
			ZipUtil.unzip(in, unzipDir);
			assertDir(unzipDir, zipHelper.root);
			IoUtil.deleteAll(unzipDir);
		}
	}
	
	@Test
	public void testZipWithLargeNumberOfFiles() throws IOException {
		// Create files z100..199 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("zip");
		for (int i = 100; i < 199; i++)
			builder.file("z" + i, "abcdefghijklmnopqrstuvwxyz");
		try (
			FileTestHelper zipHelper = builder.build();
			ZippingInputStream in = new ZippingInputStream(zipHelper.root)
		) {
			File unzipDir = new File(helper.root, "unzip");
			ZipUtil.unzip(in, unzipDir);
			assertDir(unzipDir, zipHelper.root);
			IoUtil.deleteAll(unzipDir);
		}
	}
	
	@Test
	public void testZipWithChecksum() throws IOException {
		// Create files z100..199 with contents a..z
		FileTestHelper.Builder builder = FileTestHelper.builder(helper.root).root("zip");
		for (int i = 100; i < 200; i++)
			builder.file(i + "/z" + i, "abcdefghijklmnopqrstuvwxyz");
		Checksum zipChecksum = new Adler32();
		try (
			FileTestHelper zipHelper = builder.build();
			ZippingInputStream in = new ZippingInputStream(zipHelper.root, zipChecksum, 100, 100)) {
			File unzipDir = new File(helper.root, "unzip");
			Checksum unzipChecksum = new Adler32();
			ZipUtil.unzip(in, unzipDir, unzipChecksum, 128);
			assertDir(unzipDir, zipHelper.root);
			// Checksum fails depending on buffer boundaries, unzip seems to leave unread data.
			if (checksumIssueIsSolved) assertThat(unzipChecksum.getValue(), is(zipChecksum.getValue()));
			IoUtil.deleteAll(unzipDir);
		}
	}
	
}
