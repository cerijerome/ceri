package ceri.aws.glacier;

import java.io.FileFilter;
import ceri.common.io.FileFilters;
import ceri.common.io.RegexFilenameFilter;

public class GlacierUtil {

	private GlacierUtil() {}

	public static FileFilter createUploadFilter(long lastModifiedMs, String... dirs) {
		RegexFilenameFilter.Builder builder =
			RegexFilenameFilter.builder().absolutePath(true).unixPath(true);
		for (String dir : dirs) builder.pattern("^" + dir + "/.*");
		RegexFilenameFilter regexFilter = builder.build();
		
		return FileFilters.and(FileFilters.byModifiedSince(lastModifiedMs), regexFilter);
	}
}
