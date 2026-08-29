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
import ceri.common.data.Xcoder;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.reflect.Reflect;
import ceri.common.test.Assert;
import ceri.common.test.CallSync;
import ceri.common.test.Testing;
import ceri.common.text.Strings;
import ceri.common.time.TimeSpec;
import ceri.common.util.Os;
import ceri.ffm.clib.ffm.CErrNo;
import ceri.ffm.clib.ffm.CFcntl;
import ceri.ffm.clib.ffm.CLib;
import ceri.ffm.clib.ffm.CMman;
import ceri.ffm.clib.ffm.CPoll;
import ceri.ffm.clib.ffm.CSignal;
import ceri.ffm.clib.ffm.CTermios;
import ceri.ffm.clib.ffm.CTime;
import ceri.ffm.clib.ffm.CUnistd;
import ceri.ffm.core.LastError;
import ceri.ffm.test.FfmTesting;
import ceri.ffm.type.IntType.CLong;
import ceri.ffm.type.IntType.CUlong;
import ceri.ffm.type.Memory;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.PointerType;
import ceri.ffm.type.Primitive;
import ceri.ffm.type.Route;

/**
 * Emulates c library responses.
 */
public class TestCLibNative implements CLib.Native {
	private static final CErrNo OK = null;
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
		CallSync.function(null, Result.of(CSignal.Macro.SIG_DFL.pointer()));
	public final CallSync.Function<Integer, CErrNo> raise = CallSync.function(null, OK);
	public final CallSync.Function<SigSet, CErrNo> sigset = CallSync.function(null, OK);
	public final CallSync.Function<Poll, CErrNo> poll = CallSync.function(null, OK);
	public final CallSync.Function<Control, Result<Integer>> ioctl =
		CallSync.function(null, Result.of(0));
	public final CallSync.Function<Control, Result<Integer>> fcntl =
		CallSync.function(null, Result.of(0));
	public final CallSync.Function<Tc, Result<Integer>> tc = CallSync.function(null, Result.of(0));
	public final CallSync.Function<Cf<?>, Result<Integer>> cf =
		CallSync.function(null, Result.of(0));
	public final CallSync.Function<Mmap, Result<MemorySegment>> mmap =
		CallSync.function(null, Result.of(null));
	public final CallSync.Supplier<CErrNo> general = CallSync.supplier(OK);
	private final AtomicInteger nextFd = new AtomicInteger();
	public final Set<Integer> openFds = Sets.concurrent();
	public final Map<Integer, Fd> allFds = Maps.concurrent();
	public final Map<String, String> env = Maps.concurrent();

	/**
	 * A result with value and/or error.
	 */
	public record Result<T>(T value, CErrNo errNo) {
		/** An instance with empty bytes array and no error. */
		public static final Result<byte[]> NO_BYTES = bytes();

		/**
		 * Returns an instance with the value and no error.
		 */
		public static <T> Result<T> of(T value) {
			return new Result<>(value, null);
		}

		/**
		 * Returns an instance with a byte array and no error.
		 */
		public static Result<byte[]> bytes(int... bytes) {
			return of(Array.BYTE.of(bytes));
		}

		/**
		 * Returns an instance with no value and an error.
		 */
		public static <T> Result<T> errno(CErrNo errNo) {
			return new Result<>(null, errNo);
		}
	}

	/**
	 * File descriptor open context.
	 */
	public record Fd(int fd, String path, int flags, int mode, Reflect.ThreadElement origin) {
		/**
		 * Creates an instance with auto-filled origin.
		 */
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
	public record Signal(int signum, MemorySegment handler) {
		/** Transcoder for signals as bit fields. */
		public static final Xcoder.Types<CSignal> xcoder =
			Xcoder.types(CSignal.class, s -> 1L << (s.value - 1));
	}

	/**
	 * Arguments for signal set calls.
	 */
	public record SigSet(Mask mask, int signum, Action action) {
		/**
		 * Action to be taken on the set.
		 */
		public enum Action {
			empty,
			add,
			del,
			has
		}

		/**
		 * Signal mask.
		 */
		public record Mask(long mask) {
			/**
			 * Adds a signal to the mask.
			 */
			public Mask add(int signum) {
				return new Mask(mask() | mask(signum));
			}

			/**
			 * Removes a signal from the mask.
			 */
			public Mask del(int signum) {
				return new Mask(mask() & ~mask(signum));
			}

