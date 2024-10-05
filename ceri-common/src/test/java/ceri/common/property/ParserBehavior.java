package ceri.common.property;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionFunction;
import ceri.common.util.Align.H;

public class ParserBehavior {
	private static final ExceptionFunction<RuntimeException, Integer, List<Integer>> BIT_LIST =
		i -> IntStream.of(ByteUtil.bits(i)).boxed().toList();
	private static final ExceptionFunction<RuntimeException, Integer, Integer[]> BIT_ARRAY =
		i -> IntStream.of(ByteUtil.bits(i)).boxed().toArray(Integer[]::new);

	@Test
	public void shouldAllowNullValue() {
		assertEquals(string("x").isNull(), false);
		assertEquals(string(null).isNull(), true);
		assertEquals(string(null).optional().isEmpty(), true);
		assertEquals(string("test").to(s -> null), null);
		assertEquals(string("test").as(s -> null).get(), null);
		assertEquals(string(null).split().get(), null);
		assertEquals(Parser.Type.of(null).get(), null);
		assertEquals(Parser.Type.from(() -> null).get(), null);
		assertEquals(Parser.Types.from(() -> null).empty(), true);
	}

	@Test
	public void shouldProvideDefaultValues() {
		assertEquals(Parser.Type.of(null).def("x").get(), "x");
		assertEquals(Parser.Type.of(null).def(() -> "x").get(), "x");
		assertEquals(Parser.Type.from(() -> null).def("x").get(), "x");
		assertIterable(Parser.Types.of((List<Integer>) null).def(List.of(1)).get(), 1);
		assertIterable(Parser.Types.of((List<Integer>) null).def(1).get(), 1);
		assertIterable(Parser.Types.of((List<Integer>) null).def(() -> List.of(1)).get(), 1);
		assertIterable(Parser.Strings.from(() -> null).def("x").get(), "x");
		assertIterable(Parser.Strings.from(() -> null).def(() -> List.of("x")).get(), "x");
		assertIterable(Parser.Types.from(() -> null).def(List.of(1)).get(), 1);
		assertEquals(string(null).def("x").get(), "x");
		assertEquals(string(null).def(() -> "x").get(), "x");
	}

	@Test
	public void shouldFlattenTypeAccessor() {
		String[] vals = { "0" };
		var p = Parser.Type.from(() -> vals[0]);
		var s = Parser.String.from(() -> vals[0]);
		var i = s.as(Integer::parseInt);
		assertEquals(p.get(), "0");
		assertEquals(s.get(), "0");
		assertEquals(i.get(), 0);
		vals[0] = "1";
		p = p.flat();
		s = s.flat();
		i = s.asFlat(Integer::parseInt);
		assertEquals(p.get(), "1");
		assertEquals(s.get(), "1");
		assertEquals(i.get(), 1);
		vals[0] = "2";
		assertEquals(p.get(), "1");
		assertEquals(s.get(), "1");
		assertEquals(i.get(), 1);
	}

	@Test
	public void shouldFlattenTypesAccessor() {
		List<List<String>> list = Arrays.asList(List.of("1"));
		var p = Parser.Types.from(() -> list.get(0));
		var s = Parser.Strings.from(() -> list.get(0));
		assertIterable(p.get(), "1");
		assertIterable(s.get(), "1");
		list.set(0, List.of("2", "3"));
		p = p.flat();
		s = s.flat();
		assertIterable(p.get(), "2", "3");
		assertIterable(s.get(), "2", "3");
		list.set(0, List.of("1"));
		assertIterable(p.get(), "2", "3");
		assertIterable(s.get(), "2", "3");
	}

	@Test
	public void shouldSplitValues() {
		assertIterable(Parser.Type.of(0x124).split(BIT_LIST).get(), 2, 5, 8);
		assertIterable(Parser.Type.of(0x124).splitArray(BIT_ARRAY).get(), 2, 5, 8);
		assertEquals(Parser.String.of(null).split().get(), null);
		assertIterable(Parser.String.of("1,2,3").split().get(), "1", "2", "3");
		assertIterable(Parser.String.of("1 2 3").split(Pattern.compile(" ")).get(), "1", "2", "3");
	}

	@Test
	public void shouldProvideAccessAtIndex() {
		assertEquals(Parser.Types.of(1, 2, 3).at(1).get(), 2);
		assertEquals(Parser.Types.of(1, 2, 3).at(-1).get(), null);
		assertEquals(Parser.Types.of(1, 2, 3).at(3).get(), null);
		assertEquals(Parser.Types.from(() -> null).at(0).get(), null);
		assertEquals(Parser.Strings.of("1", "2").at(0).get(), "1");
		assertEquals(Parser.Strings.of("1", "2").at(-1).get(), null);
		assertEquals(Parser.Strings.of("1", "2").at(2).get(), null);
	}

