package ceri.common.reflect;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import ceri.common.io.IoUtil;

/**
 * A simple class loader that loads specified classes from class files.
 */
public class FileClassLoader extends ClassLoader {
	private final Set<String> names;

	/**
	 * Constructor to specify which classes to load, and which to delegate.
	 */
	public FileClassLoader(Class<?>... classes) {
		names = Stream.of(classes).map(Class::getName).collect(Collectors.toUnmodifiableSet());
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		byte[] b = loadClassFromFile(name);
		return defineClass(name, b, 0, b.length);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!names.contains(name)) return super.loadClass(name);
		var cls = findLoadedClass(name);
		return cls != null ? cls : findClass(name);
	}

	private static byte[] loadClassFromFile(String name) {
		String fileName = name.replace('.', File.separatorChar) + ".class";
		return IoUtil.RUNTIME_IO_ADAPTER.get(() -> {
			try (var in = FileClassLoader.class.getClassLoader().getResourceAsStream(fileName)) {
				return in.readAllBytes();
			}
		});
	}
}
