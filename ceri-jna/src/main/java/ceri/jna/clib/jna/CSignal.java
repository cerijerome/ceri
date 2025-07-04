package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.reflect.CAnnotations.CInclude;
import ceri.jna.reflect.CAnnotations.CType;
import ceri.jna.reflect.CAnnotations.CUndefined;
import ceri.jna.type.Struct;
import ceri.jna.type.Struct.Fields;
import ceri.jna.util.PointerUtil;

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
	 * Sets a signal handler. Returns true if the result is not SIG_ERR.
	 */
	public static boolean signal(int signum, sighandler_t handler) throws CException {
		Pointer p = caller.callType(() -> lib().signal(signum, handler), "signal", signum, handler);
		return PointerUtil.peer(p) != SIG_ERR;
	}

	/**
	 * Sets a standard signal handler SIG_DFL or SIG_IGN. Returns true if the result is not SIG_ERR.
	 */
	public static boolean signal(int signum, int handler) throws CException {
		if (handler != SIG_DFL && handler != SIG_IGN)
			throw CException.of(CErrNo.EINVAL, "Only SIG_DFL or SIG_IGN allowed: %d", handler);
		Pointer p = caller.callType(() -> lib().signal(signum, new Pointer(handler)), "signal",
			signum, handler);
		return PointerUtil.peer(p) != SIG_ERR;
	}

	/**
	 * Send a signal to the caller.
	 */
	public static void raise(int sig) throws CException {
		caller.verify(() -> lib().raise(sig), "raise", sig);
	}

	/**
	 * Represents a sigset_t instance; underlying OS may use an integer type or struct.
	 */
	@CType(attrs = CType.Attr.typedef)
	@Fields({ "bytes" })
	public static class sigset_t extends Struct {
		public byte[] bytes = new byte[SIGSET_T_SIZE];
	}

	/**
	 * Initialize and empty a signal set
	 */
	public static sigset_t sigemptyset(sigset_t set) throws CException {
		caller.verify(() -> lib().sigemptyset(set.getPointer()), "sigemptyset", set);
		return set;
	}

	/**
	 * Add signal to the set.
	 */
	public static void sigaddset(sigset_t set, int signum) throws CException {
		caller.verify(() -> lib().sigaddset(set.getPointer(), signum), "sigaddset", set, signum);
	}

	/**
	 * Delete signal from the set.
	 */
	public static void sigdelset(sigset_t set, int signum) throws CException {
		caller.verify(() -> lib().sigdelset(set.getPointer(), signum), "sigdelset", set, signum);
	}

	/**
	 * Returns true if the set contains the signal.
	 */
	public static boolean sigismember(sigset_t set, int signum) throws CException {
		return caller.verifyInt(() -> lib().sigismember(set.getPointer(), signum), "sigismember",
			set, signum) == 1;
	}

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
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
