package ceri.x10.cm11a.device;

import static ceri.common.function.FunctionUtil.execQuietly;
import java.io.Closeable;
import java.io.InputStream;
import java.io.OutputStream;
import ceri.common.data.ByteStream;
import ceri.common.event.Listenable;
import ceri.common.io.IoUtil;
import ceri.common.io.PipedStream;
import ceri.common.io.StateChange;
import ceri.common.test.TestListeners;

public class Cm11aTestConnector implements Cm11aConnector, Closeable {
	public final TestListeners<StateChange> listeners = TestListeners.of();
	private PipedStream in = PipedStream.of();
	private PipedStream out = PipedStream.of();
	public final ByteStream.Reader from; // read code output
	public final ByteStream.Writer to; // write code input

	@SuppressWarnings("resource")
	public Cm11aTestConnector() {
		to = ByteStream.writer(in.out());
		from = ByteStream.reader(out.in());
	}

	public void clear() {
		listeners.clear();
		execQuietly(in::clear);
		execQuietly(out::clear);
	}

	@Override
	public Listenable<StateChange> listeners() {
		return listeners;
	}

	@Override
	public InputStream in() {
		return in.in();
	}

	@Override
	public OutputStream out() {
		return out.out();
	}

	@Override
	public void close() {
		IoUtil.close(in);
		IoUtil.close(out);
	}

}
