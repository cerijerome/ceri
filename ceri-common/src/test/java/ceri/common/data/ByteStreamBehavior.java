package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertRead;
import static ceri.common.test.AssertUtil.assertRte;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import static ceri.common.test.ErrorGen.RIX;
import static ceri.common.test.ErrorGen.RTX;
import static ceri.common.test.TestUtil.inputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.data.ByteStream.Reader;
import ceri.common.data.ByteStream.Writer;
import ceri.common.exception.ExceptionAdapter;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.PipedStream;
import ceri.common.io.RuntimeIoException;
import ceri.common.test.ErrorGen;

@SuppressWarnings("resource")
public class ByteStreamBehavior {

	/* Reader tests */

	@Test
	public void shouldReadByte() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3));
		assertEquals(r.readByte(), (byte) 1);
		assertEquals(r.readByte(), (byte) 2);
		assertEquals(r.readByte(), (byte) 3);
		assertThrown(() -> r.readByte());
	}

	@Test
	public void shouldReadByteWithErrors() {
		ErrorGen error = ErrorGen.of();
		InputStream in = IoStreamUtil.in(() -> {
			error.call(ExceptionAdapter.io);
			return 0;
		});
		Reader r = ByteStream.reader(in);
		error.setFrom(RTX);
		assertRte(() -> r.readByte());
		error.setFrom(RIX);
		assertThrown(RuntimeInterruptedException.class, () -> r.readByte());
		error.setFrom(IOX);
		assertThrown(RuntimeIoException.class, () -> r.readByte());
	}

	@Test
	public void shouldSkipReaderBytes() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		assertEquals(r.skip(3).readByte(), (byte) 4);
		assertThrown(() -> r.skip(2));
		assertThrown(() -> r.skip(1));
	}

	@Test
	public void shouldReadBytes() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		assertArray(r.readBytes(3), 1, 2, 3);
		assertThrown(() -> r.readBytes(3));
	}

	@Test
	public void shouldReadIntoByteArray() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		byte[] bytes = new byte[5];
		assertEquals(r.readInto(bytes, 1, 3), 4);
		assertArray(bytes, 0, 1, 2, 3, 0);
		assertThrown(() -> r.readInto(bytes, 0, 3));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		Mutable m = Mutable.of(5);
		assertEquals(r.readInto(m, 1, 3), 4);
		assertArray(m.copy(0), 0, 1, 2, 3, 0);
		assertThrown(() -> r.readInto(m, 0, 3));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		Reader r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		var pipe = PipedStream.of();
		assertEquals(r.transferTo(pipe.out(), 3), 3);
		assertThrown(() -> r.transferTo(pipe.out(), 3));
		assertRead(pipe.in(), 1, 2, 3);
	}

	/* Writer tests */

	@Test
	public void shouldWriteByte() {
		var out = new ByteArrayOutputStream();
		Writer w = ByteStream.writer(out);
		w.writeByte(1);
		w.writeByte(2);
		w.writeByte(3);
		assertArray(out.toByteArray(), 1, 2, 3);
	}

	@Test
	public void shouldWriteByteWithErrors() {
		ErrorGen error = ErrorGen.of();
		OutputStream out = IoStreamUtil.out(_ -> error.call(ExceptionAdapter.io));
		Writer w = ByteStream.writer(out);
		error.setFrom(RTX);
		assertRte(() -> w.writeByte(1));
		error.setFrom(RIX);
		assertThrown(RuntimeInterruptedException.class, () -> w.writeByte(2));
		error.setFrom(IOX);
		assertThrown(RuntimeIoException.class, () -> w.writeByte(3));
	}

	@Test
	public void shouldFillBytes() throws IOException {
		var pipe = PipedStream.of();
		Writer w = ByteStream.writer(pipe.out());
		w.fill(2, 1);
		w.fill(2, 2);
		assertRead(pipe.in(), 1, 1, 2, 2);
	}

	@Test
	public void shouldWriteFromByteArray() throws IOException {
		var pipe = PipedStream.of();
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(pipe.out());
		w.writeFrom(bytes, 1, 3);
		w.writeFrom(bytes, 0, 2);
		assertRead(pipe.in(), 2, 3, 4, 1);
	}

	@Test
	public void shouldWriteFromByteProvider() throws IOException {
		var pipe = PipedStream.of();
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(pipe.out());
		w.writeFrom(m, 1, 3);
		w.writeFrom(m, 0, 2);
		assertRead(pipe.in(), 2, 3, 4, 1);
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		var pipe = PipedStream.of();
		var in = inputStream(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(pipe.out());
		assertEquals(w.transferFrom(in, 3), 3);
		assertEquals(w.transferFrom(in, 3), 2);
		assertEquals(w.transferFrom(in, 3), 0);
		assertRead(pipe.in(), 1, 2, 3, 4, 5);
	}

}
