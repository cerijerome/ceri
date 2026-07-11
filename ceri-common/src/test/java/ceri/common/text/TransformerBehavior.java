package ceri.common.text;

import java.util.List;
import java.util.Map;
import org.junit.Test;
import ceri.common.collect.Immutable;
import ceri.common.collect.Maps;
import ceri.common.function.Functions;
import ceri.common.test.Assert;
import ceri.common.text.Transformer.Transform;

public class TransformerBehavior {
	private final int[][] ints = new int[][] { { -1, 1 }, null, {}, { 0 } };
	private final List<List<Integer>> lists =
		Immutable.listOf(List.of(-1, 1), null, List.of(), List.of(0));
	private final Map<Integer, Map<String, Integer>> maps = Immutable.mapOf(Maps::link, 1,
		Immutable.mapOf(Maps::link, "A", -1, "B", 1), null, Map.of(), 2, null);

	@Test
	public void shouldTransformNulls() {
		Assert.equal(Transform.STRING.apply(null, null), "null");
		Assert.equal(Transform.format("%s").apply(null, null), null);
		Assert.equal(Transform.formats(_ -> "%s").apply(null, null), null);
	}

	@Test
	public void shouldTransform() {
		Assert.equal(Transform.STRING.apply(null, 1), "1");
		Assert.equal(Transform.of(null).apply(null, "test"), null);
		Assert.equal(Transform.<Integer, Integer>of(i -> i + 1).apply(null, 1), 2);
	}

	@Test
	public void shouldReapplyTransforms() {
		var lt = Transform.format("%dL").with(Long.class);
		var it = Transform.cast(Integer.class).then(i -> i.longValue());
		assertTransform(b().add(lt).add(it).build(), 1, "1");
		assertTransform(b().add(lt).add(it.re()).build(), 1, "1L");
	}

	@Test
	public void shouldFilterTransform() {
		var it = Transform.<Integer, Integer>of(i -> i + 1).with(i -> i > 0);
		Assert.same(it.with((Functions.Predicate<Integer>) null), it);
		Assert.equal(it.apply(null, null), null);
		Assert.equal(it.apply(null, 0), null);
		Assert.equal(it.apply(null, 1), 2);
	}

	@Test
	public void shouldChainTransforms() {
		var st = Transform.format("'%s'").then(s -> s.replace('\'', '\"'));
		Assert.equal(st.apply(null, null), null);
		Assert.equal(st.apply(null, 1), "\"1\"");
		Assert.equal(st.then((Transform<String, ?>) null).apply(null, 1), null);
	}

	@Test
	public void shouldSetNestLevels() {
		assertTransform(b().levels(0).build(), ints, "..");
		assertTransform(b().levels(1).build(), ints, "[..]");
		assertTransform(b().levels(2).build(), ints, "[[..], null, [..], [..]]");
		assertTransform(b().levels(3).build(), ints, "[[-1, 1], null, [], [0]]");
		assertTransform(b().levels(0).build(), lists, "..");
		assertTransform(b().levels(1).build(), lists, "[..]");
		assertTransform(b().levels(2).build(), lists, "[[..], null, [..], [..]]");
		assertTransform(b().levels(3).build(), lists, "[[-1, 1], null, [], [0]]");
		assertTransform(b().levels(0).build(), maps, "..");
		assertTransform(b().levels(1).build(), maps, "{..}");
		assertTransform(b().levels(2).build(), maps, "{.., .., ..}");
		assertTransform(b().levels(3).build(), maps, "{1={..}, null={..}, 2=null}");
	}

	@Test
	public void shouldFormatIterables() {
		assertTransform(b().iterables(Joiner.COLON).build(), ints, "-1:1:null::0");
		assertTransform(b().iterables(Joiner.COLON).build(), lists, "-1:1:null::0");
	}

	@Test
	public void shouldFormatMaps() {
		assertTransform(b().maps(Joiner.OR, ":").build(), maps, "1:A:-1|B:1|null:|2:null");
	}

	@Test
	public void shouldSetNullTransform() {
		assertTransform(Transformer.DEFAULT, null, "null");
		assertTransform(b().setNull((Functions.Supplier<?>) null).build(), null, "null");
		assertTransform(b().setNull("").build(), null, "");
		assertTransform(b().setNull("").build(), ints, "[[-1, 1], , [], [0]]");
	}

	@Test
	public void shouldFormatTypes() {
		assertTransform(b().format(Integer.class, "0x%x").build(), ints,
			"[[0xffffffff, 0x1], null, [], [0x0]]");
		assertTransform(b().formats(Integer.class, i -> i < 0 ? "%03d" : "%02d").build(), ints,
			"[[-01, 01], null, [], [00]]");
	}

	@Test
	public void shouldAddTransforms() {
		assertTransform(b().levels(2).add(Integer.class, (c, i) -> c.apply(i + 1)).build(), 1,
			"..");
		assertTransform(b().add(Integer.class, i -> i + 1).build(), ints,
			"[[0, 2], null, [], [1]]");
	}

	@Test
	public void shouldProvideStringRepresentation() {
		Assert.find(Transformer.DEFAULT, "rules=\\d+");
		Assert.find(Transformer.DEFAULT, "levels=\\d+");
	}

	private static Transformer.Builder b() {
		return Transformer.builder();
	}

	private static void assertTransform(Transformer transformer, Object arg, String format,
		Object... args) {
		Assert.string(transformer.apply(arg), format, args);
	}
}
