package ceri.common.io;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class ByteBufferStreamBehavior {
	private byte[] buffer;
	
	@Before
	public void init() {
		buffer = new byte[256];
		for (int i = 0; i < buffer.length; i++) buffer[i] = (byte)i;
	}
	
	@Test
	public void shouldAllowTransferMoreThanCapacity() throws IOException {
		ByteBufferStream bbs = new ByteBufferStream(400);
		bbs.write(buffer);
		assertThat(bbs.available(), is(256));
		assertThat(bbs.asInputStream().read(new byte[200]), is(200));
		assertThat(bbs.available(), is(56));
		bbs.write(buffer);
		assertThat(bbs.available(), is(312));
		assertThat(bbs.asInputStream().read(new byte[200]), is(200));
		assertThat(bbs.available(), is(112));
	}

	@Test
	public void shouldReturnRemainingDataWhenOutputIsClosed() throws IOException {
		ByteBufferStream bbs = new ByteBufferStream(300);
		bbs.write(buffer);
		bbs.close();
		byte[] b = new byte[300];
		assertThat(bbs.asInputStream().read(b), is(256));
		assertThat(Arrays.copyOf(b, 256), is(buffer));
		assertThat(bbs.asInputStream().read(b), is(-1));
	}

	@Test
	public void shouldMaintainWrittenByteValuesOnBulkReads() throws IOException {
		ByteBufferStream bbs = new ByteBufferStream(1000);
		bbs.write(buffer);
		assertThat(bbs.available(), is(256));
		byte[] b = new byte[256];
		assertThat(bbs.asInputStream().read(b), is(256));
		assertThat(b, is(buffer));
	}

	@Test
	public void shouldMaintainWrittenByteValuesOnSingleReads() throws IOException {
		ByteBufferStream bbs = new ByteBufferStream(10);
		bbs.write(Byte.MIN_VALUE);
		bbs.write(Byte.MAX_VALUE);
		assertThat((byte)bbs.asInputStream().read(), is(Byte.MIN_VALUE));
		assertThat((byte)bbs.asInputStream().read(), is(Byte.MAX_VALUE));
		assertThat(bbs.available(), is(0));
	}

	@Test(expected=BufferUnderflowException.class)
	public void shouldFailOnSingleReadIfBufferIsEmpty() throws IOException {
		ByteBufferStream bbs = new ByteBufferStream(200);
		bbs.asInputStream().read();
	}

	@Test(expected=BufferUnderflowException.class)
	public void shouldFailOnReadIfBufferIsEmpty() throws IOException {
		ByteBufferStream bbs = new ByteBufferStream(200);
		bbs.asInputStream().read(new byte[100]);
	}

	@Test(expected=BufferUnderflowException.class)
	public void shouldFailOnSingleReadIfBufferHasBeenEmptied() throws IOException {
		final ByteBufferStream bbs = new ByteBufferStream(200);
		bbs.write(Byte.MIN_VALUE);
		assertThat((byte)bbs.asInputStream().read(), is(Byte.MIN_VALUE));
		bbs.asInputStream().read();
	}

	@Test(expected=BufferUnderflowException.class)
	public void shouldFailOnReadIfBufferHasBeenEmptied() throws IOException {
		final ByteBufferStream bbs = new ByteBufferStream(200);
		bbs.write(Byte.MIN_VALUE);
		assertThat((byte)bbs.asInputStream().read(), is(Byte.MIN_VALUE));
		bbs.asInputStream().read(new byte[100]);
	}

}
