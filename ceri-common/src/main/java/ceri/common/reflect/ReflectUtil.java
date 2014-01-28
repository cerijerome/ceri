/*
 * Created on Jun 12, 2004
 */
package ceri.common.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Utility methods related to reflection
 */
public class ReflectUtil {

	private ReflectUtil() {}

	/**
	 * Checks if the object is an instance of any of the given classes.
	 */
	@SafeVarargs
	public static <T> boolean instanceOfAny(T obj, Class<? extends T>...classes) {
		return instanceOfAny(obj, Arrays.asList(classes));
	}
	
	/**
	 * Checks if the object is an instance of any of the given classes.
	 */
	public static <T> boolean instanceOfAny(T obj, Iterable<Class<? extends T>> classes) {
		if (obj == null) return false;
		for (Class<?> cls : classes) if (cls.isInstance(obj)) return true;
		return false;
	}
	
	/**
	 * Gets the info on the caller of this method by looking at the stack trace.
	 */
	public static Caller currentCaller() {
		return previousCaller(1);
	}

	/**
	 * Gets the info of a previous caller of this method by looking at the stack
	 * trace.
	 */
	public static Caller previousCaller(int countBack) {
		StackTraceElement s = previousStackTraceElement("previousCaller", countBack);
		return Caller.fromStackTraceElement(s);
	}

	/**
	 * Gets the current method name by looking at the previous method on the
	 * stack trace.
	 */
	public static String currentMethodName() {
		return previousMethodName(1);
	}

	/**
	 * Gets the current method name by looking at the previous method on the
	 * stack trace.
	 */
	public static String previousMethodName(int countBack) {
		return previousCaller(countBack + 1).method;
	}

	/**
	 * Creates an object of given type, using default constructor
	 */
	public static <T> T createObject(Class<T> classType) throws CreateException {
		return createObject(classType, new Class[] {}, new Object[] {});
	}

	/**
	 * Creates an object of given type, using constructor that matches given
	 * argument types.
	 * 
	 * @param classType
	 *            new object type
	 * @param argTypes
	 *            constructor argument types
	 * @param args
	 *            constructor arguments
	 * @return new instance of given object type
	 */
	public static <T> T createObject(Class<T> classType, Class<?>[] argTypes, Object[] args)
		throws CreateException {
		try {
			Constructor<T> constructor = classType.getConstructor(argTypes);
			T obj = constructor.newInstance(args);
			if (obj == null) throw new CreateException("Created null object");
			return obj;
		} catch (NoSuchMethodException e) {
			throw new CreateException("Data type '" + classType +
				"' does not have a constructor matching arguments " + Arrays.toString(argTypes), e);
		} catch (InstantiationException e) {
			throw new CreateException("Unable to instantiate data type '" + classType + "'", e);
		} catch (IllegalAccessException e) {
			throw new CreateException("Unable to access constructor for data type '" + classType +
				"'", e);
		} catch (InvocationTargetException e) {
			throw new CreateException("Unexpected data type", e);
		}
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

}
