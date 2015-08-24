package ceri.log.binary;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BinaryLogInputStream extends FilterInputStream {
	private final BinaryPrinter printer;

	public BinaryLogInputStream(BinaryPrinter printer, InputStream in) {
		super(in);
		this.printer = printer;
	}

	@Override
	public int read() throws IOException {
		int value = in.read();
		if (value != -1) printer.print(new byte[] { (byte) value });
		return value;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = in.read(b, off, len);
		if (count != -1) printer.print(b, off, count);
		return count;
	}

}
