package ceri.ffm.clib.ffm;

import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.util.Arrays;
import ceri.common.data.Xcoder;
import ceri.common.util.Basics;
import ceri.common.util.Os;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.reflect.CAnnotations.CUndefined;
import ceri.ffm.type.Callback;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.Memory;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Struct;

/**
 * Types and functions from {@code <signal.h>}
 */
@CInclude("signal.h")
public enum CSignal {
	// Signal default actions:
	// Term = terminate the process
	// Ign = ignore the signal
	// Core = terminate the process and dump core
	// Stop = stop the process
	// Cont = continue the process if currently stopped

	/** Hangup detected on controlling terminal or death of controlling process (Term) */
	SIGHUP(1),
	/** Interrupt from keyboard (Term) */
	SIGINT(2),
	/** Quit from keyboard (Core) */
	SIGQUIT(3),
	/** Illegal instruction (Core) */
	SIGILL(4),
	/** Trace/breakpoint trap (Core) */
	SIGTRAP(5),
	/** Abort signal from abort() (Core) */
	SIGABRT(6),
	/** IOT trap; synonym for SIGABRT (Core) */
	SIGIOT(6),
	/** Bus error (bad memory access) (Core) */
	SIGBUS(Const.SIGBUS),
	/** Floating-point exception (Core) */
	SIGFPE(8),
	/** Kill signal; cannot be caught, blocked or ignored (Term) */
	SIGKILL(9),
	/** User-defined signal 1 (Term) */
	SIGUSR1(Const.SIGUSR1),
	/** Invalid memory reference (Core) */
	SIGSEGV(11),
	/** User-defined signal 2 (Term) */
	SIGUSR2(Const.SIGUSR2),
	/** Broken pipe: write to pipe with no readers (Term) */
	SIGPIPE(13),
	/** Timer signal from alarm() (Term) */
	SIGALRM(14),
	/** Termination signal (Term) */
	SIGTERM(15),
	/** Child stopped or terminated (Ign) */
	SIGCHLD(Const.SIGCHLD),
	/** Continue if stopped (Cont) */
	SIGCONT(Const.SIGCONT),
	/** Stop process; cannot be caught, blocked or ignored (Stop) */
	SIGSTOP(Const.SIGSTOP),
	/** Stop typed at terminal (Stop) */
	SIGTSTP(Const.SIGTSTP),
	/** Terminal input for background process (Stop) */
	SIGTTIN(21),
	/** Terminal output for background process (Stop) */
	SIGTTOU(22),
	/** Urgent condition on socket (Ign) */
	SIGURG(Const.SIGURG),
	/** CPU time limit exceeded (Core) */
	SIGXCPU(24),
	/** File size limit exceeded (Core) */
	SIGXFSZ(25),
	/** Virtual alarm clock (Term) */
	SIGVTALRM(26),
	/** Profiling timer expired (Term) */
	SIGPROF(27),
	/** Window resize signal (Ign) */
	SIGWINCH(28),
	/** I/O now possible (Term) */
	SIGIO(Const.SIGIO),
	/** Bad system call (Core) */
	SIGSYS(Const.SIGSYS);

	public static final Xcoder.Type<CSignal> xcoder = Xcoder.type(CSignal.class);
	public final int value;

	/**
	 * Holds a standard or non-standard signal value.
	 */
	public record Value(int value) {
		/**
		 * Returns the standard type if known.
		 */
		public CSignal signal() {
			return xcoder.decode(value());
		}

		@Override
		public final String toString() {
			return String.valueOf(Basics.def(signal(), value()));
		}
	}

	/**
	 * Built-in signal handler macros.
	 */
	@CUndefined
	public enum Macro {
		/** Error response */
		SIG_ERR(-1L),
		/** Default signal handler */
		SIG_DFL(0L),
		/** Ignore signal handler */
		SIG_IGN(1L);

		public static final Xcoder.Type<Macro> xcoder = Xcoder.type(Macro.class);
		public final long value;

		private Macro(long value) {
			this.value = value;
		}

		public MemorySegment pointer() {
			return MemorySegment.ofAddress(value);
		}
	}

