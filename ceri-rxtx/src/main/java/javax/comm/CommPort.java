package javax.comm;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class CommPort implements Closeable {
	private final gnu.io.CommPort commPort;

	CommPort(gnu.io.CommPort commPort) {
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
