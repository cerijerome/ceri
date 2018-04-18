package ceri.common.io;

import static ceri.common.collection.StreamUtil.toList;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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

	public ResourceLister(Class<?> cls, String subDir, Pattern pattern) {
		if (subDir == null || subDir.length() == 0) subDir = "";
		else if (!subDir.endsWith("/")) subDir += "/";
		this.cls = cls;
		this.subDir = subDir;
		this.pattern = pattern;
	}

	public List<String> list() throws IOException {
		return new ArrayList<>(names());
	}

	private Collection<String> names() throws IOException {
		try {
			URL url = IoUtil.getClassUrl(cls);
			if (url == null) return Collections.emptySet();
			if (FILE.equals(url.getProtocol())) return names(filePath(url));
			if (JAR.equals(url.getProtocol())) return jarNames(url);
			if (JRT.equals(url.getProtocol())) return names(jrtPath(url));
			throw new IllegalArgumentException("Unsupported URL type: " + url);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	private Collection<String> names(Path path) throws IOException {
		path = path.getParent().resolve(subDir);
		return toList(Files.list(path).filter(Files::isRegularFile).map(Path::getFileName)
			.map(Path::toString).filter(p -> pattern == null || pattern.matcher(p).matches()));
	}

	private Path filePath(URL url) throws URISyntaxException {
		return Paths.get(url.toURI());
	}

	private Path jrtPath(URL url) {
		String s = JRT_PREFIX + url.getPath(); // missing modules prefix - why?
		return Paths.get(URI.create(s));
	}

	private Collection<String> jarNames(URL url) throws IOException {
		Matcher m = RegexUtil.matched(JAR_REGEX, url.toString());
		if (m == null) throw new IllegalArgumentException("Unsupported jar format: " + url);
		String jar = m.group(1);
		String file = m.group(2);
		try (FileSystem jarFs = zipFileSystem(jar)) {
			Path path = jarFs.getPath(file);
			return names(path);
		}
	}

	private FileSystem zipFileSystem(String zipFile) throws IOException {
		URI uri = URI.create(zipFile);
		return FileSystems.newFileSystem(uri, FILE_SYSTEM_ENV);
	}

}
