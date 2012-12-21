package ceri.common.io;

import static ceri.common.test.TestUtil.isArray;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class RegexFilenameFilterBehavior {
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

	@Test
	public void testMatchesDirsAndFileNames() {
		RegexFilenameFilter filter = new RegexFilenameFilter(false, "[ac].*");
		String[] filenames = helper.root.list(filter);
		assertThat(filenames, isArray("a", "c.txt"));
	}

	@Test
	public void testMatchesDirAndFiles() {
		RegexFilenameFilter filter = new RegexFilenameFilter(false, "[ac].*");
		File[] files = helper.root.listFiles(filter.asFilename());
		assertThat(files, isArray(helper.file("a"), helper.file("c.txt")));
		files = helper.root.listFiles(filter.asFile());
		assertThat(files, isArray(helper.file("a"), helper.file("c.txt")));
	}

	@Test
	public void testMatchesAbsolutePath() {
		RegexFilenameFilter filter =
			new RegexFilenameFilter(true, ".*/" + helper.root.getName() + "/.*");
		String[] filenames = helper.root.list(filter);
		assertThat(filenames, isArray("a", "b", "c.txt"));
	}

}
