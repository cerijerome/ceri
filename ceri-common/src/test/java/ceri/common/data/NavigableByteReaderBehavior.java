package ceri.common.data;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertStream;
import static ceri.common.test.TestUtil.assertThrown;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.junit.Test;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteArray.Mutable;

public class NavigableByteReaderBehavior {
	private static final byte[] ascii = "abcde".getBytes(StandardCharsets.ISO_8859_1);
	private static final byte[] utf8 = "abcde".getBytes(StandardCharsets.UTF_8);
	private static final byte[] defCset = "abcde".getBytes(Charset.defaultCharset());

	@Test
	public void shouldCreateFromByteArray() {
		assertArray(reader(1, 2, 3).readBytes(), 1, 2, 3);
	}

	@Test
	public void shouldCreateFromByteProvider() {
		assertArray(NavigableByteReader.of(Immutable.wrap(1, 2, 3), 1).readBytes(), 2, 3);
	}

	@Test
	public void shouldMarkAndResetPosition() {
		var r = reader(1, 2, 3, 4, 5);
		assertBytes(r.skip(2).mark(), 3, 4, 5);
		assertThat(r.marked(), is(3));
		assertBytes(r.reset(), 3, 4, 5);
	}

	@Test
	public void shouldReadByte() {
		var r = reader(-1, 2);
		assertThat(r.readByte(), is((byte) -1));
		assertThat(r.readByte(), is((byte) 2));
	}

	@Test
	public void shouldReadByteAlignedValues() {
		assertThat(reader(0xff, 0x80, 0, 0).readEndian(3, true), is(0xff8000L));
		assertThat(reader(0xff, 0x80, 0, 0).readEndian(3, false), is(0x80ffL));
	}

	@Test
	public void shouldReadStrings() {
		assertThat(reader(ascii).readAscii(), is("abcde"));
		assertThat(reader(utf8).readUtf8(), is("abcde"));
		assertThat(reader(defCset).readString(), is("abcde"));
		assertThat(reader(defCset).readString(Charset.defaultCharset()), is("abcde"));
	}

	@Test
	public void shouldReadBytes() {
		assertArray(reader(0, -1, 2).readBytes(), 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2).readBytes(5));
	}

	@Test
	public void shouldReadIntoByteArray() {
		byte[] bytes = new byte[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(bytes), is(3));
		assertArray(bytes, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(bytes, 1, 3));
		assertThrown(() -> reader(0, -1).readInto(bytes));
	}

	@Test
	public void shouldReadIntoByteReceiver() {
		byte[] bytes = new byte[3];
		assertThat(reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes)), is(3));
		assertArray(bytes, 0, -1, 2);
		assertThrown(() -> reader(0, -1, 2, -3, 4).readInto(Mutable.wrap(bytes), 1, 3));
		assertThrown(() -> reader(0, -1).readInto(Mutable.wrap(bytes)));
	}

	@Test
	public void shouldTransferToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertThat(reader(0, -1, 2).transferTo(out), is(3));
		assertArray(out.toByteArray(), 0, -1, 2);
	}

	@Test
	public void shouldStreamUnsignedBytes() {
		assertStream(reader(0, -1, 2, -3, 4).ustream(), 0, 0xff, 2, 0xfd, 4);
		assertThrown(() -> reader(0, -1, 2).ustream(5).toArray());
	}

	@Test
	public void shouldSliceProvidedByteRange() {
		assertBytes(reader(0, -1, 2, -3, 4).skip(2).slice(), 2, -3, 4);
		assertBytes(reader(0, -1, 2, -3, 4).skip(2).slice(0));
		assertBytes(reader(0, -1, 2, -3, 4).skip(4).slice(-3), -1, 2, -3);
	}

	private static void assertBytes(NavigableByteReader r, int... bytes) {
		assertArray(r.readBytes(), bytes);
	}

	private static NavigableByteReader reader(int... bytes) {
		return NavigableByteReader.of(bytes);
	}

	private static NavigableByteReader reader(byte[] bytes) {
		return NavigableByteReader.of(bytes);
	}

}
