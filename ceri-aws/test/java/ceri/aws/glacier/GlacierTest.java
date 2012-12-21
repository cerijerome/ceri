package ceri.aws.glacier;

import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.aws.util.TarUtil.Compression;
import ceri.aws.util.TarringInputStream;
import ceri.common.io.FilenameIterator;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class GlacierTest {
	private static final String contentL = TestUtil.randomString(100000);
	private static final String contentS = TestUtil.randomString(1000);
	private static FileTestHelper helper = null;
	private static File srcDir = null;

	@BeforeClass
	public static void createTempFiles() throws IOException {
		helper =
			FileTestHelper.builder().file("src/a.txt", contentS).file("src/b/b.txt", contentL)
				.file("src/c/c/c.txt", contentS).file("src/d/d/d/d.txt", contentL).file(
					"src/e/e/e/e/e.txt", contentS).file("src/f/f/f/f/f/f.txt", contentL).build();
		srcDir = helper.file("src");
	}

	@AfterClass
	public static void deleteTempFiles() {
		//helper.close();
	}

	@Test
	public void test() throws IOException {
		TarringInputStream in =
			new TarringInputStream(new FilenameIterator(srcDir), Compression.gzip, null, 2000, 2000);
		GlacierFiler glacier =
			new GlacierFiler(helper.file("test.tar.bz2"), "uploadId00", "archiveId00", 0);
		MultipartUploader uploader =
			MultipartUploader.builder(glacier, in, "test-vault").partSize(1000).build();
		uploader.execute();
	}

}
