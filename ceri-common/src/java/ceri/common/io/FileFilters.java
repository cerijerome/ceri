package ceri.common.io;

import java.io.File;
import java.io.FileFilter;

public class FileFilters {
	
	public static final FileFilter NULL = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return false;
		}
	};
	
	public static final FileFilter ALL = reverse(NULL);
	
	public static final FileFilter DIR = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isDirectory();
		}
	};
	
	public static final FileFilter FILE = new FileFilter() {
		@Override
		public boolean accept(File file) {
			return file.isFile();
		}
	};

	public static FileFilter reverse(final FileFilter filter) {
		return new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return !filter.accept(pathname);
			}
		};
	}
	
	private FileFilters() {}
	
}
