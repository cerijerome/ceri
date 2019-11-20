package ceri.common.io;

import static ceri.common.collection.StreamUtil.toList;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.ImmutableUtil;
import ceri.common.text.RegexUtil;

/**
 * Resource lookup service from a directory relative to the given class. Handles file resources and
 * resources within a jar files, jst modules.
 */
public class ResourceLister {
	private static final String FILE = "file";
	private static final String JAR = "jar";
	private static final String JRT = "jrt";
	private static final String JRT_PREFIX = "jrt:/modules";
	private static final Pattern JAR_REGEX = Pattern.compile("(jar:[^!]+)!(.*)");
	private static final Map<String, String> FILE_SYSTEM_ENV =
		ImmutableUtil.asMap("create", Boolean.TRUE.toString());
	private final Class<?> cls;
	private final String subDir;
	private final Pattern pattern;

	public static ResourceLister of(Class<?> cls) {
		return of(cls, null);
	}

	public static ResourceLister of(Class<?> cls, String subDir) {
		return of(cls, subDir, (Pattern) null);
	}

	public static ResourceLister of(Class<?> cls, String subDir, String pattern) {
		return of(cls, subDir, Pattern.compile(pattern));
	}

	public static ResourceLister of(Class<?> cls, String subDir, Pattern pattern) {
		return new ResourceLister(cls, subDir, pattern);
	}

	private ResourceLister(Class<?> cls, String subDir, Pattern pattern) {
		if (subDir == null) subDir = "";
		else if (!subDir.isEmpty() && !subDir.endsWith("/")) subDir += "/";
		this.cls = cls;
		this.subDir = subDir;
		this.pattern = pattern;
	}

	public List<String> list() throws IOException {
		return list(IoUtil.classUrl(cls), subDir, pattern);
	}

	static List<String> list(URL url, String subDir, Pattern pattern) throws IOException {
		if (url == null) return Collections.emptyList();
		if (FILE.equals(url.getProtocol())) return names(filePath(url), subDir, pattern);
		if (JAR.equals(url.getProtocol())) return jarNames(url, subDir, pattern);
		if (JRT.equals(url.getProtocol())) return names(jrtPath(url), subDir, pattern);
		throw new IllegalArgumentException("Unsupported URL type: " + url);
	}

	private static Path filePath(URL url) throws IOException {
		return IoUtil.IO_ADAPTER.get(() -> Path.of(url.toURI()));
	}

	private static Path jrtPath(URL url) {
		String s = JRT_PREFIX + url.getPath(); // missing modules prefix - why?
		return Path.of(URI.create(s));
	}

	private static List<String> jarNames(URL url, String subDir, Pattern pattern)
		throws IOException {
		// must match according to jar URL spec
		Matcher m = RegexUtil.matched(JAR_REGEX, url.toString());
		String jar = m.group(1);
		String file = m.group(2);
		try (FileSystem jarFs = zipFileSystem(URI.create(jar))) {
			Path path = jarFs.getPath(file);
			return names(path, subDir, pattern);
		}
	}

	private static FileSystem zipFileSystem(URI uri) throws IOException {
		return FileSystems.newFileSystem(uri, FILE_SYSTEM_ENV);
	}

	private static List<String> names(Path path, String subDir, Pattern pattern)
		throws IOException {
		path = path.getParent().resolve(subDir);
		return toList(Files.list(path).filter(Files::isRegularFile).map(Path::getFileName)
			.map(Path::toString).filter(p -> pattern == null || pattern.matcher(p).matches()));
	}

}
