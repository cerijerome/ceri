package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;

public class IntAccessorBehavior {

	@Test
	public void shouldCreateEmptyInstance() {
		var accessor = IntAccessor.ofNull(0);
		Assert.equal(accessor.isEmpty(), true);
		Assert.equal(accessor.length(), 0);
		Assert.thrown(() -> accessor.setInt(0, 0xff));
		Assert.thrown(() -> accessor.getInt(0));
		Assert.equal(accessor.fill(0, 0xff), 0);
		Assert.thrown(() -> accessor.fill(0, 1, 0xff));
	}

	@Test
	public void shouldCreateNullInstance() {
		var accessor = IntAccessor.ofNull(5);
		Assert.equal(accessor.isEmpty(), false);
		Assert.equal(accessor.length(), 5);
		accessor.setInt(0, 0xff);
		Assert.equal(accessor.getInt(0), 0);
		Assert.equal(accessor.fill(0, 0xff), 5);
		Assert.thrown(() -> accessor.fill(1, 5, 0xff));
	}

	@Test
	public void shouldSlice() {
		var accessor = accessor(3);
		Assert.equal(accessor.slice(1, 0).length(), 0);
		Assert.equal(accessor.slice(0).length(), 3);
		Assert.thrown(() -> accessor.slice(0, 2)); // Unsupported
		Assert.thrown(() -> accessor.slice(1, 2)); // Unsupported
	}

	@Test
	public void shouldCreateNullSlice() {
		var accessor = IntAccessor.ofNull(5);
		Assert.equal(accessor.slice(0).length(), 5);
		Assert.equal(accessor.slice(5).length(), 0);
		Assert.equal(accessor.slice(2, 2).length(), 2);
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
