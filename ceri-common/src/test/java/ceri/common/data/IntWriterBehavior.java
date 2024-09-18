package ceri.common.data;

import static ceri.common.collection.ArrayUtil.ints;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertThrown;
import static java.lang.Integer.MAX_VALUE;
import static java.lang.Integer.MIN_VALUE;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.data.IntArray.Mutable;
import ceri.common.function.ExceptionConsumer;
import ceri.common.validation.ValidationUtil;

public class IntWriterBehavior {
	private static final boolean msb = ByteUtil.BIG_ENDIAN;
	private static final String str = "abc\ud83c\udc39de";
	private static final int[] cp = str.codePoints().toArray();

	@Test
	public void shouldSkipInts() {
		assertInts(3, w -> w.skip(0), 0, 0, 0);
		assertInts(5, w -> w.writeInt(1).skip(3).writeInt(MAX_VALUE), 1, 0, 0, 0, MAX_VALUE);
	}

	@Test
	public void shouldWritePrimitiveValues() {
		assertInts(1, w -> w.writeBool(false), 0);
		assertInts(1, w -> w.writeBool(true), 1);
		assertInts(1, w -> w.writeInt(MIN_VALUE), MIN_VALUE);
		assertInts(2, w -> w.writeLong(0xff01007f80L),
			msb ? ints(0xff, 0x1007f80) : ints(0x1007f80, 0xff));
		assertInts(1, w -> w.writeFloat(Float.intBitsToFloat(0xff01007f)), 0xff01007f);
		assertInts(2, w -> w.writeDouble(Double.longBitsToDouble(0xff01007f80L)),
			msb ? ints(0xff, 0x1007f80) : ints(0x1007f80, 0xff));
	}

	@Test
	public void shouldWriteIntAlignedValues() {
		assertInts(2, w -> w.writeLong(0xff01007f80L, true), 0xff, 0x1007f80);
		assertInts(2, w -> w.writeLong(0xff01007f80L, false), 0x1007f80, 0xff);
		assertInts(2, w -> w.writeDouble(Double.longBitsToDouble(0xff01007f80L), true), 0xff,
			0x1007f80);
		assertInts(2, w -> w.writeDouble(Double.longBitsToDouble(0xff01007f80L), false), 0x1007f80,
			0xff);
	}

	@Test
	public void shouldWriteEncodedString() {
		assertInts(6, w -> w.writeString(str), cp);
	}

	@Test
	public void shouldFillInts() {
		assertInts(3, w -> w.fill(0, 0xff), 0, 0, 0);
		assertInts(3, w -> w.fill(2, 0xff), 0xff, 0xff, 0);
		assertThrown(() -> writer(3).fill(4, 0xff));
	}

	@Test
	public void shouldWriteFromIntArray() {
		assertInts(3, w -> w.writeInts(1, 2, 3), 1, 2, 3);
		assertThrown(() -> writer(3).writeInts(1, 2, 3, 4));
	}

	@Test
	public void shouldWriteFromIntProvider() {
		assertInts(3, w -> w.writeFrom(Mutable.wrap(1, 2, 3)), 1, 2, 3);
		assertThrown(() -> writer(3).writeFrom(Mutable.wrap(1, 2, 3, 4)));
	}

	/**
	 * Creates a IntWriter wrapping a fixed-size int array, executes the action on the IntReceiver,
	 * and asserts the ints in the array.
	 */
	private static <E extends Exception> void assertInts(int size,
		ExceptionConsumer<E, IntWriter<?>> action, int... ints) throws E {
		Holder holder = Holder.of(size);
		action.accept(holder.writer);
		assertArray(holder.ints, ints);
	}

	/**
	 * Class to hold a int array and a simple IntReceiver wrapper.
	 */
	public static class Holder {
		public final int[] ints;
		public final SimpleIntWriter writer;

		public static Holder of(int size) {
			int[] ints = new int[size];
			return new Holder(ints, new SimpleIntWriter(ints, 0, ints.length));
		}

		private Holder(int[] ints, SimpleIntWriter writer) {
			this.ints = ints;
			this.writer = writer;
		}

		public Holder reset() {
			Arrays.fill(ints, 0);
			writer.reset();
			return this;
		}
	}

	private static IntWriter<?> writer(int size) {
		return writer(new int[size]);
	}

	/**
	 * Simple IntWriter implementation, wrapping ints.
	 */
	public static IntWriter<?> writer(int[] ints) {
		return writer(ints, 0, ints.length);
	}

	/**
	 * Returns a simple IntWriter implementation, wrapping ints.
	 */
	public static IntWriter<?> writer(int[] ints, int offset, int length) {
		return new SimpleIntWriter(ints, offset, length);
	}

	/**
	 * Simple IntWriter implementation, wrapping ints.
	 */
	public static class SimpleIntWriter implements IntWriter<SimpleIntWriter> {
		private final int[] ints;
		private final int offset;
		private final int length;
		private int index = 0;

		private SimpleIntWriter(int[] ints, int offset, int length) {
			this.ints = ints;
			this.offset = offset;
			this.length = length;
		}

		@Override
		public SimpleIntWriter writeInt(int value) {
			ValidationUtil.validateIndex(length, index);
			ints[offset + index++] = value;
			return this;
		}

		public SimpleIntWriter reset() {
			index = 0;
			return this;
		}
	}

}
