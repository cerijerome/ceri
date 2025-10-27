package ceri.common.property;

import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import ceri.common.collect.Sets;
import ceri.common.data.ByteUtil;
import ceri.common.function.Excepts;
import ceri.common.log.Level;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.Captor;

public class ParserBehavior {
	private static final Excepts.Function<RuntimeException, Integer, List<Integer>> BIT_LIST =
		i -> Streams.ints(ByteUtil.bits(i)).boxed().toList();
	private static final Excepts.Function<RuntimeException, Integer, Integer[]> BIT_ARRAY =
		i -> Streams.ints(ByteUtil.bits(i)).boxed().toArray(Integer[]::new);

	@Test
	public void shouldAllowNullValue() {
		Assert.equal(Parser.string("x").isNull(), false);
		Assert.equal(Parser.string(null).isNull(), true);
		Assert.equal(Parser.string(null).optional().isEmpty(), true);
		Assert.equal(Parser.string("test").to(_ -> null), null);
		Assert.equal(Parser.string("test").as(_ -> null).get(), null);
		Assert.equal(Parser.string(null).split().get(), null);
		Assert.equal(Parser.type(null).get(), null);
		Assert.equal(Parser.Type.from(() -> null).get(), null);
		Assert.equal(Parser.Types.from(() -> null).empty(), true);
	}

	@Test
	public void shouldProvideDefaultValues() {
		Assert.equal(Parser.type(null).def("x").get(), "x");
		Assert.equal(Parser.type(null).def(() -> "x").get(), "x");
		Assert.equal(Parser.Type.from(() -> null).def("x").get(), "x");
		Assert.ordered(Parser.types((List<Integer>) null).def(List.of(1)).get(), 1);
		Assert.ordered(Parser.types((List<Integer>) null).def(1).get(), 1);
		Assert.ordered(Parser.types((List<Integer>) null).def(() -> List.of(1)).get(), 1);
		Assert.ordered(Parser.Strings.from(() -> null).def("x").get(), "x");
		Assert.ordered(Parser.Strings.from(() -> null).def(List.of("x")).get(), "x");
		Assert.ordered(Parser.Strings.from(() -> null).def(() -> List.of("x")).get(), "x");
		Assert.ordered(Parser.Types.from(() -> null).def(List.of(1)).get(), 1);
		Assert.equal(Parser.string(null).def("x").get(), "x");
		Assert.equal(Parser.string(null).def(() -> "x").get(), "x");
	}

	@Test
	public void shouldValidateAgainstNull() {
		Assert.ordered(strings("").getValid());
		Assert.illegalArg(() -> Parser.type(null).getValid());
		Assert.illegalArg(() -> Parser.type(null).getValid("test"));
		Assert.illegalArg(() -> Parser.types((List<?>) null).getValid());
	}

	@Test
	public void shouldAcceptConsumer() {
		Parser.string("123").asInt().accept(i -> Assert.equal(i, 123));
		Parser.string(null).asInt().accept(i -> Assert.equal(i, null));
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
		Assert.equal(Parser.string(null).asBool().get(), null);
		Assert.equal(Parser.string(null).asLong().get(), null);
		Assert.equal(Parser.string(null).asDouble().get(), null);
		Assert.equal(Parser.string("true").asBool().get(), true);
		Assert.equal(Parser.string("-1").asLong().get(), -1L);
		Assert.equal(Parser.string("-1").asDouble().get(), -1.0);
	}

	@Test
	public void shouldSplitValues() {
		Assert.equal(Parser.<Integer>type(null).split(BIT_LIST).get(), null);
		Assert.ordered(Parser.type(0x124).split(BIT_LIST).get(), 2, 5, 8);
		Assert.ordered(Parser.type(0x124).splitArray(BIT_ARRAY).get(), 2, 5, 8);
	}

	@Test
	public void shouldSplitStrings() {
		Assert.equal(Parser.string(null).split().get(), null);
		Assert.ordered(Parser.string("1,2,3").split().get(), "1", "2", "3");
		Assert.ordered(Parser.string("1 2 3").split(Pattern.compile(" ")).get(), "1", "2", "3");
		Assert.ordered(Parser.string("/1/2//3/").split(Separator.SLASH).get(), "1", "2", "3");
	}

