package ceri.common.test;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import ceri.common.time.Timeout;

public class TestProcess extends Process implements Closeable {
	public final TestInputStream in = TestInputStream.of();
	public final TestInputStream err = TestInputStream.of();
	public final TestOutputStream out = TestOutputStream.of();
	public final CallSync.Get<Integer> exitValue = CallSync.supplier(0);
	public final CallSync.Apply<Timeout, Boolean> waitFor = CallSync.function(null, true);

	@SuppressWarnings("resource")
	public static TestProcess of(String in, String err, int exitValue, boolean waitFor) {
		TestProcess process = TestProcess.of();
		if (in != null) process.in.to.writeAscii(in);
		if (err != null) process.err.to.writeAscii(err);
		process.exitValue.autoResponses(exitValue);
		process.waitFor.autoResponses(waitFor);
		return process;
	}

	public static TestProcess of() {
		return new TestProcess();
	}

	protected TestProcess() {}

	public void resetState() {
		in.resetState();
		err.resetState();
		out.resetState();
		exitValue.reset();
		waitFor.reset();
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public InputStream getErrorStream() {
		return err;
	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public int exitValue() {
		return exitValue.get();
	}

	@Override
	public int waitFor() throws InterruptedException {
		return exitValue.getWithInterrupt();
	}

	@Override
	public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
		return waitFor.applyWithInterrupt(Timeout.of(timeout, unit));
	}

	@Override
	public void destroy() {}

	@Override
	public void close() throws IOException {
		in.close();
		err.close();
		out.close();
	}

}
