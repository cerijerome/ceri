package ceri.common.data;

import static ceri.common.test.Assert.assertEquals;
import org.junit.Test;
import ceri.common.test.Assert;

public class ByteAccessorBehavior {

	@Test
	public void shouldCreateEmptyInstance() {
		var accessor = ByteAccessor.ofNull(0);
		assertEquals(accessor.isEmpty(), true);
		assertEquals(accessor.length(), 0);
		Assert.thrown(() -> accessor.setByte(0, 0xff));
		Assert.thrown(() -> accessor.getByte(0));
		assertEquals(accessor.fill(0, 0xff), 0);
		Assert.thrown(() -> accessor.fill(0, 1, 0xff));
	}

	@Test
	public void shouldCreateNullInstance() {
		var accessor = ByteAccessor.ofNull(5);
		assertEquals(accessor.isEmpty(), false);
		assertEquals(accessor.length(), 5);
		accessor.setByte(0, 0xff);
		assertEquals(accessor.getByte(0), (byte) 0);
		assertEquals(accessor.fill(0, 0xff), 5);
		Assert.thrown(() -> accessor.fill(1, 5, 0xff));
	}

	@Test
	public void shouldSlice() {
		var accessor = accessor(3);
		assertEquals(accessor.slice(1, 0).length(), 0);
		assertEquals(accessor.slice(0).length(), 3);
		Assert.thrown(() -> accessor.slice(0, 2)); // Unsupported
		Assert.thrown(() -> accessor.slice(1, 2)); // Unsupported
	}

	@Test
	public void shouldCreateNullSlice() {
		var accessor = ByteAccessor.ofNull(5);
		assertEquals(accessor.slice(0).length(), 5);
		assertEquals(accessor.slice(5).length(), 0);
		assertEquals(accessor.slice(2, 2).length(), 2);
	}

	private static ByteAccessor accessor(int length) {
		byte[] array = new byte[length];
		return new ByteAccessor() {
			@Override
			public int length() {
				return length;
			}

			@Override
			public byte getByte(int index) {
				return array[index];
			}

			@Override
			public int setByte(int index, int value) {
				array[index] = (byte) value;
				return index + 1;
			}
		};
	}
}
