package ceri.ci.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.audio.Audio;
import ceri.common.io.IoUtil;

public class FileLib {
	private static final Logger logger = LogManager.getLogger();
	private static final Pattern NAME_REGEX = Pattern.compile("(.*)\\.(.*)");
	private final Map<String, File> fileMap;
	private final File dir;

	public static void main(String[] args) throws Exception {
		FileLib af = new FileLib(new File(IoUtil.getPackageDir(FileLib.class), "clip"));
		for (File file : af.files())
			Audio.create(file).play();
	}

	public FileLib(File dir, String... fileExtensions) {
		this(dir, Arrays.asList(fileExtensions));
	}

	public FileLib(File dir, Collection<String> fileExtensions) {
		this.dir = dir;
		Map<String, File> fileMap = new HashMap<>();
		for (File file : dir.listFiles()) {
			if (file.isDirectory()) continue;
			Matcher m = NAME_REGEX.matcher(file.getName());
			if (!m.find()) continue;
			String key = m.group(1);
			String ext = m.group(2).toLowerCase();
			if (!fileExtensions.isEmpty() && !fileExtensions.contains(ext)) continue;
			if (fileMap.containsKey(key)) logger.warn("Duplicate key {}, ignoring file {}", key,
				file);
			else fileMap.put(key, file);
		}
		this.fileMap = Collections.unmodifiableMap(fileMap);
	}

	public File file(String key) {
		File file = fileMap.get(key);
		if (file != null) return file;
		logger.warn("No file for key {} in directory {}", key, dir);
		return null;
	}

	public Collection<String> keys() {
		return fileMap.keySet();
	}
	
	public Collection<File> files() {
		return files(fileMap.keySet());
	}

	public Collection<File> files(Collection<String> keys) {
		Collection<File> files = new ArrayList<>();
		for (String key : keys) {
			File file = file(key);
			if (file != null) files.add(file);
		}
		return files;
	}

	public Collection<String> verifyAll(Collection<String> keys) {
		Collection<String> verifiedKeys = new ArrayList<>();
		for (String key : keys) {
			File file = file(key);
			if (file != null) verifiedKeys.add(key);
		}
		return verifiedKeys;
	}

}
