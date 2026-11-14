package ceri.common.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * A PrintStream that captures bytes written in given character set. Captured text is retrieved with
 * toString(). The stream is auto-flushed.
 */
public class StringStream extends PrintStream {
	private final Charset charset;
	private final ByteArrayOutputStream baos;

	/**
	 * Constructor using default character set.
	 */
	public static StringStream of() {
		return of(Charset.defaultCharset());
	}

	/**
	 * Constructor using given character set.
	 */
	public static StringStream of(Charset charset) {
		return new StringStream(charset);
	}

	protected StringStream(Charset charset) {
		super(new ByteArrayOutputStream(), true, charset);
		baos = (ByteArrayOutputStream) out;
		this.charset = charset;
	}

	public void clear() {
		baos.reset();
	}

	/**
	 * Returns the current output as a String.
	 */
	@Override
	public String toString() {
		return baos.toString(charset);
	}
}
