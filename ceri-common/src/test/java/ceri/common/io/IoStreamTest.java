package ceri.common.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.After;
import org.junit.Test;
import ceri.common.array.Array;
import ceri.common.function.Excepts;
import ceri.common.io.IoStream.FilterRead;
import ceri.common.io.IoStream.FilterWrite;
import ceri.common.io.IoStream.Read;
import ceri.common.io.IoStream.Write;
import ceri.common.test.Assert;
import ceri.common.test.Testing;

public class IoStreamTest {
	private static final Excepts.Function<IOException, InputStream, Integer> nullF = null;
	private static final Excepts.ObjIntPredicate<IOException, OutputStream> nullOip = null;
	private static final Excepts.IntConsumer<IOException> nullIc = null;
	private static final Excepts.IntSupplier<IOException> nullIs = null;
	private ByteArrayInputStream bin0;
	private ByteArrayInputStream bin1;
	private ByteArrayOutputStream bout0;
	private ByteArrayOutputStream bout1;
	private InputStream in;
	private OutputStream out;

	@After
	public void after() {
		in = Testing.close(in);
		out = Testing.close(out);
		bin0 = Testing.close(bin0);
		bin1 = Testing.close(bin1);
		bout0 = Testing.close(bout0);
		bout1 = Testing.close(bout1);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(IoStream.class);
	}

	@Test
	public void testNullInputStream() throws IOException {
		IoStream.nullIn.close();
		Assert.equal(IoStream.nullIn.available(), 0);
		Assert.equal(IoStream.nullIn.read(), 0);
		Assert.equal(IoStream.nullIn.read(new byte[2]), 2);
		Assert.array(IoStream.nullIn.readAllBytes());
		Assert.array(IoStream.nullIn.readNBytes(3), 0, 0, 0);
		assertReadNBytes(IoStream.nullIn, 0, 0, 0);
		Assert.equal(IoStream.nullIn.transferTo(new ByteArrayOutputStream()), 0L);
		IoStream.nullIn.close();
	}

	@Test
	public void testNullOutputStream() throws IOException {
		IoStream.nullOut.close();
		IoStream.nullOut.flush();
		IoStream.nullOut.write(0xff);
		IoStream.nullOut.write(Array.BYTE.of(-1, 0, 1));
		IoStream.nullIn.close();
	}

	// InputStream

	@Test
	public void testInWithNullByteRead() throws IOException {
		in = IoStream.in(nullIs);
		Assert.equal(in.available(), 0);
		Assert.equal(in.read(), 0);
		assertReadBytes(in, 0, 0, 0);
	}

	@Test
	public void testInWithByteRead() throws IOException {
		bin0 = bin(1, 2, 3);
		in = IoStream.in(() -> bin0.read());
		Assert.equal(in.available(), 0);
		Assert.equal(in.read(), 1);
		assertReadBytes(in, 2, 3);
		Assert.equal(in.read(), -1);
	}

	@Test
	public void testInWithAvailable() throws IOException {
		bin0 = bin(1, 2, 3);
		in = IoStream.in(() -> bin0.read(), bin0::available);
		Assert.equal(in.available(), 3);
	}

	@Test
	public void testInWithNullArrayRead() throws IOException {
		in = IoStream.in((Read) null);
		Assert.equal(in.available(), 0);
		Assert.equal(in.read(), 0);
		assertReadBytes(in, 0, 0, 0);
	}

	@Test
	public void testInWithArrayRead() throws IOException {
		bin0 = bin(1, 2, 3);
		in = IoStream.in((b, off, len) -> bin0.read(b, off, len));
		Assert.equal(in.available(), 0);
		Assert.equal(in.read(), 1);
		assertReadBytes(in, 2, 3);
		Assert.equal(in.read(), -1);
	}

	// FilterInputStream

