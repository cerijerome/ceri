package ceri.common.data;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.TestUtil.inputStream;
import static ceri.common.test.TestUtil.outputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.data.ByteStream.Reader;
import ceri.common.data.ByteStream.Writer;
import ceri.common.io.IoStreamUtil;
import ceri.common.io.RuntimeIoException;
import ceri.common.test.ErrorGen;
import ceri.common.test.ErrorGen.Mode;

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
		InputStream in = IoStreamUtil.in(error::generateIo);
		Reader r = ByteStream.reader(in);
		error.mode(Mode.rt);
		assertThrown(RuntimeException.class, () -> r.readByte());
		error.mode(Mode.rtInterrupted);
		assertThrown(RuntimeInterruptedException.class, () -> r.readByte());
		error.mode(Mode.checked);
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
		var out = outputStream(new int[6]);
		assertEquals(r.transferTo(out, 3), 3);
		assertThrown(() -> r.transferTo(out, 3));
		assertArray(out.written(), 1, 2, 3);
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
		OutputStream out = IoStreamUtil.out(i -> error.generateIo());
		Writer w = ByteStream.writer(out);
		error.mode(Mode.rt);
		assertThrown(RuntimeException.class, () -> w.writeByte(1));
		error.mode(Mode.rtInterrupted);
		assertThrown(RuntimeInterruptedException.class, () -> w.writeByte(2));
		error.mode(Mode.checked);
		assertThrown(RuntimeIoException.class, () -> w.writeByte(3));
	}

	@Test
	public void shouldFillBytes() {
		var out = outputStream(0, 0, 0); // EOF after 3 writes
		Writer w = ByteStream.writer(out);
		w.fill(2, 1);
		assertThrown(() -> w.fill(2, 2));
		assertArray(out.written(), 1, 1, 2);
	}

	@Test
	public void shouldWriteFromByteArray() {
		var out = outputStream(0, 0, 0, 0); // EOF after 4 writes
		byte[] bytes = ArrayUtil.bytes(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(out);
		w.writeFrom(bytes, 1, 3);
		assertThrown(() -> w.writeFrom(bytes, 0, 2));
		assertArray(out.written(), 2, 3, 4, 1);
	}

	@Test
	public void shouldWriteFromByteProvider() {
		var out = outputStream(0, 0, 0, 0); // EOF after 4 writes
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		Writer w = ByteStream.writer(out);
		w.writeFrom(m, 1, 3);
		assertThrown(() -> w.writeFrom(m, 0, 2));
		assertArray(out.written(), 2, 3, 4, 1);
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		var in = inputStream(1, 2, 3, 4, 5);
		var out = outputStream(new int[6]);
		Writer w = ByteStream.writer(out);
		assertEquals(w.transferFrom(in, 3), 3);
		assertEquals(w.transferFrom(in, 3), 2);
		assertEquals(w.transferFrom(in, 3), 0);
		assertArray(out.written(), 1, 2, 3, 4, 5);
	}

}
