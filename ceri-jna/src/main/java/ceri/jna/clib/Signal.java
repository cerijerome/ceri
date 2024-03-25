package ceri.jna.clib;

import java.io.IOException;
import java.lang.ref.Reference;
import java.util.function.Consumer;
import ceri.common.collection.EnumUtil;
import ceri.common.collection.StreamUtil;
import ceri.common.data.MaskTranscoder;
import ceri.common.data.TypeTranscoder;
import ceri.common.function.ExceptionCloseable;
import ceri.jna.clib.jna.CSignal;

public enum Signal {
	SIGHUP(CSignal.SIGHUP),
	SIGINT(CSignal.SIGINT),
	SIGQUIT(CSignal.SIGQUIT),
	SIGILL(CSignal.SIGILL),
	SIGTRAP(CSignal.SIGTRAP),
	SIGABRT(CSignal.SIGABRT),
	SIGIOT(CSignal.SIGIOT),
	SIGBUS(CSignal.SIGBUS),
	SIGFPE(CSignal.SIGFPE),
	SIGKILL(CSignal.SIGKILL),
	SIGUSR1(CSignal.SIGUSR1),
	SIGSEGV(CSignal.SIGSEGV),
	SIGUSR2(CSignal.SIGUSR2),
	SIGPIPE(CSignal.SIGPIPE),
	SIGALRM(CSignal.SIGALRM),
	SIGTERM(CSignal.SIGTERM),
	SIGCHLD(CSignal.SIGCHLD),
	SIGCONT(CSignal.SIGCONT),
	SIGSTOP(CSignal.SIGSTOP),
	SIGTSTP(CSignal.SIGTSTP),
	SIGTTIN(CSignal.SIGTTIN),
	SIGTTOU(CSignal.SIGTTOU),
	SIGURG(CSignal.SIGURG),
	SIGXCPU(CSignal.SIGXCPU),
	SIGXFSZ(CSignal.SIGXFSZ),
	SIGVTALRM(CSignal.SIGVTALRM),
	SIGPROF(CSignal.SIGPROF),
	SIGWINCH(CSignal.SIGWINCH),
	SIGIO(CSignal.SIGIO),
	SIGPOLL(CSignal.SIGPOLL),
	SIGSYS(CSignal.SIGSYS);

	private static final TypeTranscoder<Signal> xcoder = TypeTranscoder.of(t -> t.signal,
		MaskTranscoder.NULL, EnumUtil.enums(Signal.class), StreamUtil.mergeFirst());
	public final int signal;

	public static Signal from(int signal) {
		return xcoder.decode(signal);
	}

	private Signal(int signal) {
		this.signal = signal;
	}

	public void signalDefault() throws IOException {
		verifyHandler(CSignal.signal(signal, CSignal.SIG_DFL));
	}

	public void signalIgnore() throws IOException {
		verifyHandler(CSignal.signal(signal, CSignal.SIG_IGN));
	}

	public ExceptionCloseable<IOException> signal(Consumer<Signal> handler) throws IOException {
		CSignal.sighandler_t callback = signum -> handler.accept(from(signum));
		verifyHandler(CSignal.signal(signal, callback));
		return () -> {
			signalDefault();
			Reference.reachabilityFence(callback);
		};
	}

	public void raise() throws IOException {
		CSignal.raise(signal);
	}

	private void verifyHandler(boolean result) throws IOException {
		if (!result) throw new IOException("Failed to set handler for " + this);
	}
}
