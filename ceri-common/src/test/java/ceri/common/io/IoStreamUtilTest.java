package ceri.common.io;

import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.throwIt;
import static org.hamcrest.CoreMatchers.is;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionIntConsumer;
import ceri.common.function.ExceptionIntSupplier;
import ceri.common.function.ExceptionObjIntPredicate;
import ceri.common.io.IoStreamUtil.FilterRead;
import ceri.common.io.IoStreamUtil.FilterWrite;
import ceri.common.io.IoStreamUtil.Read;
import ceri.common.io.IoStreamUtil.Write;

public class IoStreamUtilTest {
	private final InputStream nullIn = IoStreamUtil.nullIn(); // keeps closed state
	private final ByteArrayInputStream bin0 = new ByteArrayInputStream(bytes(1, 2, 3));
	private final ByteArrayInputStream bin1 = new ByteArrayInputStream(bytes(4, 5, 6));
	private final ByteArrayOutputStream bout0 = new ByteArrayOutputStream();
	private final ByteArrayOutputStream bout1 = new ByteArrayOutputStream();

	@Before
	public void before() {
		bin0.reset();
		bin1.reset();
		bout0.reset();
		bout1.reset();
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(IoStreamUtil.class);
	}

	@Test
	public void testNullInputStream() throws IOException {
		assertThat(nullIn.available(), is(0));
		assertThat(nullIn.read(), is(0));
		assertArray(nullIn.readAllBytes());
		assertArray(nullIn.readNBytes(3), 0, 0, 0);
		assertReadNBytes(nullIn, 0, 0, 0);
	}

	@Test
	public void testNullInputStreamAfterClose() throws IOException {
		try (var nullIn = IoStreamUtil.nullIn()) {
			nullIn.close();
			assertThrown(() -> nullIn.available());
			assertThrown(() -> nullIn.read());
			assertThrown(() -> nullIn.readAllBytes());
			assertThrown(() -> nullIn.readNBytes(3));
			assertThrown(() -> nullIn.readNBytes(new byte[3], 0, 3));
		}
	}

	/* InputStream tests */

	@Test
	public void testInWithNullByteRead() throws IOException {
		try (var in = IoStreamUtil.in((ExceptionIntSupplier<IOException>) null)) {
			assertThat(in.available(), is(0));
			assertThat(in.read(), is(0));
			assertReadBytes(in, 0, 0, 0);
		}
	}

