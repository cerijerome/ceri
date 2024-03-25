package ceri.jna.clib;

import static ceri.jna.clib.OpenFlag.O_NONBLOCK;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.Connector;
import ceri.jna.clib.jna.CUnistd;
import ceri.log.util.LogUtil;

public class Pipe implements Connector, RuntimeCloseable {
	public final FileDescriptor read;
	public final FileDescriptor write;

	@SuppressWarnings("resource")
	public static Pipe of() throws IOException {
		int[] fds = CUnistd.pipe();
		return new Pipe(CFileDescriptor.of(fds[0]), CFileDescriptor.of(fds[1]));
	}

	private Pipe(FileDescriptor read, FileDescriptor write) {
		this.read = read;
		this.write = write;
	}

	public void blocking(boolean enabled) throws IOException {
		if (enabled) {
			read.flags().remove(O_NONBLOCK);
			write.flags().remove(O_NONBLOCK);
		} else {
			read.flags().add(O_NONBLOCK);
			write.flags().add(O_NONBLOCK);
		}
	}

	@Override
	public InputStream in() {
		return read.in();
	}

	@Override
	public OutputStream out() {
		return write.out();
	}

	@Override
	public void close() {
		LogUtil.close(read, write);
	}
}
