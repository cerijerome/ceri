package ceri.common.property;

import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertNpe;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.data.ByteUtil;
import ceri.common.function.ExceptionFunction;
import ceri.common.test.Captor;
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
	public void shouldValidateAgainstNull() {
		assertIterable(strings("").getValid());
		assertIllegalArg(() -> Parser.Type.of(null).getValid());
		assertIllegalArg(() -> Parser.Type.of(null).getValid("test"));
		assertIllegalArg(() -> Parser.Types.of((List<?>) null).getValid());
	}
	
	@Test
	public void shouldAcceptConsumer() {
		string("123").asInt().accept(i -> assertEquals(i, 123));
		string(null).asInt().accept(i -> assertEquals(i, null));
	}
	
	@Test
	public void shouldAcceptConsumerForEach() {
		var captor = Captor.ofInt();
		strings(null).asInts().each(captor::accept);
		captor.verifyInt();
		strings("1,2,3").asInts().each(captor::accept);
		captor.verifyInt(1, 2, 3);
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
		assertArray(strings(null).toLongArray(2), 2L);
		assertArray(strings("-0xffffffffffffffff, 0xffffffffffffffff").toLongArray(2), 1L, -1L);
		assertArray(strings(null).toDoubleArray(2), 2.0);
		assertArray(strings("-1, 0, 1").toDoubleArray(2), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldFailToConvertToArrayIfItemIsNull() {
		assertNpe(() -> Parser.Strings.of("1", null, "2").toIntArray(3, 4));
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
		assertIterable(strings("-0xffffffffffffffff,0xffffffffffffffff").asLongs().get(), 1L, -1L);
		assertIterable(strings("-1,NaN,1").asDoubles().get(), -1.0, Double.NaN, 1.0);
		assertIterable(strings("left,right").asEnums(H.class).get(), H.left, H.right);
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
		assertEquals(string(null).toBool(), null);
		assertEquals(string("true").toBool(), true);
		assertEquals(string(null).toBool(false), false);
		assertEquals(string("TRUE").toBool(false), true);
		assertEquals(string(null).toInt(), null);
		assertEquals(string("-1").toInt(), -1);
		assertEquals(string(null).toInt(2), 2);
		assertEquals(string("-0xffffffff").toInt(2), 1);
		assertEquals(string("0xffffffff").toInt(2), -1);
		assertEquals(string(null).toLong(), null);
		assertEquals(string("-1").toLong(), -1L);
		assertEquals(string(null).toLong(2), 2L);
		assertEquals(string("-0xffffffffffffffff").toLong(2), 1L);
		assertEquals(string("0xffffffffffffffff").toLong(2), -1L);
		assertEquals(string(null).toDouble(), null);
		assertEquals(string("-1").toDouble(), -1.0);
		assertEquals(string(null).toDouble(2), 2.0);
		assertEquals(string("NaN").toDouble(2), Double.NaN);
		assertEquals(string("1").toDouble(2), 1.0);
	}

	@Test
	public void shouldParseEnums() {
		assertEquals(string(null).toEnum(H.class), null);
		assertEquals(string(null).toEnum(H.left), H.left);
		assertEquals(string("right").toEnum(H.left), H.right);
		assertEquals(string("right").toEnum(H.class), H.right);
	}

	@Test
	public void shouldProvideBooleanConditionals() {
		assertEquals(string("True").toBool(1, 0), 1);
		assertEquals(string("").toBool(1, 0), 0);
		assertEquals(string(null).toBool(1, 0), null);
	}
	
	@Test
	public void shouldProvideBooleanConditionalType() {
		assertEquals(string("True").asBool(1, 0).get(), 1);
		assertEquals(string("").asBool(1, 0).get(), 0);
		assertEquals(string(null).asBool(1, 0).get(), null);
	}
	
	@Test
	public void shouldProvideBooleanConditionalTypes() {
		assertIterable(strings("True, x, false").asBools(1, 0).get(), 1, 0, 0);
		assertIterable(Parser.Strings.of("True", null, "x").asBools(1, 0).get(), 1, null, 0);
	}
	
	@Test
	public void shouldModifyStringType() {
		assertEquals(string("3").mod(s -> s + "0").toInt(), 30);
		assertEquals(string(null).mod(s -> s + "0").toInt(), null);
	}
	
	@Test
	public void shouldModifyStringTypes() {
		assertIterable(strings("1,3").modEach(s -> s + "0").asInts().get(), 10, 30);
		assertEquals(strings(null).modEach(s -> s + "0").asInts().get(), null);
		assertIterable(Parser.Strings.of("1", null).modEach(s -> s + "0").asInts().get(),
			10, null);
		//assertEquals(string(null).mod(s -> s + "0").toInt(), null);
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
