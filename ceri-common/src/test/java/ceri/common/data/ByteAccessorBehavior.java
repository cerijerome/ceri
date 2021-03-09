package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class ByteAccessorBehavior {

	@Test
	public void shouldCreateEmptyInstance() {
		var accessor = ByteAccessor.ofNull(0);
		assertEquals(accessor.isEmpty(), true);
		assertEquals(accessor.length(), 0);
		assertThrown(() -> accessor.setByte(0, 0xff));
		assertThrown(() -> accessor.getByte(0));
		assertEquals(accessor.fill(0, 0xff), 0);
		assertThrown(() -> accessor.fill(0, 1, 0xff));
	}

	@Test
	public void shouldCreateNullInstance() {
		var accessor = ByteAccessor.ofNull(5);
		assertEquals(accessor.isEmpty(), false);
		assertEquals(accessor.length(), 5);
		accessor.setByte(0, 0xff);
		assertEquals(accessor.getByte(0), (byte) 0);
		assertEquals(accessor.fill(0, 0xff), 5);
		assertThrown(() -> accessor.fill(1, 5, 0xff));
	}

	@Test
	public void shouldSlice() {
		var accessor = accessor(3);
		assertEquals(accessor.slice(1, 0).length(), 0);
		assertEquals(accessor.slice(0).length(), 3);
		assertThrown(() -> accessor.slice(0, 2)); // Unsupported
		assertThrown(() -> accessor.slice(1, 2)); // Unsupported
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