	/**
	 * An encapsulation of macro and custom signal handlers.
	 */
	public record Result(MemorySegment pointer) {
		/** A no-op result. */
		public static final Result NULL = new Result(MemorySegment.NULL);

		/**
		 * Invokes the previous (non-macro) callback if set.
		 */
		@SuppressWarnings("resource")
		public boolean invoke(int signum) {
			var callback = callback();
			if (callback == null) return false;
			callback.invoke(signum);
			return true;
		}

		/**
		 * Returns the previous handler if non-null, and not a built-in macro handler.
		 */
		public sighandler_t callback() {
			return macro() != null ? null : Callback.callback(sighandler_t.class, pointer());
		}

		/**
		 * Returns the previous built-in macro handler if set.
		 */
		public Macro macro() {
			return Macro.xcoder.decode(pointer().address());
		}

		@SuppressWarnings("resource")
		@Override
		public String toString() {
			var macro = macro();
			return String.valueOf(macro != null ? macro : Callback.toString(callback()));
		}
	}

	/**
	 * A signal handler callback.
	 */
	// void (*sighandler_t)(int)
	public interface sighandler_t extends Callback {
		/**
		 * Calls the handler with the signal.
		 */
		void invoke(int signum);
	}

	/**
	 * Represents a sigset_t instance; underlying OS may use an integer type or struct.
	 */
	@CType(attrs = CType.Attr.typedef)
	@Fields({ "bytes" })
	public static class sigset_t extends Struct<sigset_t> {
		public static final Struct.Supporter<sigset_t> $ = Struct.support(sigset_t.class);
		/** Storage, intended to be opaque. */
		public byte[] bytes = new byte[Const.SIGSET_T_SIZE];
	}

	/**
	 * Sets a signal handler. Returns the previous handler.
	 */
	public static Result signal(int signum, sighandler_t handler) throws CException {
		return signal(signum, Callback.pointer(handler), handler);
	}

	/**
	 * Sets the signal handler to SIG_DFL. Returns the previous handler.
	 */
	public static Result signalDefault(int signum) throws CException {
		return signal(signum, Macro.SIG_DFL.pointer(), Macro.SIG_DFL);
	}

	/**
	 * Sets the signal handler to SIG_DFL. Returns the previous handler.
	 */
	public static Result signalIgnore(int signum) throws CException {
		return signal(signum, Macro.SIG_IGN.pointer(), Macro.SIG_IGN);
	}

	/**
	 * Send a signal to the executing process.
	 */
	public static void raise(int sig) throws CException {
		CLib.caller.call(c -> {
			if (c.lib().raise(sig) != 0) c.verify();
		}, m -> m.accept("raise", new Value(sig)));
	}

	/**
	 * Initializes a signal set.
	 */
	public static Pointer<sigset_t> sigset(CSignal... signals) throws CException {
		return sigset(Memory.auto(), signals);
	}

	/**
	 * Initializes a signal set.
	 */
	public static Pointer<sigset_t> sigset(Iterable<CSignal> signals) throws CException {
		return sigset(Memory.auto(), signals);
	}

	/**
	 * Initializes a signal set.
	 */
	public static Pointer<sigset_t> sigset(SegmentAllocator allocator, CSignal... signals)
		throws CException {
		return sigset(allocator, Arrays.asList(signals));
	}

	/**
	 * Initializes a signal set.
	 */
	public static Pointer<sigset_t> sigset(SegmentAllocator allocator, Iterable<CSignal> signals)
		throws CException {
		if (signals == null) return null;
		var sigset = sigemptyset(sigset_t.$.pointer(allocator));
		for (var signal : signals)
			if (signal != null) signal.add(sigset);
		return sigset;
	}

	/**
	 * Initializes a signal set.
	 */
	public static Pointer<sigset_t> sigemptyset(Pointer<sigset_t> set) throws CException {
		if (set != null) CLib.caller.verifyInt(lib -> lib.sigemptyset(set), -1, "sigemptyset", set);
		return set;
	}

