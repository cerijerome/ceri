package ceri.common.text;

import java.util.Iterator;
import java.util.List;
import org.junit.Test;
import ceri.common.function.Excepts.BiConsumer;
import ceri.common.function.Excepts.Function;
import ceri.common.function.Excepts.IntFunction;
import ceri.common.function.Excepts.ObjIntConsumer;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
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
		Assert.string(b.max(null).showCount(Truth.yes).build().joinAll(1, 2, 3), "1/2/3=3");
		Assert.string(b.max(2).showCount(Truth.maybe).build().joinAll(1, 2), "1/2");
		Assert.string(b.max(2).showCount(Truth.maybe).build().joinAll(1, 2, 3), "1/.=3");
		Assert.string(b.max(2).showCount(Truth.no).build().joinAll(1, 2, 3), "1/.");
	}

	@Test
	public void shouldShowRemainder() {
		var b = Joiner.builder().separator("/").max(2).showCount(Truth.no);
		Assert.string(b.remainder(".").build().joinAll(1, 2, 3), "1/.");
		Assert.string(b.remainder(null).build().joinAll(1, 2, 3), "1/");
	}

	@Test
	public void shouldActAsStreamCollector() {
		var joiner = Joiner.ARRAY_COMPACT;
		Assert.unordered(joiner.characteristics());
		var c = joiner.supplier().get();
		joiner.accumulator().accept(c, "a");
		joiner.accumulator().accept(c, "b");
		Assert.string(joiner.finisher().apply(c), "[a,b]");
		joiner.accumulator().accept(c, "c"); // ignored
		Assert.string(joiner.finisher().apply(c), "[a,b]");
	}

	@Test
	public void shouldHandleMaxItems() {
		var b = Joiner.ARRAY.edit().separator(":");
		var j0 = b.max(0).build();
		var j1 = b.max(1).build();
		var j2 = b.max(2).build();
		Assert.string(Streams.of().collect(j0), "[]");
		Assert.string(Streams.of().collect(j1), "[]");
		Assert.string(Streams.of().collect(j2), "[]");
		Assert.string(Streams.of("a").collect(j0), "[](1)");
		Assert.string(Streams.of("a").collect(j1), "[a]");
		Assert.string(Streams.of("a").collect(j2), "[a]");
		Assert.string(Streams.of("a", "b").collect(j0), "[](2)");
		Assert.string(Streams.of("a", "b").collect(j1), "[...](2)");
		Assert.string(Streams.of("a", "b").collect(j2), "[a:b]");
		Assert.string(Streams.of("a", "b", "c").collect(j0), "[](3)");
		Assert.string(Streams.of("a", "b", "c").collect(j1), "[...](3)");
		Assert.string(Streams.of("a", "b", "c").collect(j2), "[a:...](3)");
	}

	@Test
	public void shouldHandleZeroMax() {
		var joiner = Joiner.ARRAY.edit().separator(":").max(0).build();
		Assert.string(Streams.of("a", "b").collect(joiner), "[](2)");
	}

	@Test
	public void shouldFailToCollectParallelStreams() {
		Assert.unsupportedOp(() -> List.of("a", "b").stream().parallel().collect(Joiner.ARRAY));
	}

	@Test
	public void shouldIgnoreBadJoinInput() {
		Assert.string(Joiner.COLON.joinIndex(NULL_IDX_FN, 1, 3), "");
		Assert.string(Joiner.COLON.joinIndex(NULL_IDX_APP, 1, 3), "");
		Assert.string(Joiner.COLON.joinAll(NULL_STR_FN, 1, 2, 3), "");
		Assert.string(Joiner.COLON.join(NULL_STR_FN, List.of(-1, 0, 1)), "");
		Assert.string(Joiner.COLON.join(NULL_STR_FN, List.of(-1, 0, 1).iterator()), "");
		Assert.string(Joiner.COLON.join(NULL_STR_FN, List.of(-1, 0, 1).iterator(), 1), "");
		Assert.string(Joiner.COLON.join(NULL_APP, List.of(-1, 0, 1)), "");
		Assert.string(Joiner.COLON.joinAll(NULL_ARRAY), "");
		Assert.string(Joiner.COLON.join(NULL_LIST), "");
		Assert.string(Joiner.COLON.join(NULL_ITERATOR), "");
		Assert.string(Joiner.COLON.join(NULL_ITERATOR, 1), "");
	}

	@Test
	public void shouldIgnoreBadAppendInput() {
		Assert.equal(Joiner.COLON.appendWithIndex(null, StringBuilder::append, 3), null);
		Assert.string(Joiner.COLON.appendByIndex(sb(), NULL_IDX_FN, 3), "");
		Assert.string(Joiner.COLON.appendWithIndex(sb(), NULL_IDX_APP, 3), "");
		Assert.equal(Joiner.COLON.appendAll(null, StringBuilder::append, 1, 2, 3), null);
		Assert.equal(Joiner.COLON.append(null, StringBuilder::append, List.of(1, 2, 3)), null);
		Assert.equal(Joiner.COLON.append(null, StringBuilder::append, List.of(1, 2, 3).iterator()),
			null);
		Assert.equal(
			Joiner.COLON.append(null, StringBuilder::append, List.of(1, 2, 3).iterator(), 1), null);
		Assert.string(Joiner.COLON.appendAll(sb(), NULL_STR_FN, 1, 2, 3), "");
		Assert.string(Joiner.COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3)), "");
		Assert.string(Joiner.COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3).iterator()), "");
		Assert.string(Joiner.COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3).iterator(), 1), "");
		Assert.string(Joiner.COLON.appendAll(sb(), NULL_ARRAY), "");
		Assert.string(Joiner.COLON.append(sb(), NULL_LIST), "");
		Assert.string(Joiner.COLON.append(sb(), NULL_ITERATOR), "");
		Assert.string(Joiner.COLON.append(sb(), NULL_ITERATOR, 1), "");
	}

	@Test
	public void shouldJoinByIndex() {
		Assert.string(Joiner.LIST.joinIndex((b, i) -> b.append('x').append(i), 3), "{x0, x1, x2}");
		Assert.string(Joiner.LIST.appendWithIndex(sb(), (b, i) -> b.append('x').append(i), 3),
			"{x0, x1, x2}");
		Assert.string(Joiner.LIST.appendByIndex(sb(), i -> -i, 3), "{0, -1, -2}");
	}

	@Test
	public void shouldJoinArrays() {
		Assert.string(Joiner.OR.joinAll(-1, 0, 1), "-1|0|1");
		Assert.string(Joiner.OR.joinAll(i -> -i, -1, 0, 1), "1|0|-1");
		Assert.string(Joiner.OR.appendAll(sb(), -1, 0, 1), "-1|0|1");
		Assert.string(Joiner.OR.appendAll(sb(), i -> -i, -1, 0, 1), "1|0|-1");
	}

	@Test
	public void shouldJoinCollections() {
		Assert.string(Joiner.OR.join(List.of(-1, 0, 1)), "-1|0|1");
		Assert.string(Joiner.OR.join(i -> -i, List.of(-1, 0, 1)), "1|0|-1");
		Assert.string(Joiner.OR.append(sb(), List.of(-1, 0, 1)), "-1|0|1");
		Assert.string(Joiner.OR.append(sb(), i -> -i, List.of(-1, 0, 1)), "1|0|-1");
	}

	@Test
	public void shouldJoinIterators() {
		Assert.string(Joiner.OR.join(List.of(-1, 0, 1).iterator()), "-1|0|1");
		Assert.string(Joiner.OR.join(i -> -i, List.of(-1, 0, 1).iterator()), "1|0|-1");
		Assert.string(Joiner.OR.join(List.of(-1, 0, 1).iterator(), 2), "-1|0");
		Assert.string(Joiner.OR.join(i -> -i, List.of(-1, 0, 1).iterator(), 2), "1|0");
		Assert.string(Joiner.OR.append(sb(), List.of(-1, 0, 1).iterator()), "-1|0|1");
		Assert.string(Joiner.OR.append(sb(), List.of(-1, 0, 1).iterator(), 2), "-1|0");
		Assert.string(Joiner.OR.append(sb(), i -> -i, List.of(-1, 0, 1).iterator()), "1|0|-1");
		Assert.string(Joiner.OR.append(sb(), i -> -i, List.of(-1, 0, 1).iterator(), 2), "1|0");
	}

	private static StringBuilder sb() {
		return new StringBuilder();
	}
}
