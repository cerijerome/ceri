package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import java.io.File;
import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class FilenameIteratorBehavior {
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
	public void shouldHaveRelativeFilePaths() {
		FilenameIterator iterator = new FilenameIterator(helper.root);
		assertThat(iterator.next(), is("a"));
		assertThat(IoUtil.convertPath(iterator.next()), is("a/a"));
		assertThat(IoUtil.convertPath(iterator.next()), is("a/a/a.txt"));
	}

	@Test
	public void shouldWorkAtRootDir() {
		FilenameIterator iterator = new FilenameIterator(new File("/"));
		String path = iterator.next();
		assertFalse(path.startsWith(File.separator));
	}

}