			/**
			 * Returns true if the mask contains the signal.
			 */
			public boolean has(CSignal... signals) {
				for (var signal : signals)
					if (!has(signal.value)) return false;
				return true;
			}

			/**
			 * Returns true if the mask contains the signal.
			 */
			public boolean has(int signum) {
				return (mask() & mask(signum)) != 0L;
			}

			private static long mask(int signum) {
				return signum > 0 ? 1L << (signum - 1) : 0L;
			}
		}

		/**
		 * Extracts a signal mask from the set pointer.
		 */
		public static Mask mask(Pointer<CSignal.sigset_t> pointer) {
			if (PointerType.isNull(pointer)) return null;
			return new Mask(Bytes.fromMsb(pointer.get().bytes));
		}

		/**
		 * Writes the mask to the set pointer.
		 */
		public static int write(Mask mask, Pointer<CSignal.sigset_t> pointer) {
			if (PointerType.isNull(pointer)) return 0;
			var struct = pointer.get();
			Bytes.writeMsb(mask.mask(), struct.bytes);
			pointer.write(struct);
			return 0;
		}
	}

	/**
	 * Arguments for poll and ppoll calls.
	 */
	public record Poll(CPoll.pollfd[] pollFds, TimeSpec timeout, SigSet.Mask sigmask) {
		public static Route<Pointer.OfInt> FD = CPoll.pollfd.$.route("+.fd");
		public static Route<Pointer.OfShort> EVENTS = CPoll.pollfd.$.route("+.events");

		/**
		 * Creates an instance from poll arguments.
		 */
		public static Poll of(Pointer<CPoll.pollfd> pointer, int nfds, int timeoutMs) {
			return new Poll(pollFds(pointer, nfds), TimeSpec.fromMillis(timeoutMs), null);
		}

		/**
		 * Creates an instance from ppoll arguments.
		 */
		public static Poll of(Pointer<CPoll.pollfd> pointer, int nfds, Pointer<CTime.timespec> tmo,
			Pointer<CSignal.sigset_t> sigmask) {
			var timeout = PointerType.isNull(tmo) ? null : tmo.get().get();
			return new Poll(pollFds(pointer, nfds), timeout, SigSet.mask(sigmask));
		}

		private static CPoll.pollfd[] pollFds(Pointer<CPoll.pollfd> pointer, int nfds) {
			var pollFds = pointer.getArray(nfds, false);
			for (int i = 0; i < pollFds.length; i++) { // Capture @Out fields
				pollFds[i].fd = FD.pointer(pointer, i).get();
				pollFds[i].events = EVENTS.pointer(pointer, i).get();
			}
			return pollFds;
		}
	}

	/**
	 * Args for fcntl and ioctl calls.
	 */
	public record Control(Fd fd, long request, List<Object> args) {
		public static Control of(Fd fd, long request, Object... args) {
			return new Control(fd, request, List.of(args));
		}

		/**
		 * Provide vararg argument as a typed value.
		 */
		public <T> T arg(int i) {
			return Reflect.unchecked(args().get(i));
		}

		/**
		 * Asserts control parameters.
		 */
		public Control verify(int fd, int request, Object... args) {
			Assert.equal(fd().fd(), fd);
			Assert.equal(request(), Maths.uint(request));
			for (int i = 0; i < args.length; i++)
				Assert.equal(arg(i), args[i]);
			return this;
		}
	}

	/**
	 * Arguments for termios tc calls.
	 */
	public record Tc(String name, Fd fd, List<Object> args) {
		public static Tc of(String name, Fd fd, Object... args) {
			return new Tc(name, fd, List.of(args));
		}

		/**
		 * Provides a vararg argument as a typed object.
		 */
		public <T> T arg(int i) {
			return Reflect.unchecked(args().get(i));
		}

		/**
		 * Returns the vararg argument as an os-specific termios pointer.
		 */
		public <T extends CTermios.termios<T>> Pointer<T> termios(int i) {
			return TestCLibNative.termios(arg(i));
		}

		/**
		 * Asserts parameters.
		 */
		public Tc verify(String name, int fd, Object... args) {
			Assert.equal(name(), name);
			Assert.equal(fd().fd(), fd);
			for (int i = 0; i < args.length; i++)
				Assert.equal(arg(i), args[i]);
			return this;
		}
	}

