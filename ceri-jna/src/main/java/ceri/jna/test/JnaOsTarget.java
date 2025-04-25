package ceri.jna.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import ceri.common.function.ExceptionRunnable;
import ceri.common.function.ExceptionSupplier;
import ceri.common.function.RuntimeCloseable;
import ceri.common.reflect.ClassReloader;
import ceri.common.util.CloseableUtil;
import ceri.common.util.OsUtil;

/**
 * Provides an overridden OS context, with options to reload classes.
 */
public class JnaOsTarget {
	private static final String MAC_OS = "Mac";
	private static final String LINUX_OS = "Linux";
	private final String osName;
	private final List<Class<?>> supportClasses = new ArrayList<>();
	private final List<Class<?>> reloadClasses = new ArrayList<>();
	
	/**
	 * Runs for each supported OS, overriding the current OS.
	 */
	public static <E extends Exception> void runEachOs(ExceptionRunnable<E> runnable) throws E {
		mac().run(runnable);
		linux().run(runnable);
	}

	/**
	 * Reloads and instantiates the test class for each supported OS, overriding the current OS.
	 * Support classes are reloaded if accessed.
	 */
	public static void reloadEachOs(Class<?> reloadCls, Class<?>... supportClasses) {
		mac().reload(reloadCls).support(supportClasses).run();
		linux().reload(reloadCls).support(supportClasses).run();
	}
	
	/**
	 * Instantiate a Mac target.
	 */
	public static JnaOsTarget mac() {
		return new JnaOsTarget(MAC_OS);
	}
	
	/**
	 * Instantiate a Linux target.
	 */
	public static JnaOsTarget linux() {
		return new JnaOsTarget(LINUX_OS);
	}
	
	private JnaOsTarget(String osName) {
		this.osName = osName; 
	}
	
	/**
	 * Add support classes that will be reloaded if referenced.
	 */
	public final JnaOsTarget support(Class<?>... classes) {
		return support(Arrays.asList(classes));
	}
	
	/**
	 * Add support classes that will be reloaded if referenced.
	 */
	public JnaOsTarget support(Iterable<Class<?>> classes) {
		for (var cls : classes)
			supportClasses.add(cls);
		return this;
	}
	
	/**
	 * Add classes that will be reloaded and initialized.
	 */
	public final JnaOsTarget reload(Class<?>... classes) {
		return reload(Arrays.asList(classes));
	}
	
	/**
	 * Add classes that will be reloaded and initialized.
	 */
	public JnaOsTarget reload(Iterable<Class<?>> classes) {
		for (var cls : classes)
			reloadClasses.add(cls);
		return this;
	}
	
	/**
	 * Override the OS and reload classes. close to stop the override.
	 */
	public RuntimeCloseable start() {
		var override = OsUtil.os(osName, null, null);
		try {
			ClassReloader.reloadAll(reloadClasses, supportClasses);
			return override;
		} catch (Exception e) {
			CloseableUtil.close(override);
			throw e;
		}
	}
	
	/**
	 * Overrides the OS to reload classes.
	 */
	public void run() {
		start().close();
	}
	
	/**
	 * Overrides the OS to reload classes and execute the function.
	 */
	public <E extends Exception> void run(ExceptionRunnable<E> runnable) throws E {
		try (var _ = start()) {
			runnable.run();
		}
	}
	
	/**
	 * Overrides the OS to reload classes and execute the function.
	 */
	public <E extends Exception, T> T get(ExceptionSupplier<E, T> supplier) throws E {
		try (var _ = start()) {
			return supplier.get();
		}
	}	
}
