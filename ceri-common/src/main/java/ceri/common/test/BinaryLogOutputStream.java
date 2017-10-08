package ceri.common.test;


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinaryLogOutputStream extends FilterOutputStream {
	private final BinaryPrinter printer;

	public BinaryLogOutputStream(BinaryPrinter printer, OutputStream out) {
		super(out);
		this.printer = printer;
	}

	@Override
	public void write(int b) throws IOException {
		printer.print(new byte[] { (byte) b });
		out.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		printer.print(b, off, len);
		out.write(b, off, len);
	}

}
