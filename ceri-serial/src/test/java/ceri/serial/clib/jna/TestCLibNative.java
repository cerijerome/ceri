package ceri.serial.clib.jna;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import com.sun.jna.LastErrorException;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import ceri.common.data.ByteProvider;
import ceri.common.function.ExceptionConsumer;
import ceri.common.test.CallSync;
import ceri.common.text.StringUtil;
import ceri.common.util.Enclosed;
import ceri.serial.jna.JnaUtil;

/**
 * Test implementation for CLib.
 */
public class TestCLibNative implements CLibNative {
	private AtomicInteger nextFd = new AtomicInteger();
	public final Map<Integer, Fd> fds = new ConcurrentHashMap<>();
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
	// List<?> = Fd f, int request, Object... objs
	public final CallSync.Apply<List<?>, Integer> ioctl = CallSync.function(null, 0);
	// List<?> = Fd f, Pointer termios
	public final CallSync.Apply<List<?>, Integer> tcgetattr = CallSync.function(null, 0);
	// List<?> = Fd f, int optional_actions, Pointer termios
	public final CallSync.Apply<List<?>, Integer> tcsetattr = CallSync.function(null, 0);

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

	public void reset() {
		nextFd.set(1000);
		fds.clear();
	}

	@Override
	public int open(String path, int flags) throws LastErrorException {
		return open(path, flags, 0);
	}

	@Override
	public int open(String path, int flags, int mode) throws LastErrorException {
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
	public int ioctl(int fd, int request, Object... objs) throws LastErrorException {
		return ioctl(fd(fd), request, objs);
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
		return 0;
	}

	@Override
	public int tcdrain(int fd) throws LastErrorException {
		return 0;
	}

	@Override
	public int tcflush(int fd, int queue_selector) throws LastErrorException {
		return 0;
	}

	@Override
	public int tcflow(int fd, int action) throws LastErrorException {
		return 0;
	}

	@Override
	public void cfmakeraw(Pointer termios) throws LastErrorException {}

	@Override
	public NativeLong cfgetispeed(Pointer termios) throws LastErrorException {
		return new NativeLong(0);
	}

	@Override
	public NativeLong cfgetospeed(Pointer termios) throws LastErrorException {
		return new NativeLong(0);
	}

	@Override
	public int cfsetispeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return 0;
	}

	@Override
	public int cfsetospeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return 0;
	}

	@Override
	public int cfsetspeed(Pointer termios, NativeLong speed) throws LastErrorException {
		return 0;
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

	protected int ioctl(Fd f, int request, Object... objs) throws LastErrorException {
		List<Object> list = new ArrayList<>();
		Collections.addAll(list, f, request);
		Collections.addAll(list, objs);
		return ioctl.apply(list);
	}

}
