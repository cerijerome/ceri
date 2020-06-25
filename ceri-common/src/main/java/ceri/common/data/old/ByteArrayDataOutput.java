package ceri.common.data.old;

import java.io.DataOutput;

/**
 * Provides DataOutput access to a given byte array. The underlying byte array will be modified.
 * IndexOutOfBoundsException will be thrown if attempting to write past the end of the array.
 */
@Deprecated
public class ByteArrayDataOutput implements DataOutput {
	private final byte[] data;
	private int pos;

	public ByteArrayDataOutput(byte[] data, int offset) {
		this.data = data;
		pos = offset;
	}

	@Override
	public void write(int b) {
		data[pos++] = (byte) b;
	}

	@Override
	public void write(byte[] b) {
		write(b, 0, b.length);
	}

	@Override
	public void write(byte[] b, int off, int len) {
		System.arraycopy(b, off, data, pos, len);
		pos += len;
	}

	@Override
	public void writeBoolean(boolean v) {
		write(v ? 1 : 0);
	}

	@Override
	public void writeByte(int v) {
		write(v);
	}

	@Override
	public void writeShort(int v) {
		write((v >>> 8) & 0xFF);
		write(v & 0xFF);
	}

	@Override
	public void writeChar(int v) {
		writeShort(v);
	}

	@Override
	public void writeInt(int v) {
		writeShort((v >>> 16) & 0xffff);
		writeShort(v & 0xffff);
	}

	@Override
	public void writeLong(long v) {
		writeInt((int) (v >>> 32));
		writeInt((int) v);
	}

	@Override
	public void writeFloat(float v) {
		writeInt(Float.floatToIntBits(v));
	}

	@Override
	public void writeDouble(double v) {
		writeLong(Double.doubleToLongBits(v));
	}

	@Override
	public void writeBytes(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			write((byte) s.charAt(i));
		}
	}

	@Override
	public void writeChars(String s) {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int v = s.charAt(i);
			write((v >>> 8) & 0xFF);
			write(v & 0xFF);
		}
	}

	@Override
	public void writeUTF(String str) {
		throw new UnsupportedOperationException();
	}

}