	@Test
	public void shouldConvertToArray() {
		assertArray(Parser.Types.of((List<Integer>) null).array(Integer[]::new));
		assertArray(Parser.Types.of().array(Integer[]::new));
		assertArray(Parser.Types.of(-1, 0, 1).array(Integer[]::new), -1, 0, 1);
		assertArray(Parser.Types.of(true, false).toBoolArray(t -> t), true, false);
		assertArray(Parser.Types.of(-1, 0, 1).toIntArray(t -> t), -1, 0, 1);
		assertArray(Parser.Types.of(-1, 0, 1).toLongArray(t -> t), -1L, 0L, 1L);
		assertArray(Parser.Types.of(-1, 0, 1).toDoubleArray(t -> t), -1.0, 0.0, 1.0);
		assertArray(Parser.Strings.from(() -> null).array());
		assertArray(strings("").array());
		assertArray(strings("1,2").array(), "1", "2");
		assertArray(strings(null).toBoolArray(true), true);
		assertArray(strings("true,false").toBoolArray(true), true, false);
		assertArray(strings(null).toIntArray(2), 2);
		assertArray(strings("-0xffffffff, 0xffffffff").toIntArray(2), 1, -1);
		assertArray(strings(null).toUintArray(2), 2);
		assertArray(strings("0, 0xffffffff").toUintArray(2), 0, -1);
		assertArray(strings(null).toLongArray(2), 2L);
		assertArray(strings("-0xffffffffffffffff, 0xffffffffffffffff").toLongArray(2), 1L, -1L);
		assertArray(strings(null).toUlongArray(2), 2L);
		assertArray(strings("0, 0xffffffffffffffff").toUlongArray(2), 0L, -1L);
		assertArray(strings(null).toDoubleArray(2), 2.0);
		assertArray(strings("-1, 0, 1").toDoubleArray(2), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldStreamValues() {
		assertStream(Parser.Types.from(() -> null).stream());
		assertStream(Parser.Types.of(1, null, 3).stream(), 1, null, 3);
	}

	@Test
	public void shouldConvertListItems() {
		assertEquals(Parser.Strings.from(() -> null).toEach(String::length), null);
		assertIterable(Parser.Types.of("a", "bb", "").toEach(String::length), 1, 2, 0);
		assertIterable(Parser.Types.of((List<String>) null).toEachDef(String::length, 1), 1);
		assertIterable(strings("").toEachDef(String::length, 1));
	}

	@Test
	public void shouldConvertAccessorItems() {
		assertEquals(Parser.Strings.from(() -> null).asEach(String::length).get(), null);
		assertIterable(Parser.Types.of("a", "bb", "").asEach(String::length).get(), 1, 2, 0);
		assertIterable(strings("true, false").asBools().get(), true, false);
		assertIterable(strings("-0xffffffff,0xffffffff").asInts().get(), 1, -1);
		assertIterable(strings("0,0xffffffff").asUints().get(), 0, -1);
		assertIterable(strings("-0xffffffffffffffff,0xffffffffffffffff").asLongs().get(), 1L, -1L);
		assertIterable(strings("0,0xffffffffffffffff").asUlongs().get(), 0L, -1L);
		assertIterable(strings("-1,NaN,1").asDoubles().get(), -1.0, Double.NaN, 1.0);
		assertIterable(strings("left,right").asEnums(H.class).get(), H.left, H.right);
		assertIterable(strings("p0,p1").asPaths().get(), Path.of("p0"), Path.of("p1"));
	}

	@Test
	public void shouldConvertAccessorFlatItems() {
		assertEquals(Parser.Strings.from(() -> null).asEachFlat(String::length).get(), null);
		String[] vals = { "a", "bb", "" };
		var p = Parser.Types.of(vals).asEach(String::length);
		var f = Parser.Types.of(vals).asEachFlat(String::length);
		vals[1] = "bbb";
		assertIterable(p.get(), 1, 3, 0);
		assertIterable(f.get(), 1, 2, 0);
	}

	@Test
	public void shouldParsePrimitiveStrings() {
		assertEquals(string(null).toBool(false), false);
		assertEquals(string("TRUE").toBool(false), true);
		assertEquals(string(null).toInt(2), 2);
		assertEquals(string("-0xffffffff").toInt(2), 1);
		assertEquals(string("0xffffffff").toInt(2), -1);
		assertEquals(string(null).toUint(2), 2);
		assertEquals(string("0").toInt(2), 0);
		assertEquals(string("0xffffffff").toUint(2), -1);
		assertEquals(string(null).toLong(2), 2L);
		assertEquals(string("-0xffffffffffffffff").toLong(2), 1L);
		assertEquals(string("0xffffffffffffffff").toLong(2), -1L);
		assertEquals(string(null).toUlong(2), 2L);
		assertEquals(string("0").toUlong(2), 0L);
		assertEquals(string("0xffffffffffffffff").toUlong(2), -1L);
		assertEquals(string(null).toDouble(2), 2.0);
		assertEquals(string("NaN").toDouble(2), Double.NaN);
		assertEquals(string("1").toDouble(2), 1.0);
	}

	@Test
	public void shouldParseStringTypes() {
		assertEquals(string(null).toEnum(H.left), H.left);
		assertEquals(string("right").toEnum(H.left), H.right);
		assertEquals(string(null).toPath(Path.of("def")), Path.of("def"));
		assertEquals(string("test").toPath(Path.of("def")), Path.of("test"));
	}

	@Test
	public void shouldFailForBadConversion() {
		assertIllegalArg(() -> string("x").toInt(1));
		assertIllegalArg(() -> strings("x").toIntArray(1));
	}

	@Test
	public void shouldFailForBadSplit() {
		var p = Parser.Type.of(1).split(x -> throwRuntime());
		assertIllegalArg(() -> p.get());
	}

	private static Parser.String string(String s) {
		return Parser.String.of(s);
	}

	private static Parser.Strings strings(String s) {
		return string(s).split();
	}
}
