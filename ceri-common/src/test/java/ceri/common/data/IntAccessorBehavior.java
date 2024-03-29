package ceri.common.data;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertThrown;
import org.junit.Test;

public class IntAccessorBehavior {

	@Test
	public void shouldCreateEmptyInstance() {
		var accessor = IntAccessor.ofNull(0);
		assertEquals(accessor.isEmpty(), true);
		assertEquals(accessor.length(), 0);
		assertThrown(() -> accessor.setInt(0, 0xff));
		assertThrown(() -> accessor.getInt(0));
		assertEquals(accessor.fill(0, 0xff), 0);
		assertThrown(() -> accessor.fill(0, 1, 0xff));
	}

	@Test
	public void shouldCreateNullInstance() {
		var accessor = IntAccessor.ofNull(5);
		assertEquals(accessor.isEmpty(), false);
		assertEquals(accessor.length(), 5);
		accessor.setInt(0, 0xff);
		assertEquals(accessor.getInt(0), 0);
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
		var accessor = IntAccessor.ofNull(5);
		assertEquals(accessor.slice(0).length(), 5);
		assertEquals(accessor.slice(5).length(), 0);
		assertEquals(accessor.slice(2, 2).length(), 2);
	}

	private static IntAccessor accessor(int length) {
		int[] array = new int[length];
		return new IntAccessor() {
			@Override
			public int length() {
				return length;
			}

			@Override
			public int getInt(int index) {
				return array[index];
			}

			@Override
			public int setInt(int index, int value) {
				array[index] = value;
				return index + 1;
			}
		};
	}
}
