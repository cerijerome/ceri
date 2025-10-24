package ceri.common.io;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIoe;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.Captor;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class FileVisitorsTest {
	private FileTestHelper helper = null;

	@After
	public void after() {
		helper = TestUtil.close(helper);
	}

	@Test
	public void testIgnoreOnFail() throws IOException {
		var visitor = FileVisitors.of(null, null, null, FileVisitors.ignoreOnFail());
		assertEquals(visitor.visitFileFailed(Path.of(""), new IOException()),
			FileVisitResult.CONTINUE);
	}

	@Test
	public void testThrowOnFail() {
		var visitor = FileVisitors.of(null, null, null, FileVisitors.throwOnFail());
		assertIoe(() -> visitor.visitFileFailed(Path.of(""), new IOException()));
	}

	@Test
	public void testAdapters() throws IOException {
		var visitor = FileVisitors.of(FileVisitors.adaptBiPredicate((dir, _) -> dir != null),
			FileVisitors.adaptPredicate(dir -> dir != null), FileVisitors.adapt(
				file -> file != null ? FileVisitResult.CONTINUE : FileVisitResult.TERMINATE));
		assertEquals(visitor.preVisitDirectory(null, null), FileVisitResult.SKIP_SUBTREE);
		assertEquals(visitor.preVisitDirectory(Path.of(""), null), FileVisitResult.CONTINUE);
		assertEquals(visitor.postVisitDirectory(null, null), FileVisitResult.SKIP_SUBTREE);
		assertEquals(visitor.postVisitDirectory(Path.of(""), null), FileVisitResult.CONTINUE);
		assertEquals(visitor.visitFile(null, null), FileVisitResult.TERMINATE);
		assertEquals(visitor.visitFile(Path.of(""), null), FileVisitResult.CONTINUE);
	}

	@Test
	public void testDirVisitor() throws IOException {
		initFiles();
		var dirCap = Captor.<Path>of();
		var visitor = FileVisitors.<Path>ofDir(dirCap::accept);
		Files.walkFileTree(helper.root, visitor);
		helper.assertPaths(dirCap.values, "", "a", "a/a", "b");
	}

	@Test
	public void testFileVisitor() throws IOException {
		initFiles();
		var fileCap = Captor.<Path>of();
		var visitor = FileVisitors.<Path>ofFile(fileCap::accept);
		Files.walkFileTree(helper.root, visitor);
		helper.assertPaths(fileCap.values, "a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testVisitor() throws IOException {
		initFiles();
		var preDirCap = Captor.<Path>of();
		var postDirCap = Captor.<Path>of();
		var fileCap = Captor.<Path>of();
		var visitor = FileVisitors.<Path>of(FileVisitors.adaptConsumer(preDirCap::accept),
			FileVisitors.adaptConsumer(postDirCap::accept),
			FileVisitors.adaptConsumer(fileCap::accept));
		Files.walkFileTree(helper.root, visitor);
		helper.assertPaths(preDirCap.values, "", "a", "a/a", "b");
		helper.assertPaths(postDirCap.values, "", "a", "a/a", "b");
		helper.assertPaths(fileCap.values, "a/a/a.txt", "b/b.txt", "c.txt");
	}

	@Test
	public void testVisitorWithException() throws IOException {
		initFiles();
		var ex = new IOException("Test");
		var visitor = FileVisitors.<Path>of(null, null, FileVisitors.adaptConsumer(file -> {
			if ("b.txt".equals(Paths.name(file))) throw ex;
		}));
		assertIoe(() -> Files.walkFileTree(helper.root, visitor));
	}

	@Test
	public void testVisitorWithFailure() throws IOException {
		initFiles();
		var failCap = Captor.ofBi();
		var visitor =
			FileVisitors.of(null, null, null, FileVisitors.adaptBiConsumer(failCap::accept));
		var file = helper.path("c.txt");
		var ex = new IOException("Test");
		visitor.visitFileFailed(file, ex);
		failCap.first.verify(file);
		failCap.second.verify(ex);
	}

	@Test
	public void testVisitorStop() throws IOException {
		initFiles();
		var preDirCap = Captor.<Path>of();
		var fileCap = Captor.<Path>of();
		var visitor = FileVisitors.<Path>of(FileVisitors.adaptConsumer(preDirCap::accept),
			(_, _) -> FileVisitResult.TERMINATE, FileVisitors.adaptConsumer(fileCap::accept));
		Files.walkFileTree(helper.root, visitor);
		helper.assertPaths(preDirCap.values, "", "a", "a/a");
		helper.assertPaths(fileCap.values, "a/a/a.txt");
	}

	@Test
	public void testNoOpVisitor() throws IOException {
		initFiles();
		var visitor = FileVisitors.of(null, null, null, null);
		visitor.preVisitDirectory(null, null);
		visitor.postVisitDirectory(null, null);
		visitor.visitFile(null, null);
		visitor.visitFileFailed(null, null);
		Files.walkFileTree(helper.root, visitor);
	}

	private void initFiles() throws IOException {
		helper = FileTestHelper.builder().file("a/a/a.txt", "aaa").file("b/b.txt", "bbb")
			.file("c.txt", "ccc").build();
	}
}
