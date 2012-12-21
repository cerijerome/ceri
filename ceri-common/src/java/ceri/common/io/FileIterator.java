package ceri.common.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over files under the given root directory.
 */
public class FileIterator implements Iterator<File> {
	public final File rootDir;
	private final FileFilter filter;
	private final Deque<Iterator<File>> iterators = new ArrayDeque<>();
	private File next = null;
	
	public FileIterator(File rootDir) {
		this(rootDir, null);
	}
	
	public FileIterator(File rootDir, FileFilter filter) {
		this.filter = filter == null ? FileFilters.ALL : filter;
		this.rootDir = rootDir;
		File[] files = rootDir.listFiles();
		if (files == null) files = new File[0];
		iterators.add(Arrays.asList(files).iterator());
		next = findNext();
	}
	
	@Override
	public boolean hasNext() {
		return next != null;
	}

	@Override
	public File next() {
		if (next == null) throw new NoSuchElementException();
		File file = next;
		next = findNext();
		return file;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("File iteration is immutable.");
	}
	
	private File findNext() {
		Iterator<File> iterator = iterators.getLast();
		if (!iterator.hasNext()) return null;
		File next = iterator.next();
		if (next.isDirectory()) {
			File[] files = next.listFiles();
			if (files != null && files.length > 0) {
				iterator = Arrays.asList(files).iterator();
				iterators.add(iterator);
				if (filter.accept(next)) return next;
				return findNext();
			}
		}
		while (!iterator.hasNext() && iterators.size() > 1) {
			iterators.removeLast();
			iterator = iterators.getLast();
		}
		if (filter.accept(next)) return next;
		return findNext();
	}

}
