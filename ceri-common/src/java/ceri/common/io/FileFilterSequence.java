package ceri.common.io;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class FileFilterSequence implements FileFilter {
	private final List<FileFilter> filters;

	public static class Builder {
		private final List<FileFilter> filters = new ArrayList<>();

		public final Builder add(FileFilter... filters) {
			Collections.addAll(this.filters, filters);
			return this;
		}

		public Builder add(Collection<FileFilter> filters) {
			this.filters.addAll(filters);
			return this;
		}

		public FileFilterSequence build() {
			return new FileFilterSequence(filters);
		}
	}

	FileFilterSequence(List<FileFilter> filters) {
		this.filters = Collections.unmodifiableList(new ArrayList<>(filters));
	}

	public static FileFilterSequence create(FileFilter...filters) {
		return new Builder().add(filters).build();
	}
	
	public List<FileFilter> filters() {
		return filters;
	}

	@Override
	public boolean accept(File pathname) {
		for (FileFilter filter : filters) {
			if (!filter.accept(pathname)) return false;
		}
		return true;
	}
	
}
