package ceri.common.test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.AccessFlag;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import ceri.common.array.ArrayUtil;
import ceri.common.collect.Iterables;
import ceri.common.concurrent.Concurrent;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;
import ceri.common.except.ExceptionAdapter;
import ceri.common.function.Closeables;
import ceri.common.function.Excepts;
import ceri.common.io.Resource;
import ceri.common.math.Maths;
import ceri.common.property.PropertyUtil;
import ceri.common.property.TypedProperties;
import ceri.common.reflect.Reflect;
import ceri.common.text.Chars;
import ceri.common.text.Regex;
import ceri.common.text.Strings;

/**
 * Utilities to assist tests.
 */
public class Testing {
	private static final Pattern TEST_METHOD_REGEX = Pattern.compile("^(test|should)[A-Z]");
	private static final int DELAY_MICROS = 1;
	private static final int SMALL_BUFFER_SIZE = 1024;
	private static final Random RND = new Random();
	public static final boolean isTest = Reflect.stackHasPackage(org.junit.Assert.class);
	public static final byte BMIN = Byte.MIN_VALUE;
	public static final byte BMAX = Byte.MAX_VALUE;
	public static final short SMIN = Short.MIN_VALUE;
	public static final short SMAX = Short.MAX_VALUE;
	public static final int IMIN = Integer.MIN_VALUE;
	public static final int IMAX = Integer.MAX_VALUE;
	public static final long LMIN = Long.MIN_VALUE;
	public static final long LMAX = Long.MAX_VALUE;
	public static final double DNINF = Double.NEGATIVE_INFINITY;
	public static final double DPINF = Double.POSITIVE_INFINITY;

	private Testing() {}

	/**
	 * Executes tests and prints names in readable phrases to stdout.
	 */
	public static void exec(Class<?>... classes) {
		exec(System.out, classes);
	}

	/**
	 * Executes tests and prints test names in readable phrases.
	 */
	public static void exec(PrintStream out, Class<?>... classes) {
		var core = new org.junit.runner.JUnitCore();
		var tp = new TestPrinter();
		core.addListener(tp);
		core.run(classes);
		tp.print(out);
	}

	/**
	 * Encourage gc.
	 */
	public static void gc() {
		System.gc();
		System.gc();
	}

	/**
	 * Searches all thread stack traces for a test class and method.
	 */
	public static Reflect.ThreadElement findTest() {
		return Reflect.findElement(e -> {
			var style = TestStyle.from(e.getClassName());
			var m = Regex.find(TEST_METHOD_REGEX, e.getMethodName());
			return !style.isNone() && m.hasMatch();
		});
	}

	/**
	 * Checks equals, hashCode and toString methods against first object.
	 */
	@SafeVarargs
	public static <T> void exerciseEquals(T t0, T... ts) {
		exerciseEqual(t0, t0);
		for (T t : ts)
			exerciseEqual(t0, t);
		Assert.notEqual(null, t0);
		Assert.notEqual(new Object(), t0);
	}

	/**
	 * Call this for code coverage of record hidden bytecode.
	 */
	public static void exerciseRecord(Record t) {
		exerciseRecord(t, Method::invoke);
	}

	/**
	 * Call this with Method::invoke for code coverage of record hidden bytecode when the record is
	 * not public.
	 */
	public static <T extends Record> void exerciseRecord(T t,
		Excepts.BiConsumer<ReflectiveOperationException, Method, T> invoker) {
		if (t != null) for (Field field : t.getClass().getDeclaredFields()) {
			if (!field.accessFlags().contains(AccessFlag.STATIC)) ExceptionAdapter.runtime
				.run(() -> invoker.accept(t.getClass().getMethod(field.getName()), t));
		}
	}