	/**
	 * Adds the signal number to the set.
	 */
	public static Pointer<sigset_t> sigaddset(Pointer<sigset_t> set, int signum) throws CException {
		if (set != null)
			CLib.caller.verifyInt(lib -> lib.sigaddset(set, signum), -1, "sigaddset", set, signum);
		return set;
	}

	/**
	 * Deletes the signal number from the set.
	 */
	public static Pointer<sigset_t> sigdelset(Pointer<sigset_t> set, int signum) throws CException {
		if (set != null)
			CLib.caller.verifyInt(lib -> lib.sigdelset(set, signum), -1, "sigdelset", set, signum);
		return set;
	}

	/**
	 * Returns true if the set contains the signal number.
	 */
	public static boolean sigismember(Pointer<sigset_t> set, int signum) throws CException {
		if (set == null) return false;
		return CLib.caller.verifyInt(lib -> lib.sigismember(set, signum), -1, "sigismember", set,
			signum) == 1;
	}

	private CSignal(int value) {
		this.value = value;
	}

	/**
	 * Send this signal to the executing process.
	 */
	public void raise() throws CException {
		raise(value);
	}

	/**
	 * Invokes the handler with this signal.
	 */
	public void invoke(sighandler_t handler) {
		if (handler != null) handler.invoke(value);
	}

	/**
	 * Invokes the handler with this signal.
	 */
	public void invoke(Result result) {
		if (result != null) result.invoke(value);
	}

	/**
	 * Sets a signal handler. Returns the previous handler.
	 */
	public Result set(sighandler_t handler) throws CException {
		return signal(value, handler);
	}

	/**
	 * Sets this signal's handler to SIG_IGN. Returns the previous handler.
	 */
	public Result setIgnore() throws CException {
		return signalIgnore(value);
	}

	/**
	 * Sets this signal's handler to SIG_DFL. Returns the previous handler.
	 */
	public Result setDefault() throws CException {
		return signalDefault(value);
	}

	/**
	 * Adds this signal to the set.
	 */
	public Pointer<sigset_t> add(Pointer<sigset_t> set) throws CException {
		return sigaddset(set, value);
	}

	/**
	 * Deletes this signal from the set.
	 */
	public Pointer<sigset_t> delete(Pointer<sigset_t> set) throws CException {
		return sigdelset(set, value);
	}

	/**
	 * Returns true if the set contains this signal.
	 */
	public boolean isMember(Pointer<sigset_t> set) throws CException {
		return sigismember(set, value);
	}

	// support

	private static Result signal(int signum, MemorySegment handler, Object arg) throws CException {
		if (handler == null) return null;
		return CLib.caller.callType(c -> {
			var previous = new Result(c.lib().signal(signum, handler));
			if (previous.macro() == Macro.SIG_ERR) c.verify();
			return previous;
		}, m -> m.accept("signal", new Value(signum), arg));
	}

	// os-specific initialization

	private static class Const {
		private static final int SIGSET_T_SIZE;
		private static final int SIGBUS;
		private static final int SIGUSR1;
		private static final int SIGUSR2;
		private static final int SIGCHLD;
		private static final int SIGCONT;
		private static final int SIGSTOP;
		private static final int SIGTSTP;
		private static final int SIGURG;
		private static final int SIGIO;
		private static final int SIGSYS;

		static {
			if (Os.info().mac) {
				SIGSET_T_SIZE = 4;
				SIGBUS = 10;
				SIGUSR1 = 30;
				SIGUSR2 = 31;
				SIGCHLD = 20;
				SIGCONT = 19;
				SIGSTOP = 17;
				SIGTSTP = 18;
				SIGURG = 16;
				SIGIO = 23;
				SIGSYS = 12;
			} else {
				SIGSET_T_SIZE = 128;
				SIGBUS = 7;
				SIGUSR1 = 10;
				SIGUSR2 = 12;
				SIGCHLD = 17;
				SIGCONT = 18;
				SIGSTOP = 19;
				SIGTSTP = 20;
				SIGURG = 23;
				SIGIO = 29;
				SIGSYS = 31;
			}
		}
	}
}
