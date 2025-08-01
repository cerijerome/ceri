package ceri.jna.clib.test;

import static ceri.common.test.AssertUtil.assertTrue;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import com.sun.jna.LastErrorException;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import ceri.common.array.ArrayUtil;
import ceri.common.collection.ImmutableUtil;
import ceri.common.data.ByteProvider;
import ceri.common.data.ByteUtil;
import ceri.common.reflect.ReflectUtil.ThreadElement;
import ceri.common.test.CallSync;
import ceri.common.test.TestUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.BasicUtil;
import ceri.common.util.Enclosure;
import ceri.jna.clib.jna.CErrNo;
import ceri.jna.clib.jna.CFcntl;
import ceri.jna.clib.jna.CLib;
import ceri.jna.clib.jna.CPoll.pollfd;
import ceri.jna.clib.jna.CSignal.sighandler_t;
import ceri.jna.clib.jna.CTermios;
import ceri.jna.clib.jna.CTermios.speed_t;
import ceri.jna.clib.jna.CTime;
import ceri.jna.clib.jna.CUnistd;
import ceri.jna.clib.jna.CUnistd.size_t;
import ceri.jna.clib.jna.CUnistd.ssize_t;
import ceri.jna.test.JnaTestUtil;
import ceri.jna.type.CUlong;
import ceri.jna.type.Struct;
import ceri.jna.util.JnaLibrary;
import ceri.jna.util.JnaUtil;

/**
 * Test implementation for CLib native interface.
 */
public class TestCLibNative implements CLib.Native {
	private AtomicInteger nextFd = new AtomicInteger();
	public final Set<Integer> fds = ConcurrentHashMap.newKeySet();
	public final Map<Integer, FdContext> fdContext = new ConcurrentHashMap<>();
	public final Map<String, String> env = new ConcurrentHashMap<>();
	public final CallSync.Consumer<OpenArgs> open = CallSync.consumer(null, true);
	public final CallSync.Function<Integer, Integer> close = CallSync.function(null, 0);
	public final CallSync.Function<Integer, Integer> isatty = CallSync.function(null, 0);
	public final CallSync.Function<int[], Integer> pipe = CallSync.function(null, 0);
	public final CallSync.Function<ReadArgs, ByteProvider> read =
		CallSync.function(null, ByteProvider.empty());
	public final CallSync.Function<WriteArgs, Integer> write = CallSync.function(null, 0);
	public final CallSync.Function<LseekArgs, Integer> lseek = CallSync.function(null, 0);
	public final CallSync.Supplier<Integer> pagesize = CallSync.supplier(0x1000); // 4k
	public final CallSync.Function<SignalArgs, Pointer> signal =
		CallSync.function(null, Pointer.NULL);
	public final CallSync.Function<Integer, Integer> sigset = CallSync.function(null, 0);
	public final CallSync.Function<Integer, Integer> raise = CallSync.function(null, 0);
	public final CallSync.Function<PollArgs, Integer> poll = CallSync.function(null, 0);
	public final CallSync.Function<CtlArgs, Integer> ioctl = CallSync.function(null, 0);
	public final CallSync.Function<CtlArgs, Integer> fcntl = CallSync.function(null, 0);
	public final CallSync.Function<TcArgs, Integer> tc = CallSync.function(null, 0);
	public final CallSync.Function<CfArgs, Integer> cf = CallSync.function(null, 0);
	public final CallSync.Function<MmapArgs, Presult> mmap = CallSync.function(null, Presult.OK);
	private volatile int lastFd = -1;

	/**
	 * A result type of pointer or int.
	 */
	public record Presult(Pointer p, int result) {
		public static final Presult OK = new Presult(null, 0);
	}

	/**
	 * Arguments for open calls.
	 */
	public record OpenArgs(String path, int flags, int mode) {

		public static final OpenArgs NULL = new OpenArgs("", 0, 0);

		@Override
		public final String toString() {
			return String.format("%s,0x%x,0%s", path, flags, Integer.toOctalString(mode));
		}
	}

	/**
	 * Fd context info for debugging closed fds.
	 */
	public record FdContext(int fd, OpenArgs args, ThreadElement origin) {
		public static FdContext of(int fd, OpenArgs args) {
			return new FdContext(fd, args, TestUtil.findTest());
		}

