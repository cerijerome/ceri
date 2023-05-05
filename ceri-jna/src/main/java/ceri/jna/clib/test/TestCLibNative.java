package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import com.sun.jna.LastErrorException;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.function.ExceptionConsumer;
import ceri.common.test.CallSync;
import ceri.common.text.StringUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CLib;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.jna.CSignal.sighandler_t;
import ceri.jna.clib.jna.CUnistd.size_t;
import ceri.jna.clib.jna.CUnistd.ssize_t;
import ceri.jna.util.JnaUtil;
import ceri.jna.util.Struct;

/**
 * Test implementation for CLib native interface.
 */
public class TestCLibNative implements CLib.Native {
	private AtomicInteger nextFd = new AtomicInteger();
	public final Map<Integer, Fd> fds = new ConcurrentHashMap<>();
	public final Map<String, String> env = new ConcurrentHashMap<>();
	// List<?> = String path, int flags, int mode
	public final CallSync.Accept<List<?>> open = CallSync.consumer(null, true);
	public final CallSync.Apply<Fd, Integer> close = CallSync.function(null, 0);
	// List<?> = Fd f, int len
	public final CallSync.Apply<List<?>, ByteProvider> read =
		CallSync.function(null, ByteProvider.empty());
	// List<?> = Fd f, ByteProvider data
	public final CallSync.Apply<List<?>, Integer> write = CallSync.function(null, 0);
	// List<?> = Fd f, int offset, int whence
	public final CallSync.Apply<List<?>, Integer> lseek = CallSync.function(null, 0);
	// List<?> = int signal, sighandler_t handler
	public final CallSync.Apply<List<?>, Pointer> signal = CallSync.function(null, Pointer.NULL);
	public final CallSync.Apply<Integer, Integer> raise = CallSync.function(null, 0);
	// List<?> = List<pollfd> fds, int timeoutMs
	public final CallSync.Apply<List<?>, Integer> poll = CallSync.function(null, 0);
	// List<?> = Fd f, int request, Object[] objs
	public final CallSync.Apply<List<?>, Integer> ioctl = CallSync.function(null, 0);
	// List<?> = Fd f, int cmd, Object[] objs
	public final CallSync.Apply<List<?>, Integer> fcntl = CallSync.function(null, 0);
	// List<?> = Fd f, Pointer termios
	public final CallSync.Apply<List<?>, Integer> tcgetattr = CallSync.function(null, 0);
	// List<?> = Fd f, int optional_actions, Pointer termios
	public final CallSync.Apply<List<?>, Integer> tcsetattr = CallSync.function(null, 0);
	// List<?> = Fd f, int duration
	public final CallSync.Apply<List<?>, Integer> tcsendbreak = CallSync.function(null, 0);
	public final CallSync.Apply<Fd, Integer> tcdrain = CallSync.function(null, 0);
	// List<?> = Fd f, int queue_selector
	public final CallSync.Apply<List<?>, Integer> tcflush = CallSync.function(null, 0);
	// List<?> = Fd f, int action
	public final CallSync.Apply<List<?>, Integer> tcflow = CallSync.function(null, 0);
	public final CallSync.Accept<Pointer> cfmakeraw = CallSync.consumer(null, true);
	public final CallSync.Apply<Pointer, Integer> cfgetispeed = CallSync.function(null, 0);
	// List<?> = Pointer termios, int speed
	public final CallSync.Apply<List<?>, Integer> cfsetispeed = CallSync.function(null, 0);
	public final CallSync.Apply<Pointer, Integer> cfgetospeed = CallSync.function(null, 0);
	// List<?> = Pointer termios, int speed
	public final CallSync.Apply<List<?>, Integer> cfsetospeed = CallSync.function(null, 0);

	public static record Fd(int fd, String path, int flags, int mode) {}

	public static Enclosed<RuntimeException, TestCLibNative> register() {
		return register(of());
	}

