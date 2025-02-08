/*
 * Created on Jun 12, 2004
 */
package ceri.common.reflect;

import static ceri.common.collection.ArrayUtil.EMPTY_CLASS;
import static ceri.common.text.StringUtil.NULL_STRING;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.exception.ExceptionUtil;
import ceri.common.function.ExceptionBiPredicate;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionPredicate;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

/**
 * Utility methods related to reflection
 */
public class ReflectUtil {
	public static final Pattern PACKAGE_REGEX =
		Pattern.compile("(?<![\\w$])([a-z$])[a-z0-9_$]+\\.");

	private ReflectUtil() {}

	/**
	 * A thread and stack trace element.
	 */
	public record ThreadElement(Thread thread, StackTraceElement element) {
		public static final ThreadElement NULL = new ThreadElement(null, null);

		/**
		 * Detailed descriptor.
		 */
		public String full() {
			return String.format("[%s] %s", ConcurrentUtil.name(thread()), element());
		}

		@Override
		public String toString() {
			if (element() == null) return NULL_STRING;
			return ReflectUtil.name(element.getClassName()) + "." + element.getMethodName() + ":"
				+ element.getLineNumber();
		}
	}

	/**
	 * Searches all thread stack trace elements.
	 */
	public static <E extends Exception> ThreadElement
		findElement(ExceptionPredicate<E, StackTraceElement> predicate) throws E {
		return findElement((_, e) -> predicate.test(e));
	}

	/**
	 * Searches all thread stack trace elements.
	 */
	public static <E extends Exception> ThreadElement
		findElement(ExceptionBiPredicate<E, Thread, StackTraceElement> predicate) throws E {
		for (var entry : Thread.getAllStackTraces().entrySet()) {
			for (var element : entry.getValue()) {
				if (predicate.test(entry.getKey(), element))
					return new ThreadElement(entry.getKey(), element);
			}
		}
		return ThreadElement.NULL;
	}

	/**
	 * Get the typed class of an object.
	 */
	public static <T> Class<? extends T> getClass(T t) {
		return t == null ? null : BasicUtil.uncheckedCast(t.getClass());
	}

	/**
	 * Return the number of levels in the package of the given class.
	 */
	public static int packageLevels(Class<?> cls) {
		if (cls == null) return 0;
		return packageLevels(cls.getPackageName());
	}

	/**
	 * Return the number of levels in the given package name.
	 */
	public static int packageLevels(String packageName) {
		if (packageName == null || packageName.isEmpty()) return 0;
		return (int) packageName.chars().filter(ch -> ch == '.').count() + 1;
	}

	/**
	 * Returns the object's class name without package. Undefined for class names containing '$'.
	 */
	public static String className(Object obj) {
		return name(getClass(obj));
	}

	/**
	 * Returns the class name without package. Inner class names show the hierarchy. '$' is treated
	 * as a name separator.
	 */
	public static String name(Class<?> cls) {
		if (cls == null) return NULL_STRING;
		return name(cls.getTypeName());
	}

	/**
	 * Extracts the class name without package. Inner class names show the hierarchy. '$' is treated
	 * as a name separator.
	 */
	public static String name(String fullName) {
		if (fullName == null) return NULL_STRING;
		return fullName.substring(fullName.lastIndexOf(".") + 1).replace('$', '.');
	}

	/**
	 * Returns the class name without package and outer class, if nested. Otherwise returns the
	 * class name without package (same as name()).
	 */
	public static String nestedName(Class<?> cls) {
		if (cls == null) return NULL_STRING;
		String s = cls.getTypeName();
		int i = s.indexOf('$');
		if (i > 0) return s.substring(i + 1).replace('$', '.');
		return s.substring(s.lastIndexOf(".") + 1).replace('$', '.');
	}

	/**
	 * Returns "{@code @<hex-hashcode>}" for logging/identification purposes.
	 */
	public static String hashId(Object obj) {
		if (obj == null) return null;
		return String.format("@%x", System.identityHashCode(obj));
	}

	/**
	 * Returns '&lt;class-name&gt;@&lt;hash&gt;'.
	 */
	public static String nameHash(Object obj) {
		if (obj == null) return NULL_STRING;
		return className(obj) + hashId(obj);
	}

	/**
	 * Applies the consumer only to instances of the given type.
	 */
	public static <E extends Exception, T> void acceptInstances(Class<T> cls,
		ExceptionConsumer<E, ? super T> consumer, Iterable<?> objects) throws E {
		for (var obj : objects) {
			var t = castOrNull(cls, obj);
			if (t != null) consumer.accept(t);
		}
	}

	/**
	 * Checks if the object is an instance of any of the given classes.
	 */
	@SafeVarargs
	public static <T> boolean instanceOfAny(T obj, Class<? extends T>... classes) {
		return instanceOfAny(obj, Arrays.asList(classes));
	}

