package ceri.common.data;

import static ceri.common.test.TestUtil.assertAllNotEqual;
import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertByte;
import static ceri.common.test.TestUtil.assertThrown;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray.Immutable;
import ceri.common.data.ByteArray.Mutable;

public class ByteArrayBehavior {

	/* Immutable tests */

	@Test
	public void shouldNotBreachImmutableEqualsContract() {
		Immutable t = Immutable.wrap(1, 2, 3);
		Immutable eq0 = Immutable.wrap(1, 2, 3);
		Immutable eq1 = Immutable.copyOf(ArrayUtil.bytes(1, 2, 3));
		Immutable eq2 = Immutable.copyOf(ArrayUtil.bytes(0, 1, 2, 3, 4), 1, 3);
		Immutable ne0 = Immutable.wrap(1, 2, 4);
		Immutable ne1 = Immutable.wrap(1, 2, 3, 0);
		Immutable ne2 = Immutable.wrap();
		exerciseEquals(t, eq0, eq1, eq2);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateImmutableCopy() {
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		Immutable im = Immutable.copyOf(bytes);
		bytes[1] = 0;
		assertByte(im.getByte(1), 2);
	}

	@Test
	public void shouldCreateImmutableCopyOfStream() {
		assertThat(Immutable.wrap(IntStream.of(1, 2, 3)).isEqualTo(0, 1, 2, 3), is(true));
	}

	@Test
	public void shouldCreateImmutableByteWrapper() {
		assertThat(Immutable.wrap(ArrayUtil.bytes(1, 2, 3)).isEqualTo(0, 1, 2, 3), is(true));
		assertThat(Immutable.wrap(ArrayUtil.bytes(1, 2, 3), 3).isEmpty(), is(true));
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		Immutable im = Immutable.wrap(bytes);
		bytes[1] = 0;
		assertByte(im.getByte(1), 0);
	}

	@Test
	public void shouldCreateImmutableSlice() {
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).slice(5).isEmpty(), is(true));
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).slice(0, 2).isEqualTo(0, 1, 2), is(true));
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).slice(5, -2).isEqualTo(0, 4, 5), is(true));
		Immutable im = Immutable.wrap(1, 2, 3);
		assertTrue(im.slice(0) == im);
	}

	/* Mutable tests */

	@Test
	public void shouldNotBreachMutableEqualsContract() {
		Mutable t = Mutable.of(3);
		Mutable eq0 = Mutable.of(3);
		Mutable eq1 = Mutable.wrap(new byte[3]);
		Mutable eq2 = Mutable.wrap(new byte[5], 1, 3);
		Mutable eq3 = Mutable.wrap(0, 0, 0);
		Mutable ne0 = Mutable.of(4);
		Mutable ne1 = Mutable.wrap(0, 0, 1);
		Mutable ne2 = Mutable.wrap();
		exerciseEquals(t, eq0, eq1, eq2, eq3);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldCreateMutableSlice() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.slice(5).isEmpty(), is(true));
		assertThat(m.slice(0, 2).isEqualTo(0, 1, 2), is(true));
		assertThat(m.slice(5, -2).isEqualTo(0, 4, 5), is(true));
		assertTrue(m.slice(0) == m);
	}

	@Test
	public void shouldSetByte() {
		Mutable m = Mutable.of(3);
		assertThat(m.setByte(1, 0xff), is(2));
		assertThat(m.isEqualTo(0, 0, 0xff, 0), is(true));
	}

	@Test
	public void shouldSetEndianBytes() {
		byte[] bytes = ArrayUtil.bytes(1, 2, 3);
		Mutable m = Mutable.wrap(bytes);
		assertThat(m.setEndian(1, 2, 0x1234, true), is(3));
		assertArray(bytes, 1, 0x12, 0x34);
		assertThat(m.setEndian(1, 2, 0x1234, false), is(3));
		assertArray(bytes, 1, 0x34, 0x12);
	}

	@Test
	public void shouldFillBytes() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.fill(1, 2, 0xff), is(3));
		assertThat(m.isEqualTo(0, 1, 0xff, 0xff, 4, 5), is(true));
		assertThrown(() -> m.fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromArray() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.setBytes(3, -4, -5), is(5));
		assertThat(m.isEqualTo(0, 1, 2, 3, -4, -5), is(true));
		assertThrown(() -> m.copyFrom(3, ArrayUtil.bytes(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, ArrayUtil.bytes(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldCopyFromByteProvider() {
		Mutable m = Mutable.wrap(1, 2, 3, 4, 5);
		assertThat(m.copyFrom(3, Immutable.wrap(-4, -5)), is(5));
		assertThat(m.isEqualTo(0, 1, 2, 3, -4, -5), is(true));
		assertThrown(() -> m.copyFrom(3, Immutable.wrap(1, 2, 3), 0));
		assertThrown(() -> m.copyFrom(0, Immutable.wrap(1, 2, 3), 2, 2));
	}

	@Test
	public void shouldReadFromInputStream() throws IOException {
		Mutable m = Mutable.of(5);
		ByteArrayInputStream in = new ByteArrayInputStream(ArrayUtil.bytes(1, 2, 3));
		assertThat(m.readFrom(2, in, 2), is(4));
		assertThat(m.isEqualTo(0, 0, 0, 1, 2, 0), is(true));
	}

	/* ByteArray base tests */

	@Test
	public void shouldGetEndianBytes() {
		assertThat(Immutable.wrap(0, 0x7f, 0x80, 0).getEndian(1, 2, true), is(0x7f80L));
		assertThat(Immutable.wrap(0, 0x7f, 0x80, 0).getEndian(1, 2, false), is(0x807fL));
		assertThrown(() -> Immutable.wrap(0, 0x7f, 0x80).getEndian(1, 3, false));
	}

	@Test
	public void shouldGetString() {
		assertThat(Immutable.wrap("abcde".getBytes()).getString(0), is("abcde"));
		assertThrown(() -> Immutable.wrap("abcde".getBytes()).getString(3, 10));
	}

	@Test
	public void shouldCopyToByteReceiver() {
		Mutable m = Mutable.of(3);
		assertThat(Immutable.wrap(1, 2, 3).copyTo(1, m, 1), is(3));
		assertThat(m.isEqualTo(0, 0, 2, 3), is(true));
		assertThrown(() -> Immutable.wrap(0, 1, 2).copyTo(0, m, 4));
	}

	@Test
	public void shouldWriteToOutputStream() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		assertThat(Immutable.wrap(1, 2, 3).writeTo(1, out, 0), is(1));
		assertArray(out.toByteArray());
		assertThat(Immutable.wrap(1, 2, 3).writeTo(1, out), is(3));
		assertArray(out.toByteArray(), 2, 3);
		assertThrown(() -> Immutable.wrap(0, 1, 2).writeTo(0, out, 4));
	}

	@Test
	public void shouldDetermineIfEqualToBytes() {
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, ArrayUtil.bytes(2, 3, 4)), is(true));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(2, ArrayUtil.bytes(1, 2)), is(false));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(0, ArrayUtil.bytes(1, 2), 0, 3), is(false));
	}

	@Test
	public void shouldDetermineIfEqualToProviderBytes() {
		assertThat(Immutable.wrap(1, 2, 3, 4, 5).isEqualTo(1, Immutable.wrap(2, 3, 4)), is(true));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(2, Immutable.wrap(1, 2)), is(false));
		assertThat(Immutable.wrap(1, 2, 3).isEqualTo(0, Immutable.wrap(1, 2), 0, 3), is(false));
	}

}
