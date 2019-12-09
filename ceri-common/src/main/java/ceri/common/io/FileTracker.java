package ceri.common.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * Used for creating dirs and files, and keeping track of which ones were created. Useful if an
 * atomic file-based operation fails and need to undo.
 */
public class FileTracker {
	private final Deque<Path> createdFiles = new ArrayDeque<>();

	public FileTracker() {}

	/**
	 * Adds a file to track, and creates parent dirs.
	 */
	public FileTracker file(Path file) throws IOException {
		if (Files.exists(file)) return this;
		dir(file.getParent());
		add(file);
		return this;
	}

	/**
	 * Creates and tracks dir path.
	 */
	public FileTracker dir(Path dir) throws IOException {
		if (dir == null || Files.exists(dir)) return this;
		dir(dir.getParent());
		Files.createDirectory(dir);
		add(dir);
		return this;
	}

	/**
	 * Delete all tracked files and dirs.
	 * Returns true only if all tracked paths are deleted.
	 */
	public boolean delete() {
		return delete(null);
	}
	/**
	 * Delete all tracked files and dirs. Caller can receive errors via onError callback.
	 * Returns true only if all tracked paths are deleted.
	 */
	public boolean delete(BiConsumer<IOException, Path> onError) {
		boolean success = true;
		for (Iterator<Path> i = createdFiles.iterator(); i.hasNext();) {
			Path file = i.next();
			try {
				Files.delete(file);
			} catch (IOException e) {
				success = false;
				if (onError != null) onError.accept(e, file);
			}
			i.remove();
		}
		return success;
	}

	private void add(Path file) {
		createdFiles.addFirst(file);
	}
}
