package ceri.common.data;

import org.junit.Test;
import ceri.common.test.Assert;

public class LongAccessorBehavior {

	@Test
	public void shouldCreateEmptyInstance() {
		var accessor = LongAccessor.ofNull(0);
		Assert.equal(accessor.isEmpty(), true);
		Assert.equal(accessor.length(), 0);
		Assert.thrown(() -> accessor.setLong(0, 0xff));
		Assert.thrown(() -> accessor.getLong(0));
		Assert.equal(accessor.fill(0, 0xff), 0);
		Assert.thrown(() -> accessor.fill(0, 1, 0xff));
	}

	@Test
	public void shouldCreateNullInstance() {
		var accessor = LongAccessor.ofNull(5);
		Assert.equal(accessor.isEmpty(), false);
		Assert.equal(accessor.length(), 5);
		accessor.setLong(0, 0xff);
		Assert.equal(accessor.getLong(0), 0L);
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
		var accessor = LongAccessor.ofNull(5);
		Assert.equal(accessor.slice(0).length(), 5);
		Assert.equal(accessor.slice(5).length(), 0);
		Assert.equal(accessor.slice(2, 2).length(), 2);
	}

	private static LongAccessor accessor(int length) {
		long[] array = new long[length];
		return new LongAccessor() {
			@Override
			public int length() {
				return length;
			}

			@Override
			public long getLong(int index) {
				return array[index];
			}

			@Override
			public int setLong(int index, long value) {
				array[index] = value;
				return index + 1;
			}
		};
	}
}
