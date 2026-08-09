package ceri.ffm.clib.test;

import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import ceri.common.array.Array;
import ceri.common.collect.Collectable;
import ceri.common.collect.Maps;
import ceri.common.collect.Sets;
import ceri.common.data.Bytes;
import ceri.common.function.Functions;
import ceri.common.reflect.Reflect;
import ceri.common.test.CallSync;
import ceri.common.test.Testing;
import ceri.common.text.Strings;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CFcntl;
import ceri.ffm.clib.ffm.CLib;
import ceri.ffm.clib.ffm.CPoll;
import ceri.ffm.clib.ffm.CSignal;
import ceri.ffm.clib.ffm.CUnistd;
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
	public final Set<Integer> openFds = Sets.concurrent(); // open
	public final Map<Integer, Fd> allFds = Maps.concurrent();
	public final Map<String, String> env = Maps.concurrent();
	public final CallSync.Function<Fd, CErrNo> open = CallSync.function(null, OK);
	public final CallSync.Function<Fd, CErrNo> close = CallSync.function(null, OK);
	public final CallSync.Function<Fd, CErrNo> isatty = CallSync.function(null, OK);
	public final CallSync.Function<Fd[], CErrNo> pipe = CallSync.function(null, OK);
	public final CallSync.Function<Io, Result<byte[]>> read =
		CallSync.function(null, Result.NO_BYTES);
	public final CallSync.Function<Io, Result<byte[]>> write =
		CallSync.function(null, Result.NO_BYTES);
	public final CallSync.Function<Seek, Result<Long>> lseek =
		CallSync.function(null, Result.of(0L));
	public final CallSync.Supplier<Integer> pagesize = CallSync.supplier(0x1000); // 4k
	public final CallSync.Function<Signal, Result<MemorySegment>> signal =
		CallSync.function(null, Result.of(CSignal.Macro.SIG_DFL.pointer));
	public final CallSync.Function<Integer, CErrNo> raise = CallSync.function(null, OK);
	public final CallSync.Function<SigSet, CErrNo> sigset = CallSync.function(null, OK);
	public final CallSync.Function<Poll, CErrNo> poll = CallSync.function(null, OK);
	public final CallSync.Function<Control, Result<Integer>> ioctl =
		CallSync.function(null, Result.of(0));
	public final CallSync.Function<Control, Result<Integer>> fcntl =
		CallSync.function(null, Result.of(0));
	// public final CallSync.Function<TcArgs, Integer> tc = CallSync.function(null, 0);
	// public final CallSync.Function<CfArgs, Integer> cf = CallSync.function(null, 0);
	// public final CallSync.Function<MmapArgs, Presult> mmap = CallSync.function(null, Presult.OK);
	private final AtomicInteger nextFd = new AtomicInteger();
	private volatile Fd lastFd = null;

	/**
	 * A result with value and/or error.
	 */
	public record Result<T>(T value, CErrNo errNo) {
		public static final Result<byte[]> NO_BYTES = bytes();

		public static <T> Result<T> of(T value) {
			return new Result<>(value, null);
		}

		public static Result<byte[]> bytes(int... bytes) {
			return of(Array.BYTE.of(bytes));
		}

		public static <T> Result<T> errno(CErrNo errNo) {
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
	}

	/**
	 * Arguments for read/write calls.
	 */
	public record Io(Fd fd, MemorySegment buffer, int len) {}

	/**
	 * Arguments for lseek calls.
	 */
	public record Seek(Fd fd, long offset, int whence) {}

	/**
	 * Arguments for signal calls.
	 */
	public record Signal(int signum, MemorySegment handler) {}

	/**
	 * Arguments for signal set calls.
	 */
	public record SigSet(CSignal.sigset_t sigset, Action action, int signum) {
		public enum Action {
			none,
			empty,
			add,
			del;
		}
	}

	/**
	 * Arguments for poll calls.
	 */
	public record Poll(CPoll.pollfd[] pollFds, int timeout) {}

	/**
	 * Args for fcntl and ioctl calls.
	 */
	public record Control(Fd fd, long request, List<Object> args) {
		public static Control of(Fd fd, long request, Object... args) {
			return new Control(fd, request, List.of(args));
		}

		/**
		 * Provide vararg argument as a typed object.
		 */
		public <T> T arg(int i) {
			return Reflect.unchecked(args().get(i));
		}
	}

	/**
	 * A wrapper for repeatedly overriding the library in tests.
	 */
	public static Library.Ref<TestCLibNative> ref() {
		return CLib.library.ref(TestCLibNative::of);
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
		CallSync.resetAll(/* cf, */ close, fcntl, ioctl, isatty, lseek, pagesize, /* mmap, */ open,
			pipe, poll, raise, read, signal, sigset, /* tc, */ write);
		Collectable.addAll(openFds, CUnistd.STDIN_FILENO, CUnistd.STDOUT_FILENO,
			CUnistd.STDERR_FILENO);
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
		// posix seems to allow null, int[0] and int[1]
		if (pipefd == null) pipefd = Array.INT.empty;
		var fr = pipefd.length > 0 ? open("pipe:r", CFcntl.Open.O_RDONLY.value) : -1;
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
	public CUnistd.ssize_t read(int fd, MemorySegment buffer, CUnistd.size_t len) {
		return new CUnistd.ssize_t(applyFd(fd, -1, f -> {
			var result = read.apply(new Io(f, buffer, len.intValue()));
			int n = result.value() == null ? 0 : Primitive.BYTE.writeArray(buffer, 0,
				Integer.MAX_VALUE, result.value(), 0, len.intValue(), false);
			return result(n, -1, result.errNo());
		}));
	}

	@Override
	public CUnistd.ssize_t write(int fd, MemorySegment buffer, CUnistd.size_t len) {
		return new CUnistd.ssize_t(applyFd(fd, -1, f -> {
			var result = write.apply(new Io(f, buffer, len.intValue()));
			int n = result.value() == null ? 0 : Primitive.BYTE.readArray(buffer, 0,
				Integer.MAX_VALUE, result.value(), 0, len.intValue(), false);
			return result(n, -1, result.errNo());
		}));
	}

	@Override
	public CLong lseek(int fd, CLong offset, int whence) {
		return new CLong(
			applyFd(fd, -1L, f -> result(lseek.apply(new Seek(f, offset.value(), whence)), -1L)));
	}

	@Override
	public int getpagesize() {
		return pagesize.get();
	}

	// <signal.h>

	@Override
	public MemorySegment signal(int signum, MemorySegment handler) {
		return result(signal.apply(new Signal(signum, handler)), CSignal.Macro.SIG_ERR.pointer);
	}

	@Override
	public int raise(int sig) {
		return result(0, 1, raise.apply(sig));
	}

	@Override
	public int sigemptyset(Pointer<CSignal.sigset_t> set) {
		return applySigSet(set, SigSet.Action.empty, 0, (_, _) -> 0L);
	}

	@Override
	public int sigaddset(Pointer<CSignal.sigset_t> set, int signum) {
		return applySigSet(set, SigSet.Action.add, signum, (v, s) -> v | s);
	}

	@Override
	public int sigdelset(Pointer<CSignal.sigset_t> set, int signum) {
		return applySigSet(set, SigSet.Action.del, signum, (v, s) -> v & ~s);
	}

	@Override
	public int sigismember(Pointer<CSignal.sigset_t> set, int signum) {
		var errNo = sigset.apply(new SigSet(set.get(), SigSet.Action.none, signum));
		if (!ok(errNo)) return error(-1, errNo);
		return (Bytes.fromMsb(set.get().bytes) & sigSetVal(signum)) == 0 ? 0 : 1;
	}

	// <poll.h>

	@Override
	public int poll(Pointer<CPoll.pollfd> fds, int nfds, int timeout) {
		var pollFds = fds.getArray(nfds, false);
		var errNo = poll.apply(new Poll(pollFds, timeout));
		if (!ok(errNo)) return error(-1, errNo);
		int count = 0;
		for (var pollFd : pollFds)
			if (pollFd.revents != 0) count++;
		fds.setArray(pollFds, false);
		return count;
	}

	// <fcntl.h>

	@Override
	public int open(String path, int flags, Object... args) {
		if (Strings.isEmpty(path)) return error(-1, CErrNo.ENOENT);
		if ((flags & CFcntl.Open.O_ACCMODE) == CFcntl.Open.O_ACCMODE)
			return error(-1, CErrNo.EINVAL);
		var fd = Fd.of(nextFd.getAndIncrement(), path, flags, (int) Array.at(args, 0, 0));
		lastFd = fd;
		var errNo = this.open.apply(fd);
		return ok(errNo) ? add(fd) : error(-1, errNo);
	}

	@Override
	public int fcntl(int fd, int cmd, Object... args) {
		return applyFd(fd, -1, f -> result(fcntl.apply(Control.of(f, cmd, args)), -1));
	}

	// <sys/ioctl.h>

	@Override
	public int ioctl(int fd, CUlong request, Object... args) {
		return applyFd(fd, -1, f -> result(ioctl.apply(Control.of(f, request.value(), args)), -1));
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

	private int applySigSet(Pointer<CSignal.sigset_t> set, SigSet.Action action, int signum,
		Functions.LongBiOperator operator) {
		var struct = set.get();
		var errNo = sigset.apply(new SigSet(struct, action, signum));
		if (!ok(errNo)) return error(-1, errNo);
		var value = Bytes.fromMsb(struct.bytes);
		value = operator.applyAsLong(value, sigSetVal(signum));
		Bytes.writeMsb(value, struct.bytes);
		set.set(struct);
		return 0;
	}

	private static long sigSetVal(int signum) {
		return Bytes.maskOfBits(signum - 1);
	}

	private static boolean ok(Result<?> result) {
		return ok(result.errNo());
	}

	private static boolean ok(CErrNo errNo) {
		return errNo == null; // || !errNo.defined();
	}

	private static <T> T result(Result<T> result) {
		return result(result, result.value());
	}

	private static <T> T result(Result<T> result, T error) {
		return ok(result) ? result.value() : error(error, result.errNo());
	}

	private static <T> T result(T ok, T error, CErrNo errNo) {
		return ok(errNo) ? ok : error(error, errNo);
	}

	private static <T> T error(T result, CErrNo errNo) {
		ErrNo.set(errNo.code);
		return result;
	}
}
