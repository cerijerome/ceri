package ceri.common.test;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.io.IoUtil;
import ceri.common.util.ExceptionAdapter;

/**
 * Creates files and dirs under a temp directory, and deletes them on close. Use this to test
 * file-based actions.
 */
public class FileTestHelper implements Closeable {
	public final Path root;

	public static class Builder {
		final Path parent;
		Path root = null; // relative path
		final List<Path> dirs = new ArrayList<>();
		final Map<Path, byte[]> files = new LinkedHashMap<>();

		Builder(Path parent) {
			this.parent = parent;
		}

		/**
		 * Specify the relative name of the root dir.
		 */
		public Builder root(Path root) {
			this.root = verify(root);
			return this;
		}

		/**
		 * Specify the relative name of the root dir.
		 */
		public Builder root(String root) {
			return root(Path.of(root));
		}

		/**
		 * Add a relative directory.
		 */
		public Builder dir(Path dir) {
			dirs.add(verify(dir));
			return this;
		}

		/**
		 * Add a relative directory from unix format.
		 */
		public Builder dir(String dir) {
			return dir(Path.of(dir));
		}

		/**
		 * Add a relative directory from unix format.
		 */
		public Builder dirf(String format, Object... objs) {
			return dir(String.format(format, objs));
		}

		/**
		 * Add a relative file with given content.
		 */
		public Builder file(Path file, String content) {
			return file(file, content.getBytes(StandardCharsets.UTF_8));
		}

		/**
		 * Add a relative file with given content.
		 */
		public Builder file(Path file, byte[] content) {
			files.put(verify(file), content);
			return this;
		}

		/**
		 * Add a relative file with given content.
		 */
		public Builder file(String file, String content) {
			return file(Path.of(file), content);
		}

		/**
		 * Add a relative file with given content.
		 */
		public Builder file(String file, byte[] content) {
			return file(Path.of(file), content);
		}

		/**
		 * Add a relative file with given content.
		 */
		public Builder filef(String content, String format, Object... objs) {
			return file(String.format(format, objs), content);
		}

		/**
		 * Add a relative file with given content.
		 */
		public Builder filef(byte[] content, String format, Object... objs) {
			return file(String.format(format, objs), content);
		}

		/**
		 * Build the helper.
		 */
		public FileTestHelper build() throws IOException {
			return new FileTestHelper(this);
		}

		private static Path verify(Path path) {
			if (path.isAbsolute())
				throw new IllegalArgumentException("Path must be relative: " + path);
			path = path.normalize();
			if ("..".equals(IoUtil.name(path, 0)))
				throw new IllegalArgumentException("Path cannot go above parent: " + path);
			return path;
		}
	}

	/**
	 * Use current directory as the helper root.
	 */
	public static Builder builder() {
		return builder("");
	}

	/**
	 * Use given directory as the helper root.
	 */
	public static Builder builder(Path path) {
		return new Builder(path);
	}

	/**
	 * Use given directory in unix format as the helper root.
	 */
	public static Builder builder(String path) {
		return builder(Path.of(path));
	}

	FileTestHelper(Builder builder) throws IOException {
		try {
			if (builder.root == null) root = IoUtil.createTempDir(builder.parent);
			else root = builder.parent.resolve(builder.root);
			createDirs(builder.dirs);
			createFiles(builder.files);
		} catch (RuntimeException | IOException e) {
			close();
			throw e;
		}
	}

	/**
	 * Creates a path relative to the helper root dir from unix format.
	 */
	public Path path(String path) {
		return root.resolve(path);
	}

	/**
	 * Creates a path relative to the helper root dir from unix format.
	 */
	public Path pathf(String format, Object... objs) {
		return path(String.format(format, objs));
	}

	/**
	 * Creates an array of paths relative to the helper root dir from unix format.
	 */
	public Path[] paths(String... paths) {
		return Stream.of(paths).map(this::path).toArray(Path[]::new);
	}

	/**
	 * Creates a list of paths relative to the helper root dir from unix format.
	 */
	public List<Path> pathList(String... paths) {
		return Stream.of(paths).map(this::path).collect(Collectors.toList());
	}

	@Override
	public void close() {
		ExceptionAdapter.RUNTIME.run(() -> IoUtil.deleteAll(root));
	}

	private void createDirs(List<Path> dirs) throws IOException {
		for (Path dir : dirs)
			Files.createDirectories(root.resolve(dir));	
	}
	
	private void createFiles(Map<Path, byte[]> files) throws IOException {
		for (Map.Entry<Path, byte[]> entry : files.entrySet()) {
			Path file = root.resolve(entry.getKey());
			Files.createDirectories(file.getParent());
			Files.write(file, entry.getValue());
		}
	}

}