		@Override
		public final String toString() {
			return fd + ":" + args() + "/" + origin();
		}
	}

	/**
	 * Arguments for read calls.
	 */
	public record ReadArgs(int fd, int len) {}

	/**
	 * Arguments for write calls.
	 */
	public record WriteArgs(int fd, ByteProvider data) {
		public static WriteArgs of(int fd, int... data) {
			return new WriteArgs(fd, ByteProvider.of(data));
		}
	}

	/**
	 * Arguments for lseek calls.
	 */
	public record LseekArgs(int fd, int offset, int whence) {}

	/**
	 * Arguments for signal calls. Handler type must be sighandler_t or Pointer.
	 */
	public record SignalArgs(int signal, Object handler) {
		public SignalArgs {
			assertTrue(handler instanceof sighandler_t || handler instanceof Pointer);
		}
	}

	/**
	 * Arguments for poll calls.
	 */
	public record PollArgs(List<pollfd> pollfds, Duration timeout, Set<Integer> signals) {
		public static PollArgs of(pollfd[] pollfds, int timeoutMs) {
			return new PollArgs(List.of(pollfds), Duration.ofMillis(timeoutMs), null);
		}

		public static PollArgs of(pollfd[] pollfds, long timeoutNs, int... signals) {
			return new PollArgs(List.of(pollfds), Duration.ofNanos(timeoutNs),
				ImmutableUtil.intSet(signals));
		}

		/**
		 * Provides the pollfd at index.
		 */
		public pollfd pollfd(int i) {
			return pollfds().get(i);
		}

		/**
		 * Writes pollfds to memory, and returns the count of pollfds with any revents.
		 */
		public int write() {
			int count = 0;
			for (var pollfd : pollfds())
				if (Struct.write(pollfd).revents != 0) count++;
			return count;
		}
	}

	/**
	 * Arguments for ioctl and fcntl calls.
	 */
	public record CtlArgs(int fd, int request, List<Object> args) {
		public static CtlArgs of(int fd, int request, Object... args) {
			return new CtlArgs(fd, request, List.of(args));
		}

		/**
		 * Provide vararg argument as a typed object.
		 */
		public <T> T arg(int i) {
			return BasicUtil.unchecked(args().get(i));
		}
	}

	/**
	 * Arguments for tc calls.
	 */
	public record TcArgs(String name, int fd, List<Object> args) {
		public static TcArgs of(String name, int fd, Object... args) {
			return new TcArgs(name, fd, List.of(args));
		}

		/**
		 * Provide vararg argument as a typed object.
		 */
		public <T> T arg(int i) {
			return BasicUtil.unchecked(args().get(i));
		}
	}

	/**
	 * Arguments for cf calls.
	 */
	public record CfArgs(String name, Pointer termios, List<Object> args) {
		public static CfArgs of(String name, Pointer termios, Object... args) {
			return new CfArgs(name, termios, List.of(args));
		}

		/**
		 * Returns a copy of the Linux termios struct.
		 */
		public CTermios.Linux.termios termiosLinux() {
			return Struct.copyFrom(termios, new CTermios.Linux.termios());
		}

		/**
		 * Returns a copy of the Mac termios struct.
		 */
		public CTermios.Mac.termios termiosMac() {
			return Struct.copyFrom(termios, new CTermios.Mac.termios());
		}

		/**
		 * Provide vararg argument as a typed object.
		 */
		public <T> T arg(int i) {
			return BasicUtil.unchecked(args().get(i));
		}
	}

	/**
	 * Parameters for mmap/munmap.
	 */
	public record MmapArgs(Pointer addr, long len, int prot, int flags, int fd, int offset) {}

	/**
	 * A wrapper for repeatedly overriding the library in tests.
	 */
	public static JnaLibrary.Ref<TestCLibNative> ref() {
		return CLib.library.ref(TestCLibNative::of);
	}

	/**
	 * Register a new test lib.
	 */
	public static Enclosure<TestCLibNative> register() {
		return CLib.library.enclosed(of());
	}

	/**
	 * Set last error on a call sync, based on predicate.
	 */
	public static <T, R> void autoError(CallSync.Function<T, R> sync, R response,
		Predicate<T> predicate, String errorMessage, Object... args) {
		autoError(sync, response, predicate, _ -> StringUtil.format(errorMessage, args));
	}

