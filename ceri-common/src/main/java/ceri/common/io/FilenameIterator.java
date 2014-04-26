package ceri.common.io;

import java.io.File;
import java.io.FileFilter;
import java.util.Iterator;

/**
 * Iterates over files under the given root directory. Elements are file paths relative to root.
 * Order is dependent on the underlying file system, much like File.listFiles()
 */
public class FilenameIterator implements Iterator<String> {
	public final File rootDir;
	private final FileIterator iterator;
	private final int pathOffset;

	public FilenameIterator(File rootDir) {
		this(rootDir, null);
	}

	public FilenameIterator(File rootDir, FileFilter filter) {
		this.rootDir = rootDir;
		iterator = new FileIterator(rootDir, filter);
		pathOffset = rootDir.getAbsolutePath().length() + 1;
	}

	@Override
	public boolean hasNext() {
		return iterator.hasNext();
	}

	@Override
	public String next() {
		return iterator.next().getAbsolutePath().substring(pathOffset);
	}

	@Override
	public void remove() {
		iterator.remove();
	}

}