	/**
	 * Call this for code coverage of enum hidden bytecode.
	 */
	public static void exerciseEnum(Class<? extends Enum<?>> enumClass) {
		try {
			for (Object o : (Object[]) enumClass.getMethod("values").invoke(null))
				enumClass.getMethod("valueOf", String.class).invoke(null, o.toString());
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Verifies comparator behavior for a range of values.
	 */
	public static <T> void exerciseCompare(Comparator<? super T> comparator, T subject, T lessThan,
		T equal, T greaterThan) {
		Assert.equal(comparator.compare(null, null), 0, "Compare");
		Assert.equal(comparator.compare(subject, null), 1, "Compare");
		Assert.equal(comparator.compare(null, subject), -1, "Compare");
		Assert.equal(comparator.compare(subject, lessThan), 1, "Compare");
		Assert.equal(comparator.compare(subject, equal), 0, "Compare");
		Assert.equal(comparator.compare(subject, greaterThan), -1, "Compare");
	}

	/**
	 * Call this for code coverage of switch with strings. Bytecode checks hashes then values, but
	 * the value check is only 1 of 2 branches. The methods calls the code using strings with same
	 * hashes but different values.
	 */
	public static <E extends Exception> void exerciseSwitch(Excepts.Consumer<E, String> consumer,
		String... strings) throws E {
		for (String s : strings)
			consumer.accept("\0" + s);
	}

	/**
	 * Used to initialize a variable without the need to handle checked exceptions.
	 */
	public static <T> T init(Callable<T> callable) {
		try {
			return callable.call();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Closes the type if non-null and returns null (for assignment).
	 */
	public static <T> T close(T t) {
		switch (t) {
			case ExecutorService exec -> Closeables.close(exec);
			case Future<?> future -> Closeables.close(future);
			case Process process -> Closeables.close(process);
			case AutoCloseable c -> ExceptionAdapter.shouldNotThrow.run(c::close);
			case null -> {}
			default -> throw Assert.failure("Unable to close: " + t);
		}
		return null;
	}

	/**
	 * Capture and return any thrown exception.
	 */
	public static Throwable thrown(Excepts.Runnable<?> runnable) {
		try {
			runnable.run();
			return null;
		} catch (Throwable t) {
			return t;
		}
	}

	/**
	 * Repeat action with a microsecond delay until executor is closed. Useful to avoid intermittent
	 * thread timing issues when waiting on an event, by repeatedly triggering that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.Runnable<?> runnable) {
		return runRepeat(runnable, DELAY_MICROS);
	}

	/**
	 * Repeat action with a microsecond delay until executor is closed. Useful to avoid intermittent
	 * thread timing issues when waiting on an event, by repeatedly triggering that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.Runnable<?> runnable,
		int delayUs) {
		return runRepeat(_ -> runnable.run(), delayUs);
	}

	/**
	 * Repeat action with run count and a microsecond delay until executor is closed. Useful to
	 * avoid intermittent thread timing issues when waiting on an event, by repeatedly triggering
	 * that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.IntConsumer<?> action) {
		return runRepeat(action, DELAY_MICROS);
	}

	/**
	 * Repeat action with run count and a microsecond delay until executor is closed. Useful to
	 * avoid intermittent thread timing issues when waiting on an event, by repeatedly triggering
	 * that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(Excepts.IntConsumer<?> action,
		int delayUs) {
		return SimpleExecutor.run(() -> {
			for (int i = 0;; i++) {
				action.accept(i);
				Concurrent.delayMicros(delayUs);
			}
		});
	}

	/**
	 * Execute a closable call in a separate thread. Use get() to retrieve the result.
	 */
	public static <T> SimpleExecutor<RuntimeException, T> threadCall(Callable<T> callable) {
		return SimpleExecutor.call(callable);
	}

	/**
	 * Execute a closable call in a separate thread. Use get() to wait for completion.
	 */
	public static SimpleExecutor<RuntimeException, ?> threadRun(Excepts.Runnable<?> runnable) {
		return SimpleExecutor.run(runnable);
	}

	/**
	 * Reads a string resource from the caller's package with given name.
	 */
	public static String resource(String name) {
		var cls = Reflect.previousCaller(1).cls();
		return init(() -> Resource.string(cls, name));
	}

	/**
	 * Creates TypedProperties from name.properties file under caller's package.
	 */
	public static TypedProperties properties(String name, String... prefix) {
		var cls = Reflect.previousCaller(1).cls();
		return properties(cls, name, prefix);
	}

	/**
	 * Creates TypedProperties from name.properties under class package.
	 */
	public static TypedProperties properties(Class<?> cls, String name, String... prefix) {
		var text = init(() -> Resource.string(cls, name + ".properties"));
		return TypedProperties.from(PropertyUtil.parse(text), prefix);
	}

	/**
	 * Returns the first system property name, or empty string.
	 */
	public static String firstSysPropName() {
		return Strings.safe(Iterables.first(System.getProperties().keySet()));
	}

	/**
	 * Returns the first environment variable name, or empty string.
	 */
	public static String firstEnvVarName() {
		return Iterables.first(System.getenv().keySet(), "");
	}

	/**
	 * Reads a string from stdin.
	 */
	public static String readString() {
		try {
			return readString(System.in);
		} catch (IOException e) {
			throw new RuntimeException("Shouldn't happen", e);
		}
	}

	/**
	 * Reads a string from given input stream.
	 */
	public static String readString(InputStream in) throws IOException {
		byte[] buffer = new byte[SMALL_BUFFER_SIZE];
		int n = in.read(buffer);
		if (n < 1) return "";
		return new String(buffer, 0, n).trim();
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to '?'.
	 */
	public static String readableString(byte[] array) {
		return readableString(array, 0, array.length);
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to '?'.
	 */
	public static String readableString(byte[] array, int offset, int len) {
		return readableString(array, offset, len, "UTF8", '?');
	}

	/**
	 * Converts a byte array to string, with non-visible chars converted to given char.
	 */
	public static String readableString(byte[] array, int offset, int len, String charset,
		char unreadableChar) {
		var b = new StringBuilder();
		try {
			if (Strings.isEmpty(charset)) b.append(new String(array, offset, len));
			else b.append(new String(array, offset, len, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		for (int i = 0; i < b.length(); i++)
			if (!Chars.isPrintable(b, i)) b.setCharAt(i, unreadableChar);
		return b.toString();
	}

	/**
	 * Returns a ByteProvider.Reader<?> wrapper for bytes.
	 */
	public static ByteProvider.Reader<?> reader(int... bytes) {
		return ByteProvider.of(bytes).reader(0);
	}

	/**
	 * Returns a ByteProvider.Reader<?> wrapper for chars.
	 */
	public static ByteProvider.Reader<?> reader(String s) {
		return ByteArray.Immutable.wrap(s.chars().toArray()).reader(0);
	}

	/**
	 * Creates a test input stream with given bytes.
	 */
	public static ByteArrayInputStream inputStream(int... bytes) {
		return new ByteArrayInputStream(ArrayUtil.bytes.of(bytes));
	}

	/**
	 * Creates a test input stream based on UTF8 bytes and encoded actions.
	 */
	public static ByteArrayInputStream inputStream(String format, Object... args) {
		return new ByteArrayInputStream(
			Strings.format(format, args).getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * Returns a byte array for a range of values.
	 */
	public static byte[] byteRange(int from, int to) {
		byte[] bytes = new byte[Math.abs(to - from) + 1];
		int inc = (int) Math.signum(to - from);
		for (int i = 0, n = from; i < bytes.length; i++, n += inc)
			bytes[i] = (byte) n;
		return bytes;
	}

	/**
	 * Returns a random boolean.
	 */
	public static boolean randomBool() {
		return ThreadLocalRandom.current().nextBoolean();
	}

	/**
	 * Returns a random byte array of given size.
	 */
	public static byte[] randomBytes(int size) {
		byte[] bytes = new byte[size];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) Maths.random(0, Maths.MAX_UBYTE);
		return bytes;
	}

	/**
	 * Create a random string of given size.
	 */
	public static String randomString(long size) {
		return randomString(size, ' ', '~');
	}

	/**
	 * Create a random string of given size between char min and max (inclusive).
	 */
	public static String randomString(long size, int min, int max) {
		StringBuilder b = new StringBuilder();
		while (size-- > 0)
			b.append((char) (RND.nextInt(1 + max - min) + min));
		return b.toString();
	}

	// support

	private static <T> void exerciseEqual(T t0, T t1) {
		Assert.equal(t0, t1);
		if (t0 == t1) t0.equals(t1);
		Assert.equal(t0.hashCode(), t1.hashCode());
		Assert.equal(t0.toString(), t1.toString());
	}
}
