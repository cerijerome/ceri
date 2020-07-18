package ceri.common.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * A PrintStream that captures bytes written in given character set. Captured text is retrieved with
 * toString(). The stream is auto-flushed.
 */
public class StringPrintStream extends PrintStream {
	private final Charset charSet;
	private final ByteArrayOutputStream baos;

	/**
	 * Constructor using default character set.
	 */
	public StringPrintStream() {
		super(new ByteArrayOutputStream(), true);
		baos = (ByteArrayOutputStream) out;
		this.charSet = null;
	}

	/**
	 * Constructor using given character set.
	 */
	public StringPrintStream(Charset charSet) {
		super(new ByteArrayOutputStream(), true, charSet);
		baos = (ByteArrayOutputStream) out;
		this.charSet = charSet;
	}

	public void clear() {
		baos.reset();
	}

	/**
	 * Returns the current output as a String.
	 */
	@Override
	public String toString() {
		if (charSet == null) return baos.toString();
		return baos.toString(charSet);
	}

}