	public static <E extends Exception> void exec(ExceptionConsumer<E, TestCLibNative> consumer)
		throws E {
		try (var enc = TestCLibNative.register(TestCLibNative.of())) {
			consumer.accept(enc.subject);
		}
	}

	public static <T extends TestCLibNative> Enclosed<RuntimeException, T> register(T lib) {
		return CLib.library.enclosed(lib);
	}

	public static LastErrorException lastError(CError error) {
		return new LastErrorException(error.code);
	}

	public static <T, R> void autoError(CallSync.Apply<T, R> sync, R response,
		Predicate<T> predicate, String errorMessage, Object... args) {
		autoError(sync, response, predicate, t -> StringUtil.format(errorMessage, args));
	}

	public static <T, R> void autoError(CallSync.Apply<T, R> sync, R response,
		Predicate<T> predicate, Function<T, String> errorMessageFn) {
		sync.autoResponse(t -> {
			if (predicate.test(t)) return response;
			throw new LastErrorException(errorMessageFn.apply(t));
		});
	}

	public static TestCLibNative of() {
		return new TestCLibNative();
	}

	protected TestCLibNative() {
		reset();
	}

	/**
	 * Set ioctl auto response to 0, and pass ioctl varargs to given consumer to process.
	 */
	public void ioctlAutoResponse(ToIntFunction<Object[]> fn) {
		ioctl.autoResponse(list -> fn.applyAsInt((Object[]) list.get(2)));
	}

	/**
	 * Assert ioctl request was called with fd, request and optional args.
	 */
	public void assertIoctl(int fd, int request, Object... args) {
		assertIoctlArgs(fd, request, objs -> assertArray(objs, args));
	}

	/**
	 * Assert ioctl request was called with fd, and use consumer to verify vararg fields.
	 */
	public void assertIoctlArgs(int fd, int request, Consumer<Object[]> consumer) {
		var list = ioctl.awaitAuto();
		assertEquals(list.get(0), fd(fd));
		assertEquals(list.get(1), request);
		if (consumer != null) consumer.accept((Object[]) list.get(2));
	}

	/**
	 * Set fcntl auto response to 0, and pass fcntl varargs to given consumer to process.
	 */
	public void fcntlAutoResponse(ToIntFunction<Object[]> fn) {
		fcntl.autoResponse(list -> fn.applyAsInt((Object[]) list.get(2)));
	}

	/**
	 * Assert fcntl request was called with fd, command and optional args.
	 */
	public void assertFcntl(int fd, int command, Object... args) {
		assertFcntlArgs(fd, command, objs -> assertArray(objs, args));
	}

	/**
	 * Assert fcntl command was called with fd, and use consumer to verify vararg fields.
	 */
	public void assertFcntlArgs(int fd, int command, Consumer<Object[]> consumer) {
		var list = fcntl.awaitAuto();
		assertEquals(list.get(0), fd(fd));
		assertEquals(list.get(1), command);
		if (consumer != null) consumer.accept((Object[]) list.get(2));
	}

	/**
	 * Clear fds.
	 */
	public void reset() {
		nextFd.set(1000);
		fds.clear();
	}

	@Override
	public int open(String path, int flags, Object... args) throws LastErrorException {
		int mode = args.length == 0 ? 0 : (int) args[0];
		Fd f = new Fd(nextFd.getAndIncrement(), path, flags, mode);
		fds.put(f.fd, f);
		open.accept(List.of(path, flags, mode));
		return f.fd;
	}

	@Override
	public int close(int fd) throws LastErrorException {
		Fd f = fd(fd);
		int result = close.apply(f);
		fds.remove(f.fd);
		return result;
	}

	@Override
	public ssize_t read(int fd, Pointer buffer, size_t len) throws LastErrorException {
		ByteProvider data = read.apply(List.of(fd(fd), len.intValue()));
		if (data == null || data.length() == 0) return new ssize_t(0);
		JnaUtil.write(buffer, data.copy(0));
		return new ssize_t(data.length());
	}

