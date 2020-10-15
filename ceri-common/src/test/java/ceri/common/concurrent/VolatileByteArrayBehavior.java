package ceri.common.concurrent;

import static ceri.common.test.TestUtil.assertArray;
import static ceri.common.test.TestUtil.assertThat;
import static org.hamcrest.CoreMatchers.is;
import org.junit.Test;
import ceri.common.collection.ArrayUtil;
import ceri.common.data.ByteArray;
import ceri.common.data.ByteProvider;

public class VolatileByteArrayBehavior {

	@Test
	public void shouldCreateWithGivenSize() {
		VolatileByteArray a = VolatileByteArray.of(4);
		assertArray(a.copy(0), 0, 0, 0, 0);
	}

	@Test
	public void shouldCreateFromByteProvider() {
		ByteProvider b = ByteArray.Immutable.wrap(1, 2, 3);
		VolatileByteArray a = VolatileByteArray.wrap(b.copy(0));
		a.setByte(1, 0);
		assertArray(a.copy(0), 1, 0, 3);
		assertArray(b.copy(0), 1, 2, 3);
	}

	@Test
	public void shouldCreateACopyFromByteArray() {
		byte[] b = ArrayUtil.bytes(0xff, 0x80, 0x7f);
		VolatileByteArray a = VolatileByteArray.copyOf(ArrayUtil.bytes(0xff, 0x80, 0x7f));
		a.setByte(2, 0);
		assertArray(b, 0xff, 0x80, 0x7f);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldCreateByWrappingBytes() {
		byte[] b = ArrayUtil.bytes(0xff, 0x80, 0x7f);
		VolatileByteArray a = VolatileByteArray.wrap(b);
		a.setByte(2, 0);
		assertArray(b, 0xff, 0x80, 0);
		assertArray(a.copy(0), 0xff, 0x80, 0);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertThat(VolatileByteArray.of(0).isEmpty(), is(true));
		assertThat(VolatileByteArray.wrap().isEmpty(), is(true));
		assertThat(VolatileByteArray.wrap(1).isEmpty(), is(false));
	}

	@Test
	public void shouldCreateSlicesOfTheArray() {
		VolatileByteArray a0 = VolatileByteArray.wrap(1, 2, 3, 4, 5);
		VolatileByteArray a1 = a0.slice(2);
		VolatileByteArray a2 = a0.slice(0, 3);
		VolatileByteArray a3 = a2.slice(2);
		a0.setByte(2, 0xff);
		assertArray(a0.copy(0), 1, 2, 0xff, 4, 5);
		assertArray(a1.copy(0), 0xff, 4, 5);
		assertArray(a2.copy(0), 1, 2, 0xff);
		assertArray(a3.copy(0), 0xff);
	}

}
