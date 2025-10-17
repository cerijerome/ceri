package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.assertUnsupported;
import static ceri.common.text.Joiner.COLON;
import static ceri.common.text.Joiner.LIST;
import static ceri.common.text.Joiner.OR;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.function.Excepts.BiConsumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.IntFunction;
import ceri.common.function.Excepts.ObjIntConsumer;
import ceri.common.stream.Streams;
import ceri.common.util.Truth;

public class JoinerBehavior {
	private static final IntFunction<RuntimeException, ?> NULL_IDX_FN = null;
	private static final Function<RuntimeException, Integer, ?> NULL_STR_FN = null;
	private static final ObjIntConsumer<RuntimeException, StringBuilder> NULL_IDX_APP = null;
	private static final BiConsumer<RuntimeException, StringBuilder, Integer> NULL_APP = null;
	private static final Integer[] NULL_ARRAY = null;
	private static final List<Integer> NULL_LIST = null;
	private static final Iterator<Integer> NULL_ITERATOR = null;

	@Test
	public void shouldShowCount() {
		var b = Joiner.builder().separator("/").remainder(".").countFormat("=%d");
		assertString(b.max(null).showCount(Truth.yes).build().joinAll(1, 2, 3), "1/2/3=3");
		assertString(b.max(2).showCount(Truth.maybe).build().joinAll(1, 2), "1/2");
		assertString(b.max(2).showCount(Truth.maybe).build().joinAll(1, 2, 3), "1/.=3");
		assertString(b.max(2).showCount(Truth.no).build().joinAll(1, 2, 3), "1/.");
	}

	@Test
	public void shouldShowRemainder() {
		var b = Joiner.builder().separator("/").max(2).showCount(Truth.no);
		assertString(b.remainder(".").build().joinAll(1, 2, 3), "1/.");
		assertString(b.remainder(null).build().joinAll(1, 2, 3), "1/");
	}

	@Test
	public void shouldActAsStreamCollector() {
		var joiner = Joiner.ARRAY_COMPACT;
		assertUnordered(joiner.characteristics());
		var c = joiner.supplier().get();
		joiner.accumulator().accept(c, "a");
		joiner.accumulator().accept(c, "b");
		assertString(joiner.finisher().apply(c), "[a,b]");
		joiner.accumulator().accept(c, "c"); // ignored
		assertString(joiner.finisher().apply(c), "[a,b]");
	}

	@Test
	public void shouldHandleMaxItems() {
		var b = Joiner.ARRAY.edit().separator(":");
		var j0 = b.max(0).build();
		var j1 = b.max(1).build();
		var j2 = b.max(2).build();
		assertString(Streams.of().collect(j0), "[]");
		assertString(Streams.of().collect(j1), "[]");
		assertString(Streams.of().collect(j2), "[]");
		assertString(Streams.of("a").collect(j0), "[](1)");
		assertString(Streams.of("a").collect(j1), "[a]");
		assertString(Streams.of("a").collect(j2), "[a]");
		assertString(Streams.of("a", "b").collect(j0), "[](2)");
		assertString(Streams.of("a", "b").collect(j1), "[...](2)");
		assertString(Streams.of("a", "b").collect(j2), "[a:b]");
		assertString(Streams.of("a", "b", "c").collect(j0), "[](3)");
		assertString(Streams.of("a", "b", "c").collect(j1), "[...](3)");
		assertString(Streams.of("a", "b", "c").collect(j2), "[a:...](3)");
	}

	@Test
	public void shouldHandleZeroMax() {
		var joiner = Joiner.ARRAY.edit().separator(":").max(0).build();
		assertString(Streams.of("a", "b").collect(joiner), "[](2)");
	}

	@Test
	public void shouldFailToCollectParallelStreams() {
		assertUnsupported(() -> List.of("a", "b").stream().parallel().collect(Joiner.ARRAY));
	}

