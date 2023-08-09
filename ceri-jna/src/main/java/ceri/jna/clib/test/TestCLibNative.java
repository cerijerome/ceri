package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;
import com.sun.jna.LastErrorException;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.function.ExceptionConsumer;
import ceri.common.test.CallSync;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.Enclosed;
import ceri.jna.clib.jna.CError;
import ceri.jna.clib.jna.CFcntl;
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
	public final CallSync.Consumer<List<?>> open = CallSync.consumer(null, true);
	public final CallSync.Function<Fd, Integer> close = CallSync.function(null, 0);
	public final CallSync.Function<Fd, Integer> isatty = CallSync.function(null, 0);
	public final CallSync.Function<Fd[], Integer> pipe = CallSync.function(null, 0);
	// List<?> = Fd f, int len
	public final CallSync.Function<List<?>, ByteProvider> read =
		CallSync.function(null, ByteProvider.empty());
	// List<?> = Fd f, ByteProvider data
	public final CallSync.Function<List<?>, Integer> write = CallSync.function(null, 0);
	// List<?> = Fd f, int offset, int whence
	public final CallSync.Function<List<?>, Integer> lseek = CallSync.function(null, 0);
	// List<?> = int signal, sighandler_t handler
	public final CallSync.Function<List<?>, Pointer> signal = CallSync.function(null, Pointer.NULL);
	public final CallSync.Function<Integer, Integer> raise = CallSync.function(null, 0);
	// List<?> = List<pollfd> fds, int timeoutMs
	public final CallSync.Function<List<?>, Integer> poll = CallSync.function(null, 0);
	// List<?> = Fd f, int request, Object[] objs
	public final CallSync.Function<List<?>, Integer> ioctl = CallSync.function(null, 0);
	// List<?> = Fd f, int cmd, Object[] objs
	public final CallSync.Function<List<?>, Integer> fcntl = CallSync.function(null, 0);
	// List<?> = String callName, Fd f, ...
	public final CallSync.Function<List<?>, Integer> tc = CallSync.function(null, 0);
	// List<?> = String callName, ...
	public final CallSync.Function<List<?>, Integer> cf = CallSync.function(null, 0);

	public static record Fd(int fd, String path, int flags, int mode) {}

	public static Enclosed<RuntimeException, TestCLibNative> register() {
		return register(of());
	}

	public static <E extends Exception> void exec(ExceptionConsumer<E, TestCLibNative> consumer)
		throws E {
		try (var enc = TestCLibNative.register(TestCLibNative.of())) {
			consumer.accept(enc.ref);
		}
	}

	public static <T extends TestCLibNative> Enclosed<RuntimeException, T> register(T lib) {
		return CLib.library.enclosed(lib);
	}

	public static <T, R> void autoError(CallSync.Function<T, R> sync, R response,
		Predicate<T> predicate, String errorMessage, Object... args) {
		autoError(sync, response, predicate, t -> StringUtil.format(errorMessage, args));
	}

	public static <T, R> void autoError(CallSync.Function<T, R> sync, R response,
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
	 * Pass pollfd array and timeout to given consumer to process, and set auto response to the
	 * number of pollfds with non-zero revents.
	 */
	public void pollAutoResponse(ObjIntConsumer<pollfd[]> consumer) {
		poll.autoResponse(list -> {
			var pollfds = BasicUtil.<List<pollfd>>uncheckedCast(list.get(0)).toArray(pollfd[]::new);
			consumer.accept(pollfds, (Integer) list.get(1));
			Struct.write(pollfds);
			return (int) Stream.of(pollfds).filter(pollfd -> pollfd.revents != 0).count();
		});
	}

	/**
	 * Pass ioctl varargs to given consumer to process, and set auto response.
	 */
	public void ioctlAutoResponse(ToIntFunction<Object[]> fn) {
		ioctl.autoResponse(list -> fn.applyAsInt((Object[]) list.get(2)));
	}

	/**
	 * Set ioctl auto response to 0, and pass ioctl varargs to given consumer to process.
	 */
	public void ioctlAutoResponseOk(Consumer<Object[]> fn) {
		ioctlAutoResponse(objs -> {
			fn.accept(objs);
			return 0;
		});
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
	 * Clear fds and call-sync states.
	 */
	public void reset() {
		nextFd.set(1000);
		fds.clear();
		env.clear();
		CallSync.resetAll(open, read, signal, close, isatty, pipe, write, lseek, raise, poll, ioctl,
			fcntl, tc, cf);
	}

	@Override
	public int open(String path, int flags, Object... args) throws LastErrorException {
		int mode = args.length == 0 ? 0 : (int) args[0];
		var fd = createFd(path, flags, mode);
		open.accept(List.of(path, flags, mode));
		return fd.fd;
	}

	@Override
	public int close(int fd) throws LastErrorException {
		Fd f = fd(fd);
		int result = close.apply(f);
		fds.remove(f.fd);
		return result;
	}

	@Override
	public int isatty(int fd) {
		return isatty.apply(fd(fd));
	}

	@Override
	public int pipe(int[] pipefd) throws LastErrorException {
		var fr = createFd("pipe:r", CFcntl.O_RDONLY, 0);
		var fw = createFd("pipe:w", CFcntl.O_WRONLY, 0);
		int result = pipe.apply(new Fd[] { fr, fw });
		pipefd[0] = fr.fd;
		pipefd[1] = fw.fd;
		return result;
	}

	@Override
	public ssize_t read(int fd, Pointer buffer, size_t len) throws LastErrorException {
		ByteProvider data = read.apply(List.of(fd(fd), len.intValue()));
		if (data == null || data.length() == 0) return new ssize_t(0);
		int n = Math.min(data.length(), len.intValue());
		JnaUtil.write(buffer, data.copy(0), 0, n);
		return new ssize_t(n);
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
		return tc.apply(List.of("tcgetattr", fd(fd), termios));
	}

	@Override
	public int tcsetattr(int fd, int optional_actions, Pointer termios) throws LastErrorException {
		return tc.apply(List.of("tcsetattr", fd(fd), optional_actions, termios));
	}

	@Override
	public int tcsendbreak(int fd, int duration) throws LastErrorException {
		return tc.apply(List.of("tcsendbreak", fd(fd), duration));
	}

	@Override
	public int tcdrain(int fd) throws LastErrorException {
		return tc.apply(List.of("tcdrain", fd(fd)));
	}

	@Override
	public int tcflush(int fd, int queue_selector) throws LastErrorException {
		return tc.apply(List.of("tcflush", fd(fd), queue_selector));
	}

	@Override
	public int tcflow(int fd, int action) throws LastErrorException {
		return tc.apply(List.of("tcflow", fd(fd), action));
	}

	@Override
	public void cfmakeraw(Pointer termios) throws LastErrorException {
		cf.apply(List.of("cfmakeraw", termios));
	}

	@Override
	public NativeLong cfgetispeed(Pointer termios) throws LastErrorException {
		return JnaUtil.unlong(cf.apply(List.of("cfgetispeed", termios)));
	}

	@Override
	public NativeLong cfgetospeed(Pointer termios) throws LastErrorException {
		return JnaUtil.unlong(cf.apply(List.of("cfgetospeed", termios)));
	}

	@Override
	public int cfsetispeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return cf.apply(List.of("cfsetispeed", termios, speed.intValue()));
	}

	@Override
	public int cfsetospeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return cf.apply(List.of("cfsetospeed", termios, speed.intValue()));
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

	private Fd createFd(String path, int flags, int mode) {
		Fd f = new Fd(nextFd.getAndIncrement(), path, flags, mode);
		fds.put(f.fd, f);
		return f;
	}
}
