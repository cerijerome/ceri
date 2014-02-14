package ceri.common.io;

import java.io.DataInput;

/**
 * Provides DataInput access to a given byte array. The underlying byte array will be modified.
 */
public class ByteArrayDataInput implements DataInput {
	private final byte[] data;
	private int pos;

	public ByteArrayDataInput(byte[] data, int offset) {
		this.data = data;
		pos = offset;
	}

	@Override
	public void readFully(byte b[]) {
		readFully(b, 0, b.length);
	}

	@Override
	public void readFully(byte b[], int off, int len) {
		System.arraycopy(data, pos, b, off, len);
		pos += len;
	}

	@Override
	public int skipBytes(int n) {
		if ((long)pos + n > data.length) n = data.length - pos;
		pos += n;
		return n;
	}

	@Override
	public boolean readBoolean() {
		return readByte() != 0;
	}

	@Override
	public byte readByte() {
		return data[pos++];
	}

	@Override
	public int readUnsignedByte() {
		return readByte() & 0xff;
	}

	@Override
	public short readShort() {
		return (short) (((data[pos++] & 0xff) << 8) + (data[pos++] & 0xff));
	}

	@Override
	public int readUnsignedShort() {
		return readShort() & 0xffff;
	}

	@Override
	public char readChar() {
		return (char) readShort();
	}

	@Override
	public int readInt() {
		return (((data[pos++] & 0xff) << 24) + ((data[pos++] & 0xff) << 16) +
			((data[pos++] & 0xff) << 8) + (data[pos++] & 0xff));
	}

	@Override
	public long readLong() {
		return (((long) data[pos++] << 56) + ((long) (data[pos++] & 0xff) << 48) +
			((long) (data[pos++] & 0xff) << 40) + ((long) (data[pos++] & 0xff) << 32) +
			((long) (data[pos++] & 0xff) << 24) + ((data[pos++] & 0xff) << 16) +
			((data[pos++] & 0xff) << 8) + ((data[pos++] & 0xff) << 0));
	}

	@Override
	public float readFloat() {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	@Deprecated
	public String readLine() {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() {
		throw new UnsupportedOperationException();
	}

}