	@Test
	public void shouldConvertToArray() {
		Assert.array(Parser.types().array(Integer[]::new));
		Assert.array(Parser.types(-1, 0, 1).array(Integer[]::new), -1, 0, 1);
		Assert.array(Parser.types(true, false).toBoolArray(t -> t), true, false);
		Assert.array(Parser.types(-1, 0, 1).toIntArray(t -> t), -1, 0, 1);
		Assert.array(Parser.types(-1, 0, 1).toLongArray(t -> t), -1L, 0L, 1L);
		Assert.array(Parser.types(-1, 0, 1).toDoubleArray(t -> t), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldConvertStringsToArray() {
		Assert.array(strings("").array());
		Assert.array(strings("1,2").array(), "1", "2");
		Assert.array(strings(null).toBoolArray(true), true);
		Assert.array(strings("true,false").toBoolArray(true), true, false);
		Assert.array(strings(null).toIntArray(2), 2);
		Assert.array(strings("-0xffffffff, 0xffffffff").toIntArray(2), 1, -1);
		Assert.array(strings(null).toLongArray(2), 2L);
		Assert.array(strings("-0xffffffffffffffff, 0xffffffffffffffff").toLongArray(2), 1L, -1L);
		Assert.array(strings(null).toDoubleArray(2), 2.0);
		Assert.array(strings("-1, 0, 1").toDoubleArray(2), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldConvertStringsToArrayWithDefault() {
		Assert.array(strings("").arrayDef("a", "b", "c"));
	}

	@Test
	public void shouldConvertToArrayWithDefault() {
		Assert.array(Parser.Types.from(() -> null).arrayDef(Integer[]::new, 0, 1, 2), 0, 1, 2);
		Assert.array(Parser.types(-1, 0, 1).arrayDef(Integer[]::new, 0, 1, 2), -1, 0, 1);
	}

	@Test
	public void shouldFailToConvertToArrayIfItemIsNull() {
		Assert.isNull(Parser.types((List<Integer>) null).array(Integer[]::new));
		Assert.isNull(Parser.Strings.from(() -> null).array());
		Assert.array(Parser.strings("1", null, "2").toIntArray(3, 4), 1, 2);
	}

	@Test
	public void shouldStreamValues() {
		Assert.stream(Parser.Types.from(() -> null).stream());
		Assert.stream(Parser.Types.<String>from(() -> null).intStream(Integer::parseInt));
		Assert.stream(Parser.Types.<String>from(() -> null).longStream(Long::parseLong));
		Assert.stream(Parser.Types.<String>from(() -> null).doubleStream(Double::parseDouble));
		Assert.stream(Parser.types(1, null, 3).stream(), 1, null, 3);
		Assert.stream(Parser.types(-1.0, 0.0, 1.0).intStream(d -> d.intValue()), -1, 0, 1);
		Assert.stream(Parser.types(-1, 0, 1).longStream(i -> i.longValue()), -1L, 0L, 1L);
		Assert.stream(Parser.types(-1, 0, 1).doubleStream(i -> i.doubleValue()), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldStreamConvertedPrimitiveValues() {
		Assert.stream(strings("-1,0,1").intStream(), -1, 0, 1);
		Assert.stream(strings("-1,0,1").longStream(), -1L, 0L, 1L);
		Assert.stream(strings("-1,0,1").doubleStream(), -1.0, 0.0, 1.0);
	}

	@Test
	public void shouldFilterValues() {
		Assert.equal(Parser.Types.from(() -> null).filter().get(), null);
		Assert.ordered(Parser.types().filter().get());
		Assert.ordered(Parser.types(1, null, 2, null).filter().get(), 1, 2);
	}

	@Test
	public void shouldFilterStringValues() {
		Assert.equal(Parser.Strings.from(() -> null).filter().get(), null);
		Assert.ordered(Parser.strings().filter().get());
		Assert.ordered(Parser.strings("1", null, "2", null).filter().asInts().get(), 1, 2);
		Assert.ordered(
			Parser.strings("1", "a", "2", null).filter(Pattern.compile("\\d+")).asInts().get(), 1,
			2);
	}

	@Test
	public void shouldCollectValues() {
		Assert.equal(Parser.Types.from(() -> null).collect(Sets::of), null);
		Assert.equal(Parser.Types.from(() -> null).toList(), null);
		Assert.equal(Parser.Types.from(() -> null).toSet(), null);
		Assert.ordered(Parser.types(1, -1, 0).collect(Sets::tree), -1, 0, 1);
		Assert.ordered(Parser.types(-1, 0, 1).toList(), -1, 0, 1);
		Assert.ordered(Parser.types(-1, 0, 1).toSet(), -1, 0, 1);
	}

	@Test
	public void shouldConvertListItems() {
		Assert.equal(Parser.Strings.from(() -> null).toEach(String::length), null);
		Assert.ordered(Parser.types("a", "bb", "").toEach(String::length), 1, 2, 0);
		Assert.ordered(Parser.types((List<String>) null).toEachDef(String::length, 1), 1);
		Assert.ordered(strings("").toEachDef(String::length, 1));
	}

	@Test
	public void shouldSortItems() {
		Assert.equal(Parser.Types.from(() -> null).sort().get(), null);
		Assert.ordered(Parser.types(1, -1, 2, 0, -2).sort().get(), -2, -1, 0, 1, 2);
		Assert.thrown(ClassCastException.class, () -> Parser.types(1, "2", -1.0).sort());
		Assert.ordered(strings("2,0,1").sort().asInts().get(), 0, 1, 2);
	}

	@Test
	public void shouldConvertAccessorItems() {
		Assert.equal(Parser.Strings.from(() -> null).asEach(String::length).get(), null);
		Assert.ordered(Parser.types("a", "bb", "").asEach(String::length).get(), 1, 2, 0);
		Assert.ordered(strings("true, false").asBools().get(), true, false);
		Assert.ordered(strings("-0xffffffff,0xffffffff").asInts().get(), 1, -1);
		Assert.ordered(strings("-0xffffffffffffffff,0xffffffffffffffff").asLongs().get(), 1L, -1L);
		Assert.ordered(strings("-1,NaN,1").asDoubles().get(), -1.0, Double.NaN, 1.0);
		Assert.ordered(strings("WARN,INFO").asEnums(Level.class).get(), Level.WARN, Level.INFO);
	}

	@Test
	public void shouldCreateFromSupplier() {
		Assert.equal(Parser.String.from(() -> null).get(), null);
		Assert.equal(Parser.String.from(() -> null).get("test"), "test");
		Assert.equal(Parser.String.from(() -> "test").get(), "test");
	}

	@Test
	public void shouldParsePrimitiveStrings() {
		Assert.equal(Parser.string(null).toBool(), null);
		Assert.equal(Parser.string("true").toBool(), true);
		Assert.equal(Parser.string(null).toBool(false), false);
		Assert.equal(Parser.string("TRUE").toBool(false), true);
		Assert.equal(Parser.string(null).toInt(), null);
		Assert.equal(Parser.string("-1").toInt(), -1);
		Assert.equal(Parser.string(null).toInt(2), 2);
		Assert.equal(Parser.string("-0xffffffff").toInt(2), 1);
		Assert.equal(Parser.string("0xffffffff").toInt(2), -1);
		Assert.equal(Parser.string(null).toLong(), null);
		Assert.equal(Parser.string("-1").toLong(), -1L);
		Assert.equal(Parser.string(null).toLong(2), 2L);
		Assert.equal(Parser.string("-0xffffffffffffffff").toLong(2), 1L);
		Assert.equal(Parser.string("0xffffffffffffffff").toLong(2), -1L);
		Assert.equal(Parser.string(null).toDouble(), null);
		Assert.equal(Parser.string("-1").toDouble(), -1.0);
		Assert.equal(Parser.string(null).toDouble(2), 2.0);
		Assert.equal(Parser.string("NaN").toDouble(2), Double.NaN);
		Assert.equal(Parser.string("1").toDouble(2), 1.0);
	}

	@Test
	public void shouldParseEnums() {
		Assert.equal(Parser.string(null).toEnum(Level.class), null);
		Assert.equal(Parser.string(null).toEnum(Level.TRACE), Level.TRACE);
		Assert.equal(Parser.string(null).asEnum(Level.class).get(), null);
		Assert.equal(Parser.string(null).asEnum(Level.class).get(Level.WARN), Level.WARN);
		Assert.equal(Parser.string("WARN").toEnum(Level.ALL), Level.WARN);
		Assert.illegalArg(() -> Parser.string("warn").toEnum(Level.class));
		Assert.equal(Parser.string("WARN").toEnum(Level.class), Level.WARN);
		Assert.equal(Parser.string("WARN").asEnum(Level.class).get(), Level.WARN);
	}

	@Test
	public void shouldProvideBooleanConditionals() {
		Assert.equal(Parser.string("True").toBool(1, 0), 1);
		Assert.equal(Parser.string("0").toBool(1, 0), 0);
		Assert.equal(Parser.string(null).toBool(1, 0), null);
	}

	@Test
	public void shouldProvideBooleanConditionalType() {
		Assert.equal(Parser.string("True").asBool(1, 0).get(), 1);
		Assert.equal(Parser.string("0").asBool(1, 0).get(), 0);
		Assert.equal(Parser.string(null).asBool(1, 0).get(), null);
	}

	@Test
	public void shouldProvideBooleanConditionalTypes() {
		Assert.ordered(strings("True, 1, false").asBools(1, 0).get(), 1, 1, 0);
		Assert.ordered(Parser.strings("True", null, "0").asBools(1, 0).get(), 1, null, 0);
	}

	@Test
	public void shouldModifyStringType() {
		Assert.equal(Parser.string("3").mod(s -> s + "0").toInt(), 30);
		Assert.equal(Parser.string(null).mod(s -> s + "0").toInt(), null);
	}

	@Test
	public void shouldModifyStringTypes() {
		Assert.ordered(strings("1,3").modEach(s -> s + "0").asInts().get(), 10, 30);
		Assert.equal(strings(null).modEach(s -> s + "0").asInts().get(), null);
		Assert.ordered(Parser.strings("1", null).modEach(s -> s + "0").asInts().get(), 10, null);
	}

	@Test
	public void shouldFailForBadConversion() {
		Assert.illegalArg(() -> Parser.string("x").toInt(1));
		Assert.illegalArg(() -> strings("x").toIntArray(1));
	}

	@Test
	public void shouldFailForBadSplit() {
		Assert.illegalArg(() -> Parser.type(1).split(_ -> Assert.throwRuntime()));
	}

	private static Parser.Strings strings(String s) {
		return Parser.string(s).split();
	}
}
