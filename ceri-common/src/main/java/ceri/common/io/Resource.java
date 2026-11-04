package ceri.common.io;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import ceri.common.function.Closeables;
import ceri.common.function.Excepts;
import ceri.common.function.Functions;
import ceri.common.net.Urls;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Regex;

/**
 * Provides a path to a resource directory or content file. Can be used to walk/list directories.
 * Supports regular files, jrt modules, and jar/zip containers. For jar/zip containers, an open
 * FileSystem is held, and must be closed by closing this resource after use.
 */
public class Resource implements Functions.Closeable {
	private static final String CLASS_SUFFIX = ".class";
	private static final String FILE_PROTOCOL = "file";
	private static final String JRT_PROTOCOL = "jrt";
	private static final Pattern ZIP_REGEX = Pattern.compile("([^!]+)!(.*)");
	private final FileSystem closeableFs;
	private final Path path;

	/**
	 * Returns the URL path for class.
	 */
	public static URL url(Class<?> cls) {
		if (cls == null) return null;
		return cls.getResource(cls.getSimpleName() + CLASS_SUFFIX);
	}

	/**
	 * Reads content from a resource with paths applied relative to class directory.
	 */
	public static byte[] bytes(Class<?> cls, String... paths) throws IOException {
		try (var r = of(cls, paths)) {
			return r.bytes();
		}
	}

	/**
	 * Reads content from a resource with paths applied relative to class directory.
	 */
	public static String string(Class<?> cls, String... paths) throws IOException {
		return string(cls, Chars.UTF8, paths);
	}

	/**
	 * Reads content from a resource with paths applied relative to class directory.
	 */
	public static String string(Class<?> cls, Charset charset, String... paths) throws IOException {
		try (var r = of(cls, paths)) {
			return r.string(charset);
		}
	}

	/**
	 * Returns the resource path applied relative to class package root directory.
	 */
	public static Resource root(Class<?> cls, String... paths) {
		int levels = Reflect.packageLevels(cls) + 1;
		return resource(cls, path -> Paths.extend(Paths.shorten(path, levels), paths));
	}

	/**
	 * Returns the resource path applied relative to class directory.
	 */
	public static Resource of(Class<?> cls, String... paths) {
		return resource(cls, path -> Paths.extend(path.getParent(), paths));
	}

	private Resource(FileSystem fs, Path path) {
		this.closeableFs = fs;
		this.path = path;
	}

	/**
	 * Returns the resource path.
	 */
	public Path path() {
		return path;
	}

	/**
	 * Resolves a path relative to this resource path.
	 */
	public Path resolve(String... paths) {
		return Paths.extend(path, paths);
	}

	/**
	 * Reads the resource as bytes.
	 */
	public byte[] bytes() throws IOException {
		return Files.readAllBytes(path);
	}

	/**
	 * Reads the resource as a string.
	 */
	public String string() throws IOException {
		return string(Chars.UTF8);
	}

	/**
	 * Reads the resource as a string.
	 */
	public String string(Charset charset) throws IOException {
		return Files.readString(path, charset);
	}

	@Override
	public void close() {
		Closeables.close(closeableFs);
	}

	@Override
	public String toString() {
		return path.toString();
	}

	// support

	private static Resource resource(Class<?> cls, Functions.Operator<Path> pathAdjuster) {
		var url = url(cls);
		if (url == null) return null;
		return switch (url.getProtocol()) {
			case FILE_PROTOCOL, JRT_PROTOCOL -> file(url, pathAdjuster);
			default -> zip(url, pathAdjuster); // zip, jar
		};
	}

	private static <E extends Exception> Resource file(URL url,
		Excepts.Function<E, Path, Path> pathAdjuster) throws E {
		var path = Path.of(Urls.uri(url));
		return new Resource(null, pathAdjuster.apply(path));
	}

	@SuppressWarnings("resource")
	private static Resource zip(URL url, Functions.Operator<Path> pathAdjuster) {
		var m = Regex.validMatch(ZIP_REGEX, url.toString());
		var zipName = m.group(1);
		var pathName = m.group(2);
		return Closeables.applyOrClose(Paths.fs(URI.create(zipName)),
			fs -> new Resource(fs, pathAdjuster.apply(fs.getPath(pathName))));
	}
}
