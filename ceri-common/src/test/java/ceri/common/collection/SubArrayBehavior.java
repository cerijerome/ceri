package ceri.common.collection;

import static ceri.common.collection.ArrayUtil.array;
import static ceri.common.collection.ArrayUtil.bytes;
import static ceri.common.collection.ArrayUtil.ints;
import static ceri.common.collection.ArrayUtil.longs;
import static ceri.common.test.AssertUtil.assertAllNotEqual;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.TestUtil.exerciseEquals;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;
import ceri.common.test.TestUtil;

public class SubArrayBehavior {

	@Test
	public void shouldNotBreachEqualsContractForTypes() {
		String[] array = { "", "test", null, "TEST", "x" };
		var t = SubArray.of(array, 1, 3);
		var eq0 = SubArray.of(array, 1, 3);
		var eq1 = SubArray.of(array("test", null, "TEST"), 0, 3);
		var ne0 = SubArray.of(array, 1, 4);
		var ne1 = SubArray.of(array, 0, 3);
		var ne2 = SubArray.of(array("test", "", "TEST"), 0, 3);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldNotBreachEqualsContractForBytes() {
		byte[] array = bytes(0, 1, 2, 3, 4);
		var t = SubArray.of(array, 1, 3);
		var eq0 = SubArray.of(array, 1, 3);
		var eq1 = SubArray.of(bytes(1, 2, 3), 0, 3);
		var ne0 = SubArray.of(array, 1, 4);
		var ne1 = SubArray.of(array, 0, 3);
		var ne2 = SubArray.of(bytes(1, 1, 3), 0, 3);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldNotBreachEqualsContractForInts() {
		int[] array = ints(0, 1, 2, 3, 4);
		var t = SubArray.of(array, 1, 3);
		var eq0 = SubArray.of(array, 1, 3);
		var eq1 = SubArray.of(ints(1, 2, 3), 0, 3);
		var ne0 = SubArray.of(array, 1, 4);
		var ne1 = SubArray.of(array, 0, 3);
		var ne2 = SubArray.of(ints(1, 1, 3), 0, 3);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldNotBreachEqualsContractForLongs() {
		long[] array = longs(0, 1, 2, 3, 4);
		var t = SubArray.of(array, 1, 3);
		var eq0 = SubArray.of(array, 1, 3);
		var eq1 = SubArray.of(longs(1, 2, 3), 0, 3);
		var ne0 = SubArray.of(array, 1, 4);
		var ne1 = SubArray.of(array, 0, 3);
		var ne2 = SubArray.of(ints(1, 1, 3), 0, 3);
		exerciseEquals(t, eq0, eq1);
		assertAllNotEqual(t, ne0, ne1, ne2);
	}

	@Test
	public void shouldBeAppliedByFunction() {
		SubArray.Ints sub = SubArray.of(ints(1, 2, 3, 4, 5), 1, 3);
		int[] array = sub.apply(ArrayUtil::copyOf);
		assertArray(array, 2, 3, 4);
	}

	@Test
	public void shouldBeAppliedAsIntByFunction() throws IOException {
		try (var in = TestUtil.inputStream(1, 2, 3)) {
			byte[] bytes = new byte[5];
			assertEquals(SubArray.of(bytes, 1, 3).applyAsInt(in::read), 3);
			assertArray(bytes, 0, 1, 2, 3, 0);
		}
	}

	@Test
	public void shouldBeAcceptedByFunction() {
		var out = new ByteArrayOutputStream();
		SubArray.of(ArrayUtil.bytes(1, 2, 3, 4, 5), 1, 3).accept(out::write);
		assertArray(out.toByteArray(), 2, 3, 4);
	}

	@Test
	public void shouldProvideToIndex() {
		SubArray.Types<String> sub = SubArray.of(array("", "test", null, "Test", "x"), 1, 3);
		String[] array = Arrays.copyOfRange(sub.array, sub.offset, sub.to());
		assertArray(array, "test", null, "Test");
	}

}
