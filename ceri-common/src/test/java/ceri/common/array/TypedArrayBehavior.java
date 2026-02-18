package ceri.common.array;

import java.util.Objects;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.text.Format;
import ceri.common.text.Joiner;

public class TypedArrayBehavior {
	private static final Integer[] NULL = null;
	private static final Integer[] EMPTY = new Integer[0];
	private final Integer[] ints = Array.of(-1, null, 1, null, 1);
	private final TypedArray.Type.Integral<Integer> typed = Array.INT.box();

	@Test
	public void shouldProvideComponentType() {
		Assert.equal(Array.OBJECT.component(), Object.class);
		Assert.equal(Array.INT.component(), int.class);
		Assert.equal(typed.component(), Integer.class);
	}

	@Test
	public void shouldCreateArray() {
		Assert.array(Array.OBJECT.array(1), (Object) null);
		Assert.array(typed.array(0));
		Assert.array(typed.array(1), (Integer) null);
	}

	@Test
	public void shouldDetermineIfEmpty() {
		Assert.equal(typed.isEmpty(NULL), true);
		Assert.equal(typed.isEmpty(EMPTY), true);
		Assert.equal(typed.isEmpty(ints), false);
	}

	@Test
	public void shouldProvideElementAtIndex() {
		Assert.equal(typed.at(NULL, 0), null);
		Assert.equal(typed.at(ints, -1), null);
		Assert.equal(typed.at(ints, 0), -1);
		Assert.equal(typed.at(ints, 1), null);
		Assert.equal(typed.at(ints, 2), 1);
		Assert.equal(typed.at(ints, 5), null);
	}

	@Test
	public void shouldProvideLastElement() {
		Assert.equal(typed.last(NULL), null);
		Assert.equal(typed.last(ints), 1);
	}

	@Test
	public void shouldMakeCopyOfElements() {
		Assert.array(typed.copyOf(NULL));
		Assert.array(typed.copyOf(ints), -1, null, 1, null, 1);
	}

	@Test
	public void shouldCopyElements() {
		Assert.same(typed.copy(NULL, NULL), NULL);
		Assert.array(typed.copy(NULL, new Integer[3]), null, null, null);
		Assert.same(typed.copy(ints, NULL), NULL);
		Assert.array(typed.copy(ints, new Integer[3]), -1, null, 1);
	}

	@Test
	public void shouldAppendElements() {
		Assert.equal(typed.append(NULL, 0, 3), null);
		Assert.same(typed.append(ints, NULL), ints);
		Assert.array(typed.append(ints, 0, 3), -1, null, 1, null, 1, 0, 3);
	}

	@Test
	public void shouldInsertElements() {
		Assert.equal(typed.insert(NULL, 1, 0), null);
		Assert.same(typed.insert(ints, 1, NULL), ints);
		Assert.array(typed.insert(ints, 1, 0, 3), -1, 0, 3, null, 1, null, 1);
	}

	@Test
	public void shouldDetermineIfArrayHasElement() {
		Assert.equal(typed.has(NULL, null), false);
		Assert.equal(typed.has(ints, null), true);
		Assert.equal(typed.has(ints, 1), true);
		Assert.equal(typed.has(ints, 0), false);
	}

	@Test
	public void shouldDetermineIfElementsContained() {
		Assert.equal(typed.contains(NULL, NULL), false);
		Assert.equal(typed.contains(NULL, (Integer) null), false);
		Assert.equal(typed.contains(ints, NULL), false);
		Assert.equal(typed.contains(ints, (Integer) null), true);
		Assert.equal(typed.contains(ints, null, 1), true);
		Assert.equal(typed.contains(ints, -1, 1), false);
	}

	@Test
	public void shouldDetermineIndexOfElements() {
		Assert.equal(typed.indexOf(NULL, NULL), -1);
		Assert.equal(typed.indexOf(NULL, (Integer) null), -1);
		Assert.equal(typed.indexOf(ints, NULL), -1);
		Assert.equal(typed.indexOf(ints, (Integer) null), 1);
		Assert.equal(typed.indexOf(ints, null, 1), 1);
		Assert.equal(typed.indexOf(ints, -1, 1), -1);
	}

	@Test
	public void shouldDetermineLastIndexOfElements() {
		Assert.equal(typed.lastIndexOf(NULL, NULL), -1);
		Assert.equal(typed.lastIndexOf(NULL, (Integer) null), -1);
		Assert.equal(typed.lastIndexOf(ints, NULL), -1);
		Assert.equal(typed.lastIndexOf(ints, (Integer) null), 3);
		Assert.equal(typed.lastIndexOf(ints, null, 1), 3);
		Assert.equal(typed.lastIndexOf(ints, -1, 1), -1);
	}

	@Test
	public void shouldReverseElements() {
		Assert.equal(typed.reverse(NULL), null);
		Assert.array(typed.reverse(ints.clone()), 1, null, 1, null, -1);
	}

	@Test
	public void shouldHashElements() {
		Assert.equal(typed.hash(NULL), 0);
		Assert.equal(typed.hash(ints), Objects.hash((Object[]) ints));
	}

	@Test
	public void shouldDetermineElementEquality() {
		Assert.equal(typed.equals(NULL, NULL), true);
		Assert.equal(typed.equals(NULL, EMPTY), false);
		Assert.equal(typed.equals(ints, ints), true);
		Assert.equal(typed.equals(ints, -1, null, 1, null, 1), true);
		Assert.equal(typed.equals(ints, -1, null, 1, null), false);
	}

	@Test
	public void shouldProvideString() {
		Assert.equal(typed.toString(NULL), "null");
		Assert.equal(typed.toString(EMPTY), "[]");
		Assert.equal(typed.toString(ints), "[-1, null, 1, null, 1]");
	}

	@Test
	public void shouldProvideStringWithJoiner() {
		Assert.equal(typed.toString(Joiner.OR, NULL), "null");
		Assert.equal(typed.toString(Joiner.OR, EMPTY), "");
		Assert.equal(typed.toString(Joiner.OR, ints), "-1|null|1|null|1");
	}

	@Test
	public void shouldProvideCustomStringWithJoiner() {
		Assert.equal(typed.toString((t, i) -> Format.HEX.uint(t[i]), Joiner.OR, NULL), "null");
		Assert.equal(typed.toString((t, i) -> Format.HEX.uint(t[i]), Joiner.OR, EMPTY), "");
		Assert.equal(typed.toString((t, i) -> Format.HEX.uint(t[i]), Joiner.OR, ints),
			"0xffffffff|null|0x1|null|0x1");
	}

	@Test
	public void shouldProvideHexString() {
		Assert.equal(typed.toHex(NULL), "null");
		Assert.equal(typed.toHex(EMPTY), "[]");
		Assert.equal(typed.toHex(ints), "[0xffffffff, null, 0x1, null, 0x1]");
		Assert.equal(typed.toHex(Joiner.OR, ints), "0xffffffff|null|0x1|null|0x1");
	}
}
