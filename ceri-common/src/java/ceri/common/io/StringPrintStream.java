package ceri.common.io;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class StringPrintStream extends PrintStream {
	private final String charSet;
	private final ByteArrayOutputStream baos;
	
	public StringPrintStream() {
		super(new ByteArrayOutputStream(), true);
		baos = (ByteArrayOutputStream)out;
		this.charSet = null;
	}

	public StringPrintStream(String charSet) throws UnsupportedEncodingException {
		super(new ByteArrayOutputStream(), true, charSet);
		baos = (ByteArrayOutputStream)out;
		this.charSet = charSet;
	}

	@Override
	public String toString() {
		if (charSet == null) return baos.toString();
		try {
			return baos.toString(charSet);
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e); // Should not happen
		}
	}
}
