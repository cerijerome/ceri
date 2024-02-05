package ceri.common.test;

import static ceri.common.exception.ExceptionAdapter.RUNTIME;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNotEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.runner.JUnitCore;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.ConcurrentUtil;
import ceri.common.concurrent.SimpleExecutor;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteProvider;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionConsumer;
import ceri.common.function.ExceptionRunnable;
import ceri.common.io.IoUtil;
import ceri.common.io.SystemIo;
import ceri.common.math.MathUtil;
import ceri.common.property.BaseProperties;
import ceri.common.property.PropertyUtil;
import ceri.common.reflect.ReflectUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;

public class TestUtil {
	private static final int DELAY_MICROS = 1;
	private static final int SMALL_BUFFER_SIZE = 1024;
	private static final Random RND = new Random();
	public static final boolean isTest = ReflectUtil.stackHasPackage(Assert.class);

	private TestUtil() {}

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
		JUnitCore core = new JUnitCore();
		TestPrinter tp = new TestPrinter();
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
	 * Repeat action with a 1us delay until executor is closed. Useful to avoid intermittent thread
	 * timing issues when waiting on an event by repeatedly triggering that event.
	 */
	public static SimpleExecutor<RuntimeException, ?> runRepeat(ExceptionRunnable<?> runnable) {
		return SimpleExecutor.run(() -> {
			while (true) {
				runnable.run();
				ConcurrentUtil.delayMicros(DELAY_MICROS);
			}
		});
	}

	public static String firstSystemPropertyName() {
		Set<Object> keys = System.getProperties().keySet();
		return BasicUtil.conditional(keys.isEmpty(), "", String.valueOf(keys.iterator().next()));
	}

	public static String firstSystemProperty() {
		return System.getProperty(firstSystemPropertyName());
	}

	public static String firstEnvironmentVariableName() {
		Set<String> keys = System.getenv().keySet();
		return BasicUtil.conditional(keys.isEmpty(), "", keys.iterator().next());
	}

	public static String firstEnvironmentVariable() {
		return System.getenv(firstEnvironmentVariableName());
	}

	/**
	 * Reads a string resource from the caller's package with given name.
	 */
	public static String resource(String name) {
		Class<?> cls = ReflectUtil.previousCaller(1).cls();
		return init(() -> IoUtil.resourceString(cls, name));
	}

	/**
	 * Creates BaseProperties from name.properties file under caller's package.
	 */
	public static BaseProperties baseProperties(String name) {
		Class<?> cls = ReflectUtil.previousCaller(1).cls();
		return baseProperties(cls, name);
	}

	/**
	 * Creates BaseProperties from name.properties under class package.
	 */
	public static BaseProperties baseProperties(Class<?> cls, String name) {
		return BaseProperties.from(properties(cls, name));
	}

	/**
	 * Creates Properties from name.properties file under caller's package.
	 */
	public static Properties properties(String name) {
		Class<?> cls = ReflectUtil.previousCaller(1).cls();
		return properties(cls, name);
	}