	@Test
	public void testFilterInWithNullByteRead() throws IOException {
		bin0 = bin(1, 2, 3);
		in = IoStream.filterIn(bin0, nullF);
		Assert.equal(in.available(), 3);
		Assert.equal(in.read(), 1);
		assertReadBytes(in, new byte[2], 2, 3);
		Assert.equal(in.read(), -1);
	}

	@Test
	public void testFilterInWithByteRead() throws IOException {
		bin0 = bin(1, 2, 3);
		bin1 = bin(4, 5, 6);
		in = IoStream.filterIn(bin1, _ -> readOrNull(bin0));
		Assert.equal(in.available(), 3);
		Assert.equal(in.read(), 1);
		assertReadBytes(in, 2, 3, 4);
		Assert.equal(in.skip(1), 1L);
		assertReadBytes(in, new byte[0]);
		assertReadBytes(in, new byte[3], 6);
		Assert.equal(in.read(), -1);
		Assert.equal(in.read(new byte[3], 0, 3), -1);
	}

	@Test
	public void testFilterInWithAvailable() throws IOException {
		bin0 = bin(1, 2, 3);
		bin1 = bin(4, 5, 6);
		in = IoStream.filterIn(bin1, _ -> readOrNull(bin0), _ -> availableOrNull(bin0));
		Assert.equal(in.available(), 3);
		in.read();
		Assert.equal(in.available(), 2);
		in.readNBytes(2);
		Assert.equal(in.available(), 3);
	}

	@Test
	public void testFilterInWithByteReadError() throws IOException {
		bin0 = bin(1, 2, 3);
		in = IoStream.filterIn(IoStream.nullIn, _ -> readOrError(bin0));
		Assert.equal(in.read(), 1);
		assertReadBytes(in, new byte[3], 2, 3);
		Assert.thrown(in::read);
	}

	@Test
	public void testFilterInWithNullArrayRead() throws IOException {
		bin0 = bin(1, 2, 3);
		in = IoStream.filterIn(bin0, (FilterRead) null);
		Assert.equal(in.available(), 3);
		Assert.equal(in.read(), 1);
		assertReadBytes(in, new byte[2], 2, 3);
		Assert.equal(in.read(), -1);
	}

	@Test
	public void testFilterInWithArrayRead() throws IOException {
		bin0 = bin(1, 2, 3);
		bin1 = bin(4, 5, 6);
		in = IoStream.filterIn(bin1, (_, b, off, len) -> readOrNull(bin0, b, off, len));
		Assert.equal(in.available(), 3);
		Assert.equal(in.read(), 1);
		assertReadBytes(in, new byte[3], 2, 3);
		Assert.equal(in.skip(1), 1L);
		assertReadBytes(in, new byte[3], 5, 6);
		Assert.equal(in.read(), -1);
	}

	@Test
	public void testFilterInSkip() throws IOException {
		bin0 = bin(1, 2, 3);
		bin1 = bin(4, 5, 6);
		in = IoStream.filterIn(bin1, (_, b, off, len) -> readOrNull(bin0, b, off, len));
		Assert.equal(in.skip(0), 0L);
		Assert.equal(in.skip(4), 4L);
		Assert.equal(in.skip(4), 2L);
	}

	// OutputStream

	@Test
	public void testOutWithNullByteWrite() throws IOException {
		out = IoStream.out(nullIc);
		out.write(Array.BYTE.of(1, 2));
		out.write(3);
	}

	@Test
	public void testOutWithByteWrite() throws IOException {
		bout0 = new ByteArrayOutputStream();
		out = IoStream.out(b -> bout0.write(b));
		out.write(Array.BYTE.of(1, 2));
		out.write(3);
		Assert.array(bout0.toByteArray(), 1, 2, 3);
	}

	@Test
	public void testOutWithNullArrayWrite() throws IOException {
		out = IoStream.out((Write) null);
		out.write(Array.BYTE.of(1, 2));
		out.write(3);
	}

