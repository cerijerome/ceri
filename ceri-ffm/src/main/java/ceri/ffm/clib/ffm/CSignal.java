package ceri.ffm.clib.ffm;

import java.lang.foreign.MemorySegment;
import ceri.common.util.Os;
import ceri.ffm.reflect.CAnnotations.CInclude;
import ceri.ffm.reflect.CAnnotations.CType;
import ceri.ffm.reflect.CAnnotations.CUndefined;
import ceri.ffm.type.Callback;
import ceri.ffm.type.Group.Fields;
import ceri.ffm.type.Pointer;
import ceri.ffm.type.Struct;

/**
 * Types and functions from {@code <signal.h>}
 */
@CInclude("signal.h")
public class CSignal {
	private static final int SIGSET_T_SIZE;

	// Signal default actions:
	// Term = terminate the process
	// Ign = ignore the signal
	// Core = terminate the process and dump core
	// Stop = stop the process
	// Cont = continue the process if currently stopped

	/** Hangup detected on controlling terminal or death of controlling process (Term) */
	public static final int SIGHUP = 1;
	/** Interrupt from keyboard (Term) */
	public static final int SIGINT = 2;
	/** Quit from keyboard (Core) */
	public static final int SIGQUIT = 3;
	/** Illegal instruction (Core) */
	public static final int SIGILL = 4;
	/** Trace/breakpoint trap (Core) */
	public static final int SIGTRAP = 5;
	/** Abort signal from abort() (Core) */
	public static final int SIGABRT = 6;
	/** IOT trap; synonym for SIGABRT (Core) */
	public static final int SIGIOT = 6;
	/** Bus error (bad memory access) (Core) */
	public static final int SIGBUS;
	/** Floating-point exception (Core) */
	public static final int SIGFPE = 8;
	/** Kill signal; cannot be caught, blocked or ignored (Term) */
	public static final int SIGKILL = 9;
	/** User-defined signal 1 (Term) */
	public static final int SIGUSR1;
	/** Invalid memory reference (Core) */
	public static final int SIGSEGV = 11;
	/** User-defined signal 2 (Term) */
	public static final int SIGUSR2;
	/** Broken pipe: write to pipe with no readers (Term) */
	public static final int SIGPIPE = 13;
	/** Timer signal from alarm() (Term) */
	public static final int SIGALRM = 14;
	/** Termination signal (Term) */
	public static final int SIGTERM = 15;
	/** Child stopped or terminated (Ign) */
	public static final int SIGCHLD;
	/** Continue if stopped (Cont) */
	public static final int SIGCONT;
	/** Stop process; cannot be caught, blocked or ignored (Stop) */
	public static final int SIGSTOP;
	/** Stop typed at terminal (Stop) */
	public static final int SIGTSTP;
	/** Terminal input for background process (Stop) */
	public static final int SIGTTIN = 21;
	/** Terminal output for background process (Stop) */
	public static final int SIGTTOU = 22;
	/** Urgent condition on socket (Ign) */
	public static final int SIGURG;
	/** CPU time limit exceeded (Core) */
	public static final int SIGXCPU = 24;
	/** File size limit exceeded (Core) */
	public static final int SIGXFSZ = 25;
	/** Virtual alarm clock (Term) */
	public static final int SIGVTALRM = 26;
	/** Profiling timer expired (Term) */
	public static final int SIGPROF = 27;
	/** Window resize signal (Ign) */
	public static final int SIGWINCH = 28;
	/** I/O now possible (Term) */
	public static final int SIGIO;
	/** Bad system call (Core) */
	public static final int SIGSYS;
	/** Default signal handler */
	@CUndefined // cast to pointer
	public static final int SIG_DFL = 0;
	/** Ignore signal handler */
	@CUndefined // cast to pointer
	public static final int SIG_IGN = 1;
	/** Error response */
	@CUndefined // cast to pointer
	public static final int SIG_ERR = -1;

	private CSignal() {}

	// void (*sighandler_t)(int)
	public static interface sighandler_t extends Callback {
		void invoke(int signum);
	}