	/**
	 * Checks if the object is an instance of any of the given classes.
	 */
	public static <T> boolean instanceOfAny(T obj, Iterable<Class<? extends T>> classes) {
		if (obj == null) return false;
		for (Class<?> cls : classes)
			if (cls.isInstance(obj)) return true;
		return false;
	}

	/**
	 * Checks if the class is the same as, or a sub-type of any of the given classes.
	 */
	@SafeVarargs
	public static boolean assignableFromAny(Class<?> cls, Class<?>... classes) {
		return assignableFromAny(cls, Arrays.asList(classes));
	}

	/**
	 * Checks if the class is the same as, or a sub-type of any of the given classes.
	 */
	public static boolean assignableFromAny(Class<?> cls, Iterable<Class<?>> classes) {
		if (cls == null) return false;
		for (Class<?> c : classes)
			if (c.isAssignableFrom(cls)) return true;
		return false;
	}

	/**
	 * Gets the caller's stack trace element.
	 */
	public static StackTraceElement currentElement() {
		return previousElement(1);
	}

	/**
	 * Gets a previous stack trace element.
	 */
	public static StackTraceElement previousElement(int countBack) {
		return previousStackTraceElement("previousStackTraceElement", countBack + 1);
	}

	/**
	 * Gets the info of the caller.
	 */
	public static Caller currentCaller() {
		return previousCaller(1);
	}

	/**
	 * Gets the info of a previous caller.
	 */
	public static Caller previousCaller(int countBack) {
		StackTraceElement s = previousElement(countBack + 1);
		return Caller.fromStackTraceElement(s);
	}

	/**
	 * Gets the method name of the caller.
	 */
	public static String currentMethodName() {
		return previousMethodName(1);
	}

	/**
	 * Gets the method name of a previous caller.
	 */
	public static String previousMethodName(int countBack) {
		return previousElement(countBack + 1).getMethodName();
	}

	/**
	 * Gets the class and line number of the caller.
	 */
	public static String currentClassLine() {
		return previousClassLine(1);
	}

	/**
	 * Gets the class and line number of a previous caller.
	 */
	public static String previousClassLine(int countBack) {
		StackTraceElement element = ReflectUtil.previousElement(countBack + 1);
		return element.getClassName() + ":" + element.getLineNumber();
	}