	/**
	 * Creates Properties from name.properties under class package.
	 */
	public static Properties properties(Class<?> cls, String name) {
		String text = init(() -> IoUtil.resourceString(cls, name + ".properties"));
		return PropertyUtil.parse(text);
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
	 * Return a SystemIo instance with System.out and System.err output nullified.
	 */
	@SuppressWarnings("resource")
	public static SystemIo nullOutErr() {
		SystemIo sys = SystemIo.of();
		sys.out(IoUtil.nullPrintStream());
		sys.err(IoUtil.nullPrintStream());
		return sys;
	}

	/**
	 * Simple map creation with alternating keys and values.
	 */
	public static <K, V> Map<K, V> testMap(Object... objs) {
		Map<K, V> map = new LinkedHashMap<>();
		int i = 0;
		while (i < objs.length) {
			K key = BasicUtil.uncheckedCast(objs[i++]);
			V value = i < objs.length ? BasicUtil.uncheckedCast(objs[i++]) : null;
			map.put(key, value);
		}
		return map;
	}

	/**
	 * Checks equals, hashCode and toString methods against first object.
	 */
	@SafeVarargs
	public static <T> void exerciseEquals(T t0, T... ts) {
		exerciseEqual(t0, t0);
		for (T t : ts)
			exerciseEqual(t0, t);
		assertNotEquals(null, t0);
		assertNotEquals(new Object(), t0);
	}

	private static <T> void exerciseEqual(T t0, T t1) {
		assertEquals(t0, t1);
		if (t0 == t1) t0.equals(t1);
		assertEquals(t0.hashCode(), t1.hashCode());
		assertEquals(t0.toString(), t1.toString());
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
		ExceptionBiConsumer<ReflectiveOperationException, Method, T> invoker) {
		if (t != null) for (Field field : t.getClass().getDeclaredFields())
			RUNTIME.run(() -> invoker.accept(t.getClass().getMethod(field.getName()), t));
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
	 * Call this for code coverage of switch with strings. Bytecode checks hashes then values, but
	 * the value check is only 1 of 2 branches. The methods calls the code using strings with same
	 * hashes but different values.
	 */
	public static <E extends Exception> void exerciseSwitch(ExceptionConsumer<E, String> consumer,
		String... strings) throws E {
		for (String s : strings)
			consumer.accept("\0" + s);
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
	 * Capture and return any thrown exception.
	 */
	public static Throwable thrown(ExceptionRunnable<?> runnable) {
		try {
			runnable.run();
			return null;
		} catch (Throwable t) {
			return t;
		}
	}

	/**
	 * Convert collection of files to list of unix-format paths
	 */
	public static List<String> pathsToUnix(Collection<Path> paths) {
		return paths.stream().map(IoUtil::pathToUnix).collect(Collectors.toList());
	}

	/**
	 * Convert collection of files to list of unix-format paths
	 */
	public static List<String> pathNamesToUnix(Collection<String> pathNames) {
		return pathNames.stream().map(IoUtil::pathToUnix).collect(Collectors.toList());
	}

	/**
	 * Returns a ByteProvider wrapper for bytes.
	 */
	public static ByteProvider provider(int... bytes) {
		return Immutable.wrap(bytes);
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
	 * Returns a random byte array of given size.
	 */
	public static byte[] randomBytes(int size) {
		byte[] bytes = new byte[size];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) MathUtil.random(0, MathUtil.MAX_UBYTE);
		return bytes;
	}

	/**
	 * Returns a ByteProvider.Reader<?> wrapper for bytes.
	 */
	public static ByteProvider.Reader<?> reader(int... bytes) {
		return provider(bytes).reader(0);
	}

	/**
	 * Returns a ByteProvider.Reader<?> wrapper for chars.
	 */
	public static ByteProvider.Reader<?> reader(String s) {
		return Immutable.wrap(s.chars().toArray()).reader(0);
	}

	/**
	 * Creates a test input stream with given bytes.
	 */
	public static ByteArrayInputStream inputStream(int... bytes) {
		return new ByteArrayInputStream(ArrayUtil.bytes(bytes));
	}

	/**
	 * Creates a test input stream based on UTF8 bytes and encoded actions.
	 */
	public static ByteArrayInputStream inputStream(String format, Object... args) {
		return new ByteArrayInputStream(
			StringUtil.format(format, args).getBytes(StandardCharsets.UTF_8));
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
	public static SimpleExecutor<RuntimeException, ?> threadRun(ExceptionRunnable<?> runnable) {
		return SimpleExecutor.run(runnable);
	}

	/**
	 * Returns a random boolean.
	 */
	public static boolean randomBool() {
		return ThreadLocalRandom.current().nextBoolean();
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
		StringBuilder b = new StringBuilder();
		try {
			if (charset == null || charset.isEmpty()) b.append(new String(array, offset, len));
			else b.append(new String(array, offset, len, charset));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalArgumentException(e);
		}
		for (int i = 0; i < b.length(); i++) {
			if (!StringUtil.isPrintable(b.charAt(i))) b.setCharAt(i, unreadableChar);
			// if (b.charAt(i) < ' ') b.setCharAt(i, readableChar);
		}
		return b.toString();
	}

}
