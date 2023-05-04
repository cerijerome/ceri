package ceri.jna.util;

import ceri.common.reflect.ReflectUtil;
import ceri.common.time.TimeSupplier;
import ceri.jna.clib.jna.CException;
import ceri.jna.clib.jna.CSignal;
import ceri.jna.clib.jna.CSignal.sighandler_t;

public class CallbackTester {
	private static volatile long lastCallbackMs = 0;

	public static void main(String[] args) throws CException {
		runWithoutGc(CSignal.SIGINT, 10, 1000, 3000);
		runWithGc(CSignal.SIGINT, 10, 1000, 3000);
	}

	public static void runWithoutGc(int signal, int n, int delayMs, int timeoutMs)
		throws CException {
		System.out.println("Running without gc");
		try (var x = JnaUtil.closeable(set(signal))) {
			exec(signal, n, delayMs, timeoutMs);
		}
	}

	public static void runWithGc(int signal, int n, int delayMs, int timeoutMs) throws CException {
		System.out.println("Running with gc");
		set(signal);
		exec(signal, n, delayMs, timeoutMs);
	}

	private static void exec(int signal, int n, int delayMs, int timeoutMs) throws CException {
		for (int i = 0; i < n; i++) {
			if (timeout(timeoutMs)) System.out.println("timeout");
			CSignal.raise(signal);
			TimeSupplier.millis.delay(delayMs);
			System.gc();
		}
		System.out.println("Complete\n");
	}

	private static boolean timeout(int timeoutMs) {
		return System.currentTimeMillis() - lastCallbackMs >= timeoutMs;
	}

	private static sighandler_t set(int signal) throws CException {
		sighandler_t cb = callback();
		CSignal.signal(signal, cb);
		lastCallbackMs = System.currentTimeMillis();
		System.out.println("callback=" + ReflectUtil.hashId(cb));
		return cb;
	}

	private static sighandler_t callback() {
		return new sighandler_t() {
			@Override
			public void invoke(int signal) {
				System.out.println("signal=" + signal);
				lastCallbackMs = System.currentTimeMillis();
			}

			@Override
			protected void finalize() throws Throwable {
				System.out.printf("gc! (callback=%s)%n", ReflectUtil.hashId(this));
			}
		};
	}
}