	/**
	 * Arguments for termios cf calls.
	 */
	public record Cf<T extends CTermios.termios<T>>(String name, Pointer<T> termios,
		List<Object> args) {
		public static <T extends CTermios.termios<T>> Cf<T> of(String name, MemorySegment memory,
			Object... args) {
			return new Cf<>(name, TestCLibNative.<T>termios(memory), List.of(args));
		}

		/**
		 * Provide vararg argument as a typed object.
		 */
		public <R> R arg(int i) {
			return Reflect.unchecked(args().get(i));
		}

		/**
		 * Asserts parameters.
		 */
		public Cf<T> verify(String name, Pointer<? extends CTermios.termios<?>> termios,
			Object... args) {
			Assert.equal(name(), name);
			Assert.equal(termios(), termios);
			for (int i = 0; i < args.length; i++)
				Assert.equal(arg(i), args[i]);
			return this;
		}
	}

	/**
	 * Parameters for mmap/munmap.
	 */
	public record Mmap(MemorySegment addr, long len, int prot, int flags, int fd, int off,
		boolean map) {}

	/**
	 * Returns a wrapper for repeatedly overriding the native library.
	 */
	public static FfmTesting.Lib<TestCLibNative> lib() {
		return FfmTesting.lib(CLib.library, TestCLibNative::of);
	}

	/**
	 * Creates an instance of this test library.
	 */
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
		CallSync.resetAll(cf, close, fcntl, ioctl, isatty, lseek, pagesize, mmap, open, pipe, poll,
			raise, read, signal, sigset, tc, write, general);
		Collectable.addAll(openFds, CUnistd.STDIN_FILENO, CUnistd.STDOUT_FILENO,
			CUnistd.STDERR_FILENO);
	}

	public Fd fd(int fd) {
		return allFds.get(fd);
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
		return result(signal.apply(new Signal(signum, handler)), CSignal.Macro.SIG_ERR.pointer());
	}

	@Override
	public int raise(int sig) {
		return result(0, 1, raise.apply(sig));
	}

	@Override
	public int sigemptyset(Pointer<CSignal.sigset_t> set) {
		return sigset(set, 0, SigSet.Action.del, _ -> SigSet.write(new SigSet.Mask(0L), set));
	}

	@Override
	public int sigaddset(Pointer<CSignal.sigset_t> set, int signum) {
		return sigset(set, signum, SigSet.Action.add, mask -> SigSet.write(mask.add(signum), set));
	}

	@Override
	public int sigdelset(Pointer<CSignal.sigset_t> set, int signum) {
		return sigset(set, signum, SigSet.Action.del, mask -> SigSet.write(mask.del(signum), set));
	}

	@Override
	public int sigismember(Pointer<CSignal.sigset_t> set, int signum) {
		return sigset(set, signum, SigSet.Action.has, mask -> mask.has(signum) ? 1 : 0);
	}

	// <poll.h>

	@Override
	public int poll(Pointer<CPoll.pollfd> fds, int nfds, int timeout) {
		return poll(fds, Poll.of(fds, nfds, timeout));
	}

	@Override
	public int ppoll(Pointer<CPoll.pollfd> fds, int nfds, Pointer<CTime.timespec> tmo,
		Pointer<CSignal.sigset_t> sigmask) {
		return poll(fds, Poll.of(fds, nfds, tmo, sigmask));
	}

	// <fcntl.h>

