package ceri.common.property;

import static ceri.common.property.PropertyUtil.load;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertMatch;
import static ceri.common.test.AssertUtil.assertNull;
import static ceri.common.test.AssertUtil.assertPath;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.BeforeClass;
import org.junit.Test;

public class TypedPropertiesBehavior {
	private static Properties properties = new Properties();

	private enum E {
		A,
		AB,
		ABC;
	}

	@BeforeClass
	public static void createProperties() {
		properties.put("xyz", "");
		properties.put("x", "X");
		properties.put("y", "YyY,yy , y   ,");
		properties.put("z", ",");
		properties.put("z.b", "true, false, true");
		properties.put("z.i", "0x12345678, -1, 255");
		properties.put("z.l", "0x123456789abcdef0, -1, 255");
		properties.put("z.d", "123.4, -0.1, 1e3");
		properties.put("a", "A");
		properties.put("a.b", "AB");
		properties.put("a.abc", "A,ABC");
		properties.put("a.b.c", "3");
		properties.put("a.b.c.d", "4");
		properties.put("a.y", "true");
		properties.put("a.n", "false");
		properties.put("a.l", "0xfedcba987654321");
		properties.put("m.n.0.a", "mn0a");
		properties.put("m.n.0.b", "mn0b");
		properties.put("m.n.0.b.c", "mn0bc");
		properties.put("m.n.0.b.c.d", "mn0bcd");
		properties.put("m.n.0.b.d", "mn0bd");
		properties.put("m.n.1", "mn1");
		properties.put("m.n.2.a", "mn2a");
		properties.put("3.1", "31");
		properties.put("7.2", "72");
		properties.put("7.2.b", "true,false");
		properties.put("7.2.i", "7,2");
		properties.put("7.2.f", "7.2, 0.1");
	}

	@Test
	public void shouldMerge() throws IOException {
		TypedProperties bp = TypedProperties.merge( //
			TypedProperties.from(load(getClass(), "property-test-a-b-c.properties")),
			TypedProperties.from(load(getClass(), "property-test-d-e-f.properties")));
		assertEquals(bp.value("name"), "property-test-d-e-f");
		assertEquals(bp.value("a.b.c"), "true");
		assertEquals(bp.value("d.e.f"), "true");
	}

	@Test
	public void shouldCreateEmptyPropertiesFromNull() {
		TypedProperties bp = TypedProperties.from((TypedProperties) null, "m.n.0");
		assertNull(bp.value("a.b.c"));
		assertNull(bp.value(""));
	}

	@Test
	public void shouldCreateFromResourceBundle() {
		ResourceBundle r = ResourceBundle.getBundle( //
			"ceri.common.property.PropertyAccessor", Locale.ENGLISH);
		TypedProperties bp = TypedProperties.from(r);
		assertEquals(bp.value("name"), "PropertyAccessor");
	}

	@Test
	public void shouldReturnDescendants() {
		TypedProperties bp = TypedProperties.from(properties, "m.n.0");
		assertCollection(bp.descendants(), "a", "b", "b.c", "b.c.d", "b.d");
		assertCollection(bp.descendants("b"), "c", "c.d", "d");
	}

	@Test
	public void shouldReturnChildren() {
		TypedProperties bp = TypedProperties.from(properties);
		assertCollection(bp.children(), "xyz", "x", "y", "z", "a", "m", "3", "7");
		assertCollection(bp.children("m.n.0"), "a", "b");
		bp = TypedProperties.from(properties, "m.n.0");
		assertCollection(bp.children(), "a", "b");
		assertCollection(bp.children("b"), "c", "d");
	}

	@Test
	public void shouldCheckIfChildrenExist() {
		TypedProperties bp = TypedProperties.from(properties, "m.n");
		assertTrue(bp.hasChild("0"));
		assertTrue(bp.hasChild("0.b"));
		assertFalse(bp.hasChild("0.c"));
		assertTrue(bp.hasChild("0.b.c"));
		assertTrue(bp.hasChild("1"));
		assertTrue(bp.hasChild("2.a"));
		assertFalse(bp.hasChild("3"));
	}

	@Test
	public void shouldReturnChildIds() {
		TypedProperties bp = TypedProperties.from(properties);
		assertCollection(bp.childIds("m.n"), 0, 1, 2);
		assertCollection(bp.childIds("m"));
		assertCollection(bp.childIds(""), 3, 7);
		assertCollection(bp.childIds(), 3, 7);
		bp = TypedProperties.from(properties, "m");
		assertCollection(bp.childIds("n"), 0, 1, 2);
		bp = TypedProperties.from(properties, "m.n");
		assertCollection(bp.childIds(""), 0, 1, 2);
	}