	@Override
	public ssize_t write(int fd, Pointer buffer, size_t len) throws LastErrorException {
		byte[] bytes = new byte[len.intValue()];
		if (buffer != null) JnaUtil.read(buffer, bytes);
		int n = write.apply(List.of(fd(fd), ByteProvider.of(bytes)));
		return new ssize_t(n);
	}

	@Override
	public int lseek(int fd, int offset, int whence) throws LastErrorException {
		return lseek.apply(List.of(fd(fd), offset, whence));
	}

	@Override
	public Pointer signal(int signum, sighandler_t handler) {
		return signal.apply(List.of(signum, handler));
	}

	@Override
	public Pointer signal(int signum, Pointer handler) {
		return signal.apply(List.of(signum, handler));
	}

	@Override
	public int raise(int sig) {
		return raise.apply(sig);
	}

	@Override
	public int poll(Pointer fds, int nfds, int timeout) throws LastErrorException {
		var array = Struct.arrayByVal(fds, pollfd::new, pollfd[]::new, nfds);
		return poll.apply(List.of(List.of(array), timeout));
	}

	@Override
	public int ioctl(int fd, NativeLong request, Object... objs) throws LastErrorException {
		return ioctl.apply(List.of(fd(fd), request.intValue(), objs));
	}

	@Override
	public int fcntl(int fd, int cmd, Object... objs) throws LastErrorException {
		return fcntl.apply(List.of(fd(fd), cmd, objs));
	}

	@Override
	public int tcgetattr(int fd, Pointer termios) throws LastErrorException {
		return tcgetattr.apply(List.of(fd(fd), termios));
	}

	@Override
	public int tcsetattr(int fd, int optional_actions, Pointer termios) throws LastErrorException {
		return tcsetattr.apply(List.of(fd(fd), optional_actions, termios));
	}

	@Override
	public int tcsendbreak(int fd, int duration) throws LastErrorException {
		return tcsendbreak.apply(List.of(fd(fd), duration));
	}

	@Override
	public int tcdrain(int fd) throws LastErrorException {
		return tcdrain.apply(fd(fd));
	}

	@Override
	public int tcflush(int fd, int queue_selector) throws LastErrorException {
		return tcflush.apply(List.of(fd(fd), queue_selector));
	}

	@Override
	public int tcflow(int fd, int action) throws LastErrorException {
		return tcflow.apply(List.of(fd(fd), action));
	}

	@Override
	public void cfmakeraw(Pointer termios) throws LastErrorException {
		cfmakeraw.accept(termios);
	}

	@Override
	public NativeLong cfgetispeed(Pointer termios) throws LastErrorException {
		return JnaUtil.unlong(cfgetispeed.apply(termios));
	}

	@Override
	public NativeLong cfgetospeed(Pointer termios) throws LastErrorException {
		return JnaUtil.unlong(cfgetospeed.apply(termios));
	}

	@Override
	public int cfsetispeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return cfsetispeed.apply(List.of(termios, speed.intValue()));
	}

	@Override
	public int cfsetospeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return cfsetospeed.apply(List.of(termios, speed.intValue()));
	}

	@Override
	public int setenv(String name, String value, int overwrite) throws LastErrorException {
		if (overwrite != 0) env.put(name, value);
		else env.putIfAbsent(name, value);
		return 0;
	}

	@Override
	public String getenv(String name) {
		return env.get(name);
	}

	public Fd fd(int fd) {
		return fd(fd, CError.EBADF);
	}

	protected Fd fd(int fd, CError error) {
		return fd(fd, error.code);
	}

	protected Fd fd(int fd, int errorCode) {
		Fd fdObj = fds.get(fd);
		if (fdObj != null) return fdObj;
		throw new LastErrorException(errorCode);
	}
}
