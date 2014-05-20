package ceri.ci.common;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.CollectionUtil;
import ceri.common.io.IoUtil;

/**
 * Resource lookup service from a directory relative to the given class. Handles file resources and
 * resources within a jar file. The directory is scanned and resources are mapped, based on the name
 * of the resource file without its extension. A list of allowed extensions is passed in.
 */
public class ResourceMap {
	private static final Logger logger = LogManager.getLogger();
	private static final String FILE = "file";
	private static final String JAR = "jar";
	private static final String JAR_SEPARATOR = "!/";
	private static final Pattern NAME_REGEX = Pattern.compile("([^/]+)\\.([^/]+)$");
	private final Map<String, String> map;
	private final Class<?> cls;
	private final String subDir;

	public ResourceMap(Class<?> cls, String subDir, String... fileExtensions) throws IOException {
		this(cls, subDir, Arrays.asList(fileExtensions));
	}

	public ResourceMap(Class<?> cls, String subDir, Collection<String> fileExtensions)
		throws IOException {
		if (subDir == null || subDir.length() == 0) subDir = "";
		else if (!subDir.endsWith("/")) subDir += "/";
		map = Collections.unmodifiableMap(map(cls, subDir, fileExtensions));
		this.cls = cls;
		this.subDir = subDir;
	}

	public Resource resource(String key) throws IOException {
		String name = verify(key);
		if (name == null) return null;
		byte[] data = IoUtil.getResource(cls, subDir + name);
		return new Resource(key, data);
	}

	public Collection<String> keys() {
		return map.keySet();
	}

	public Collection<Resource> resources() throws IOException {
		return resources(keys());
	}

	public Collection<Resource> resources(Collection<String> keys) throws IOException {
		Collection<Resource> resources = new ArrayList<>();
		for (String key : keys) {
			Resource resource = resource(key);
			if (resource != null) resources.add(resource);
		}
		return resources;
	}

	public String verify(String key) {
		String name = map.get(key);
		if (name == null) {
			URL url = cls.getResource(subDir);
			if (url == null) logger.warn("No resource for key '{}'", key);
			else logger.warn("No resource for key '{}' in directory {}", key, url);
		}
		return name;
	}

	public Collection<String> verifyAll(Collection<String> keys) {
		Collection<String> verifiedKeys = new ArrayList<>();
		for (String key : keys) {
			String name = verify(key);
			if (name != null) verifiedKeys.add(key);
		}
		return verifiedKeys;
	}

	private Map<String, String> map(Class<?> cls, String subDir, Collection<String> fileExtensions)
		throws IOException {
		try {
			URL url = IoUtil.getClassUrl(cls);
			if (url == null) return Collections.emptyMap();
			if (FILE.equals(url.getProtocol())) return fileMap(url, subDir, fileExtensions);
			if (JAR.equals(url.getProtocol())) return jarMap(url, subDir, fileExtensions);
			throw new IllegalArgumentException("Unsupported URL type: " + url);
		} catch (URISyntaxException e) {
			throw new IOException(e);
		}
	}

	private Map<String, String> jarMap(URL url, String subDir, Collection<String> fileExtensions)
		throws IOException {
		Map<String, String> map = new TreeMap<>();
		String prefix = jarPrefix(url.toString(), subDir);
		int prefixLen = prefix.length();
		JarURLConnection connection = (JarURLConnection) url.openConnection();
		try (JarFile jar = connection.getJarFile()) {
			for (JarEntry entry : CollectionUtil.iterable(jar.entries())) {
				String name = entry.getName();
				if (name == null || !name.startsWith(prefix)) continue;
				name = name.substring(prefixLen);
				if (name.isEmpty() || name.contains("/")) continue;
				addMatchingEntry(map, name, fileExtensions);
			}
		}
		return map;
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

	private Map<String, String> fileMap(URL url, String subDir, Collection<String> fileExtensions)
		throws URISyntaxException {
		File dir = new File(new File(url.toURI()).getParentFile(), subDir);
		Map<String, String> map = new TreeMap<>();
		if (dir.exists()) for (File file : dir.listFiles()) {
			if (file.isDirectory()) continue;
			addMatchingEntry(map, file.getName(), fileExtensions);
		}
		else logger.warn("Directory does not exist, using empty resource map: " +
			dir.getAbsolutePath());
		return map;
	}

	private boolean addMatchingEntry(Map<String, String> map, String name,
		Collection<String> fileExtensions) {
		Matcher m = NAME_REGEX.matcher(name);
		if (!m.find()) return false;
		String key = m.group(1);
		String ext = m.group(2).toLowerCase();
		if (!fileExtensions.isEmpty() && !fileExtensions.contains(ext)) return false;
		if (map.containsKey(key)) logger.warn("Duplicate key {}, ignoring file {}", key, name);
		else map.put(key, m.group());
		return true;
	}

}
