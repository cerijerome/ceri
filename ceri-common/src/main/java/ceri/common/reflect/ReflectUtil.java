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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;
import ceri.common.exception.ExceptionUtil;
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
	 * Returns the class name without package. Undefined for class names containing '$'.
	 */
	public static String name(Class<?> cls) {
		if (cls == null) return NULL_STRING;
		String s = cls.getTypeName();
		return s.substring(s.lastIndexOf(".") + 1).replace('$', '.');
	}

	/**
	 * Returns the class name without package and outer class, if nested. Otherwise returns the
	 * class name without package (same as name()). Undefined for class names containing '$'.
	 */
	public static String nestedName(Class<?> cls) {
		if (cls == null) return NULL_STRING;
		String s = cls.getTypeName();
		int i = s.indexOf('$');
		if (i > 0) return s.substring(i + 1).replace('$', '.');
		return s.substring(s.lastIndexOf(".") + 1).replace('$', '.');
	}

	/**
	 * Returns toString() value, or hash code as '@&lt;hash&gt;' if toString has not been
	 * overridden. Assumes non-overridden toString is of the form '...@&lt;hash&gt;'. Useful for
	 * displaying shorter string identifiers of lambdas.
	 */
	public static String toStringOrHash(Object obj) {
		if (obj == null) return null;
		String s = obj.toString();
		int i = s.lastIndexOf('@');
		if (i == -1 || i == s.length() - 1) return s;
		return s.substring(i);
	}

	/**
	 * Returns "{@code @<hex-hashcode>}" for logging/identification purposes.
	 */
	public static String hashId(Object obj) {
		if (obj == null) return null;
		return String.format("@%x", System.identityHashCode(obj));
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
	 * Gets the caller's stack trace element.
	 */
	public static StackTraceElement currentStackTraceElement() {
		return previousStackTraceElement(1);
	}

	/**
	 * Gets a previous stack trace element.
	 */
	public static StackTraceElement previousStackTraceElement(int countBack) {
		return previousStackTraceElement("previousStackTraceElement", countBack + 1);
	}

	/**
	 * Gets the info on the caller of this method by looking at the stack trace.
	 */
	public static Caller currentCaller() {
		return previousCaller(1);
	}

	/**
	 * Gets the info of a previous caller of this method by looking at the stack trace.
	 */
	public static Caller previousCaller(int countBack) {
		StackTraceElement s = previousStackTraceElement(countBack + 1);
		return Caller.fromStackTraceElement(s);
	}

	/**
	 * Gets the current method name by looking at the previous method on the stack trace.
	 */
	public static String currentMethodName() {
		return previousMethodName(1);
	}

	/**
	 * Gets the current method name by looking at the previous method on the stack trace.
	 */
	public static String previousMethodName(int countBack) {
		return previousStackTraceElement(countBack + 1).getMethodName();
	}

	/**
	 * Gets the current class and line number of the caller.
	 */
	public static String currentClassLine() {
		return previousClassLine(1);
	}

	public static String previousClassLine(int countBack) {
		StackTraceElement element = ReflectUtil.previousStackTraceElement(countBack + 1);
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
	 * Loads a class from its class file.
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
		throws CreateException {
		try {
			return cls.getConstructor(argTypes);
		} catch (NoSuchMethodException e) {
			StringBuilder b = new StringBuilder();
			b.append("constructor ").append(cls.getSimpleName()).append('(');
			StringUtil.append(b, ", ", Class::getSimpleName, argTypes).append(") not found");
			throw new CreateException(b.toString(), e);
		}
	}

	/**
	 * Creates an object of given type, using default constructor
	 */
	public static <T> T create(Class<T> classType) throws CreateException {
		return create(classType, EMPTY_CLASS);
	}

	/**
	 * Creates an object of given type, using constructor that matches given argument types.
	 */
	public static <T> T create(Class<T> cls, Class<?>[] argTypes, Object... args) {
		return create(constructor(cls, argTypes), args);
	}

	public static <T> T create(Constructor<T> constructor, Object... args) {
		Throwable t = null;
		try {
			return constructor.newInstance(args);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException e) {
			t = e;
		} catch (InvocationTargetException e) {
			t = BasicUtil.defaultValue(e.getCause(), e);
		}
		StringBuilder b = new StringBuilder();
		b.append("new ").append(constructor.getDeclaringClass().getSimpleName()).append('(');
		StringUtil.append(b, ", ", Class::getSimpleName, constructor.getParameterTypes());
		b.append(") failed with args (");
		StringUtil.append(b, ", ", args).append(')');
		throw new CreateException(b.toString(), t);
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
	public static Object publicFieldValue(Object obj, Field field) {
		if (obj == null || field == null) return null;
		try {
			return field.get(obj);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			return null;
		}
	}

	/**
	 * Returns the public field value from the instance. Returns null if not found.
	 */
	public static Object publicValue(Object obj, String name) {
		if (obj == null) return null;
		return publicFieldValue(obj, publicField(obj.getClass(), name));
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
		return PACKAGE_REGEX.matcher(stackTrace).replaceAll(m -> "$1.");
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
			Proxy.newProxyInstance(cls.getClassLoader(), ifaces, (obj, method, args) -> {
				consumer.accept(method, args);
				return method.invoke(delegate, args);
			}));
	}
}
