package ceri.common.io;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.net.NetUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.RegexUtil;

/**
 * Provides a path to a resource directory or content file. Can be used to walk/list directories.
 * Supports regular files, jrt modules, and jar/zip containers. For jar/zip containers, an open
 * FileSystem is held, and must be closed by closing the ResourcePath after use.
 */
public class ResourcePath implements Closeable {
	private static final String FILE_PROTOCOL = "file";
	private static final String JRT_PROTOCOL = "jrt";
	private static final String JRT_MODULES = JRT_PROTOCOL + ":/modules";
	private static final Pattern ZIP_REGEX = Pattern.compile("([^!]+)!(.*)");
	private final FileSystem closeableFs;
	private final Path path;

	/**
	 * Returns the path of class name with suffix.
	 */
	public static ResourcePath ofSuffix(Class<?> cls, String suffix) throws IOException {
		return of(cls, cls.getSimpleName() + "." + suffix);
	}

	/**
	 * Returns the path applied relative to class package root directory.
	 */
	public static ResourcePath ofRoot(Class<?> cls, String... paths) throws IOException {
		int levels = ReflectUtil.packageLevels(cls) + 1;
		return of(cls, path -> IoUtil.extend(IoUtil.shorten(path, levels), paths));
	}

	/**
	 * Returns the path applied relative to class directory.
	 */
	public static ResourcePath of(Class<?> cls, String... paths) throws IOException {
		return of(cls, path -> IoUtil.extend(path.getParent(), paths));
	}

	private static ResourcePath of(Class<?> cls, Function<Path, Path> pathAdjuster)
		throws IOException {
		URL url = IoUtil.classUrl(cls);
		if (url == null) return null;
		switch (url.getProtocol()) {
		case FILE_PROTOCOL:
			return ofFile(url, pathAdjuster);
		case JRT_PROTOCOL:
			return ofJrt(url, pathAdjuster);
		default:
			return ofZip(url, pathAdjuster); // zip, jar
		}
	}

	private static ResourcePath ofFile(URL url, Function<Path, Path> pathAdjuster) {
		Path path = Path.of(NetUtil.uri(url));
		return new ResourcePath(null, pathAdjuster.apply(path));
	}

	private static ResourcePath ofJrt(URL url, Function<Path, Path> pathAdjuster) {
		Path path = Path.of(URI.create(JRT_MODULES + url.getPath()));
		return new ResourcePath(null, pathAdjuster.apply(path));
	}

	@SuppressWarnings("resource")
	private static ResourcePath ofZip(URL url, Function<Path, Path> pathAdjuster)
		throws IOException {
		Matcher m = RegexUtil.matched(ZIP_REGEX, url.toString());
		Objects.requireNonNull(m);
		String zipName = m.group(1);
		String pathName = m.group(2);
		FileSystem fs = FileSystems.newFileSystem(URI.create(zipName), Map.of());
		try {
			Path path = fs.getPath(pathName);
			return new ResourcePath(fs, pathAdjuster.apply(path));
		} catch (RuntimeException e) {
			fs.close();
			throw e;
		}
	}

	private ResourcePath(FileSystem fs, Path path) {
		this.closeableFs = fs;
		this.path = path;
	}

	public Path path() {
		return path;
	}

	public Path resolve(String... paths) {
		return IoUtil.extend(path, paths);
	}

	public byte[] readBytes() throws IOException {
		return Files.readAllBytes(path);
	}

	public String readString() throws IOException {
		return readString(StandardCharsets.UTF_8);
	}

	public String readString(Charset charset) throws IOException {
		return Files.readString(path, charset);
	}

	@Override
	public void close() throws IOException {
		if (closeableFs != null) closeableFs.close();
	}

	@Override
	public String toString() {
		return path.toString();
	}
}
