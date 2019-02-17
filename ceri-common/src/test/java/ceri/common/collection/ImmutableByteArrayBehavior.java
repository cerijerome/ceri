package ceri.common.collection;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class ImmutableByteArrayBehavior {
	// Don't overwrite!
	private final byte[] b = { 0, -1, 1, Byte.MAX_VALUE, Byte.MIN_VALUE };

	@Test
	public void shouldFindIndexOfBytes() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		assertThat(ba.indexOf(-1, 1), is(1));
		assertThat(ba.indexOf(1, -1), is(-1));
	}

	@Test
	public void shouldAppendBytes() {
		ImmutableByteArray ba1 = ImmutableByteArray.wrap(0, 1, 2);
		ImmutableByteArray ba2 = ImmutableByteArray.wrap(3, 4, 5);
		assertArray(ba1.append(ImmutableByteArray.EMPTY).copy(), 0, 1, 2);
		assertArray(ba1.append(ba2).copy(), 0, 1, 2, 3, 4, 5);
		assertArray(ba1.append(ba2.copy()).copy(), 0, 1, 2, 3, 4, 5);
	}

	@Test
	public void shouldResize() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(0, 1, 2);
		assertThat(ba.resize(0), is(ImmutableByteArray.EMPTY));
		assertThat(ba.resize(0, 3), is(ba));
		assertThat(ba.resize(0, 3), is(ba));
		assertThat(ba.resize(0, 2), is(ImmutableByteArray.wrap(0, 1)));
		assertThat(ba.resize(1, 3), is(ImmutableByteArray.wrap(1, 2, 0)));
	}

	@Test
	public void shouldIterateEachByte() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ba.forEach(b -> out.write(b));
		assertArray(out.toByteArray(), b);
	}

	@Test
	public void shouldWriteToOutput() throws IOException {
		byte[] bytes = bytes(Byte.MIN_VALUE, -1, 1, Byte.MAX_VALUE, 0);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImmutableByteArray ba = ImmutableByteArray.wrap(bytes);
		ba.writeTo(out, 0, 0);
		assertThat(out.size(), is(0));
		ba.writeTo(out);
		assertArray(out.toByteArray(), bytes);
	}

	@Test
	public void shouldEqualMatchingImmutableByteArrayWithOffset() {
		byte[] bytes = bytes(Byte.MIN_VALUE, -1, 1, Byte.MAX_VALUE, 0);
		ImmutableByteArray ba = ImmutableByteArray.wrap(bytes, 1, 3);
		ImmutableByteArray ba2 = ImmutableByteArray.wrap(bytes, 0, 4);
		ImmutableByteArray ba3 = ImmutableByteArray.wrap(bytes);
		ImmutableByteArray ba4 = ImmutableByteArray.wrap(bytes(-1, 1, -1));
		ImmutableByteArray ba5 = ImmutableByteArray.wrap(1, Byte.MAX_VALUE, 0);
		assertTrue(ba.equals(ba2, 1));
		assertTrue(ba.equals(ba3, 1, 3));
		assertTrue(ba3.equals(1, ba2, 1));
		assertFalse(ba2.equals(1, ba4, 0));
		assertTrue(ba3.equals(2, ba5));
	}

	@Test
	public void shouldEqualMatchingByteArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap((byte) -1, (byte) 1, Byte.MAX_VALUE);
		assertTrue(ba.equals(bytes(-1, 1, Byte.MAX_VALUE)));
		assertTrue(ba.equals(bytes(Byte.MIN_VALUE, -1, 1, Byte.MAX_VALUE), 1));
		assertTrue(ba.equals(bytes(Byte.MIN_VALUE, -1, 1, Byte.MAX_VALUE, 0), 1, 3));
		assertFalse(ba.equals(bytes(-1, 1, 0)));
	}

	@Test
	public void shouldFollowTheEqualsContract() {
		ImmutableByteArray ba0 = ImmutableByteArray.wrap((byte) -1, (byte) 1, Byte.MAX_VALUE);
		ImmutableByteArray ba1 = ImmutableByteArray.wrap(b, 1, 3);
		ImmutableByteArray ba2 = ImmutableByteArray.copyOf(b, 1, 3);
		ImmutableByteArray ba3 = ImmutableByteArray.wrap(b).slice(1, 3);
		ImmutableByteArray ba4 = ImmutableByteArray.wrap(b, 0, 3);
		ImmutableByteArray ba5 = ImmutableByteArray.wrap(b, 1, 2);
		ImmutableByteArray ba6 = ImmutableByteArray.wrap(b, 1, 0);
		ImmutableByteArray ba7 = ImmutableByteArray.wrap(b, 5, 0);
		assertThat(ba0, is(ba1));
		assertThat(ba1, is(ba2));
		assertThat(ba1, is(ba3));
		assertThat(ba3, is(ba2));
		assertThat(ba1, is(not(b)));
		assertThat(ba1, is(not(ba4)));
		assertThat(ba1, is(not(ba5)));
		assertThat(ba1, is(not(ba6)));
		assertThat(ba6, is(ba7));
		assertThat(ba0.hashCode(), is(ba1.hashCode()));
		assertThat(ba2.hashCode(), is(ba3.hashCode()));
		assertThat(ba3.toString(), is(ba1.toString()));
	}

	@Test
	public void shouldCopyTheByteArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		assertArray(ba.copy(3), Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertArray(ba.copy(1, 2), (byte) -1, (byte) 1);
		assertArray(ba.copy(4, 0));
		byte[] b2 = new byte[5];
		ba.copyTo(b2);
		assertArray(b2, b);
		b2 = new byte[5];
		ba.copyTo(0, b2, 2, 2);
		assertArray(b2, (byte) 0, (byte) 0, (byte) 0, (byte) -1, (byte) 0);
	}

	@Test
	public void shouldReturnTheNextOffsetWhenCopyingToAByteArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		byte[] b2 = new byte[10];
		assertThat(ba.copyTo(b2), is(5));
		assertThat(ba.copyTo(b2, 4), is(9));
		assertThat(ba.copyTo(3, b2, 5), is(7));
		assertThat(ba.copyTo(3, b2, 5, 0), is(5));
		assertThat(ba.copyTo(1, b2, 3, 2), is(5));
	}

	@Test
	public void shouldAllowMultipleSlicesOfTheSameArray() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		ImmutableByteArray ba2 = ba.slice(0, 5);
		ImmutableByteArray ba3 = ba2.slice(2);
		ImmutableByteArray ba4 = ba3.slice(1, 1);
		assertArray(ba.copy(), b);
		assertArray(ba2.copy(), b);
		assertArray(ba3.copy(), (byte) 1, Byte.MAX_VALUE, Byte.MIN_VALUE);
		assertArray(ba4.copy(), Byte.MAX_VALUE);
	}

	@Test
	public void shouldOnlyAllowOffsetsAndLengthsWithinRange() {
		assertException(() -> ImmutableByteArray.wrap(b, -1, 0));
		assertException(() -> ImmutableByteArray.wrap(b, 0, -1));
		assertException(() -> ImmutableByteArray.wrap(b, 6, 0));
		assertException(() -> ImmutableByteArray.wrap(b, 5, 1));
		assertException(() -> ImmutableByteArray.wrap(b, 0, 6));
		assertException(() -> ImmutableByteArray.wrap(b, 3, 3));
		ImmutableByteArray ba = ImmutableByteArray.wrap(b);
		assertException(() -> ba.at(-1));
		assertException(() -> ba.at(5));
		assertException(() -> ba.copy(-1, 0));
		assertException(() -> ba.copy(0, -1));
		assertException(() -> ba.copy(5, 1));
		assertException(() -> ba.copy(0, 6));
		assertException(() -> ba.copy(3, 3));
		byte[] b2 = new byte[10];
		assertException(() -> ba.copyTo(-1, b2, 0, 0));
		assertException(() -> ba.copyTo(0, b2, 0, -1));
		assertException(() -> ba.copyTo(5, b2, 0, 1));
		assertException(() -> ba.copyTo(0, b2, 0, 6));
		assertException(() -> ba.copyTo(3, b2, 0, 3));
		assertException(() -> ba.slice(-1, 0));
		assertException(() -> ba.slice(0, -1));
		assertException(() -> ba.slice(5, 1));
		assertException(() -> ba.slice(0, 6));
		assertException(() -> ba.slice(3, 3));
	}

	@Test
	public void shouldMakeAnImmutableCopyOfAGivenByteArray() {
		byte[] b = { 0, -1, 1, Byte.MAX_VALUE, Byte.MIN_VALUE };
		ImmutableByteArray ba = ImmutableByteArray.copyOf(b);
		assertArray(ba.copy(), b);
		b[2] = 0;
		assertThat(ba.at(2), is((byte) 1));
	}

	@Test
	public void shouldConvertBytesToString() {
		ImmutableByteArray ba = ImmutableByteArray.wrap(0x00, 0xff, 0x80, 0x7f, 'a', 'b', 'c');
		assertThat(ba.asString(StandardCharsets.ISO_8859_1), is("\0\u00ff\u0080\u007fabc"));
		assertThat(ba.asString(), is("\0\ufffd\ufffd\u007fabc"));
	}

	private byte[] bytes(int... is) {
		byte[] bytes = new byte[is.length];
		for (int i = 0; i < is.length; i++)
			bytes[i] = (byte) is[i];
		return bytes;
	}

}
