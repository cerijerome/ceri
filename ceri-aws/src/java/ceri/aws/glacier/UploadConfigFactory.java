package ceri.aws.glacier;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import ceri.common.date.ImmutableDate;
import ceri.common.property.Key;

public enum UploadConfigFactory {
	instance;

	private static final String UPLOAD = "upload";
	private static final String NAMES = "names";
	private static final String MODIFIED_SINCE = "modifiedSince";
	private static final String ROOT = "root";
	private static final String DIRS = "dirs";
	private static final Pattern SPLIT_REGEX = Pattern.compile(",?+\\s+");
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public Map<String, UploadConfig> createFromProperties(Properties properties)
		throws ParseException {
		Map<String, UploadConfig> configMap = new HashMap<>();
		Key uploadKey = Key.create(UPLOAD);
		String namesStr = property(properties, uploadKey.child(NAMES));
		String[] names = SPLIT_REGEX.split(namesStr);
		for (String name : names) {
			UploadConfig config = createFromProperties(uploadKey.child(name), properties);
			configMap.put(name, config);
		}
		return Collections.unmodifiableMap(configMap);
	}

	private UploadConfig createFromProperties(Key key, Properties properties) throws ParseException {
		String modifiedSinceStr = property(properties, key.child(MODIFIED_SINCE));
		String rootStr = property(properties, key.child(ROOT));
		String dirsStr = property(properties, key.child(DIRS));

		Date modifiedSince = parse(modifiedSinceStr);
		File root = new File(rootStr);
		String[] dirs = SPLIT_REGEX.split(dirsStr);
		return new UploadConfig(modifiedSince, root, dirs);
	}

	private String property(Properties properties, Key key) {
		return properties.getProperty(key.value);
	}

	private synchronized Date parse(String dateStr) throws ParseException {
		if (dateStr == null || dateStr.isEmpty()) return null;
		return new ImmutableDate(dateFormat.parse(dateStr));
	}

	public static void main(String[] args) {
		System.out.println(new Date());
	}

}
