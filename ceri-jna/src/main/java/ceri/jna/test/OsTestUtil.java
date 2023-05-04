package ceri.jna.test;

import java.lang.reflect.Modifier;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.RuntimeCloseable;
import ceri.common.reflect.ClassReloader;
import ceri.common.util.SystemVars;

public class OsTestUtil {

	private OsTestUtil() {}

	/**
	 * try-with-resources to temporarily override OS name; use ClassReloader if classes under test
	 * have already been initialized. .
	 */
	public static RuntimeCloseable asMac() {
		return SystemVars.removable("os.name", "Mac");
	}

	/**
	 * try-with-resources to temporarily override OS name; use ClassReloader if classes under test
	 * have already been initialized.
	 */
	public static RuntimeCloseable asLinux() {
		return SystemVars.removable("os.name", "Linux");
	}

	/**
	 * Access enums and public static fields under each supported OS. Classes passed in are
	 * reloaded, and the first class is tested.
	 */
	public static void testFieldsForEachOs(Class<?>... classes) {
		testForEachOs(cr -> {
			var cls = cr.load(classes[0]);
			checkEnums(cls);
			checkStaticFields(cls);
		}, classes);
	}

	/**
	 * Run test under each supported OS. Classes passed in are reloaded for each OS.
	 */
	public static <E extends Exception> void
		testForEachOs(ExceptionConsumer<E, ClassReloader> tester, Class<?>... classes) throws E {
		try (var x = asMac()) {
			tester.accept(ClassReloader.of(classes));
		}
		try (var x = asLinux()) {
			tester.accept(ClassReloader.of(classes));
		}
	}

	/**
	 * Accesses enum constants if defined.
	 */
	public static void checkEnums(Class<?> cls) {
		cls.getEnumConstants();
	}

	/**
	 * Accesses each public static field.
	 */
	public static void checkStaticFields(Class<?> cls) {
		ExceptionUtil.shouldNotThrow(() -> {
			for (var f : cls.getDeclaredFields()) {
				int modifiers = f.getModifiers();
				if (!Modifier.isStatic(modifiers)) continue;
				if (!Modifier.isPublic(modifiers)) continue;
				f.get(null);
			}
		});
	}

}
