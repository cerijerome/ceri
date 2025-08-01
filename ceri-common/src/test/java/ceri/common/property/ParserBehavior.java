package ceri.common.property;

import static ceri.common.property.Parser.string;
import static ceri.common.test.AssertUtil.assertArray;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertOrdered;
import static ceri.common.test.AssertUtil.assertStream;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.throwRuntime;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import org.junit.Test;
import ceri.common.data.ByteUtil;
import ceri.common.function.Excepts.Function;
import ceri.common.test.Captor;
import ceri.common.util.Align.H;

public class ParserBehavior {
	private static final Function<RuntimeException, Integer, List<Integer>> BIT_LIST =
		i -> IntStream.of(ByteUtil.bits(i)).boxed().toList();
	private static final Function<RuntimeException, Integer, Integer[]> BIT_ARRAY =
		i -> IntStream.of(ByteUtil.bits(i)).boxed().toArray(Integer[]::new);

	@Test
	public void shouldAllowNullValue() {
		assertEquals(string("x").isNull(), false);
		assertEquals(string(null).isNull(), true);
		assertEquals(string(null).optional().isEmpty(), true);
		assertEquals(string("test").to(_ -> null), null);
		assertEquals(string("test").as(_ -> null).get(), null);
		assertEquals(string(null).split().get(), null);
		assertEquals(Parser.type(null).get(), null);
		assertEquals(Parser.Type.from(() -> null).get(), null);
		assertEquals(Parser.Types.from(() -> null).empty(), true);
	}

	@Test
	public void shouldProvideDefaultValues() {
		assertEquals(Parser.type(null).def("x").get(), "x");
		assertEquals(Parser.type(null).def(() -> "x").get(), "x");
		assertEquals(Parser.Type.from(() -> null).def("x").get(), "x");
		assertOrdered(Parser.types((List<Integer>) null).def(List.of(1)).get(), 1);
		assertOrdered(Parser.types((List<Integer>) null).def(1).get(), 1);
		assertOrdered(Parser.types((List<Integer>) null).def(() -> List.of(1)).get(), 1);
		assertOrdered(Parser.Strings.from(() -> null).def("x").get(), "x");
		assertOrdered(Parser.Strings.from(() -> null).def(List.of("x")).get(), "x");
		assertOrdered(Parser.Strings.from(() -> null).def(() -> List.of("x")).get(), "x");
		assertOrdered(Parser.Types.from(() -> null).def(List.of(1)).get(), 1);
		assertEquals(string(null).def("x").get(), "x");
		assertEquals(string(null).def(() -> "x").get(), "x");
	}

