package ceri.aws.glacier;

import java.io.FileFilter;
import ceri.common.io.FileFilterSequence;
import ceri.common.io.FileFilters;
import ceri.common.io.RegexFilenameFilter;

public class GlacierUtil {

	private GlacierUtil() {
	}
	
	public static FileFilter createUploadFilter(long lastModifiedMs, String...patterns) {
		return FileFilterSequence.create(FileFilters.byModifiedSince(lastModifiedMs), 
			new RegexFilenameFilter(false, patterns));
	}
}