	@Test
	public void shouldReadCommaSeparatedValues() {
		TypedProperties bp = TypedProperties.from(properties);
		assertCollection(bp.values(String::length, "y"), 3, 2, 1);
		assertCollection(bp.values(Collections.singletonList(999), String::length, "xx"), 999);
		assertCollection(bp.booleanValues("7.2.b"), true, false);
		assertCollection(bp.byteValues("7.2.i"), (byte) 7, (byte) 2);
		assertCollection(bp.charValues("7.2.i"), '7', '2');
		assertCollection(bp.shortValues("7.2.i"), (short) 7, (short) 2);
		assertCollection(bp.intValues("7.2.i"), 7, 2);
		assertCollection(bp.longValues("7.2.i"), 7L, 2L);
		assertCollection(bp.floatValues("7.2.f"), 7.2f, 0.1f);
		assertCollection(bp.doubleValues("7.2.f"), 7.2, 0.1);
	}

	@Test
	public void shouldReadEnums() {
		TypedProperties bp = TypedProperties.from(properties);
		assertEquals(bp.enumValue(E.class, "a.b"), E.AB);
		assertEquals(bp.enumValue(E.class, E.A, "a.b"), E.AB);
		assertEquals(bp.enumValue(E.class, E.A, "xx"), E.A);
		assertThrown(() -> bp.enumValue(E.class, "a.b.c"));
		assertCollection(bp.enumValues(E.class, "a", "abc"), E.A, E.ABC);
	}

	@Test
	public void shouldReadValues() {
		TypedProperties bp = TypedProperties.from(properties);
		assertNull(bp.value("xyz"));
		assertEquals(bp.stringValue("", "a"), "A");
		assertFalse(bp.booleanValue("a"));
		assertFalse(bp.booleanValue(true, "a"));
		assertEquals(bp.byteValue("a.b.c"), (byte) 3);
		assertEquals(bp.byteValue((byte) 0, "a.b.c"), (byte) 3);
		assertEquals(bp.charValue("a"), 'A');
		assertEquals(bp.charValue('B', "a"), 'A');
		assertEquals(bp.shortValue("a.b.c"), (short) 3);
		assertEquals(bp.shortValue((short) 1, "a.b.c"), (short) 3);
		assertEquals(bp.intValue("a.b.c"), 3);
		assertEquals(bp.intValue(1, "a.b.c"), 3);
		assertEquals(bp.longValue("a.b.c"), 3L);
		assertEquals(bp.longValue(1L, "a.b.c"), 3L);
		assertEquals(bp.floatValue("a.b.c"), 3.0f);
		assertEquals(bp.floatValue(1.0f, "a.b.c"), 3.0f);
		assertEquals(bp.doubleValue("a.b.c"), 3.0);
		assertEquals(bp.doubleValue(1.0, "a.b.c"), 3.0);
		assertPath(bp.pathValue("m.n.0.b.c.d"), "mn0bcd");
		assertPath(bp.pathValue(java.nio.file.Path.of("a"), "m.n.0.b.c.d"), "mn0bcd");
	}

	@Test
	public void shouldReadAndConvertValues() {
		TypedProperties bp = TypedProperties.from(properties);
		assertEquals(bp.valueFromBoolean(b -> b ? "Y" : "N", "a.y"), "Y");
		assertEquals(bp.valueFromBoolean(b -> b ? "Y" : "N", "a.n"), "N");
		assertEquals(bp.valueFromBoolean(1, 2, "a.y"), 1);
		assertEquals(bp.valueFromBoolean(1, 2, "a.n"), 2);
		assertEquals(bp.valueFromBoolean(0, 1, 2, "a.y"), 1);
		assertEquals(bp.valueFromBoolean(0, 1, 2, "a.n"), 2);
		assertEquals(bp.valueFromBoolean(0, 1, 2, "a.x"), 0);
		assertEquals(bp.valueFromInt(Integer::toString, "a.b.c"), "3");
		assertEquals(bp.valueFromDouble(Double::toString, "a.b.c"), "3.0");
		assertEquals(bp.valueFromLong(Long::toHexString, "a.l"), "fedcba987654321");
	}

	@Test
	public void shouldReadAndConvertListValues() {
		TypedProperties bp = TypedProperties.from(properties);
		assertIterable(bp.valuesFromBoolean(Boolean::toString, "z.b"), "true", "false", "true");
		assertIterable(bp.valuesFromInt(Integer::toHexString, "z.i"), "12345678", "ffffffff", "ff");
		assertIterable(bp.valuesFromLong(Long::toHexString, "z.l"), "123456789abcdef0",
			"ffffffffffffffff", "ff");
		assertIterable(bp.valuesFromDouble(Double::toString, "z.d"), "123.4", "-0.1", "1000.0");
	}

	@Test
	public void shouldHaveStringRepresentationOfProperties() {
		Properties properties = new Properties();
		properties.put("a", "A");
		properties.put("b", "B");
		TypedProperties bp = TypedProperties.from(properties);
		assertMatch(bp.toString(), ".*a=A.*");
		assertMatch(bp.toString(), ".*b=B.*");
	}

