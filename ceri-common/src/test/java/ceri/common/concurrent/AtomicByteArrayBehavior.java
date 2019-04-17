package ceri.common.concurrent;

import static ceri.common.data.ByteUtil.bytes;
import static ceri.common.test.TestUtil.assertArray;
import org.junit.Test;
import ceri.common.collection.ImmutableByteArray;

public class AtomicByteArrayBehavior {

	@Test
	public void shouldCreateWithGivenSize() {
		AtomicByteArray a = AtomicByteArray.of(4);
		assertArray(a.copy(), 0, 0, 0, 0);
	}

	@Test
	public void shouldCreateFromImmutableByteArray() {
		ImmutableByteArray b = ImmutableByteArray.wrap(1, 2, 3);
		AtomicByteArray a = AtomicByteArray.from(b);
		a.set(1, 0);
		assertArray(a.copy(), 1, 0, 3);
		assertArray(b.copy(), 1, 2, 3);
	}

	@Test
	public void shouldCreateACopyFromByteArray() {
		byte[] b = bytes(0xff, 0x80, 0x7f);
		AtomicByteArray a = AtomicByteArray.copyOf(bytes(0xff, 0x80, 0x7f));
		a.set(2, 0);
		assertArray(b, 0xff, 0x80, 0x7f);
		assertArray(a.copy(), 0xff, 0x80, 0);
	}

	@Test
	public void shouldCreateByWrappingBytes() {
		byte[] b = bytes(0xff, 0x80, 0x7f);
		AtomicByteArray a = AtomicByteArray.wrap(b);
		a.set(2, 0);
		assertArray(b, 0xff, 0x80, 0);
		assertArray(a.copy(), 0xff, 0x80, 0);
	}

	@Test
	public void shouldCreateSlicesOfTheArray() {
		AtomicByteArray a0 = AtomicByteArray.wrap(1, 2, 3, 4, 5);
		AtomicByteArray a1 = a0.slice(2);
		AtomicByteArray a2 = a0.slice(0, 3);
		AtomicByteArray a3 = a2.slice(2);
		a0.set(2, 0xff);
		assertArray(a0.copy(), 1, 2, 0xff, 4, 5);
		assertArray(a1.copy(), 0xff, 4, 5);
		assertArray(a2.copy(), 1, 2, 0xff);
		assertArray(a3.copy(), 0xff);
	}

}
