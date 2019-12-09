package ceri.ci.common;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.IoUtil;
import ceri.common.io.ResourcePath;

/**
 * Resource lookup service from a directory relative to the given class. Handles file resources and
 * resources within a jar file. The directory is scanned and resources are mapped, based on the name
 * of the resource file without its extension. A list of allowed extensions is passed in.
 */
public class ResourceMap {
	private static final Logger logger = LogManager.getLogger();
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
		map = Collections.unmodifiableMap(map(cls, subDir, fileExtensions));
		this.cls = cls;
		this.subDir = subDir;
	}

	public Resource resource(String key) throws IOException {
		String name = verify(key);
		if (name == null) return null;
		byte[] data = IoUtil.resource(cls, subDir, name);
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
		try (ResourcePath rp = ResourcePath.of(cls, subDir)) {
			if (rp == null) return Collections.emptyMap();
			return map(rp.path(), fileExtensions);
		}
	}

	private Map<String, String> map(Path dir, Collection<String> fileExtensions)
		throws IOException {
		Map<String, String> map = new TreeMap<>();
		if (Files.exists(dir)) for (Path file : IoUtil.list(dir)) {
			if (Files.isDirectory(file)) continue;
			addMatchingEntry(map, IoUtil.fileName(file), fileExtensions);
		}
		else logger
			.warn("Directory does not exist, using empty resource map: " + dir.toAbsolutePath());
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
