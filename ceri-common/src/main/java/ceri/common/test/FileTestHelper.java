package ceri.common.test;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ceri.common.io.IoUtil;

/**
 * Creates files and dirs under a temp directory, and deletes them on close. Use
 * this to test file-based actions.
 */
public class FileTestHelper implements Closeable {
	public final File root;

	/**
	 * Use current directory as the root.
	 */
	public static Builder builder() {
		return new Builder(null);
	}

	/**
	 * Use given directory as the root.
	 */
	public static Builder builder(File root) {
		return new Builder(root);
	}

	FileTestHelper(Builder builder) throws IOException {
		if (builder.root == null) root = IoUtil.createTempDir(builder.parent);
		else root = new File(builder.parent, builder.root);
		for (String dir : builder.dirs) {
			file(dir).mkdirs();
		}
		for (Map.Entry<String, String> entry : builder.files.entrySet()) {
			File file = file(entry.getKey());
			file.getParentFile().mkdirs();
			IoUtil.setContentString(file, entry.getValue());
		}
	}

	/**
	 * Creates a file object relative to the temp dir.
	 */
	public File file(String path) {
		return new File(root, path);
	}

	/**
	 * Creates an array of file objects relative to the temp dir.
	 */
	public File[] files(String... paths) {
		File[] files = new File[paths.length];
		int i = 0;
		for (String path : paths)
			files[i++] = file(path);
		return files;
	}

	/**
	 * Creates a list of file objects relative to the temp dir.
	 */
	public List<File> fileList(String... paths) {
		List<File> list = new ArrayList<>();
		for (String path : paths)
			list.add(file(path));
		return list;
	}

	@Override
	public void close() {
		IoUtil.deleteAll(root);
	}

	public static class Builder {
		final File parent;
		String root;
		final List<String> dirs = new ArrayList<>();
		final Map<String, String> files = new LinkedHashMap<>();

		Builder(File parent) {
			this.parent = parent;
		}

		/**
		 * Specify the relative name of the root dir.
		 */
		public Builder root(String root) {
			this.root = root;
			return this;
		}

		/**
		 * Add a directory.
		 */
		public Builder dir(String dir) {
			dirs.add(dir);
			return this;
		}

		/**
		 * Add a file with given content.
		 */
		public Builder file(String file, String content) {
			files.put(file, content);
			return this;
		}

		/**
		 * Build the helper.
		 */
		public FileTestHelper build() throws IOException {
			return new FileTestHelper(this);
		}
	}

}