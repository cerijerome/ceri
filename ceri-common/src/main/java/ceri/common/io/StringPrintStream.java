package ceri.common.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;

/**
 * A PrintStream where the output can be captured with toString(). Stream is auto-flushed.
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

	/**
	 * Returns the current output as a String.
	 */
	@Override
	public String toString() {
		if (charSet == null) return baos.toString();
		return baos.toString(charSet);
	}

	public void clear() {
		baos.reset();
	}
}
