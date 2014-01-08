package ceri.common.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

/**
 * A PrintStream where the output can be captured with toString().
 * Stream is auto-flushed.
 */
public class StringPrintStream extends PrintStream {
	private final String charSet;
	private final ByteArrayOutputStream baos;
	
	/**
	 * Constructor using default character set. 
	 */
	public StringPrintStream() {
		super(new ByteArrayOutputStream(), true);
		baos = (ByteArrayOutputStream)out;
		this.charSet = null;
	}

	/**
	 * Constructor using given character set. 
	 */
	public StringPrintStream(String charSet) throws UnsupportedEncodingException {
		super(new ByteArrayOutputStream(), true, charSet);
		baos = (ByteArrayOutputStream)out;
		this.charSet = charSet;
	}

	/**
	 * Returns the current output as a String.
	 */
	@Override
	public String toString() {
		if (charSet == null) return baos.toString();
		try {
			return baos.toString(charSet);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("Should not happen", e);
		}
	}
}
