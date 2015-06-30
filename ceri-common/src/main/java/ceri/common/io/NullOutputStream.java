package ceri.common.io;

import java.io.OutputStream;

public class NullOutputStream extends OutputStream {
	
	NullOutputStream() {}
	
	@Override
	public void write(int b) {}
	
	@Override
	public void write(byte[] b) {}

	@Override
	public void write(byte[] b, int off, int len) {}
	
}
