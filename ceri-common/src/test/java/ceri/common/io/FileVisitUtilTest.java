package ceri.common.io;

import static ceri.common.test.TestUtil.assertHelperPaths;
import static ceri.common.test.TestUtil.assertThrown;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.Capturer;
import ceri.common.test.FileTestHelper;

public class FileVisitUtilTest {
	private static FileTestHelper helper = null;

	@BeforeClass
	public static void createFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bbb")
			.file("c.txt", "ccc").build();
	}

	@AfterClass
	public static void deleteFiles() {
		helper.close();
	}

	@Test
	public void testIgnoreOnFail() throws IOException {
		FileVisitor<Path> visitor =
			FileVisitUtil.visitor(null, null, null, FileVisitUtil.ignoreOnFail());
		assertThat(visitor.visitFileFailed(Path.of(""), new IOException()), is(CONTINUE));
	}

	@Test
	public void testThrowOnFail() {
		FileVisitor<Path> visitor =
			FileVisitUtil.visitor(null, null, null, FileVisitUtil.throwOnFail());
		assertThrown(() -> visitor.visitFileFailed(Path.of(""), new IOException()));
	}

	@Test
	public void testAdapters() throws IOException {
		FileVisitor<Path> visitor = FileVisitUtil.visitor( //
			FileVisitUtil.adaptBiPredicate((dir, attr) -> dir != null),
			FileVisitUtil.adaptPredicate(dir -> dir != null),
			FileVisitUtil.adapt(file -> file != null ? CONTINUE : TERMINATE));
		assertThat(visitor.preVisitDirectory(null, null), is(SKIP_SUBTREE));
		assertThat(visitor.preVisitDirectory(Path.of(""), null), is(CONTINUE));
		assertThat(visitor.postVisitDirectory(null, null), is(SKIP_SUBTREE));
		assertThat(visitor.postVisitDirectory(Path.of(""), null), is(CONTINUE));
		assertThat(visitor.visitFile(null, null), is(TERMINATE));
		assertThat(visitor.visitFile(Path.of(""), null), is(CONTINUE));
	}

	@Test
	public void testDirVisitor() throws IOException {
		Capturer<Path> dirCap = Capturer.of();
		FileVisitor<Path> visitor = FileVisitUtil.dirVisitor(dirCap::accept);
		Files.walkFileTree(helper.root, visitor);
		assertHelperPaths(dirCap.values, helper, "", "a", "a/a", "b");
	}

	@Test
	public void testFileVisitor() throws IOException {
		Capturer<Path> fileCap = Capturer.of();
		FileVisitor<Path> visitor = FileVisitUtil.<Path>fileVisitor(fileCap::accept);
		Files.walkFileTree(helper.root, visitor);
		assertHelperPaths(fileCap.values, helper, "a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testVisitor() throws IOException {
		Capturer<Path> preDirCap = Capturer.of();
		Capturer<Path> postDirCap = Capturer.of();
		Capturer<Path> fileCap = Capturer.of();
		FileVisitor<Path> visitor = FileVisitUtil.visitor( //
			FileVisitUtil.adaptConsumer(preDirCap::accept),
			FileVisitUtil.adaptConsumer(postDirCap::accept),
			FileVisitUtil.adaptConsumer(fileCap::accept));
		Files.walkFileTree(helper.root, visitor);
		assertHelperPaths(preDirCap.values, helper, "", "a", "a/a", "b");
		assertHelperPaths(postDirCap.values, helper, "", "a", "a/a", "b");
		assertHelperPaths(fileCap.values, helper, "a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testVisitorWithException() {
		IOException ex = new IOException("Test");
		FileVisitor<Path> visitor = FileVisitUtil.visitor(null, null, //
			FileVisitUtil.adaptConsumer(file -> {
				if ("b.txt".equals(IoUtil.fileName(file))) throw ex;
			}));
		assertThrown(() -> Files.walkFileTree(helper.root, visitor));
	}

	@Test
	public void testVisitorWithFailure() throws IOException {
		Capturer.Bi<Path, IOException> failCap = Capturer.ofBi();
		FileVisitor<Path> visitor = FileVisitUtil.visitor(null, null, null, //
			FileVisitUtil.adaptBiConsumer(failCap::accept));
		Path file = helper.path("c.txt");
		IOException ex = new IOException("Test");
		visitor.visitFileFailed(file, ex);
		failCap.first.verify(file);
		failCap.second.verify(ex);
	}

	@Test
	public void testVisitorStop() throws IOException {
		Capturer<Path> preDirCap = Capturer.of();
		Capturer<Path> fileCap = Capturer.of();
		FileVisitor<Path> visitor = FileVisitUtil.visitor( //
			FileVisitUtil.adaptConsumer(preDirCap::accept), //
			(dir, e) -> TERMINATE, //
			FileVisitUtil.adaptConsumer(fileCap::accept));
		Files.walkFileTree(helper.root, visitor);
		assertHelperPaths(preDirCap.values, helper, "", "a", "a/a");
		assertHelperPaths(fileCap.values, helper, "a/a/a.txt");
	}

	@Test
	public void testNoOpVisitor() throws IOException {
		FileVisitor<Path> visitor = FileVisitUtil.visitor(null, null, null, null);
		visitor.preVisitDirectory(null, null);
		visitor.postVisitDirectory(null, null);
		visitor.visitFile(null, null);
		visitor.visitFileFailed(null, null);
		Files.walkFileTree(helper.root, visitor);
	}

}