	@Override
	public int open(String path, int flags, Object... args) {
		if (Strings.isEmpty(path)) return error(-1, CErrNo.ENOENT);
		if ((flags & CFcntl.Open.O_ACCMODE) == CFcntl.Open.O_ACCMODE)
			return error(-1, CErrNo.EINVAL);
		var fd = Fd.of(nextFd.getAndIncrement(), path, flags, (int) Array.at(args, 0, 0));
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

	@Override
	public int tcgetattr(int fd, MemorySegment termios) {
		return result(tc.apply(Tc.of("tcgetattr", fd(fd), termios)), -1);
	}

	@Override
	public int tcsetattr(int fd, int optional_actions, MemorySegment termios) {
		return result(tc.apply(Tc.of("tcsetattr", fd(fd), optional_actions, termios)), -1);
	}

	@Override
	public int tcsendbreak(int fd, int duration) {
		return result(tc.apply(Tc.of("tcsendbreak", fd(fd), duration)), -1);
	}

	@Override
	public int tcdrain(int fd) {
		return result(tc.apply(Tc.of("tcdrain", fd(fd))), -1);
	}

	@Override
	public int tcflush(int fd, int queue_selector) {
		return result(tc.apply(Tc.of("tcflush", fd(fd), queue_selector)), -1);
	}

	@Override
	public int tcflow(int fd, int action) {
		return result(tc.apply(Tc.of("tcflow", fd(fd), action)), -1);
	}

	@Override
	public void cfmakeraw(MemorySegment termios) {
		cf.apply(Cf.of("cfmakeraw", termios));
	}

	@Override
	public CTermios.speed_t cfgetispeed(MemorySegment termios) {
		return new CTermios.speed_t(cf.apply(Cf.of("cfgetispeed", termios)).value());
	}

	@Override
	public CTermios.speed_t cfgetospeed(MemorySegment termios) {
		return new CTermios.speed_t(cf.apply(Cf.of("cfgetospeed", termios)).value());
	}

	@Override
	public int cfsetispeed(MemorySegment termios, CTermios.speed_t speed) {
		return result(cf.apply(Cf.of("cfsetispeed", termios, speed.value())), -1);
	}

	@Override
	public int cfsetospeed(MemorySegment termios, CTermios.speed_t speed) {
		return result(cf.apply(Cf.of("cfsetospeed", termios, speed.value())), -1);
	}

	// <sys/mman.h>

	@Override
	public MemorySegment mmap(MemorySegment addr, CUnistd.size_t len, int prot, int flags, int fd,
		int off) {
		if (Memory.isNull(addr) || len.value() == 0) return error(CMman.MAP_FAILED, CErrNo.EINVAL);
		var result = mmap.apply(new Mmap(addr, len.value(), prot, flags, fd, off, true));
		if (!ok(result)) return error(CMman.MAP_FAILED, result.errNo());
		var memory = result.value();
		if (memory == null) memory = Memory.auto().allocate(len.value());
		return memory;
	}

	@Override
	public int munmap(MemorySegment addr, CUnistd.size_t len) {
		if (Memory.isNull(addr) || len.value() == 0) return error(-1, CErrNo.EINVAL);
		var result = mmap.apply(new Mmap(addr, len.value(), 0, 0, 0, 0, false));
		return result(0, -1, result.errNo());
	}

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
		general.get();
		return "Error message " + errnum;
	}

	// support

	private void remove(int... fds) {
		for (var fd : fds)
			this.openFds.remove(fd);
	}

	private int add(Fd fd) {
		allFds.put(fd.fd(), fd);
		openFds.add(fd.fd());
		return fd.fd();
	}

	private <T> T applyFd(int fd, T error, Functions.Function<Fd, T> op) {
		if (openFds.contains(fd)) return op.apply(fd(fd));
		return error(error, CErrNo.EBADF);
	}

	private int sigset(Pointer<CSignal.sigset_t> set, int signum, SigSet.Action action,
		Functions.ToIntFunction<SigSet.Mask> function) {
		var mask = SigSet.mask(set);
		var errNo = sigset.apply(new SigSet(mask, signum, action));
		if (!ok(errNo)) return error(-1, errNo);
		return function.applyAsInt(mask);
	}

	private int poll(Pointer<CPoll.pollfd> fds, Poll poll) {
		var errNo = this.poll.apply(poll);
		if (!ok(errNo)) return error(-1, errNo);
		int count = 0;
		for (var pollFd : poll.pollFds())
			if (pollFd.revents != 0) count++;
		fds.writeArray(poll.pollFds(), false);
		return count;
	}

	private static <T extends CTermios.termios<T>> Pointer<T> termios(MemorySegment memory) {
		return Reflect.unchecked(Os.info().mac ? CTermios.Mac.termios.$.pointer(memory) :
			CTermios.Linux.termios.$.pointer(memory));
	}

	private static boolean ok(Result<?> result) {
		return ok(result.errNo());
	}

	private static boolean ok(CErrNo errNo) {
		return errNo == null; // || !errNo.defined();
	}

	private static <T> T result(Result<T> result, T error) {
		return ok(result) ? result.value() : error(error, result.errNo());
	}

	private static <T> T result(T ok, T error, CErrNo errNo) {
		return ok(errNo) ? ok : error(error, errNo);
	}

	private static <T> T error(T result, CErrNo errNo) {
		LastError.set(errNo.code);
		return result;
	}
}
