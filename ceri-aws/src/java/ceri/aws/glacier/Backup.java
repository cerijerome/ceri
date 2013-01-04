package ceri.aws.glacier;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import ceri.aws.util.TarUtil.Compression;
import ceri.aws.util.TarringInputStream;
import ceri.common.io.FileFilters;
import ceri.common.io.FilenameIterator;
import ceri.common.io.IoUtil;
import ceri.common.io.RegexFilenameFilter;
import ceri.common.property.PropertyUtil;
import com.amazonaws.services.glacier.AmazonGlacier;

public class Backup {
	private static final int MB = 1024 * 1024;
	private final DateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");

	public void execute(String... names) throws IOException, ParseException {
		Properties properties = PropertyUtil.load(getClass(), "glacier.properties");
		Map<String, UploadConfig> configMap =
			UploadConfigFactory.instance.createFromProperties(properties);
		for (String name : names) {
			UploadConfig config = configMap.get(name);
			execute(name, config);
		}
	}

	private void execute(String name, UploadConfig config) throws IOException {
		// Rename existing glacier vault/archive to <name>-<date>?
		Date date = new Date();
		String archiveName = name + "_" + format.format(date);
		FileFilter filter = createFilterFromUploadConfig(config);
		FilenameIterator iterator = new FilenameIterator(config.root, filter);
		File outFile = new File(archiveName + ".tgz");
		String uploadId = "uploadId00";
		
		System.out.println("Backing up archive " + archiveName);
		System.out.println(config);
		System.out.println(" ==> " + outFile.getAbsolutePath());
		
		TarringInputStream in = new TarringInputStream(iterator, Compression.gzip, null, 10 * MB, 0);
		GlacierFiler glacierFiler = new GlacierFiler(outFile, uploadId, archiveName, 0);
		AmazonGlacier glacier = new GlacierListener(glacierFiler);
		MultipartUploader uploader =
			MultipartUploader.builder(glacier, in, "backup").partSize(100 * MB).build();
		uploader.execute();
	}

	private FileFilter createFilterFromUploadConfig(UploadConfig config) {
		RegexFilenameFilter.Builder builder =
			RegexFilenameFilter.builder().absolutePath(true).unixPath(true);
		for (String dir : config.dirs) {
			String path = IoUtil.getPath(new File(config.root, dir));
			builder.pattern("^" + path + "/.*");
		}
		RegexFilenameFilter regexFilter = builder.build();
		if (config.modifiedSince == null) return regexFilter;
		FileFilter modifiedSinceFilter =
			FileFilters.byModifiedSince(config.modifiedSince.getTime());
		return FileFilters.and(modifiedSinceFilter, regexFilter);
	}

	public static void main(String[] args) throws Exception {
		new Backup().execute("core", "other");
	}
}
