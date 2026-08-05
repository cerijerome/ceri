package ceri.ffm.clib.test;

import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import ceri.common.array.Array;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.function.Enclosure;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.test.CallSync;
import ceri.common.test.Testing;
import ceri.common.text.Strings;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CFcntl;
import ceri.ffm.clib.ffm.CLib;
import ceri.ffm.clib.ffm.CSignal.sigset_t;
import ceri.ffm.clib.ffm.CUnistd;
import ceri.ffm.clib.ffm.CUnistd.size_t;
import ceri.ffm.clib.ffm.CUnistd.ssize_t;
import ceri.ffm.core.ErrNo;
import ceri.ffm.core.Library;
import ceri.ffm.type.IntType.CLong;
import ceri.ffm.type.IntType.CUlong;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Primitive;

/**
 * Emulates c library responses.
 */
public class TestCLibNative implements CLib.Native {
	private static final CErrNo OK = null;
	private AtomicInteger nextFd = new AtomicInteger();
	public final Set<Integer> openFds = Sets.concurrent(); // open
	public final Map<Integer, Fd> allFds = Maps.concurrent();
	public final Map<String, String> env = Maps.concurrent();
	public final CallSync.Function<Fd, CErrNo> open = CallSync.function(null, OK);
	public final CallSync.Function<Fd, CErrNo> close = CallSync.function(null, OK);
	public final CallSync.Function<Fd, CErrNo> isatty = CallSync.function(null, OK);
	public final CallSync.Function<Fd[], CErrNo> pipe = CallSync.function(null, OK);
	public final CallSync.Function<Read, Result<byte[]>> read = CallSync.function(null);
	public final CallSync.Function<Write, Result<byte[]>> write = CallSync.function(null);
	public final CallSync.Function<Lseek, Result<Long>> lseek = CallSync.function(null);
	public final CallSync.Supplier<Integer> pagesize = CallSync.supplier(0x1000); // 4k
	// public final CallSync.Function<SignalArgs, Pointer> signal =
	// CallSync.function(null, Pointer.NULL);
	public final CallSync.Function<Integer, Integer> sigset = CallSync.function(null, 0);
	public final CallSync.Function<Integer, Integer> raise = CallSync.function(null, 0);
	// public final CallSync.Function<PollArgs, Integer> poll = CallSync.function(null, 0);
	// public final CallSync.Function<CtlArgs, Integer> ioctl = CallSync.function(null, 0);
	// public final CallSync.Function<CtlArgs, Integer> fcntl = CallSync.function(null, 0);
	// public final CallSync.Function<TcArgs, Integer> tc = CallSync.function(null, 0);
	// public final CallSync.Function<CfArgs, Integer> cf = CallSync.function(null, 0);
	// public final CallSync.Function<MmapArgs, Presult> mmap = CallSync.function(null, Presult.OK);
	private volatile Fd lastFd = null;

	/**
	 * A result with value and/or error.
	 */
	public record Result<T>(T value, CErrNo errNo) {
		public static <T> Result<T> of(T value) {
			return new Result<>(value, null);
		}

		public static Result<byte[]> ofBytes(int... bytes) {
			return of(Array.BYTE.of(bytes));
		}

		public static <T> Result<T> error(CErrNo errNo) {
			return new Result<>(null, errNo);
		}
	}

	/**
	 * File descriptor open context.
	 */
	public record Fd(int fd, String path, int flags, int mode, Reflect.ThreadElement origin) {
		public static Fd of(int fd, String path, int flags, int mode) {
			return new Fd(fd, path, flags, mode, Testing.findTest());
		}

		@Override
		public final String toString() {
			return String.format("%d:%s,0x%x,0%o:%s", path(), flags(), mode(), origin());
		}
	}

	/**
	 * Arguments for read calls.
	 */
	public record Read(Fd fd, MemorySegment buffer, int len) {}

	/**
	 * Arguments for write calls.
	 */
	public record Write(Fd fd, MemorySegment buffer, int len) {}

	/**
	 * Arguments for lseek calls.
	 */
	public record Lseek(Fd fd, long offset, int whence) {}

	/**
	 * A wrapper for repeatedly overriding the library in tests.
	 */
	public static Library.Ref<TestCLibNative> ref() {
		return CLib.library.ref(TestCLibNative::of);
	}

	/**
	 * Register a new test lib.
	 */
	public static Enclosure<TestCLibNative> register() {
		return CLib.library.enclosed(of());
	}

	public static TestCLibNative of() {
		return new TestCLibNative();
	}

	protected TestCLibNative() {
		reset();
	}

	/**
	 * Clear fds and call-sync states.
	 */
	public void reset() {
		nextFd.set(1000);
		openFds.clear();
		allFds.clear();
		env.clear();
		// CallSync.resetAll(cf, close, fcntl, ioctl, isatty, lseek, pagesize, mmap, open, pipe,
		// poll,
		// raise, read, signal, sigset, tc, write);
		openFds.add(CUnistd.STDIN_FILENO);
		openFds.add(CUnistd.STDOUT_FILENO);
		openFds.add(CUnistd.STDERR_FILENO);
	}

	// <unistd.h>

	@Override
	public int close(int fd) {
		return applyFd(fd, -1, f -> {
			var errNo = close.apply(f);
			remove(fd);
			return result(0, -1, errNo);
		});
	}

	@Override
	public int isatty(int fd) {
		return applyFd(fd, -1, f -> result(1, 0, isatty.apply(f)));
	}