	/**
	 * Loads a class by name, throws {@link IllegalArgumentException} if not found.
	 */
	public static Class<?> forName(String className) {
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Class not found", e);
		}
	}

	/**
	 * Loads class bytes from its class file.
	 */
	public static byte[] loadClassFile(String name) throws IOException {
		String fileName = name.replace('.', File.separatorChar) + ".class";
		try (var in = ReflectUtil.class.getClassLoader().getResourceAsStream(fileName)) {
			return in.readAllBytes();
		}
	}

	/**
	 * Wraps class getConstructor with unchecked exception.
	 */
	public static <T> Constructor<T> constructor(Class<T> cls, Class<?>... argTypes)
		throws RuntimeInvocationException {
		try {
			return cls.getConstructor(argTypes);
		} catch (NoSuchMethodException e) {
			StringBuilder b = new StringBuilder();
			b.append("constructor ").append(cls.getSimpleName()).append('(');
			StringUtil.append(b, ", ", Class::getSimpleName, argTypes).append(") not found");
			throw new RuntimeInvocationException(b.toString(), e);
		}
	}

	/**
	 * Creates an object of given type, using default constructor
	 */
	public static <T> T create(Class<T> classType) throws RuntimeInvocationException {
		return create(classType, EMPTY_CLASS);
	}

	/**
	 * Creates an object of given type, using constructor that matches given argument types.
	 */
	public static <T> T create(Class<T> cls, Class<?>[] argTypes, Object... args) {
		return create(constructor(cls, argTypes), args);
	}

	/**
	 * Creates an object of given type, using constructor that matches given argument types.
	 */
	public static <T> T create(Constructor<T> constructor, Object... args) {
		Throwable t = null;
		try {
			return constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			t = e;
		} catch (InvocationTargetException e) {
			t = BasicUtil.defaultValue(e.getCause(), e);
		}
		throw new RuntimeInvocationException(String.format("new %s(%s) failed with args %s",
			constructor.getDeclaringClass().getSimpleName(), types(constructor.getParameterTypes()),
			args(args)), t);
	}

	/**
	 * Invokes the method with given arguments.
	 */
	public static <T> T invoke(Method method, Object subject, Object... args) {
		Throwable t = null;
		try {
			return BasicUtil.uncheckedCast(method.invoke(subject, args));
		} catch (InvocationTargetException e) {
			t = BasicUtil.defaultValue(e.getCause(), e);
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			t = e;
		}
		throw new RuntimeInvocationException(String.format("%s.%s(%s) failed with args (%s) on %s",
			method.getDeclaringClass().getSimpleName(), method.getName(),
			types(method.getParameterTypes()), args(args), subject), t);
	}

	/**
	 * Determines if the field/method is static.
	 */
	public static boolean isStatic(Member member) {
		return member != null && Modifier.isStatic(member.getModifiers());
	}

	/**
	 * Returns the public field from the class, including super-types. Returns null if not found.
	 */
	public static Field publicField(Class<?> cls, String name) {
		if (cls == null || name == null) return null;
		try {
			return cls.getField(name);
		} catch (NoSuchFieldException e) {
			return null;
		}
	}

	/**
	 * Returns the public field value from the instance. Returns null if not found.
	 */
	public static <T> T publicFieldValue(Object obj, Field field) {
		if (field == null) return null;
		if (obj == null && !isStatic(field)) return null;
		try {
			return BasicUtil.uncheckedCast(field.get(obj));
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Returns the public field value from the instance. Returns null if not found.
	 */
	public static <T> T publicValue(Object obj, String name) {
		if (obj == null) return null;
		return publicFieldValue(obj, publicField(obj.getClass(), name));
	}

	/**
	 * Provide the static fields of given type for a class.
	 */
	public static <T> Stream<T> staticFields(Class<?> source, Class<T> type) {
		return Stream.of(source.getDeclaredFields())
			.map(f -> castOrNull(type, publicFieldValue(null, f))).filter(Objects::nonNull);
	}

	private static StackTraceElement previousStackTraceElement(String callingMethodName,
		int countBack) {
		StackTraceElement[] e = Thread.currentThread().getStackTrace();
		int count = countBack;
		for (StackTraceElement s : e) {
			if (count < 0) return s;
			String methodName = s.getMethodName();
			if (count < countBack || methodName.equals(callingMethodName)) {
				count--;
			}
		}
		return null;
	}

	/**
	 * Abbreviates package names, e.g. abc.def.MyClass -> a.d.MyClass
	 */
	public static String abbreviatePackages(String stackTrace) {
		if (stackTrace == null) return null;
		return PACKAGE_REGEX.matcher(stackTrace).replaceAll(_ -> "$1.");
	}

	/**
	 * Returns true if the current stack trace contains the given class package.
	 */
	public static boolean stackHasPackage(Class<?> cls) {
		if (cls == null) return false;
		return stackHasPackage(cls.getPackageName());
	}

	/**
	 * Returns true if the current stack trace contains the given package.
	 */
	public static boolean stackHasPackage(String pkg) {
		if (pkg == null) return false;
		return Stream.of(Thread.currentThread().getStackTrace())
			.map(StackTraceElement::getClassName).anyMatch(s -> s.startsWith(pkg));
	}

	/**
	 * Get enum value as a field.
	 */
	public static Field enumField(Enum<?> en) {
		if (en == null) return null;
		return ExceptionUtil.shouldNotThrow(() -> en.getClass().getField(en.name()));
	}

	/**
	 * Casts object to given type or returns null if not compatible.
	 */
	public static <T> T castOrNull(Class<T> cls, Object obj) {
		if (!cls.isInstance(obj)) return null;
		return cls.cast(obj);
	}

	/**
	 * Return the current list of JVM arguments.
	 */
	public static List<String> jvmArgs() {
		return ManagementFactory.getRuntimeMXBean().getInputArguments();
	}

	/**
	 * Returns a proxy that calls the consumer before delegating each method call. iface type must
	 * be a non-sealed, non-hidden interface.
	 */
	public static <T> T interceptor(Class<T> iface, T delegate,
		BiConsumer<Method, Object[]> consumer) {
		return methodInterceptor(delegate, consumer, iface);
	}

	/**
	 * Returns a proxy that calls the consumer before delegating each method call. T must be a
	 * non-sealed, non-hidden interface.
	 */
	public static <T> T interceptor(T delegate, BiConsumer<Method, Object[]> consumer) {
		return methodInterceptor(delegate, consumer, delegate.getClass().getInterfaces());
	}

	private static <T> T methodInterceptor(T delegate, BiConsumer<Method, Object[]> consumer,
		Class<?>... ifaces) {
		var cls = delegate.getClass();
		return BasicUtil.uncheckedCast(
			Proxy.newProxyInstance(cls.getClassLoader(), ifaces, (_, method, args) -> {
				consumer.accept(method, args);
				return method.invoke(delegate, args);
			}));
	}

	private static String args(Object[] args) {
		return StringUtil.append(new StringBuilder(), ", ", args).toString();
	}

	private static String types(Class<?>[] types) {
		return StringUtil.append(new StringBuilder(), ", ", Class::getSimpleName, types).toString();
	}
}
