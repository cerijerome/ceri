package ceri.common.property;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.test.CallSync;

@SuppressWarnings("serial")
public class TestProperties extends Properties {
	public final CallSync.Consumer<Object> load = CallSync.consumer(null, true);
	public final CallSync.Consumer<List<?>> store = CallSync.consumer(null, true);

	public static TestProperties of() {
		return new TestProperties();
	}

	private TestProperties() {}

	@Override
	public void load(InputStream inStream) throws IOException {
		load.accept(inStream, ExceptionAdapter.io);
	}

	@Override
	public void load(Reader reader) throws IOException {
		load.accept(reader, ExceptionAdapter.io);
	}

	@Override
	public void store(OutputStream out, String comments) throws IOException {
		store.accept(List.of(out, comments), ExceptionAdapter.io);
	}

	@Override
	public void store(Writer writer, String comments) throws IOException {
		store.accept(List.of(writer, comments), ExceptionAdapter.io);
	}
}
