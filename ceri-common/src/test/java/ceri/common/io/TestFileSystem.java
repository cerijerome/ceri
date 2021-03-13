package ceri.common.io;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.List;
import java.util.Set;
import ceri.common.test.CallSync;

public class TestFileSystem extends FileSystem {
	public final CallSync.Get<List<Path>> getRootDirectories = CallSync.supplier(List.of());

	public static TestFileSystem of() {
		return new TestFileSystem();
	}

	private TestFileSystem() {}

	@Override
	public FileSystemProvider provider() {
		return null;
	}

	@Override
	public void close() throws IOException {}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isReadOnly() {
		return false;
	}

	@Override
	public String getSeparator() {
		return null;
	}

	@Override
	public Iterable<Path> getRootDirectories() {
		return getRootDirectories.get();
	}

	@Override
	public Iterable<FileStore> getFileStores() {
		return null;
	}

	@Override
	public Set<String> supportedFileAttributeViews() {
		return null;
	}

	@Override
	public Path getPath(String first, String... more) {
		return null;
	}

	@Override
	public PathMatcher getPathMatcher(String syntaxAndPattern) {
		return null;
	}

	@Override
	public UserPrincipalLookupService getUserPrincipalLookupService() {
		return null;
	}

	@Override
	public WatchService newWatchService() throws IOException {
		return null;
	}
}