	@Test
	public void testInWithByteRead() throws IOException {
		try (var in = IoStreamUtil.in(() -> bin0.read())) {
			assertThat(in.available(), is(0));
			assertThat(in.read(), is(1));
			assertReadBytes(in, 2, 3);
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void testInWithAvailable() throws IOException {
		try (var in = IoStreamUtil.in(() -> bin0.read(), bin0::available)) {
			assertThat(in.available(), is(3));
		}
	}

	@Test
	public void testInWithNullArrayRead() throws IOException {
		try (var in = IoStreamUtil.in((Read) null)) {
			assertThat(in.available(), is(0));
			assertThat(in.read(), is(0));
			assertReadBytes(in, 0, 0, 0);
		}
	}

	@Test
	public void testInWithArrayRead() throws IOException {
		try (var in = IoStreamUtil.in((b, off, len) -> bin0.read(b, off, len))) {
			assertThat(in.available(), is(0));
			assertThat(in.read(), is(1));
			assertReadBytes(in, 2, 3);
			assertThat(in.read(), is(-1));
		}
	}

	/* FilterInputStream tests */

	@Test
	public void testFilterInWithNullByteRead() throws IOException {
		try (var in = IoStreamUtil.filterIn(bin0,
			(ExceptionFunction<IOException, InputStream, Integer>) null)) {
			assertThat(in.available(), is(3));
			assertThat(in.read(), is(1));
			assertReadBytes(in, new byte[2], 2, 3);
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void testFilterInWithByteRead() throws IOException {
		try (var in = IoStreamUtil.filterIn(bin1, is -> readOrNull(bin0))) {
			assertThat(in.available(), is(3));
			assertThat(in.read(), is(1));
			assertReadBytes(in, 2, 3, 4);
			assertThat(in.skip(1), is(1L));
			assertReadBytes(in, new byte[0]);
			assertReadBytes(in, new byte[3], 6);
			assertThat(in.read(), is(-1));
			assertThat(in.read(new byte[3], 0, 3), is(-1));
		}
	}

	@Test
	public void testFilterInWithAvailable() throws IOException {
		try (var in =
			IoStreamUtil.filterIn(bin1, is -> readOrNull(bin0), is -> availableOrNull(bin0))) {
			assertThat(in.available(), is(3));
			in.read();
			assertThat(in.available(), is(2));
			in.readNBytes(2);
			assertThat(in.available(), is(3));
		}
	}

	@Test
	public void testFilterInWithByteReadError() throws IOException {
		try (var in = IoStreamUtil.filterIn(nullIn, is -> readOrError(bin0))) {
			assertThat(in.read(), is(1));
			assertReadBytes(in, new byte[3], 2, 3);
			assertThrown(in::read);
		}
	}

	@Test
	public void testFilterInWithNullArrayRead() throws IOException {
		try (var in = IoStreamUtil.filterIn(bin0, (FilterRead) null)) {
			assertThat(in.available(), is(3));
			assertThat(in.read(), is(1));
			assertReadBytes(in, new byte[2], 2, 3);
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void testFilterInWithArrayRead() throws IOException {
		try (var in =
			IoStreamUtil.filterIn(bin1, (is, b, off, len) -> readOrNull(bin0, b, off, len))) {
			assertThat(in.available(), is(3));
			assertThat(in.read(), is(1));
			assertReadBytes(in, new byte[3], 2, 3);
			assertThat(in.skip(1), is(1L));
			assertReadBytes(in, new byte[3], 5, 6);
			assertThat(in.read(), is(-1));
		}
	}

	@Test
	public void testFilterInSkip() throws IOException {
		try (var in =
			IoStreamUtil.filterIn(bin1, (is, b, off, len) -> readOrNull(bin0, b, off, len))) {
			assertThat(in.skip(0), is(0L));
			assertThat(in.skip(4), is(4L));
			assertThat(in.skip(4), is(2L));
		}
	}

	/* OutputStream tests */

	@Test
	public void testOutWithNullByteWrite() throws IOException {
		try (var out = IoStreamUtil.out((ExceptionIntConsumer<IOException>) null)) {
			out.write(bytes(1, 2));
			out.write(3);
		}
	}

	@Test
	public void testOutWithByteWrite() throws IOException {
		try (var out = IoStreamUtil.out(b -> bout0.write(b))) {
			out.write(bytes(1, 2));
			out.write(3);
			assertArray(bout0.toByteArray(), 1, 2, 3);
		}
	}

	@Test
	public void testOutWithNullArrayWrite() throws IOException {
		try (var out = IoStreamUtil.out((Write) null)) {
			out.write(bytes(1, 2));
			out.write(3);
		}
	}

	@Test
	public void testOutWithArrayWrite() throws IOException {
		try (var out = IoStreamUtil.out((b, off, len) -> bout0.write(b, off, len))) {
			out.write(bytes(1, 2));
			out.write(3);
			assertArray(bout0.toByteArray(), 1, 2, 3);
		}
	}

	/* FilterOutputStream tests */

	@Test
	public void testFilterOutWithNullByteWrite() throws IOException {
		try (var out =
			IoStreamUtil.filterOut(bout0, (ExceptionObjIntPredicate<IOException, OutputStream>) null)) {
			out.write(bytes(1, 2));
			out.write(3);
			assertArray(bout0.toByteArray(), 1, 2, 3);
		}
	}

	@Test
	public void testFilterOutWithByteWrite() throws IOException {
		try (var out = IoStreamUtil.filterOut(bout1, (os, b) -> writeMax(bout0, 3, b))) {
			out.write(bytes(1, 2, 3, 4));
			out.write(5);
			assertArray(bout0.toByteArray(), 1, 2, 3);
			assertArray(bout1.toByteArray(), 4, 5);
		}
	}

	@Test
	public void testFilterOutWithNullArrayWrite() throws IOException {
		try (var out = IoStreamUtil.filterOut(bout0, (FilterWrite) null)) {
			out.write(bytes(1, 2));
			out.write(3);
			assertArray(bout0.toByteArray(), 1, 2, 3);
		}
	}

	@Test
	public void testFilterOutWithArrayWrite() throws IOException {
		try (var out =
			IoStreamUtil.filterOut(bout1, (os, b, off, len) -> writeMax(bout0, 3, b, off, len))) {
			out.write(bytes(1, 2, 3));
			out.write(bytes(4, 5));
			out.write(6);
			assertArray(bout0.toByteArray(), 1, 2, 3);
			assertArray(bout1.toByteArray(), 4, 5, 6);
		}
	}

	/* Support methods */

	private static Integer readOrNull(InputStream in) throws IOException {
		return in.available() == 0 ? null : in.read();
	}

	private static Integer readOrNull(InputStream in, byte[] b, int off, int len)
		throws IOException {
		return in.available() == 0 ? null : in.read(b, off, len);
	}

	private static Integer readOrError(InputStream in) throws IOException {
		return in.available() == 0 ? throwIt(new IOException("test")) : in.read();
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
		assertThat(in.read(bytes), is(actual.length));
		assertArray(Arrays.copyOf(bytes, actual.length), actual);
	}

	private static void assertReadBytes(InputStream in, int... actual) throws IOException {
		byte[] b = new byte[actual.length];
		assertThat(in.read(b), is(b.length));
		assertArray(b, actual);
	}

	private static void assertReadNBytes(InputStream in, int... actual) throws IOException {
		byte[] b = new byte[actual.length];
		assertThat(in.readNBytes(b, 0, b.length), is(b.length));
		assertArray(b, actual);
	}
}
