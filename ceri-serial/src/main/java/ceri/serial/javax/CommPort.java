package ceri.serial.javax;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.RuntimeCloseable;

public abstract class CommPort implements RuntimeCloseable {
	private final purejavacomm.CommPort commPort;

	CommPort(purejavacomm.CommPort commPort) {
		this.commPort = commPort;
	}

	@Override
	public void close() {
		commPort.close();
	}

	public InputStream getInputStream() throws IOException {
		return commPort.getInputStream();
	}

	public OutputStream getOutputStream() throws IOException {
		return commPort.getOutputStream();
	}

}
