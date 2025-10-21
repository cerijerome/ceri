package ceri.common.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Interface for reading lines of input.
 */
public interface LineReader {
	/** A no-op, stateless instance. */
	LineReader NULL = () -> "";

	/**
	 * Reads the next line of input.
	 */
	String readLine() throws IOException;

	/**
	 * Returns true if any input is available.
	 */
	@SuppressWarnings("unused")
	default boolean ready() throws IOException {
		return false;
	}

	/**
	 * Adapt a buffered reader.
	 */
	static LineReader of(BufferedReader reader) {
		return new LineReader() {
			@Override
			public String readLine() throws IOException {
				return reader.readLine();
			}

			@Override
			public boolean ready() throws IOException {
				return reader.ready();
			}
		};
	}

	/**
	 * Adapt an input stream.
	 */
	static LineReader of(InputStream in) {
		return of(new BufferedReader(new InputStreamReader(in)));
	}
}