	@Test
	public void shouldIgnoreBadJoinInput() {
		assertString(COLON.joinIndex(NULL_IDX_FN, 1, 3), "");
		assertString(COLON.joinIndex(NULL_IDX_APP, 1, 3), "");
		assertString(COLON.joinAll(NULL_STR_FN, 1, 2, 3), "");
		assertString(COLON.join(NULL_STR_FN, List.of(-1, 0, 1)), "");
		assertString(COLON.join(NULL_STR_FN, List.of(-1, 0, 1).iterator()), "");
		assertString(COLON.join(NULL_STR_FN, List.of(-1, 0, 1).iterator(), 1), "");
		assertString(COLON.join(NULL_APP, List.of(-1, 0, 1)), "");
		assertString(COLON.joinAll(NULL_ARRAY), "");
		assertString(COLON.join(NULL_LIST), "");
		assertString(COLON.join(NULL_ITERATOR), "");
		assertString(COLON.join(NULL_ITERATOR, 1), "");
	}

	@Test
	public void shouldIgnoreBadAppendInput() {
		assertEquals(COLON.appendWithIndex(null, StringBuilder::append, 3), null);
		assertString(COLON.appendByIndex(sb(), NULL_IDX_FN, 3), "");
		assertString(COLON.appendWithIndex(sb(), NULL_IDX_APP, 3), "");
		assertEquals(COLON.appendAll(null, StringBuilder::append, 1, 2, 3), null);
		assertEquals(COLON.append(null, StringBuilder::append, List.of(1, 2, 3)), null);
		assertEquals(COLON.append(null, StringBuilder::append, List.of(1, 2, 3).iterator()), null);
		assertEquals(COLON.append(null, StringBuilder::append, List.of(1, 2, 3).iterator(), 1),
			null);
		assertString(COLON.appendAll(sb(), NULL_STR_FN, 1, 2, 3), "");
		assertString(COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3)), "");
		assertString(COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3).iterator()), "");
		assertString(COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3).iterator(), 1), "");
		assertString(COLON.appendAll(sb(), NULL_ARRAY), "");
		assertString(COLON.append(sb(), NULL_LIST), "");
		assertString(COLON.append(sb(), NULL_ITERATOR), "");
		assertString(COLON.append(sb(), NULL_ITERATOR, 1), "");
	}

	@Test
	public void shouldJoinByIndex() {
		assertString(LIST.joinIndex((b, i) -> b.append('x').append(i), 3), "{x0, x1, x2}");
		assertString(LIST.appendWithIndex(sb(), (b, i) -> b.append('x').append(i), 3),
			"{x0, x1, x2}");
		assertString(LIST.appendByIndex(sb(), i -> -i, 3), "{0, -1, -2}");
	}

	@Test
	public void shouldJoinArrays() {
		assertString(OR.joinAll(-1, 0, 1), "-1|0|1");
		assertString(OR.joinAll(i -> -i, -1, 0, 1), "1|0|-1");
		assertString(OR.appendAll(sb(), -1, 0, 1), "-1|0|1");
		assertString(OR.appendAll(sb(), i -> -i, -1, 0, 1), "1|0|-1");
	}

	@Test
	public void shouldJoinCollections() {
		assertString(OR.join(List.of(-1, 0, 1)), "-1|0|1");
		assertString(OR.join(i -> -i, List.of(-1, 0, 1)), "1|0|-1");
		assertString(OR.append(sb(), List.of(-1, 0, 1)), "-1|0|1");
		assertString(OR.append(sb(), i -> -i, List.of(-1, 0, 1)), "1|0|-1");
	}

	@Test
	public void shouldJoinIterators() {
		assertString(OR.join(List.of(-1, 0, 1).iterator()), "-1|0|1");
		assertString(OR.join(i -> -i, List.of(-1, 0, 1).iterator()), "1|0|-1");
		assertString(OR.join(List.of(-1, 0, 1).iterator(), 2), "-1|0");
		assertString(OR.join(i -> -i, List.of(-1, 0, 1).iterator(), 2), "1|0");
		assertString(OR.append(sb(), List.of(-1, 0, 1).iterator()), "-1|0|1");
		assertString(OR.append(sb(), List.of(-1, 0, 1).iterator(), 2), "-1|0");
		assertString(OR.append(sb(), i -> -i, List.of(-1, 0, 1).iterator()), "1|0|-1");
		assertString(OR.append(sb(), i -> -i, List.of(-1, 0, 1).iterator(), 2), "1|0");
	}

	private static StringBuilder sb() {
		return new StringBuilder();
	}
}