	/**
	 * Set last error on a call sync, based on predicate.
	 */
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
	 * Pass poll arguments to a consumer, write structs to memory, and set auto response to the
	 * number of pollfds with non-zero revents.
	 */
	public void pollAuto(Consumer<PollArgs> consumer) {
		poll.autoResponse(args -> {
			consumer.accept(args);
			return args.write();
		});
	}

	/**
	 * Clear fds and call-sync states.
	 */
	public void reset() {
		nextFd.set(1000);
		fds.clear();
		fdContext.clear();
		env.clear();
		CallSync.resetAll(cf, close, fcntl, ioctl, isatty, lseek, pagesize, mmap, open, pipe, poll,
			raise, read, signal, sigset, tc, write);
		fds.add(CUnistd.STDIN_FILENO);
		fds.add(CUnistd.STDOUT_FILENO);
		fds.add(CUnistd.STDERR_FILENO);
	}

	@Override
	public int open(String path, int flags, Object... args) {
		var openArgs = new OpenArgs(path, flags, args.length == 0 ? 0 : (int) args[0]);
		var fd = createFd(openArgs);
		open.accept(openArgs);
		return fd;
	}

	@Override
	public int close(int fd) {
		int result = close.apply(fd(fd));
		fds.remove(fd);
		return result;
	}

	@Override
	public int isatty(int fd) {
		return isatty.apply(fd(fd));
	}

	@Override
	public int pipe(int[] pipefd) {
		var fr = createFd(new OpenArgs("pipe:r", CFcntl.O_RDONLY, 0));
		var fw = createFd(new OpenArgs("pipe:w", CFcntl.O_WRONLY, 0));
		int result = pipe.apply(new int[] { fr, fw });
		pipefd[0] = fr;
		pipefd[1] = fw;
		return result;
	}

	@Override
	public ssize_t read(int fd, Pointer buffer, size_t len) {
		ByteProvider data = read.apply(new ReadArgs(fd(fd), len.intValue()));
		if (data == null || data.length() == 0) return new ssize_t(0);
		int n = Math.min(data.length(), len.intValue());
		JnaUtil.write(buffer, data.copy(0), 0, n);
		return new ssize_t(n);
	}

	@Override
	public ssize_t write(int fd, Pointer buffer, size_t len) {
		byte[] bytes = new byte[len.intValue()];
		if (buffer != null) JnaUtil.read(buffer, bytes);
		int n = write.apply(new WriteArgs(fd(fd), ByteProvider.of(bytes)));
		return new ssize_t(n);
	}

	@Override
	public int lseek(int fd, int offset, int whence) {
		return lseek.apply(new LseekArgs(fd(fd), offset, whence));
	}

	@Override
	public int getpagesize() {
		return pagesize.get();
	}

	@Override
	public Pointer signal(int signum, sighandler_t handler) {
		return signal.apply(new SignalArgs(signum, handler));
	}

	@Override
	public Pointer signal(int signum, Pointer handler) {
		return signal.apply(new SignalArgs(signum, handler));
	}

	@Override
	public int raise(int sig) {
		return raise.apply(sig);
	}

	@Override
	public int sigemptyset(Pointer set) {
		return sigset(set, 0);
	}

	@Override
	public int sigaddset(Pointer set, int signum) {
		return sigset(set, ByteUtil.applyBitsInt(set.getInt(0), true, signum));
	}

	@Override
	public int sigdelset(Pointer set, int signum) {
		return sigset(set, ByteUtil.applyBitsInt(set.getInt(0), false, signum));
	}

	@Override
	public int sigismember(Pointer set, int signum) {
		int mask = set.getInt(0);
		int result = sigset.apply(mask);
		if (result == 0) return ByteUtil.bit(mask, signum) ? 1 : 0;
		return result;
	}

	@Override
	public int poll(Pointer fds, int nfds, int timeout) {
		var array = Struct.arrayByVal(fds, pollfd::new, pollfd[]::new, nfds);
		return poll.apply(PollArgs.of(array, timeout));
	}

