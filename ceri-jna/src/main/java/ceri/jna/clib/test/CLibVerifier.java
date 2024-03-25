package ceri.jna.clib.test;

import java.io.IOException;
import java.io.PrintStream;
import org.apache.logging.log4j.Level;
import ceri.common.test.CallSync;
import ceri.common.text.StringUtil;
import ceri.common.util.OsUtil;
import ceri.jna.clib.Signal;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.util.JnaLibrary;
import ceri.log.test.LogModifier;

/**
 * Verification logic to run on a target system.
 */
public class CLibVerifier {
	private final PrintStream out;
	private final PrintStream err;
	private int errors = 0;

	public static void main(String[] args) throws IOException {
		LogModifier.run(() -> {
			new CLibVerifier(System.out, System.err).run();
		}, Level.OFF, JnaLibrary.class);
	}

	private CLibVerifier(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}

	private void run() throws IOException {
		errors = 0;
		out("CLib JNA system check");
		var os = OsUtil.os();
		out("OS: " + os.full());
		// test mode_t

		// test ioctl

		// test nfds_t

		verifySignal();
		verifySigset();
		err("hello");
		if (errors > 0) err.println("Total errors = " + errors);
	}

	private void verifySignal() throws IOException {
		var sync = CallSync.consumer(0, true);
		if (!CSignal.signal(CSignal.SIGUSR1, signal -> {
			sync.accept(signal);
			out("%d received", signal);
		})) err("signal(SIGUSR1, ...) failed");
		CSignal.raise(CSignal.SIGUSR1);
		if (!CSignal.signal(CSignal.SIGUSR1, CSignal.SIG_DFL)) err("signal(SIGUSR1, ...) failed");

	}

	private void verifySigset() throws IOException {
		var sigset = new CSignal.sigset_t();
		CSignal.sigemptyset(sigset);
		for (var signal : Signal.values()) {
			int signum = signal.signal;
			CSignal.sigaddset(sigset, signum);
			if (!CSignal.sigismember(sigset, signum))
				err("sigismember(sigset, %s) = false", signal);
			CSignal.sigdelset(sigset, signum);
			if (CSignal.sigismember(sigset, signum)) err("sigismember(sigset, %s) = true", signal);
		}
	}

	private void out(String format, Object... args) {
		out.println(StringUtil.format(format, args));
	}

	private void err(String format, Object... args) {
		err.println("ERROR: " + StringUtil.format(format, args));
		errors++;
	}
}
