package ceri.common.io;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import ceri.common.function.ExceptionBiFunction;

public class FileVisitUtil {
	private static final String FS_ROOT = "/";

	/**
	 * Walks the tree of a structured file (zip/jar).
	 */
	public static void walk(Path file, FileVisitor<Path> visitor) throws IOException {
		walk(file, FS_ROOT, visitor);
	}

	/**
	 * Walks the tree of a structured file (zip/jar).
	 */
	public static void walk(Path file, String root, FileVisitor<Path> visitor) throws IOException {
		try (FileSystem fs = FileSystems.newFileSystem(file, null)) {
			Path rootPath = fs.getPath(root);
			Files.walkFileTree(rootPath, visitor);
		}
	}

	/**
	 * Adapts a predicate that checks indexed part of a path.
	 */
	public static Predicate<Path> byIndex(int index, Predicate<String> test) {
		if (test == null) return null;
		return path -> path.getNameCount() != index + 1 ||
			test.test(path.getName(index).toString());
	}

	/**
	 * Returns result type on whether filter passes or not.
	 */
	public static FileVisitResult result(boolean filter) {
		return filter ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
	}

	/**
	 * Adapts a receiver to a compatible visit function.
	 */
	public static ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult>
		adapt(Function<Path, FileVisitResult> fn) {
		return fn == null ? null : (path, attr) -> fn.apply(path);
	}

	/**
	 * Adapts a receiver to a compatible visit function.
	 */
	public static ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult>
		adaptBiPredicate(BiPredicate<Path, BasicFileAttributes> test) {
		return test == null ? null : (path, attr) -> result(test.test(path, attr));
	}

	/**
	 * Adapts a receiver to a compatible visit function.
	 */
	public static ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult>
		adaptPredicate(Predicate<Path> test) {
		return test == null ? null : (path, attr) -> result(test.test(path));
	}

	/**
	 * Adapts a receiver to a compatible visit function.
	 */
	public static ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult>
		adaptBiConsumer(BiConsumer<Path, BasicFileAttributes> consumer) {
		return consumer == null ? null : (path, attr) -> {
			consumer.accept(path, attr);
			return result(true);
		};
	}

	/**
	 * Adapts a receiver to a compatible visit function.
	 */
	public static ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult>
		adaptConsumer(Consumer<Path> consumer) {
		return consumer == null ? null : (path, attr) -> {
			consumer.accept(path);
			return result(true);
		};
	}

	/**
	 * Visitor to be used with Files.walkFileTree.
	 */
	public static FileVisitor<Path> dirVisitor(
		ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult> dirFn) {
		return visitor(dirFn, null);
	}

	/**
	 * Visitor to be used with Files.walkFileTree, checking path and attributes.
	 */
	public static FileVisitor<Path> fileVisitor(
		ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult> fileFn) {
		return visitor(null, fileFn);
	}

	/**
	 * Visitor to be used with Files.walkFileTree, checking path and attributes.
	 */
	public static FileVisitor<Path> visitor(
		ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult> dirFn,
		ExceptionBiFunction<IOException, Path, BasicFileAttributes, FileVisitResult> fileFn) {
		return new SimpleFileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
				throws IOException {
				if (dirFn != null) return dirFn.apply(dir, attrs);
				return super.preVisitDirectory(dir, attrs);
			}

			@Override
			public FileVisitResult visitFile(Path dir, BasicFileAttributes attrs)
				throws IOException {
				if (fileFn != null) return fileFn.apply(dir, attrs);
				return super.visitFile(dir, attrs);
			}
		};
	}

}
