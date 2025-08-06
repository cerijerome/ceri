package ceri.common.reflect;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import ceri.common.collection.CollectionUtil;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.util.BasicUtil;

/**
 * ClassLoader that reloads from class files.
 */
public class ClassReloader extends ClassLoader {
	private final Map<String, ProtectionDomain> classes;

	/**
	 * Reloads and initializes the given class; support classes are reloaded if accessed.
	 */
	public static <T> Class<T> reload(Class<T> cls, Class<?>... supportClasses) {
		return reload(cls, Arrays.asList(supportClasses));
	}

	/**
	 * Reloads and initializes the given class; support classes are reloaded if accessed.
	 */
	public static <T> Class<T> reload(Class<T> cls, Collection<Class<?>> supportClasses) {
		var reloader = of(CollectionUtil.joinAsList(cls, supportClasses));
		return BasicUtil.unchecked(Reflect.forName(cls.getName(), true, reloader));
	}

	/**
	 * Constructor for reloading specified classes.
	 */
	public static ClassReloader of(Class<?>... classes) {
		return of(Arrays.asList(classes));
	}

	/**
	 * Constructor for reloading specified classes.
	 */
	public static ClassReloader of(Collection<Class<?>> classes) {
		return new ClassReloader(classes);
	}

	/**
	 * Constructor for reloading specified classes and their declared nested classes.
	 */
	public static ClassReloader ofNested(Class<?>... classes) {
		return ofNested(Arrays.asList(classes));
	}

	/**
	 * Constructor for reloading specified classes and their declared nested classes.
	 */
	public static ClassReloader ofNested(Iterable<Class<?>> classes) {
		return new ClassReloader(Reflect.nested(classes));
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
		return ExceptionAdapter.runtimeIo.get(() -> {
			var bytes = Reflect.loadClassFile(name);
			var pd = classes.get(name);
			return defineClass(name, bytes, 0, bytes.length, pd);
		});
	}

	/**
	 * Loads the class from its given name. For a class specified in the constructor, it is loaded
	 * once only from its class file resource. For other classes, standard delegating ClassLoader
	 * logic is applied.
	 */
	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (!classes.containsKey(name)) return super.loadClass(name);
		var cls = findLoadedClass(name);
		return cls != null ? cls : findClass(name);
	}

	/**
	 * Loads the class with optional initialization. For a class specified in the constructor, it is
	 * loaded once only from its class file resource. For other classes, standard delegating
	 * ClassLoader logic is applied.
	 */
	public Class<?> forName(String name, boolean init) {
		return Reflect.forName(name, init, this);
	}

	/**
	 * Loads the class with optional initialization. For a class specified in the constructor, it is
	 * loaded once only from its class file resource. For other classes, standard delegating
	 * ClassLoader logic is applied.
	 */
	public <T> Class<T> forName(Class<T> cls, boolean init) {
		return BasicUtil.unchecked(forName(cls.getName(), init));
	}
}