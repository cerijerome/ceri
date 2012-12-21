package ceri.common.io;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Used for creating dirs and files, and keeping track of which ones were created.
 * Useful if an atomic file-based operation fails and need to undo.
 */
public class FileTracker {
	private final List<File> createdFiles = new ArrayList<>();
	
	public FileTracker() {
	}

	/**
	 * Adds a file to track, and creates parent dirs.
	 */
	public FileTracker file(File file) {
		if (file.exists()) return this;
		dir(file.getParentFile());
		add(file);
		return this;
	}
	
	/**
	 * Creates and tracks dir path.
	 */
	public FileTracker dir(File dir) {
		if (dir == null || dir.exists()) return this;
		dir(dir.getParentFile());
		dir.mkdir();
		add(dir);
		return this;
	}
	
	/**
	 * Delete all tracked files and dirs.
	 */
	public void delete() {
		for (Iterator<File> i = createdFiles.iterator(); i.hasNext(); ) {
			File file = i.next();
			file.delete();
			i.remove();
		}
	}
	
	private void add(File file) {
		createdFiles.add(0, file);
	}
}
