package ceri.common.io;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static org.junit.Assert.assertFalse;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class FileIteratorBehavior {
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
	public void shouldHaveNoElementsIfRootIsAFile() {
		FileIterator iterator = new FileIterator(helper.file("a/a/a.txt"));
		assertFalse(iterator.hasNext());
	}
	
	@Test
	public void shouldThrowExceptionIfNoMoreElements() {
		final FileIterator iterator = new FileIterator(helper.file("a/a"));
		iterator.next();
		assertException(NoSuchElementException.class, () -> iterator.next());
	}
	
	@Test
	public void shouldIterateAllFilesByDefault() {
		FileIterator iterator = new FileIterator(helper.root);
		Collection<File> files = new HashSet<>();
		files.add(iterator.next());
		files.add(iterator.next());
		files.add(iterator.next());
		files.add(iterator.next());
		files.add(iterator.next());
		files.add(iterator.next());
		assertFalse(iterator.hasNext());
		assertCollection(files,  helper.files("a", "a/a", "a/a/a.txt", "b", "b/b.txt", "c.txt"));
	}

	@Test
	public void shouldHandleLargeDirTree() {
		for (int i = 10; i <= 50; i++) {
			for (int j = 10; j <= 50; j++) {
				helper.file("dirs/" + i + "/" + j).mkdirs();
			}
		}
		FileIterator iterator = new FileIterator(helper.root);
		while (iterator.hasNext()) iterator.next();
		IoUtil.deleteAll(helper.file("dirs"));
	}
	
	@Test
	public void shouldOnlyListFilesThatMatchTheFilter() {
		FileIterator iterator = new FileIterator(helper.root, FileFilters.FILE);
		Collection<File> files = new HashSet<>();
		files.add(iterator.next());
		files.add(iterator.next());
		files.add(iterator.next());
		assertFalse(iterator.hasNext());
		assertCollection(files,  helper.files("a/a/a.txt", "b/b.txt", "c.txt"));
		iterator = new FileIterator(helper.root, FileFilters.DIR);
		files = new HashSet<>();
		files.add(iterator.next());
		files.add(iterator.next());
		files.add(iterator.next());
		assertFalse(iterator.hasNext());
		assertCollection(files,  helper.files("a", "a/a", "b"));
	}

	@Test
	public void shouldHaveNoElementsForNonMatchingFilter() {
		FileIterator iterator = new FileIterator(helper.root, FileFilters.NULL);
		assertFalse(iterator.hasNext());
	}

	@Test
	public void shouldHaveNoElementsForNonExistentRoot() {
		FileIterator iterator = new FileIterator(helper.file("x"));
		assertFalse(iterator.hasNext());
	}

}