	@Test
	public void shouldValidateAgainstNull() {
		assertOrdered(strings("").getValid());
		assertIllegalArg(() -> Parser.type(null).getValid());
		assertIllegalArg(() -> Parser.type(null).getValid("test"));
		assertIllegalArg(() -> Parser.types((List<?>) null).getValid());
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
	public void shouldConvertStrings() {
		assertEquals(string(null).asBool().get(), null);
		assertEquals(string(null).asLong().get(), null);
		assertEquals(string(null).asDouble().get(), null);
		assertEquals(string("true").asBool().get(), true);
		assertEquals(string("-1").asLong().get(), -1L);
		assertEquals(string("-1").asDouble().get(), -1.0);
	}

	@Test
	public void shouldSplitValues() {
		assertEquals(Parser.<Integer>type(null).split(BIT_LIST).get(), null);
		assertOrdered(Parser.type(0x124).split(BIT_LIST).get(), 2, 5, 8);
		assertOrdered(Parser.type(0x124).splitArray(BIT_ARRAY).get(), 2, 5, 8);
	}

	@Test
	public void shouldSplitStrings() {
		assertEquals(string(null).split().get(), null);
		assertOrdered(string("1,2,3").split().get(), "1", "2", "3");
		assertOrdered(string("1 2 3").split(Pattern.compile(" ")).get(), "1", "2", "3");
		assertOrdered(string("/1/2//3/").split(Separator.SLASH).get(), "1", "2", "3");
	}

	@Test
	public void shouldConvertToArray() {
		assertArray(Parser.types().array(Integer[]::new));
		assertArray(Parser.types(-1, 0, 1).array(Integer[]::new), -1, 0, 1);
		assertArray(Parser.types(true, false).toBoolArray(t -> t), true, false);
		assertArray(Parser.types(-1, 0, 1).toIntArray(t -> t), -1, 0, 1);
		assertArray(Parser.types(-1, 0, 1).toLongArray(t -> t), -1L, 0L, 1L);
		assertArray(Parser.types(-1, 0, 1).toDoubleArray(t -> t), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldConvertStringsToArray() {
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
	public void shouldConvertStringsToArrayWithDefault() {
		assertArray(strings("").arrayDef("a", "b", "c"));
	}

	@Test
	public void shouldConvertToArrayWithDefault() {
		assertArray(Parser.Types.from(() -> null).arrayDef(Integer[]::new, 0, 1, 2), 0, 1, 2);
		assertArray(Parser.types(-1, 0, 1).arrayDef(Integer[]::new, 0, 1, 2), -1, 0, 1);
	}

	@Test
	public void shouldFailToConvertToArrayIfItemIsNull() {
		assertNull(Parser.types((List<Integer>) null).array(Integer[]::new));
		assertNull(Parser.Strings.from(() -> null).array());
		assertArray(Parser.strings("1", null, "2").toIntArray(3, 4), 1, 2);
	}

	@Test
	public void shouldStreamValues() {
		assertStream(Parser.Types.from(() -> null).stream());
		assertStream(Parser.Types.<String>from(() -> null).intStream(Integer::parseInt));
		assertStream(Parser.Types.<String>from(() -> null).longStream(Long::parseLong));
		assertStream(Parser.Types.<String>from(() -> null).doubleStream(Double::parseDouble));
		assertStream(Parser.types(1, null, 3).stream(), 1, null, 3);
		assertStream(Parser.types(-1.0, 0.0, 1.0).intStream(d -> d.intValue()), -1, 0, 1);
		assertStream(Parser.types(-1, 0, 1).longStream(i -> i.longValue()), -1L, 0L, 1L);
		assertStream(Parser.types(-1, 0, 1).doubleStream(i -> i.doubleValue()), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldStreamConvertedPrimitiveValues() {
		assertStream(strings("-1,0,1").intStream(), -1, 0, 1);
		assertStream(strings("-1,0,1").longStream(), -1L, 0L, 1L);
		assertStream(strings("-1,0,1").doubleStream(), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldFilterValues() {
		assertEquals(Parser.Types.from(() -> null).filter().get(), null);
		assertOrdered(Parser.types().filter().get());
		assertOrdered(Parser.types(1, null, 2, null).filter().get(), 1, 2);
	}

	@Test
	public void shouldFilterStringValues() {
		assertEquals(Parser.Strings.from(() -> null).filter().get(), null);
		assertOrdered(Parser.strings().filter().get());
		assertOrdered(Parser.strings("1", null, "2", null).filter().asInts().get(), 1, 2);
		assertOrdered(
			Parser.strings("1", "a", "2", null).filter(Pattern.compile("\\d+")).asInts().get(), 1,
			2);
	}

	@Test
	public void shouldCollectValues() {
		assertEquals(Parser.Types.from(() -> null).collect(TreeSet::new), null);
		assertEquals(Parser.Types.from(() -> null).toList(), null);
		assertEquals(Parser.Types.from(() -> null).toSet(), null);
		assertOrdered(Parser.types(1, -1, 0).collect(TreeSet::new), -1, 0, 1);
		assertOrdered(Parser.types(-1, 0, 1).toList(), -1, 0, 1);
		assertOrdered(Parser.types(-1, 0, 1).toSet(), -1, 0, 1);
	}

	@Test
	public void shouldConvertListItems() {
		assertEquals(Parser.Strings.from(() -> null).toEach(String::length), null);
		assertOrdered(Parser.types("a", "bb", "").toEach(String::length), 1, 2, 0);
		assertOrdered(Parser.types((List<String>) null).toEachDef(String::length, 1), 1);
		assertOrdered(strings("").toEachDef(String::length, 1));
	}

	@Test
	public void shouldSortItems() {
		assertEquals(Parser.Types.from(() -> null).sort().get(), null);
		assertOrdered(Parser.types(1, -1, 2, 0, -2).sort().get(), -2, -1, 0, 1, 2);
		assertThrown(ClassCastException.class, () -> Parser.types(1, "2", -1.0).sort());
		assertOrdered(strings("2,0,1").sort().asInts().get(), 0, 1, 2);
	}

	@Test
	public void shouldConvertAccessorItems() {
		assertEquals(Parser.Strings.from(() -> null).asEach(String::length).get(), null);
		assertOrdered(Parser.types("a", "bb", "").asEach(String::length).get(), 1, 2, 0);
		assertOrdered(strings("true, false").asBools().get(), true, false);
		assertOrdered(strings("-0xffffffff,0xffffffff").asInts().get(), 1, -1);
		assertOrdered(strings("-0xffffffffffffffff,0xffffffffffffffff").asLongs().get(), 1L, -1L);
		assertOrdered(strings("-1,NaN,1").asDoubles().get(), -1.0, Double.NaN, 1.0);
		assertOrdered(strings("left,right").asEnums(H.class).get(), H.left, H.right);
	}

	@Test
	public void shouldCreateFromSupplier() {
		assertEquals(Parser.String.from(() -> null).get(), null);
		assertEquals(Parser.String.from(() -> null).get("test"), "test");
		assertEquals(Parser.String.from(() -> "test").get(), "test");
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
		assertEquals(string(null).asEnum(H.class).get(), null);
		assertEquals(string(null).asEnum(H.class).get(H.left), H.left);
		assertEquals(string("right").toEnum(H.left), H.right);
		assertEquals(string("right").toEnum(H.class), H.right);
		assertEquals(string("right").asEnum(H.class).get(), H.right);
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
		assertOrdered(strings("True, x, false").asBools(1, 0).get(), 1, 0, 0);
		assertOrdered(Parser.strings("True", null, "x").asBools(1, 0).get(), 1, null, 0);
	}

	@Test
	public void shouldModifyStringType() {
		assertEquals(string("3").mod(s -> s + "0").toInt(), 30);
		assertEquals(string(null).mod(s -> s + "0").toInt(), null);
	}

	@Test
	public void shouldModifyStringTypes() {
		assertOrdered(strings("1,3").modEach(s -> s + "0").asInts().get(), 10, 30);
		assertEquals(strings(null).modEach(s -> s + "0").asInts().get(), null);
		assertOrdered(Parser.strings("1", null).modEach(s -> s + "0").asInts().get(), 10, null);
	}

	@Test
	public void shouldFailForBadConversion() {
		assertIllegalArg(() -> string("x").toInt(1));
		assertIllegalArg(() -> strings("x").toIntArray(1));
	}

	@Test
	public void shouldFailForBadSplit() {
		assertIllegalArg(() -> Parser.type(1).split(_ -> throwRuntime()));
	}

	private static Parser.Strings strings(String s) {
		return string(s).split();
	}
}