	@Test
	public void testOutWithArrayWrite() throws IOException {
		bout0 = new ByteArrayOutputStream();
		out = IoStream.out((b, off, len) -> bout0.write(b, off, len));
		out.write(Array.BYTE.of(1, 2));
		out.write(3);
		Assert.array(bout0.toByteArray(), 1, 2, 3);
	}

	// FilterOutputStream

	@Test
	public void testFilterOutWithNullByteWrite() throws IOException {
		bout0 = new ByteArrayOutputStream();
		out = IoStream.filterOut(bout0, nullOip);
		out.write(Array.BYTE.of(1, 2));
		out.write(3);
		Assert.array(bout0.toByteArray(), 1, 2, 3);
	}

	@Test
	public void testFilterOutWithByteWrite() throws IOException {
		bout0 = new ByteArrayOutputStream();
		bout1 = new ByteArrayOutputStream();
		out = IoStream.filterOut(bout1, (_, b) -> writeMax(bout0, 3, b));
		out.write(Array.BYTE.of(1, 2, 3, 4));
		out.write(5);
		Assert.array(bout0.toByteArray(), 1, 2, 3);
		Assert.array(bout1.toByteArray(), 4, 5);
	}

	@Test
	public void testFilterOutWithNullArrayWrite() throws IOException {
		bout0 = new ByteArrayOutputStream();
		out = IoStream.filterOut(bout0, (FilterWrite) null);
		out.write(Array.BYTE.of(1, 2));
		out.write(3);
		Assert.array(bout0.toByteArray(), 1, 2, 3);
	}

	@Test
	public void testFilterOutWithArrayWrite() throws IOException {
		bout0 = new ByteArrayOutputStream();
		bout1 = new ByteArrayOutputStream();
		out = IoStream.filterOut(bout1, (_, b, off, len) -> writeMax(bout0, 3, b, off, len));
		out.write(Array.BYTE.of(1, 2, 3));
		out.write(Array.BYTE.of(4, 5));
		out.write(6);
		Assert.array(bout0.toByteArray(), 1, 2, 3);
		Assert.array(bout1.toByteArray(), 4, 5, 6);
	}

	// other

	@Test
	public void testNullPrintStream() throws IOException {
		out = IoStream.nullPrint();
		out.write(-1);
		out.write(new byte[1000]);
		out.write(new byte[1000], 1, 999);
	}

	// support

	private ByteArrayInputStream bin(int... bytes) {
		return new ByteArrayInputStream(Array.BYTE.of(bytes));
	}

	private static Integer readOrNull(InputStream in) throws IOException {
		return in.available() == 0 ? null : in.read();
	}

	private static Integer readOrNull(InputStream in, byte[] b, int off, int len)
		throws IOException {
		return in.available() == 0 ? null : in.read(b, off, len);
	}

	private static Integer readOrError(InputStream in) throws IOException {
		return in.available() == 0 ? Assert.throwIo() : in.read();
	}

	private static Integer availableOrNull(InputStream in) throws IOException {
		return in.available() == 0 ? null : in.available();
	}

	private static boolean writeMax(ByteArrayOutputStream out, int max, int b) {
		if (out.size() >= max) return false;
		out.write(b);
		return true;
	}

	private static boolean writeMax(ByteArrayOutputStream out, int max, byte[] b, int off,
		int len) {
		if (out.size() >= max) return false;
		out.write(b, off, len);
		return true;
	}

	private static void assertReadBytes(InputStream in, byte[] bytes, int... actual)
		throws IOException {
		Assert.equal(in.read(bytes), actual.length);
		Assert.array(Arrays.copyOf(bytes, actual.length), actual);
	}

	private static void assertReadBytes(InputStream in, int... actual) throws IOException {
		byte[] b = new byte[actual.length];
		Assert.equal(in.read(b), b.length);
		Assert.array(b, actual);
	}

	private static void assertReadNBytes(InputStream in, int... actual) throws IOException {
		byte[] b = new byte[actual.length];
		Assert.equal(in.readNBytes(b, 0, b.length), b.length);
		Assert.array(b, actual);
	}
}
