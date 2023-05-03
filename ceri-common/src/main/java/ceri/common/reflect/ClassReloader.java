package ceri.common.reflect;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import ceri.common.exception.ExceptionUtil;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

/**
 * ClassLoader that reloads from class files.
 */
public class ClassReloader extends ClassLoader {
	private final Map<String, ProtectionDomain> classes;

	/**
	 * Constructor specifying classes to reload.
	 */
	public static ClassReloader of(Class<?>... classes) {
		return of(Arrays.asList(classes));
	}

	/**
	 * Constructor specifying classes to reload.
	 */
	public static ClassReloader of(Collection<Class<?>> classes) {
		return new ClassReloader(classes);
	}

	private ClassReloader(Collection<Class<?>> classes) {
		this.classes = classes.stream()
			.collect(Collectors.toUnmodifiableMap(Class::getName, Class::getProtectionDomain));
	}

	/**
	 * Called when a constructor-specified class has not yet been loaded. Defines a new class
	 * instance from its class file resource.
	 */
	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		return IoUtil.RUNTIME_IO_ADAPTER.get(() -> {
			var bytes = ReflectUtil.loadClassFile(name);
			var pd = classes.get(name);
			return defineClass(name, bytes, 0, bytes.length, pd);
		});
	}

	/**
	 * Loads the class from its given name. For a constructor-specified class, it is loaded once
	 * only from its class file resource. For other classes, standard delegating ClassLoader logic
	 * is applied.
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!classes.containsKey(name)) return super.loadClass(name);
		var cls = findLoadedClass(name);
		return cls != null ? cls : findClass(name);
	}

	/**
	 * Loads the class. For a constructor-specified class, it is loaded once only from its class
	 * file resource. For other classes, standard delegating ClassLoader logic is applied.
	 */
	public <T> Class<T> load(Class<T> cls) {
		return BasicUtil
			.uncheckedCast(ExceptionUtil.shouldNotThrow(() -> loadClass(cls.getName())));
	}

}