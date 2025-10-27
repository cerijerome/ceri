package ceri.common.data;

import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.array.ArrayUtil;
import ceri.common.function.Excepts.Consumer;
import ceri.common.test.Assert;

public class IntReceiverBehavior {
	private static final boolean msb = ByteUtil.IS_BIG_ENDIAN;
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	/* IntReceiver tests */

	@Test
	public void shouldProvideAnEmptyInstance() {
		Assert.equal(IntReceiver.empty().length(), 0);
		Assert.thrown(() -> IntReceiver.empty().setInt(0, 0));
	}

	@Test
	public void shouldDetermineIfEmpty() {
		Assert.no(receiver(3).isEmpty());
		Assert.yes(receiver(0).isEmpty());
		Assert.yes(IntReceiver.empty().isEmpty());
	}

	@Test
	public void shouldReceivePrimitiveValues() {
		assertInts(2, br -> Assert.equal(br.setBool(0, true), 1), 1, 0);
		assertInts(2, br -> Assert.equal(br.setBool(1, false), 2), 0, 0);
		assertInts(1, br -> Assert.equal(br.setInt(0, -1), 1), -1);
		assertInts(3, br -> Assert.equal(br.setInt(1, MAX_VALUE), 2), 0, MAX_VALUE, 0);
		assertInts(2, br -> Assert.equal(br.setInt(0, MIN_VALUE), 1), MIN_VALUE, 0);
		assertInts(2, br -> Assert.equal(br.setLong(0, 0x1234567890L), 2),
			msb ? ArrayUtil.ints.of(0x12, 0x34567890) : ArrayUtil.ints.of(0x34567890, 0x12));
		assertInts(2, br -> Assert.equal(br.setFloat(0, Float.intBitsToFloat(0x12345678)), 1),
			0x12345678, 0);
		assertInts(2,
			br -> Assert.equal(br.setDouble(0, Double.longBitsToDouble(0x1234567890L)), 2),
			msb ? ArrayUtil.ints.of(0x12, 0x34567890) : ArrayUtil.ints.of(0x34567890, 0x12));
	}

	@Test
	public void shouldReceiveIntAlignedValues() {
		assertInts(2, br -> Assert.equal(br.setLong(0, 0x1234567890L, true), 2), 0x12, 0x34567890);
		assertInts(2, br -> Assert.equal(br.setLong(0, 0x1234567890L, false), 2), 0x34567890, 0x12);
		assertInts(2,
			br -> Assert.equal(br.setDouble(0, Double.longBitsToDouble(0x1234567890L), true), 2),
			0x12, 0x34567890);
		assertInts(2,
			br -> Assert.equal(br.setDouble(0, Double.longBitsToDouble(0x1234567890L), false), 2),
			0x34567890, 0x12);
	}

	@Test
	public void shouldReceiveCodePoints() {
		assertInts(6, br -> Assert.equal(br.setString(0, str), 6), cp);
	}

	@Test
	public void shouldSetEachInt() {
		assertInts(0, br -> Assert.equal(br.setEachInt(i -> -i), 0));
		assertInts(3, br -> Assert.equal(br.setEachInt(i -> -i), 3), 0, -1, -2);
	}

	@Test
	public void shouldSliceReceivingIntRange() {
		int[] ints = new int[5];
		IntReceiver br = receiver(ints, 0, ints.length);
		Assert.yes(br.slice(5).isEmpty());
		Assert.yes(br.slice(4, 0).isEmpty());
		Assert.equal(br.slice(0), br);
		Assert.thrown(() -> br.slice(1, 4));
		Assert.thrown(() -> br.slice(0, 4));
	}

	@Test
	public void shouldFillInts() {
		assertInts(5, br -> Assert.equal(br.fill(2, 0xff), 5), 0, 0, 0xff, 0xff, 0xff);
		Assert.thrown(() -> receiver(5).fill(3, 3, 0));
	}

	@Test
	public void shouldCopyFromIntArray() {
		assertInts(5, br -> Assert.equal(br.setInts(1, 1, 2, 3), 4), 0, 1, 2, 3, 0);
		Assert.thrown(() -> receiver(5).setInts(4, 1, 2, 3));
	}

