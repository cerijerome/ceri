package ceri.common.data;

import static org.junit.Assert.assertArrayEquals;
import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class ByteArrayDataOutputBehavior {
	private byte[] outBytes;
	private final ByteArrayOutputStream truthOut = new ByteArrayOutputStream();
	private final DataOutput truth = new DataOutputStream(truthOut);
	private ByteArrayDataOutput out;

	@Before
	public void reset() {
		outBytes = new byte[8];
		truthOut.reset();
		out = new ByteArrayDataOutput(outBytes, 0);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotSupportWriteUtf8() {
		out.writeUTF("test");
	}

	@Test
	public void shouldWriteLongCorrectly() throws IOException {
		out.writeLong(Long.MAX_VALUE);
		truth.writeLong(Long.MAX_VALUE);
		assertArrayEquals(outBytes, truthOut.toByteArray());
		reset();
		out.writeLong(Long.MIN_VALUE);
		truth.writeLong(Long.MIN_VALUE);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteDoubleCorrectly() throws IOException {
		out.writeDouble(Double.MAX_VALUE);
		truth.writeDouble(Double.MAX_VALUE);
		assertArrayEquals(outBytes, truthOut.toByteArray());
		reset();
		out.writeDouble(Double.NaN);
		truth.writeDouble(Double.NaN);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteIntCorrectly() throws IOException {
		out.writeInt(Integer.MAX_VALUE);
		out.writeInt(Integer.MIN_VALUE);
		truth.writeInt(Integer.MAX_VALUE);
		truth.writeInt(Integer.MIN_VALUE);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteFloatCorrectly() throws IOException {
		out.writeFloat(Float.MAX_VALUE);
		out.writeFloat(Float.MIN_VALUE);
		truth.writeFloat(Float.MAX_VALUE);
		truth.writeFloat(Float.MIN_VALUE);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteShortCorrectly() throws IOException {
		out.writeShort(Short.MAX_VALUE);
		out.writeShort(-1);
		out.writeShort(1);
		out.writeShort(Short.MIN_VALUE);
		truth.writeShort(Short.MAX_VALUE);
		truth.writeShort(-1);
		truth.writeShort(1);
		truth.writeShort(Short.MIN_VALUE);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteCharsCorrectly() throws IOException {
		out.writeChars("\0\t\uffff\u1fff");
		truth.writeChars("\0\t\uffff\u1fff");
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}
	
	@Test
	public void shouldWriteCharCorrectly() throws IOException {
		out.writeChar(Character.MAX_CODE_POINT);
		out.writeChar(-1);
		out.writeChar(1);
		out.writeChar(Character.MIN_CODE_POINT);
		truth.writeChar(Character.MAX_CODE_POINT);
		truth.writeChar(-1);
		truth.writeChar(1);
		truth.writeChar(Character.MIN_CODE_POINT);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteByteCorrectly() throws IOException {
		out.writeByte(Byte.MAX_VALUE);
		out.writeByte(-1);
		out.writeByte(1);
		out.writeByte(Byte.MIN_VALUE);
		out.write(new byte[] { 0, Byte.MIN_VALUE, -1, Byte.MAX_VALUE });
		truth.writeByte(Byte.MAX_VALUE);
		truth.writeByte(-1);
		truth.writeByte(1);
		truth.writeByte(Byte.MIN_VALUE);
		truth.write(new byte[] { 0, Byte.MIN_VALUE, -1, Byte.MAX_VALUE });
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteBytesCorrectly() throws IOException {
		out.writeBytes("test\0\t\uffff\u7fff");
		truth.writeBytes("test\0\t\uffff\u7fff");
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

	@Test
	public void shouldWriteBooleanCorrectly() throws IOException {
		out.writeBoolean(false);
		out.writeBoolean(true);
		out.writeBoolean(true);
		out.writeBoolean(false);
		out.writeInt(0);
		truth.writeBoolean(false);
		truth.writeBoolean(true);
		truth.writeBoolean(true);
		truth.writeBoolean(false);
		truth.writeInt(0);
		assertArrayEquals(outBytes, truthOut.toByteArray());
	}

}