	@Override
	public int pipe(int[] pipefd) {
		// trying to match posix behavior
		if (pipefd == null) pipefd = Array.INT.empty;
		var fr = pipefd.length > 1 ? open("pipe:r", CFcntl.Open.O_RDONLY.value) : -1;
		var fw = pipefd.length > 1 ? open("pipe:w", CFcntl.Open.O_WRONLY.value) : -1;
		var errNo = pipe.apply(new Fd[] { fd(fr), fd(fw) });
		if (!ok(errNo)) {
			remove(fw, fr);
			return error(-1, errNo);
		}
		if (fr != -1) pipefd[0] = fr;
		if (fw != -1) pipefd[1] = fw;
		return 0;
	}

	@Override
	public ssize_t read(int fd, MemorySegment buffer, size_t len) {
		return new ssize_t(applyFd(fd, -1, f -> {
			var result = read.apply(new Read(f, buffer, len.intValue()));
			int n = result.value() == null ? 0 : Primitive.BYTE.writeArray(buffer, 0,
				Integer.MAX_VALUE, result.value(), 0, len.intValue(), false);
			return result(n, -1, result.errNo());
		}));
	}

	@Override
	public ssize_t write(int fd, MemorySegment buffer, size_t len) {
		return new ssize_t(applyFd(fd, -1, f -> {
			var result = write.apply(new Write(f, buffer, len.intValue()));
			int n = result.value() == null ? 0 : Primitive.BYTE.readArray(buffer, 0,
				Integer.MAX_VALUE, result.value(), 0, len.intValue(), false);
			return result(n, -1, result.errNo());
		}));
	}

	@Override
	public CLong lseek(int fd, CLong offset, int whence) {
		return new CLong(
			applyFd(fd, -1L, f -> result(lseek.apply(new Lseek(f, offset.value(), whence)), -1L)));
	}

	@Override
	public int getpagesize() {
		return pagesize.get();
	}

	// <signal.h>

	@Override
	public MemorySegment signal(int signum, MemorySegment handler) {
		return null;
	}

	@Override
	public int raise(int sig) {
		return 0;
	}

	@Override
	public int sigemptyset(Pointer<sigset_t> set) {
		return 0;
	}

	@Override
	public int sigaddset(Pointer<sigset_t> set, int signum) {
		return 0;
	}

	@Override
	public int sigdelset(Pointer<sigset_t> set, int signum) {
		return 0;
	}

	@Override
	public int sigismember(Pointer<sigset_t> set, int signum) {
		return 0;
	}

	// <poll.h>

	// int poll(Pointer fds, int nfds, int timeout);

	// int ppoll(Pointer fds, int nfds, Pointer tmo_p, Pointer sigmask);

	// <fcntl.h>

	@Override
	public int open(String path, int flags, Object... args) {
		if ((flags & CFcntl.Open.O_ACCMODE) == CFcntl.Open.O_ACCMODE)
			return error(-1, CErrNo.EINVAL);
		var fd = Fd.of(nextFd.getAndIncrement(), path, flags, (int) Array.at(args, 0, 0));
		lastFd = fd;
		var errNo = this.open.apply(fd);
		return ok(errNo) ? add(fd) : error(-1, errNo);
	}

	@Override
	public int fcntl(int fd, int cmd, Object... args) {
		return 0;
	}

	// <sys/ioctl.h>

	@Override
	public int ioctl(int fd, CUlong request, Object... args) {
		return 0;
	}

	// <termios.h>

	// int tcgetattr(int fd, Pointer termios);

	// int tcsetattr(int fd, int optional_actions, Pointer termios);

	// int tcsendbreak(int fd, int duration);

	// int tcdrain(int fd);

	// int tcflush(int fd, int queue_selector);

	// int tcflow(int fd, int action);

	// void cfmakeraw(Pointer termios);

	// speed_t cfgetispeed(Pointer termios);

	// speed_t cfgetospeed(Pointer termios);

	// int cfsetispeed(Pointer termios, speed_t speed);

	// int cfsetospeed(Pointer termios, speed_t speed);

	// <sys/mman.h>

	// Pointer mmap(Pointer addr, size_t len, int prot, int flags, int fd, int offset)

	// int munmap(Pointer addr, size_t len);

	// <stdlib.h>

	@Override
	public int setenv(String name, String value, int overwrite) {
		if (Strings.isEmpty(name) || name.contains("=")) return error(-1, CErrNo.EINVAL);
		if (overwrite != 0) env.put(name, value);
		else env.putIfAbsent(name, value);
		return 0;
	}

	@Override
	public String getenv(String name) {
		return env.get(name);
	}

	// <string.h>

	@Override
	public String strerror(int errnum) {
		return "Error message " + errnum;
	}

	// support

	private Fd fd(int fd) {
		return allFds.get(fd);
	}

	private void remove(int... fds) {
		for (var fd : fds)
			this.openFds.remove(fd);
	}

	private int add(Fd fd) {
		allFds.put(fd.fd(), fd);
		openFds.add(fd.fd());
		lastFd = fd;
		return fd.fd();
	}

	private <T> T applyFd(int fd, T error, Functions.Function<Fd, T> op) {
		if (openFds.contains(fd)) return op.apply(fd(fd));
		return error(error, CErrNo.EBADF);
	}

	private boolean ok(CErrNo errNo) {
		return errNo == null; // || !errNo.defined();
	}

	private <T> T result(Result<T> result, T error) {
		return ok(result.errNo()) ? result.value() : error(error, result.errNo());
	}

	private <T> T result(T ok, T error, CErrNo errNo) {
		return ok(errNo) ? ok : error(error, errNo);
	}

	private <T> T error(T result, CErrNo errNo) {
		ErrNo.set(errNo.code);
		return result;
	}
}
