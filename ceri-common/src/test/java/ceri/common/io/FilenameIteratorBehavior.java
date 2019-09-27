package ceri.common.io;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertThrown;
import static org.junit.Assert.assertFalse;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

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
	public void shouldFailOnRemove() {
		FilenameIterator iterator = new FilenameIterator(helper.file("a"));
		TestUtil.assertThrown(iterator::remove);
		iterator.next();
		TestUtil.assertThrown(iterator::remove);
	}

	@Test
	public void shouldHaveRelativeFilePaths() {
		FilenameIterator iterator = new FilenameIterator(helper.root);
		Collection<String> filenames = new HashSet<>();
		filenames.add(IoUtil.unixPath(iterator.next()));
		filenames.add(IoUtil.unixPath(iterator.next()));
		filenames.add(IoUtil.unixPath(iterator.next()));
		filenames.add(IoUtil.unixPath(iterator.next()));
		filenames.add(IoUtil.unixPath(iterator.next()));
		filenames.add(IoUtil.unixPath(iterator.next()));
		assertFalse(iterator.hasNext());
		assertCollection(filenames, "a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt");
	}

	@Test
	public void shouldWorkAtRootDir() {
		FilenameIterator iterator = new FilenameIterator(new File("/"));
		String path = iterator.next();
		assertFalse(path.startsWith(File.separator));
	}

}