	@Test
	public void shouldCopyFromIntProvider() {
		IntProvider bp = IntProviderBehavior.provider(1, 2, 3);
		assertInts(5, br -> Assert.equal(br.copyFrom(1, bp), 4), 0, 1, 2, 3, 0);
		Assert.thrown(() -> receiver(5).copyFrom(4, bp));
	}

	@Test
	public void shouldProvideWriterAccessToInts() {
		assertInts(5, br -> br.writer(3).fill(0xff), 0, 0, 0, 0xff, 0xff);
		assertInts(5, br -> br.writer(3, 0).fill(0xff), 0, 0, 0, 0, 0);
		assertInts(5, br -> br.writer(5).fill(0xff), 0, 0, 0, 0, 0);
		Assert.thrown(() -> receiver(5).writer(6));
		Assert.thrown(() -> receiver(5).writer(1, 5));
		Assert.thrown(() -> receiver(5).writer(-1));
	}

	/* IntReceiver.Writer tests */

	@Test
	public void shouldWriteInt() {
		assertInts(3, br -> br.writer(0).writeInt(1).writeInt(2), 1, 2, 0);
		Assert.thrown(() -> receiver(3).writer(1, 0).writeInt(2));
	}

	@Test
	public void shouldWriteString() {
		assertInts(5, br -> br.writer(0).writeString("abc"), 'a', 'b', 'c', 0, 0);
	}

	@Test
	public void shouldWriteFromIntArray() {
		int[] ints = ArrayUtil.ints.of(1, 2, 3, 4, 5);
		assertInts(5, br -> br.writer(1).writeFrom(ints, 1, 3), 0, 2, 3, 4, 0);
	}

	@Test
	public void shouldWriteFromIntProvider() {
		IntProvider bp = IntArray.Immutable.wrap(1, 2, 3, 4, 5);
		assertInts(5, br -> br.writer(1).writeFrom(bp, 1, 3), 0, 2, 3, 4, 0);
	}

	@Test
	public void shouldSkipInts() {
		assertInts(5, br -> br.writer(1).skip(2).fill(1), 0, 0, 0, 1, 1);
	}

	@Test
	public void shouldReturnWriterIntProvider() {
		IntReceiver br = receiver(ArrayUtil.ints.of(1, 2, 3, 4, 5));
		Assert.equal(br.writer(0).receiver(), br);
		Assert.yes(br.writer(5, 0).receiver().isEmpty());
		Assert.thrown(() -> br.writer(2).receiver());
	}

	@Test
	public void shouldSliceWriter() {
		assertInts(5, br -> br.writer(2).slice().fill(1), 0, 0, 1, 1, 1);
		assertInts(5, br -> br.writer(2).slice(2).fill(1), 0, 0, 1, 1, 0);
		Assert.thrown(() -> receiver(5).writer(2).slice(4));
		Assert.thrown(() -> receiver(5).writer(2).slice(-1));
	}

	/* Support methods */

	/**
	 * Creates a IntReceiver wrapping a fixed-size int array, executes the action on the
	 * IntReceiver, and asserts the ints in the array.
	 */
	private static <E extends Exception> void assertInts(int size, Consumer<E, IntReceiver> action,
		int... ints) throws E {
		Holder holder = Holder.of(size);
		action.accept(holder.receiver);
		Assert.array(holder.ints, ints);
	}

	/**
	 * Class to hold a int array and a simple IntReceiver wrapper.
	 */
	public static class Holder {
		public final int[] ints;
		public final IntReceiver receiver;

		public static IntReceiverBehavior.Holder of(int size) {
			int[] ints = new int[size];
			return new Holder(ints, receiver(ints));
		}

		private Holder(int[] ints, IntReceiver receiver) {
			this.ints = ints;
			this.receiver = receiver;
		}

		public Holder clear() {
			Arrays.fill(ints, 0);
			return this;
		}
	}

	private static IntReceiver receiver(int size) {
		return receiver(new int[size]);
	}

	/**
	 * Simple IntReceiver implementation, wrapping ints.
	 */
	public static IntReceiver receiver(int[] ints) {
		return receiver(ints, 0, ints.length);
	}

	/**
	 * Simple IntReceiver implementation, wrapping ints.
	 */
	public static IntReceiver receiver(int[] ints, int offset, int length) {
		return new IntReceiver() {
			@Override
			public int length() {
				return length;
			}

			@Override
			public int setInt(int index, int value) {
				ints[offset + index] = value;
				return index + 1;
			}
		};
	}
}
