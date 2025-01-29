package ceri.common.io;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionBiFunction;
import ceri.common.function.ExceptionBiPredicate;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionPredicate;

public class FileVisitUtil {

	private FileVisitUtil() {}

	/**
	 * Deletion function for path visitor. Can be used for post and file callbacks.
	 */
	public static <T> ExceptionBiFunction<IOException, Path, T, FileVisitResult> deletion() {
		return (path, _) -> {
			Files.delete(path);
			return result(true);
		};
	}

	/**
	 * Ignore exception function for failed file visit.
	 */
	public static <T> ExceptionBiFunction<IOException, T, IOException, FileVisitResult>
		ignoreOnFail() {
		return (_, _) -> result(true);
	}

	/**
	 * Throw exception function for failed file visit.
	 */
	public static <T> ExceptionBiFunction<IOException, T, IOException, FileVisitResult>
		throwOnFail() {
		return (_, ex) -> {
			throw ex;
		};
	}

	/**
	 * Returns result type on whether filter passes or not.
	 */
	public static FileVisitResult result(boolean filter) {
		return filter ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
	}

	/**
	 * Adapts a receiver to a compatible visit function. Can be used for pre, post and file
	 * callbacks.
	 */
	public static <T, U> ExceptionBiFunction<IOException, T, U, FileVisitResult>
		adapt(ExceptionFunction<IOException, T, FileVisitResult> fn) {
		return (t, _) -> fn.apply(t);
	}

	/**
	 * Adapts a receiver to a compatible visit function. Can be used for pre, post and file
	 * callbacks.
	 */
	public static <T, U> ExceptionBiFunction<IOException, T, U, FileVisitResult>
		adaptBiPredicate(ExceptionBiPredicate<IOException, T, U> test) {
		return (t, u) -> result(test.test(t, u));
	}

	/**
	 * Adapts a receiver to a compatible visit function. Can be used for pre, post and file
	 * callbacks.
	 */
	public static <T, U> ExceptionBiFunction<IOException, T, U, FileVisitResult>
		adaptPredicate(ExceptionPredicate<IOException, T> test) {
		return (t, _) -> result(test.test(t));
	}

	/**
	 * Adapts a receiver to a compatible visit function. Can be used for pre, post and file
	 * callbacks.
	 */
	public static <T, U> ExceptionBiFunction<IOException, T, U, FileVisitResult>
		adaptBiConsumer(ExceptionBiConsumer<IOException, T, U> consumer) {
		return (t, u) -> {
			consumer.accept(t, u);
			return result(true);
		};
	}

	/**
	 * Adapts a receiver to a compatible visit function. Can be used for pre, post and file
	 * callbacks.
	 */
	public static <T, U> ExceptionBiFunction<IOException, T, U, FileVisitResult>
		adaptConsumer(ExceptionConsumer<IOException, T> consumer) {
		return (t, _) -> {
			consumer.accept(t);
			return result(true);
		};
	}

	/**
	 * Visitor to be used with Files.walkFileTree, checking path and attributes.
	 */
	public static <T> FileVisitor<T> dirVisitor(ExceptionConsumer<IOException, T> dirFn) {
		return visitor(adaptConsumer(dirFn), null, null);
	}

	/**
	 * Visitor to be used with Files.walkFileTree, checking path and attributes.
	 */
	public static <T> FileVisitor<T> fileVisitor(ExceptionConsumer<IOException, T> fileFn) {
		return visitor(null, null, adaptConsumer(fileFn));
	}

	/**
	 * Visitor to be used with Files.walkFileTree, checking path and attributes.
	 */
	public static <T> FileVisitor<T> visitor(
		ExceptionBiFunction<IOException, T, BasicFileAttributes, FileVisitResult> preDirFn,
		ExceptionBiFunction<IOException, T, IOException, FileVisitResult> postDirFn,
		ExceptionBiFunction<IOException, T, BasicFileAttributes, FileVisitResult> fileFn) {
		return visitor(preDirFn, postDirFn, fileFn, null);
	}

	/**
	 * Visitor to be used with Files.walkFileTree, checking path and attributes.
	 */
	public static <T> FileVisitor<T> visitor(
		ExceptionBiFunction<IOException, T, BasicFileAttributes, FileVisitResult> preDirFn,
		ExceptionBiFunction<IOException, T, IOException, FileVisitResult> postDirFn,
		ExceptionBiFunction<IOException, T, BasicFileAttributes, FileVisitResult> fileFn,
		ExceptionBiFunction<IOException, T, IOException, FileVisitResult> failedFileFn) {
		return new FileVisitor<>() {
			@Override
			public FileVisitResult preVisitDirectory(T dir, BasicFileAttributes attrs)
				throws IOException {
				if (preDirFn != null) return preDirFn.apply(dir, attrs);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(T dir, IOException ex) throws IOException {
				if (postDirFn != null) return postDirFn.apply(dir, ex);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(T file, BasicFileAttributes attrs) throws IOException {
				if (fileFn != null) return fileFn.apply(file, attrs);
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(T file, IOException ex) throws IOException {
				if (failedFileFn != null) return failedFileFn.apply(file, ex);
				return FileVisitResult.CONTINUE;
			}
		};
	}

}