	@Override
	public int ppoll(Pointer fds, int nfds, Pointer tmo_p, Pointer sigmask)
		throws LastErrorException {
		var array = Struct.arrayByVal(fds, pollfd::new, pollfd[]::new, nfds);
		var tmo = Struct.read(new CTime.timespec(tmo_p));
		var d = Duration.ofSeconds(tmo.tv_sec.longValue(), tmo.tv_nsec.longValue());
		return poll.apply(PollArgs.of(array, d.toNanos(), signals(sigmask)));
	}

	private static int[] signals(Pointer sigmask) {
		if (sigmask == null) return ArrayUtil.ints.empty;
		return ByteUtil.bits(sigmask.getInt(0));
	}

	@Override
	public int ioctl(int fd, CUlong request, Object... objs) {
		return ioctl.apply(CtlArgs.of(fd(fd), request.intValue(), objs));
	}

	@Override
	public int fcntl(int fd, int cmd, Object... objs) {
		return fcntl.apply(CtlArgs.of(fd(fd), cmd, objs));
	}

	@Override
	public int tcgetattr(int fd, Pointer termios) {
		return tc.apply(TcArgs.of("tcgetattr", fd(fd), termios));
	}

	@Override
	public int tcsetattr(int fd, int optional_actions, Pointer termios) {
		return tc.apply(TcArgs.of("tcsetattr", fd(fd), optional_actions, termios));
	}

	@Override
	public int tcsendbreak(int fd, int duration) {
		return tc.apply(TcArgs.of("tcsendbreak", fd(fd), duration));
	}

	@Override
	public int tcdrain(int fd) {
		return tc.apply(TcArgs.of("tcdrain", fd(fd)));
	}

	@Override
	public int tcflush(int fd, int queue_selector) {
		return tc.apply(TcArgs.of("tcflush", fd(fd), queue_selector));
	}

	@Override
	public int tcflow(int fd, int action) {
		return tc.apply(TcArgs.of("tcflow", fd(fd), action));
	}

	@Override
	public void cfmakeraw(Pointer termios) {
		cf.apply(CfArgs.of("cfmakeraw", termios));
	}

	@Override
	public speed_t cfgetispeed(Pointer termios) {
		return new speed_t(cf.apply(CfArgs.of("cfgetispeed", termios)));
	}

	@Override
	public speed_t cfgetospeed(Pointer termios) {
		return new speed_t(cf.apply(CfArgs.of("cfgetospeed", termios)));
	}

	@Override
	public int cfsetispeed(Pointer termios, speed_t speed) {
		return cf.apply(CfArgs.of("cfsetispeed", termios, speed.intValue()));
	}

	@Override
	public int cfsetospeed(Pointer termios, speed_t speed) {
		return cf.apply(CfArgs.of("cfsetospeed", termios, speed.intValue()));
	}

	@Override
	public Pointer mmap(Pointer addr, size_t len, int prot, int flags, int fd, int offset)
		throws LastErrorException {
		var presult = mmap.apply(new MmapArgs(addr, len.longValue(), prot, flags, fd, offset));
		if (presult.p() != null) return presult.p();
		return addr != null ? addr : new Memory(len.longValue());
	}

	@Override
	public int munmap(Pointer addr, size_t len) {
		return mmap.apply(new MmapArgs(addr, len.longValue(), 0, 0, 0, 0)).result();
	}

	@Override
	public int setenv(String name, String value, int overwrite) {
		if (overwrite != 0) env.put(name, value);
		else env.putIfAbsent(name, value);
		return 0;
	}

	@Override
	public String getenv(String name) {
		return env.get(name);
	}

	public int lastFd() {
		return lastFd;
	}

	public int fd(int fd) {
		return fd(fd, CErrNo.EBADF);
	}

	protected int fd(int fd, int errorCode) {
		if (fds.contains(fd)) return fd;
		throw JnaTestUtil.lastError(errorCode, String.valueOf(fdContext.get(fd)));
	}

	private int sigset(Pointer set, int mask) {
		int result = sigset.apply(mask);
		if (result == 0) set.setInt(0, mask);
		return result;
	}

	private int createFd(OpenArgs open) {
		int fd = nextFd.getAndIncrement();
		fds.add(fd);
		fdContext.put(fd, FdContext.of(fd, open));
		lastFd = fd;
		return fd;
	}
}
