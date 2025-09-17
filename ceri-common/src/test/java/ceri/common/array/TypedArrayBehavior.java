package ceri.common.array;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertSame;
import java.util.Objects;
import org.junit.Test;
import ceri.common.text.Formats;
import ceri.common.text.Joiner;

public class TypedArrayBehavior {
	private static final Integer[] NULL = null;
	private static final Integer[] EMPTY = new Integer[0];
	private final Integer[] ints = ArrayUtil.of(-1, null, 1, null, 1);
	private final TypedArray.Type.Integral<Integer> typed = ArrayUtil.ints.box;

	@Test
	public void shouldCreateArray() {
		assertArray(TypedArray.OBJ.array(1), (Object) null);
		assertArray(typed.array(0));
		assertArray(typed.array(1), (Integer) null);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertEquals(typed.isEmpty(NULL), true);
		assertEquals(typed.isEmpty(EMPTY), true);
		assertEquals(typed.isEmpty(ints), false);
	}

	@Test
	public void shouldProvideElementAtIndex() {
		assertEquals(typed.at(NULL, 0), null);
		assertEquals(typed.at(ints, -1), null);
		assertEquals(typed.at(ints, 0), -1);
		assertEquals(typed.at(ints, 1), null);
		assertEquals(typed.at(ints, 2), 1);
		assertEquals(typed.at(ints, 5), null);
	}

	@Test
	public void shouldProvideLastElement() {
		assertEquals(typed.last(NULL), null);
		assertEquals(typed.last(ints), 1);
	}

	@Test
	public void shouldMakeCopyOfElements() {
		assertArray(typed.copyOf(NULL));
		assertArray(typed.copyOf(ints), -1, null, 1, null, 1);
	}

	@Test
	public void shouldCopyElements() {
		assertSame(typed.copy(NULL, NULL), NULL);
		assertArray(typed.copy(NULL, new Integer[3]), null, null, null);
		assertSame(typed.copy(ints, NULL), NULL);
		assertArray(typed.copy(ints, new Integer[3]), -1, null, 1);
	}

	@Test
	public void shouldAppendElements() {
		assertEquals(typed.append(NULL, 0, 3), null);
		assertSame(typed.append(ints, NULL), ints);
		assertArray(typed.append(ints, 0, 3), -1, null, 1, null, 1, 0, 3);
	}

	@Test
	public void shouldInsertElements() {
		assertEquals(typed.insert(NULL, 1, 0), null);
		assertSame(typed.insert(ints, 1, NULL), ints);
		assertArray(typed.insert(ints, 1, 0, 3), -1, 0, 3, null, 1, null, 1);
	}

	@Test
	public void shouldDetermineIfArrayHasElement() {
		assertEquals(typed.has(NULL, null), false);
		assertEquals(typed.has(ints, null), true);
		assertEquals(typed.has(ints, 1), true);
		assertEquals(typed.has(ints, 0), false);
	}

	@Test
	public void shouldDetermineIfElementsContained() {
		assertEquals(typed.contains(NULL, NULL), false);
		assertEquals(typed.contains(NULL, (Integer) null), false);
		assertEquals(typed.contains(ints, NULL), false);
		assertEquals(typed.contains(ints, (Integer) null), true);
		assertEquals(typed.contains(ints, null, 1), true);
		assertEquals(typed.contains(ints, -1, 1), false);
	}

	@Test
	public void shouldDetermineIndexOfElements() {
		assertEquals(typed.indexOf(NULL, NULL), -1);
		assertEquals(typed.indexOf(NULL, (Integer) null), -1);
		assertEquals(typed.indexOf(ints, NULL), -1);
		assertEquals(typed.indexOf(ints, (Integer) null), 1);
		assertEquals(typed.indexOf(ints, null, 1), 1);
		assertEquals(typed.indexOf(ints, -1, 1), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfElements() {
		assertEquals(typed.lastIndexOf(NULL, NULL), -1);
		assertEquals(typed.lastIndexOf(NULL, (Integer) null), -1);
		assertEquals(typed.lastIndexOf(ints, NULL), -1);
		assertEquals(typed.lastIndexOf(ints, (Integer) null), 3);
		assertEquals(typed.lastIndexOf(ints, null, 1), 3);
		assertEquals(typed.lastIndexOf(ints, -1, 1), -1);
	}

	@Test
	public void shouldReverseElements() {
		assertEquals(typed.reverse(NULL), null);
		assertArray(typed.reverse(ints.clone()), 1, null, 1, null, -1);
	}

	@Test
	public void shouldHashElements() {
		assertEquals(typed.hash(NULL), 0);
		assertEquals(typed.hash(ints), Objects.hash((Object[]) ints));
	}

	@Test
	public void shouldDetermineElementEquality() {
		assertEquals(typed.equals(NULL, NULL), true);
		assertEquals(typed.equals(NULL, EMPTY), false);
		assertEquals(typed.equals(ints, ints), true);
		assertEquals(typed.equals(ints, -1, null, 1, null, 1), true);
		assertEquals(typed.equals(ints, -1, null, 1, null), false);
	}

	@Test
	public void shouldProvideString() {
		assertEquals(typed.toString(NULL), "null");
		assertEquals(typed.toString(EMPTY), "[]");
		assertEquals(typed.toString(ints), "[-1, null, 1, null, 1]");
	}

	@Test
	public void shouldProvideStringWithJoiner() {
		assertEquals(typed.toString(Joiner.OR, NULL), "null");
		assertEquals(typed.toString(Joiner.OR, EMPTY), "");
		assertEquals(typed.toString(Joiner.OR, ints), "-1|null|1|null|1");
	}

	@Test
	public void shouldProvideCustomStringWithJoiner() {
		assertEquals(typed.toString((t, i) -> Formats.HEX.uint(t[i]), Joiner.OR, NULL), "null");
		assertEquals(typed.toString((t, i) -> Formats.HEX.uint(t[i]), Joiner.OR, EMPTY), "");
		assertEquals(typed.toString((t, i) -> Formats.HEX.uint(t[i]), Joiner.OR, ints),
			"0xffffffff|null|0x1|null|0x1");
	}

	@Test
	public void shouldProvideHexString() {
		assertEquals(typed.toHex(NULL), "null");
		assertEquals(typed.toHex(EMPTY), "[]");
		assertEquals(typed.toHex(ints), "[0xffffffff, null, 0x1, null, 0x1]");
		assertEquals(typed.toHex(Joiner.OR, ints), "0xffffffff|null|0x1|null|0x1");
	}
}
