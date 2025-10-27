package ceri.common.data;

import static ceri.common.test.TestUtil.inputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.concurrent.RuntimeInterruptedException;
import ceri.common.data.ByteArray.Mutable;
import ceri.common.except.ExceptionAdapter;
import ceri.common.io.IoExceptions;
import ceri.common.io.IoStream;
import ceri.common.io.PipedStream;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;

@SuppressWarnings("resource")
public class ByteStreamBehavior {

	// Reader tests

	@Test
	public void shouldReadByte() {
		var r = ByteStream.reader(inputStream(1, 2, 3));
		Assert.equal(r.readByte(), (byte) 1);
		Assert.equal(r.readByte(), (byte) 2);
		Assert.equal(r.readByte(), (byte) 3);
		Assert.thrown(() -> r.readByte());
	}

	@Test
	public void shouldReadByteWithErrors() {
		var error = ErrorGen.of();
		var in = IoStream.in(() -> {
			error.call(ExceptionAdapter.io);
			return 0;
		});
		var r = ByteStream.reader(in);
		error.setFrom(ErrorGen.RTX);
		Assert.runtime(() -> r.readByte());
		error.setFrom(ErrorGen.RIX);
		Assert.thrown(RuntimeInterruptedException.class, () -> r.readByte());
		error.setFrom(ErrorGen.IOX);
		Assert.thrown(IoExceptions.Runtime.class, () -> r.readByte());
	}

	@Test
	public void shouldSkipReaderBytes() {
		var r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		Assert.equal(r.skip(3).readByte(), (byte) 4);
		Assert.thrown(() -> r.skip(2));
		Assert.thrown(() -> r.skip(1));
	}

	@Test
	public void shouldReadBytes() {
		var r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		Assert.array(r.readBytes(3), 1, 2, 3);
		Assert.thrown(() -> r.readBytes(3));
	}

	@Test
	public void shouldReadIntoByteArray() {
		var r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		byte[] bytes = new byte[5];
		Assert.equal(r.readInto(bytes, 1, 3), 4);
		Assert.array(bytes, 0, 1, 2, 3, 0);
		Assert.thrown(() -> r.readInto(bytes, 0, 3));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		var r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		var m = Mutable.of(5);
		Assert.equal(r.readInto(m, 1, 3), 4);
		Assert.array(m.copy(0), 0, 1, 2, 3, 0);
		Assert.thrown(() -> r.readInto(m, 0, 3));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		var r = ByteStream.reader(inputStream(1, 2, 3, 4, 5));
		var pipe = PipedStream.of();
		Assert.equal(r.transferTo(pipe.out(), 3), 3);
		Assert.thrown(() -> r.transferTo(pipe.out(), 3));
		Assert.read(pipe.in(), 1, 2, 3);
	}

	// Writer tests

	@Test
	public void shouldWriteByte() {
		var out = new ByteArrayOutputStream();
		var w = ByteStream.writer(out);
		w.writeByte(1);
		w.writeByte(2);
		w.writeByte(3);
		Assert.array(out.toByteArray(), 1, 2, 3);
	}

	@Test
	public void shouldWriteByteWithErrors() {
		var error = ErrorGen.of();
		var out = IoStream.out(_ -> error.call(ExceptionAdapter.io));
		var w = ByteStream.writer(out);
		error.setFrom(ErrorGen.RTX);
		Assert.runtime(() -> w.writeByte(1));
		error.setFrom(ErrorGen.RIX);
		Assert.thrown(RuntimeInterruptedException.class, () -> w.writeByte(2));
		error.setFrom(ErrorGen.IOX);
		Assert.thrown(IoExceptions.Runtime.class, () -> w.writeByte(3));
	}

	@Test
	public void shouldFillBytes() throws IOException {
		var pipe = PipedStream.of();
		var w = ByteStream.writer(pipe.out());
		w.fill(2, 1);
		w.fill(2, 2);
		Assert.read(pipe.in(), 1, 1, 2, 2);
	}

	@Test
	public void shouldWriteFromByteArray() throws IOException {
		var pipe = PipedStream.of();
		byte[] bytes = ArrayUtil.bytes.of(1, 2, 3, 4, 5);
		var w = ByteStream.writer(pipe.out());
		w.writeFrom(bytes, 1, 3);
		w.writeFrom(bytes, 0, 2);
		Assert.read(pipe.in(), 2, 3, 4, 1);
	}

	@Test
	public void shouldWriteFromByteProvider() throws IOException {
		var pipe = PipedStream.of();
		var m = Mutable.wrap(1, 2, 3, 4, 5);
		var w = ByteStream.writer(pipe.out());
		w.writeFrom(m, 1, 3);
		w.writeFrom(m, 0, 2);
		Assert.read(pipe.in(), 2, 3, 4, 1);
	}

	@Test
	public void shouldTransferFromInputStream() throws IOException {
		var pipe = PipedStream.of();
		var in = inputStream(1, 2, 3, 4, 5);
		var w = ByteStream.writer(pipe.out());
		Assert.equal(w.transferFrom(in, 3), 3);
		Assert.equal(w.transferFrom(in, 3), 2);
		Assert.equal(w.transferFrom(in, 3), 0);
		Assert.read(pipe.in(), 1, 2, 3, 4, 5);
	}
}
