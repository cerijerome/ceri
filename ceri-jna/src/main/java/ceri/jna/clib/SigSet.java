package ceri.jna.clib;

import java.io.IOException;
import java.util.Arrays;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.jna.CSignal.sigset_t;

public class SigSet {
	private final sigset_t sigset;

	public static sigset_t struct(SigSet sigset) {
		return sigset == null ? null : sigset.sigset;
	}

	public static SigSet of(Signal... signals) throws IOException {
		return new SigSet(new sigset_t()).clear().add(signals);
	}

	private SigSet(sigset_t sigset) {
		this.sigset = sigset;
	}

	public SigSet clear() throws IOException {
		CSignal.sigemptyset(sigset);
		return this;
	}

	public SigSet add(Signal... signals) throws IOException {
		return add(Arrays.asList(signals));
	}

	public SigSet add(Iterable<Signal> signals) throws IOException {
		for (var signal : signals)
			CSignal.sigaddset(sigset, signal.signal);
		return this;
	}

	public SigSet remove(Signal... signals) throws IOException {
		return remove(Arrays.asList(signals));
	}

	public SigSet remove(Iterable<Signal> signals) throws IOException {
		for (var signal : signals)
			CSignal.sigdelset(sigset, signal.signal);
		return this;
	}

	public boolean has(Signal signal) throws IOException {
		return CSignal.sigismember(sigset, signal.signal);
	}
}
