package ceri.common.io;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import java.io.IOException;
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
		assertException(NoSuchElementException.class, new Runnable() {
			@Override
			public void run() {
				iterator.next();
			}
		});
	}
	
	@Test
	public void shouldIterateAllFilesByDefault() {
		FileIterator iterator = new FileIterator(helper.root);
		assertThat(iterator.next(), is(helper.file("a")));
		assertThat(iterator.next(), is(helper.file("a/a")));
		assertThat(iterator.next(), is(helper.file("a/a/a.txt")));
		assertThat(iterator.next(), is(helper.file("b")));
		assertThat(iterator.next(), is(helper.file("b/b.txt")));
		assertThat(iterator.next(), is(helper.file("c.txt")));
		assertFalse(iterator.hasNext());
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
		assertThat(iterator.next(), is(helper.file("a/a/a.txt")));
		assertThat(iterator.next(), is(helper.file("b/b.txt")));
		assertThat(iterator.next(), is(helper.file("c.txt")));
		assertFalse(iterator.hasNext());
		iterator = new FileIterator(helper.root, FileFilters.DIR);
		assertThat(iterator.next(), is(helper.file("a")));
		assertThat(iterator.next(), is(helper.file("a/a")));
		assertThat(iterator.next(), is(helper.file("b")));
		assertFalse(iterator.hasNext());
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