	@Test
	public void shouldExtendPrefixWhenCreatingFromBaseProperties() {
		TypedProperties bp0 = TypedProperties.from(properties);
		TypedProperties bp1 = TypedProperties.from(bp0);
		assertEquals(bp1.value("a"), "A");
		TypedProperties bp2 = TypedProperties.from(bp1, "a");
		assertEquals(bp2.value("b"), "AB");
		TypedProperties bp3 = TypedProperties.from(bp2, "b", "c");
		assertEquals(bp3.value("d"), "4");
	}

	@Test
	public void shouldReturnStringValuesFromACommaSeparatedList() {
		TypedProperties bp = TypedProperties.from(properties);
		assertEquals(bp.stringValues("x"), Arrays.asList("X"));
		assertEquals(bp.stringValues("y"), Arrays.asList("YyY", "yy", "y"));
		assertEquals(bp.stringValues("z"), Arrays.asList());
		assertNull(bp.stringValues("Z"));
		List<String> def = Arrays.asList("d,ef");
		assertEquals(bp.stringValues(def, "x"), Arrays.asList("X"));
		assertEquals(bp.stringValues(def, "y"), Arrays.asList("YyY", "yy", "y"));
		assertEquals(bp.stringValues(def, "z"), Arrays.asList());
		assertEquals(bp.stringValues(def, "Z"), def);
	}

	@Test
	public void shouldAllowNullPrefix() {
		TypedProperties bp = TypedProperties.from(properties, new String[] { null });
		assertEquals(bp.value("a"), "A");
		bp = TypedProperties.from(properties, (String[]) null);
		assertEquals(bp.value("a"), "A");
		bp = TypedProperties.from(properties, null, null);
		assertEquals(bp.value("a"), "A");
		bp = TypedProperties.from(properties, "a", null);
		assertEquals(bp.value("b"), "AB");
		bp = TypedProperties.from(properties, null, "a");
		assertEquals(bp.value("b"), "AB");
	}

	@Test
	public void shouldOnlyReadPrefixedProperties() {
		TypedProperties bp = TypedProperties.from(properties, "a");
		assertEquals(bp.key("b.c"), "a.b.c");
		assertCollection(bp.keys(), "a.b.c.d", "a.b.c", "a.b", "a.abc", "a", "a.y", "a.n", "a.l");
		bp = TypedProperties.from(properties);
		assertEquals(bp.key("a.b"), "a.b");
		assertCollection(bp.keys(), properties.keySet());
	}

	@Test
	public void shouldAccessValuesWithKeySuffixes() {
		TypedProperties bp = TypedProperties.from(properties, "a");
		assertEquals(bp.value("b"), "AB");
		assertEquals(bp.intValue("b.c"), 3);
	}

	@Test
	public void shouldReturnDefaultValuesForMissingProperties() {
		TypedProperties bp = TypedProperties.from(properties);
		assertTrue(bp.booleanValue(true, "xx"));
		assertEquals(bp.charValue('x', "xx"), 'x');
		assertEquals(bp.stringValue("x", "xx"), "x");
		assertEquals(bp.byteValue(Byte.MIN_VALUE, "xx"), Byte.MIN_VALUE);
		assertEquals(bp.shortValue(Short.MIN_VALUE, "xx"), Short.MIN_VALUE);
		assertEquals(bp.intValue(Integer.MIN_VALUE, "xx"), Integer.MIN_VALUE);
		assertEquals(bp.longValue(Long.MIN_VALUE, "xx"), Long.MIN_VALUE);
		assertEquals(bp.floatValue(Float.MIN_VALUE, "xx"), Float.MIN_VALUE);
		assertEquals(bp.doubleValue(Double.MIN_VALUE, "xx"), Double.MIN_VALUE);
		assertPath(bp.pathValue(java.nio.file.Path.of("a"), "xx"), "a");
	}

	@Test
	public void shouldReadBooleanValuesAsFalseForUnparseableStrings() {
		TypedProperties bp = TypedProperties.from(properties);
		assertFalse(bp.booleanValue("a.b"));
		assertFalse(bp.booleanValue("a.b.c"));
	}

	@Test
	public void shouldReadCharValuesAsFirstCharInString() {
		TypedProperties bp = TypedProperties.from(properties);
		assertEquals(bp.charValue("a.b"), 'A');
		assertEquals(bp.charValue("a.b.c"), '3');
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableByte() {
		TypedProperties bp = TypedProperties.from(properties);
		bp.byteValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableShort() {
		TypedProperties bp = TypedProperties.from(properties);
		bp.shortValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableInt() {
		TypedProperties bp = TypedProperties.from(properties);
		bp.intValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableLong() {
		TypedProperties bp = TypedProperties.from(properties);
		bp.longValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableFloat() {
		TypedProperties bp = TypedProperties.from(properties);
		bp.floatValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableDouble() {
		TypedProperties bp = TypedProperties.from(properties);
		bp.doubleValue("x");
	}

}
