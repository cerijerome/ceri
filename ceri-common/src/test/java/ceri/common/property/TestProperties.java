package ceri.common.property;

import static ceri.common.io.IoUtil.IO_ADAPTER;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Properties;
import ceri.common.test.CallSync;

public class TestProperties extends Properties {
	private static final long serialVersionUID = 1L;
	public final CallSync.Accept<Object> load = CallSync.consumer(null, true);
	public final CallSync.Accept<List<?>> store = CallSync.consumer(null, true);

	public static TestProperties of() {
		return new TestProperties();
	}

	private TestProperties() {}

	@Override
	public void load(InputStream inStream) throws IOException {
		load.accept(inStream, IO_ADAPTER);
	}

	@Override
	public void load(Reader reader) throws IOException {
		load.accept(reader, IO_ADAPTER);
	}

	@Override
	public void store(OutputStream out, String comments) throws IOException {
		store.accept(List.of(out, comments), IO_ADAPTER);
	}

	@Override
	public void store(Writer writer, String comments) throws IOException {
		store.accept(List.of(writer, comments), IO_ADAPTER);
	}
}
