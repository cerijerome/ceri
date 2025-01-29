package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertHelperPaths;
import static ceri.common.test.AssertUtil.assertThrown;
import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;
import static java.nio.file.FileVisitResult.TERMINATE;
import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.Captor;
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
		assertEquals(visitor.visitFileFailed(Path.of(""), new IOException()), CONTINUE);
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
			FileVisitUtil.adaptBiPredicate((dir, _) -> dir != null),
			FileVisitUtil.adaptPredicate(dir -> dir != null),
			FileVisitUtil.adapt(file -> file != null ? CONTINUE : TERMINATE));
		assertEquals(visitor.preVisitDirectory(null, null), SKIP_SUBTREE);
		assertEquals(visitor.preVisitDirectory(Path.of(""), null), CONTINUE);
		assertEquals(visitor.postVisitDirectory(null, null), SKIP_SUBTREE);
		assertEquals(visitor.postVisitDirectory(Path.of(""), null), CONTINUE);
		assertEquals(visitor.visitFile(null, null), TERMINATE);
		assertEquals(visitor.visitFile(Path.of(""), null), CONTINUE);
	}

	@Test
	public void testDirVisitor() throws IOException {
		Captor<Path> dirCap = Captor.of();
		FileVisitor<Path> visitor = FileVisitUtil.dirVisitor(dirCap::accept);
		Files.walkFileTree(helper.root, visitor);
		assertHelperPaths(dirCap.values, helper, "", "a", "a/a", "b");
	}

	@Test
	public void testFileVisitor() throws IOException {
		Captor<Path> fileCap = Captor.of();
		FileVisitor<Path> visitor = FileVisitUtil.<Path>fileVisitor(fileCap::accept);
		Files.walkFileTree(helper.root, visitor);
		assertHelperPaths(fileCap.values, helper, "a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testVisitor() throws IOException {
		Captor<Path> preDirCap = Captor.of();
		Captor<Path> postDirCap = Captor.of();
		Captor<Path> fileCap = Captor.of();
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
		Captor.Bi<Path, IOException> failCap = Captor.ofBi();
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
		Captor<Path> preDirCap = Captor.of();
		Captor<Path> fileCap = Captor.of();
		FileVisitor<Path> visitor = FileVisitUtil.visitor( //
			FileVisitUtil.adaptConsumer(preDirCap::accept), //
			(_, _) -> TERMINATE, //
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