	/**
	 * An encapsulation of system and custom signal handlers.
	 */
	@CUndefined
	public static class sighandler {
		public static final sighandler ERR = new sighandler(MemorySegment.ofAddress(SIG_ERR));
		public static final sighandler IGN = new sighandler(MemorySegment.ofAddress(SIG_IGN));
		public static final sighandler DFL = new sighandler(MemorySegment.ofAddress(SIG_DFL));
		public final MemorySegment pointer;

		private sighandler(MemorySegment pointer) {
			this.pointer = pointer;
		}

		@SuppressWarnings("resource")
		public boolean invoke(int signum) {
			var callback = callback();
			if (callback == null) return false;
			callback.invoke(signum);
			return true;
		}

		private sighandler_t callback() {
			return isCallback() ? Callback.callback(sighandler_t.class, pointer) : null;
		}

		public boolean isCallback() {
			long address = pointer.address();
			return address != SIG_ERR && address != SIG_IGN && address != SIG_DFL;
		}

		public boolean isDefault() {
			return pointer.address() == SIG_DFL;
		}

		public boolean isIgnore() {
			return pointer.address() == SIG_IGN;
		}

		public boolean isError() {
			return pointer.address() == SIG_ERR;
		}

		@SuppressWarnings("resource")
		@Override
		public String toString() {
			if (isDefault()) return "SIG_DFL";
			if (isIgnore()) return "SIG_IGN";
			if (isError()) return "SIG_ERR";
			return Callback.toString(callback());
		}
	}

	/**
	 * Sets a signal handler. Returns true if the result is not SIG_ERR.
	 */
	public static sighandler signal(int signum, sighandler_t handler) throws CException {
		return signal(signum, Callback.pointer(handler), handler);
	}

	/**
	 * Sets a standard signal handler SIG_DFL or SIG_IGN. Returns true if the result is not SIG_ERR.
	 */
	public static sighandler signal(int signum, int handler) throws CException {
		if (handler != SIG_DFL && handler != SIG_IGN)
			throw CErrNo.EINVAL.error("Only SIG_DFL or SIG_IGN allowed: %d", handler);
		return signal(signum, MemorySegment.ofAddress(handler), handler);
	}

	/**
	 * Send a signal to the caller.
	 */
	public static void raise(int sig) throws CException {
		CLib.caller.callInt(c -> c.lib().raise(sig), "raise", sig);
	}

	/**
	 * Represents a sigset_t instance; underlying OS may use an integer type or struct.
	 */
	@CType(attrs = CType.Attr.typedef)
	@Fields({ "bytes" })
	public static class sigset_t extends Struct<sigset_t> {
		public static final Struct.Supporter<sigset_t> $ = Struct.support(sigset_t.class);
		public byte[] bytes = new byte[SIGSET_T_SIZE];
	}

	/**
	 * Initializes a signal set.
	 */
	public static Pointer<sigset_t> sigemptyset(Pointer<sigset_t> set) throws CException {
		CLib.caller.verifyInt(lib -> lib.sigemptyset(set), -1, "sigemptyset", set);
		return set;
	}

	// /**
	// * Add signal to the set.
	// */
	// public static void sigaddset(sigset_t set, int signum) throws CException {
	// caller.verify(() -> lib().sigaddset(set.getPointer(), signum), "sigaddset", set, signum);
	// }
	//
	// /**
	// * Delete signal from the set.
	// */
	// public static void sigdelset(sigset_t set, int signum) throws CException {
	// caller.verify(() -> lib().sigdelset(set.getPointer(), signum), "sigdelset", set, signum);
	// }
	//
	// /**
	// * Returns true if the set contains the signal.
	// */
	// public static boolean sigismember(sigset_t set, int signum) throws CException {
	// return caller.verifyInt(() -> lib().sigismember(set.getPointer(), signum), "sigismember",
	// set, signum) == 1;
	// }

	// support

	private static sighandler signal(int signum, MemorySegment handler, Object arg)
		throws CException {
		return CLib.caller.callType(c -> {
			var previous = new sighandler(c.lib().signal(signum, handler));
			if (previous.isError()) c.lastError();
			return previous;
		}, "signal", signum, arg);
	}

	// os-specific initialization

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
