package ceri.common.io;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import ceri.common.collection.CollectionUtil;

/**
 * Resource lookup service from a directory relative to the given class. Handles file resources and
 * resources within a jar file.
 */
public class ResourceLister {
	private static final String FILE = "file";
	private static final String JAR = "jar";
	private static final String JAR_SEPARATOR = "!/";
	private final Class<?> cls;
	private final String subDir;
	private final Pattern pattern;

	public ResourceLister(Class<?> cls) {
		this(cls, null, null);
	}
	
	public ResourceLister(Class<?> cls, String subDir, Pattern pattern) {
		if (subDir == null || subDir.length() == 0) subDir = "";
		else if (!subDir.endsWith("/")) subDir += "/";
		this.cls = cls;
		this.subDir = subDir;
		this.pattern = pattern;
	}

	public List<String> list() throws IOException {
		return new ArrayList<>(names(cls, subDir)); 
	}
	
	private Collection<String> names(Class<?> cls, String subDir) throws IOException {
		try {
			URL url = IoUtil.getClassUrl(cls);
			if (url == null) return Collections.emptySet();
			if (FILE.equals(url.getProtocol())) return fileNames(url, subDir);
			if (JAR.equals(url.getProtocol())) return jarNames(url, subDir);
			throw new IllegalArgumentException("Unsupported URL type: " + url);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	private Collection<String> jarNames(URL url, String subDir) throws IOException {
		Collection<String> names = new TreeSet<>();
		String prefix = jarPrefix(url.toString(), subDir);
		int prefixLen = prefix.length();
		JarURLConnection connection = (JarURLConnection) url.openConnection();
		try (JarFile jar = connection.getJarFile()) {
			for (JarEntry entry : CollectionUtil.iterable(jar.entries())) {
				String name = entry.getName();
				if (name == null || !name.startsWith(prefix)) continue;
				name = name.substring(prefixLen);
				if (name.isEmpty() || name.contains("/")) continue;
				addName(names, name);
			}
		}
		return names;
	}

	private String jarPrefix(String urlStr, String subDir) {
		int start = urlStr.indexOf(JAR_SEPARATOR);
		if (start < 0) start = 0;
		else start += JAR_SEPARATOR.length();
		int end = urlStr.lastIndexOf("/");
		if (end < start) end = start;
		else end++;
		return urlStr.substring(start, end) + subDir;
	}

	private Collection<String> fileNames(URL url, String subDir) throws URISyntaxException {
		File dir = new File(new File(url.toURI()).getParentFile(), subDir);
		Collection<String> names = new TreeSet<>();
		if (dir.exists()) for (File file : dir.listFiles()) {
			if (file.isDirectory()) continue;
			addName(names, file.getName());
		}
		return names;
	}

	private void addName(Collection<String> names, String name) {
		if (pattern != null && !pattern.matcher(name).matches()) return;
		names.add(name);
	}
	
}
