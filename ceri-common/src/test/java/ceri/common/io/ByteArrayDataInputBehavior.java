package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;

public class ByteArrayDataInputBehavior {
	private static final byte[] data = { Byte.MAX_VALUE, 0, Byte.MIN_VALUE, -1, 1, Byte.MIN_VALUE,
		Byte.MAX_VALUE, -2 };
	private DataInput truth;
	private ByteArrayDataInput in;

	@Before
	public void init() {
		truth = new DataInputStream(new ByteArrayInputStream(data));
		in = new ByteArrayDataInput(data, 0);
	}

	@Test
	public void shouldSkipCorrectly() throws IOException {
		assertThat(in.skipBytes(4), is(truth.skipBytes(4)));
		assertThat(in.skipBytes(0), is(truth.skipBytes(0)));
		assertThat(in.skipBytes(1), is(truth.skipBytes(1)));
		assertThat(in.skipBytes(Integer.MAX_VALUE), is(truth.skipBytes(Integer.MAX_VALUE)));
	}

	@Test
	public void shouldReadFully() throws IOException {
		byte[] b0 = new byte[data.length];
		byte[] b1 = new byte[data.length];
		in.readFully(b0);
		truth.readFully(b1);
		assertArrayEquals(b0, b1);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void shouldNotSupportReadUtf8() {
		in.readUTF();
	}

	@Test
	public void shouldReadLongCorrectly() throws IOException {
		assertThat(in.readLong(), is(truth.readLong()));
	}

	@Test
	public void shouldReadDoubleCorrectly() throws IOException {
		assertThat(in.readDouble(), is(truth.readDouble()));
	}

	@Test
	public void shouldReadIntCorrectly() throws IOException {
		assertThat(in.readInt(), is(truth.readInt()));
		assertThat(in.readInt(), is(truth.readInt()));
	}

	@Test
	public void shouldReadFloatCorrectly() throws IOException {
		assertThat(in.readFloat(), is(truth.readFloat()));
		assertThat(in.readFloat(), is(truth.readFloat()));
	}

	@Test
	public void shouldReadShortCorrectly() throws IOException {
		assertThat(in.readShort(), is(truth.readShort()));
		assertThat(in.readShort(), is(truth.readShort()));
		assertThat(in.readShort(), is(truth.readShort()));
		assertThat(in.readShort(), is(truth.readShort()));
	}

	@Test
	public void shouldReadUnsignedShortCorrectly() throws IOException {
		assertThat(in.readUnsignedShort(), is(truth.readUnsignedShort()));
		assertThat(in.readUnsignedShort(), is(truth.readUnsignedShort()));
		assertThat(in.readUnsignedShort(), is(truth.readUnsignedShort()));
		assertThat(in.readUnsignedShort(), is(truth.readUnsignedShort()));
	}

	@Test
	public void shouldReadCharCorrectly() throws IOException {
		assertThat(in.readChar(), is(truth.readChar()));
		assertThat(in.readChar(), is(truth.readChar()));
		assertThat(in.readChar(), is(truth.readChar()));
		assertThat(in.readChar(), is(truth.readChar()));
	}

	@Test
	public void shouldReadByteCorrectly() throws IOException {
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
		assertThat(in.readByte(), is(truth.readByte()));
	}

	@Test
	public void shouldReadUnsignedByteCorrectly() throws IOException {
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
		assertThat(in.readUnsignedByte(), is(truth.readUnsignedByte()));
	}

	@Test
	public void shouldReadBooleanCorrectly() throws IOException {
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
		assertThat(in.readBoolean(), is(truth.readBoolean()));
	}

}
