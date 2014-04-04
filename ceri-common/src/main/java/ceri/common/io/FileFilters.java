package ceri.common.io;

import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collection;

public class FileFilters {

	private FileFilters() {}

	/**
	 * A filter that rejects all.
	 */
	public static final FileFilter NULL = file -> {
		return false;
	};

	/**
	 * A filter that accepts all.
	 */
	public static final FileFilter ALL = reverse(NULL);

	/**
	 * A filter that only accepts directories.
	 */
	public static final FileFilter DIR = file -> {
		return file.isDirectory();
	};

	/**
	 * A filter that only accepts files.
	 */
	public static final FileFilter FILE = file -> {
		return file.isFile();
	};

	/**
	 * Creates a filter that reverses the given filter.
	 */
	public static FileFilter reverse(final FileFilter filter) {
		return pathname -> {
			return !filter.accept(pathname);
		};
	}

	/**
	 * Creates a filter that only accepts files modified since the given time in ms.
	 */
	public static FileFilter byModifiedSince(final long ms) {
		return pathname -> {
			return pathname.lastModified() > ms;
		};
	}

	/**
	 * Creates a filter that only accepts files up to the given length in bytes.
	 */
	public static FileFilter byMaxLength(final long maxSize) {
		return file -> {
			return file.length() <= maxSize;
		};
	}

	/**
	 * Create a filter that accepts a file if any filters accept.
	 */
	public static FileFilter or(FileFilter... filters) {
		return or(Arrays.asList(filters));
	}

	/**
	 * Create a filter that accepts a file if any filters accept.
	 */
	public static FileFilter or(final Collection<FileFilter> filters) {
		return pathname -> {
			for (FileFilter filter : filters) {
				if (filter.accept(pathname)) return true;
			}
			return false;
		};
	}

	/**
	 * Create a filter that accepts a file only if all filters accept.
	 */
	public static FileFilter and(FileFilter... filters) {
		return and(Arrays.asList(filters));
	}

	/**
	 * Create a filter that accepts a file only if all filters accept.
	 */
	public static FileFilter and(final Collection<FileFilter> filters) {
		return pathname -> {
			for (FileFilter filter : filters)
				if (!filter.accept(pathname)) return false;
			return true;
		};
	}

}
