package ceri.aws.glacier;

import java.io.File;
import java.util.Date;
import java.util.List;
import ceri.common.collection.CollectionUtil;

public class UploadConfig {
	public final Date modifiedSince;
	public final File root;
	public final List<String> dirs;

	public UploadConfig(Date modifiedSince, File root, String...dirs) {
		this.modifiedSince = modifiedSince;
		this.root = root;
		this.dirs = CollectionUtil.immutableList(dirs);
	}
	
	public UploadConfig(Date modifiedSince, File root, List<String> dirs) {
		this.modifiedSince = modifiedSince;
		this.root = root;
		this.dirs = CollectionUtil.immutableList(dirs);
	}

}
