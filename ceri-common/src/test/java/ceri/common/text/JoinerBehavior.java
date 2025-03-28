package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.text.Joiner.COLON;
import static ceri.common.text.Joiner.LIST;
import static ceri.common.text.Joiner.OR;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.Test;
import ceri.common.exception.ExceptionUtil.Rte;
import ceri.common.function.ExceptionBiConsumer;
import ceri.common.function.ExceptionFunction;
import ceri.common.function.ExceptionIntFunction;
import ceri.common.function.ExceptionObjIntConsumer;
import ceri.common.util.Truth;

public class JoinerBehavior {
	private static final ExceptionIntFunction<Rte, ?> NULL_IDX_FN = null;
	private static final ExceptionFunction<Rte, Integer, ?> NULL_STR_FN = null;
	private static final ExceptionObjIntConsumer<Rte, StringBuilder> NULL_IDX_APP = null;
	private static final ExceptionBiConsumer<Rte, StringBuilder, Integer> NULL_APP = null;
	private static final Integer[] NULL_ARRAY = null;
	private static final List<Integer> NULL_LIST = null;
	private static final Stream<Integer> NULL_STREAM = null;
	private static final Iterator<Integer> NULL_ITERATOR = null;

	@Test
	public void shouldDetermineCount() {
		var joiner = Joiner.builder().build();
		assertEquals(joiner.limited(1), false);
		assertEquals(joiner.count(10), 10);
		joiner = Joiner.builder().max(3).build();
		assertEquals(joiner.count(0), 0);
		assertEquals(joiner.count(4), 3);
		assertEquals(joiner.limited(1), false);
		assertEquals(joiner.limited(4), true);
		joiner = Joiner.builder().max(0).build();
		assertEquals(joiner.count(0), 0);
		assertEquals(joiner.count(1), 0);
		assertEquals(joiner.limited(0), false);
		assertEquals(joiner.limited(1), true);
	}

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
	public void shouldIgnoreBadJoinInput() {
		assertString(COLON.joinIndex(NULL_IDX_FN, 1, 3), "");
		assertString(COLON.joinIndex(NULL_IDX_APP, 1, 3), "");
		assertString(COLON.joinAll(NULL_STR_FN, 1, 2, 3), "");
		assertString(COLON.join(NULL_STR_FN, List.of(-1, 0, 1)), "");
		assertString(COLON.join(NULL_STR_FN, Stream.of(-1, 0, 1)), "");
		assertString(COLON.join(NULL_STR_FN, List.of(-1, 0, 1).iterator()), "");
		assertString(COLON.join(NULL_STR_FN, List.of(-1, 0, 1).iterator(), 1), "");
		assertString(COLON.join(NULL_APP, List.of(-1, 0, 1)), "");
		assertString(COLON.joinAll(NULL_ARRAY), "");
		assertString(COLON.join(NULL_LIST), "");
		assertString(COLON.join(NULL_STREAM), "");
		assertString(COLON.join(NULL_ITERATOR), "");
		assertString(COLON.join(NULL_ITERATOR, 1), "");
	}

	@Test
	public void shouldIgnoreBadAppendInput() {
		assertEquals(COLON.appendIndex(null, StringBuilder::append, 3), null);
		assertString(COLON.appendIndex(sb(), NULL_IDX_FN, 3), "");
		assertString(COLON.appendIndex(sb(), NULL_IDX_APP, 3), "");
		assertEquals(COLON.appendAll(null, StringBuilder::append, 1, 2, 3), null);
		assertEquals(COLON.append(null, StringBuilder::append, List.of(1, 2, 3)), null);
		assertEquals(COLON.append(null, StringBuilder::append, Stream.of(1, 2, 3)), null);
		assertEquals(COLON.append(null, StringBuilder::append, List.of(1, 2, 3).iterator()), null);
		assertEquals(COLON.append(null, StringBuilder::append, List.of(1, 2, 3).iterator(), 1),
			null);
		assertString(COLON.appendAll(sb(), NULL_STR_FN, 1, 2, 3), "");
		assertString(COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3)), "");
		assertString(COLON.append(sb(), NULL_STR_FN, Stream.of(1, 2, 3)), "");
		assertString(COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3).iterator()), "");
		assertString(COLON.append(sb(), NULL_STR_FN, List.of(1, 2, 3).iterator(), 1), "");
		assertString(COLON.appendAll(sb(), NULL_ARRAY), "");
		assertString(COLON.append(sb(), NULL_LIST), "");
		assertString(COLON.append(sb(), NULL_STREAM), "");
		assertString(COLON.append(sb(), NULL_ITERATOR), "");
		assertString(COLON.append(sb(), NULL_ITERATOR, 1), "");
	}

	@Test
	public void shouldJoinByIndex() {
		assertString(LIST.joinIndex((b, i) -> b.append('x').append(i), 3), "{x0, x1, x2}");
		assertString(LIST.appendIndex(sb(), (b, i) -> b.append('x').append(i), 3), "{x0, x1, x2}");
		assertString(LIST.appendIndex(sb(), i -> -i, 3), "{0, -1, -2}");
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
	public void shouldJoinStreams() {
		assertString(OR.join(Stream.of(-1, 0, 1)), "-1|0|1");
		assertString(OR.join(i -> -i, Stream.of(-1, 0, 1)), "1|0|-1");
		assertString(OR.append(sb(), Stream.of(-1, 0, 1)), "-1|0|1");
		assertString(OR.append(sb(), i -> -i, Stream.of(-1, 0, 1)), "1|0|-1");
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
