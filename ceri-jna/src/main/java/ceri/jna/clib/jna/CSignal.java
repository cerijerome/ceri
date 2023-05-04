package ceri.jna.clib.jna;

import static ceri.jna.clib.jna.CLib.caller;
import static ceri.jna.clib.jna.CLib.lib;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;
import ceri.common.util.OsUtil;
import ceri.jna.util.PointerUtil;

/**
 * Types and functions from {@code <signal.h>}
 */
public class CSignal {
	public static final int SIGHUP = 1;
	public static final int SIGINT = 2;
	public static final int SIGQUIT = 3;
	public static final int SIGILL = 4;
	public static final int SIGTRAP = 5;
	public static final int SIGABRT = 6;
	public static final int SIGIOT = 6;
	public static final int SIGBUS;
	public static final int SIGFPE = 8;
	public static final int SIGKILL = 9;
	public static final int SIGUSR1;
	public static final int SIGSEGV = 11;
	public static final int SIGUSR2;
	public static final int SIGPIPE = 13;
	public static final int SIGALRM = 14;
	public static final int SIGTERM = 15;
	public static final int SIG_DFL = 0;
	public static final int SIG_IGN = 1;
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
		if (handler < SIG_DFL || handler > SIG_IGN)
			throw CException.of(CError.EINVAL, "Only SIG_DFL or SIG_IGN allowed: %d", handler);
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

	/* os-specific initialization */

	static {
		if (OsUtil.os().mac) {
			SIGBUS = 10;
			SIGUSR1 = 30;
			SIGUSR2 = 31;
		} else {
			SIGBUS = 7;
			SIGUSR1 = 10;
			SIGUSR2 = 12;
		}
	}

}
