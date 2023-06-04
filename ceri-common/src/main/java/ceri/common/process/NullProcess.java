package ceri.common.process;

import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.io.IoStreamUtil;

public class NullProcess extends Process {

	public static NullProcess of() {
		return new NullProcess();
	}

	protected NullProcess() {}

	@Override
	public void destroy() {}

	@Override
	public int exitValue() {
		return 0;
	}

	@Override
	public InputStream getErrorStream() {
		return IoStreamUtil.nullIn;
	}

	@Override
	public InputStream getInputStream() {
		return IoStreamUtil.nullIn;
	}

	@Override
	public OutputStream getOutputStream() {
		return IoStreamUtil.nullOut;
	}

	@Override
	public int waitFor() throws InterruptedException {
		return exitValue();
	}
}
