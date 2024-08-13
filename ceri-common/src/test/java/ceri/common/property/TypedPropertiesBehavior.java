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
		TypedProperties tp = TypedProperties.merge( //
			TypedProperties.from(load(getClass(), "property-test-a-b-c.properties")),
			TypedProperties.from(load(getClass(), "property-test-d-e-f.properties")));
		assertEquals(tp.value("name"), "property-test-d-e-f");
		assertEquals(tp.value("a.b.c"), "true");
		assertEquals(tp.value("d.e.f"), "true");
	}

	@Test
	public void shouldCreateEmptyPropertiesFromNull() {
		TypedProperties tp = TypedProperties.from((TypedProperties) null, "m.n.0");
		assertNull(tp.value("a.b.c"));
		assertNull(tp.value(""));
	}

	@Test
	public void shouldCreateFromResourceBundle() {
		ResourceBundle r = ResourceBundle.getBundle( //
			"ceri.common.property.PropertyAccessor", Locale.ENGLISH);
		TypedProperties tp = TypedProperties.from(r);
		assertEquals(tp.value("name"), "PropertyAccessor");
	}

	@Test
	public void shouldSetValues() {
		var p = new Properties();
		p.setProperty("a.b.c", "123");
		var tp = TypedProperties.from(p, "a");
		tp.setValue(456, "b", "c");
		assertEquals(p.getProperty("a.b.c"), "456");
	}

	@Test
	public void shouldRemoveValues() {
		var p = new Properties();
		p.setProperty("a.b.c", "123");
		var tp = TypedProperties.from(p, "a");
		tp.setValue(null, "b", "c");
		assertFalse(p.containsKey("a.b.d"));
	}

	@Test
	public void shouldReturnDescendants() {
		TypedProperties tp = TypedProperties.from(properties, "m.n.0");
		assertCollection(tp.descendants(), "a", "b", "b.c", "b.c.d", "b.d");
		assertCollection(tp.descendants("b"), "c", "c.d", "d");
	}

	@Test
	public void shouldReturnChildren() {
		TypedProperties tp = TypedProperties.from(properties);
		assertCollection(tp.children(), "xyz", "x", "y", "z", "a", "m", "3", "7");
		assertCollection(tp.children("m.n.0"), "a", "b");
		tp = TypedProperties.from(properties, "m.n.0");
		assertCollection(tp.children(), "a", "b");
		assertCollection(tp.children("b"), "c", "d");
	}

	@Test
	public void shouldCheckIfChildrenExist() {
		TypedProperties tp = TypedProperties.from(properties, "m.n");
		assertTrue(tp.hasChild("0"));
		assertTrue(tp.hasChild("0.b"));
		assertFalse(tp.hasChild("0.c"));
		assertTrue(tp.hasChild("0.b.c"));
		assertTrue(tp.hasChild("1"));
		assertTrue(tp.hasChild("2.a"));
		assertFalse(tp.hasChild("3"));
	}

	@Test
	public void shouldReturnChildIds() {
		TypedProperties tp = TypedProperties.from(properties);
		assertCollection(tp.childIds("m.n"), 0, 1, 2);
		assertCollection(tp.childIds("m"));
		assertCollection(tp.childIds(""), 3, 7);
		assertCollection(tp.childIds(), 3, 7);
		tp = TypedProperties.from(properties, "m");
		assertCollection(tp.childIds("n"), 0, 1, 2);
		tp = TypedProperties.from(properties, "m.n");
		assertCollection(tp.childIds(""), 0, 1, 2);
	}

	@Test
	public void shouldReadCommaSeparatedValues() {
		TypedProperties tp = TypedProperties.from(properties);
		assertCollection(tp.values(String::length, "y"), 3, 2, 1);
		assertCollection(tp.values(Collections.singletonList(999), String::length, "xx"), 999);
		assertCollection(tp.booleanValues("7.2.b"), true, false);
		assertCollection(tp.byteValues("7.2.i"), (byte) 7, (byte) 2);
		assertCollection(tp.charValues("7.2.i"), '7', '2');
		assertCollection(tp.shortValues("7.2.i"), (short) 7, (short) 2);
		assertCollection(tp.intValues("7.2.i"), 7, 2);
		assertCollection(tp.longValues("7.2.i"), 7L, 2L);
		assertCollection(tp.floatValues("7.2.f"), 7.2f, 0.1f);
		assertCollection(tp.doubleValues("7.2.f"), 7.2, 0.1);
	}

	@Test
	public void shouldReadEnums() {
		TypedProperties tp = TypedProperties.from(properties);
		assertEquals(tp.enumValue(E.class, "a.b"), E.AB);
		assertEquals(tp.enumValue(E.class, E.A, "a.b"), E.AB);
		assertEquals(tp.enumValue(E.class, E.A, "xx"), E.A);
		assertThrown(() -> tp.enumValue(E.class, "a.b.c"));
		assertCollection(tp.enumValues(E.class, "a", "abc"), E.A, E.ABC);
	}

	@Test
	public void shouldReadValues() {
		TypedProperties tp = TypedProperties.from(properties);
		assertNull(tp.value("xyz"));
		assertEquals(tp.stringValue("", "a"), "A");
		assertFalse(tp.booleanValue("a"));
		assertFalse(tp.booleanValue(true, "a"));
		assertEquals(tp.byteValue("a.b.c"), (byte) 3);
		assertEquals(tp.byteValue((byte) 0, "a.b.c"), (byte) 3);
		assertEquals(tp.charValue("a"), 'A');
		assertEquals(tp.charValue('B', "a"), 'A');
		assertEquals(tp.shortValue("a.b.c"), (short) 3);
		assertEquals(tp.shortValue((short) 1, "a.b.c"), (short) 3);
		assertEquals(tp.intValue("a.b.c"), 3);
		assertEquals(tp.intValue(1, "a.b.c"), 3);
		assertEquals(tp.longValue("a.b.c"), 3L);
		assertEquals(tp.longValue(1L, "a.b.c"), 3L);
		assertEquals(tp.floatValue("a.b.c"), 3.0f);
		assertEquals(tp.floatValue(1.0f, "a.b.c"), 3.0f);
		assertEquals(tp.doubleValue("a.b.c"), 3.0);
		assertEquals(tp.doubleValue(1.0, "a.b.c"), 3.0);
		assertPath(tp.pathValue("m.n.0.b.c.d"), "mn0bcd");
		assertPath(tp.pathValue(java.nio.file.Path.of("a"), "m.n.0.b.c.d"), "mn0bcd");
	}

	@Test
	public void shouldReadAndConvertValues() {
		TypedProperties tp = TypedProperties.from(properties);
		assertEquals(tp.valueFromBoolean(b -> b ? "Y" : "N", "a.y"), "Y");
		assertEquals(tp.valueFromBoolean(b -> b ? "Y" : "N", "a.n"), "N");
		assertEquals(tp.valueFromBoolean(1, 2, "a.y"), 1);
		assertEquals(tp.valueFromBoolean(1, 2, "a.n"), 2);
		assertEquals(tp.valueFromBoolean(0, 1, 2, "a.y"), 1);
		assertEquals(tp.valueFromBoolean(0, 1, 2, "a.n"), 2);
		assertEquals(tp.valueFromBoolean(0, 1, 2, "a.x"), 0);
		assertEquals(tp.valueFromInt(Integer::toString, "a.b.c"), "3");
		assertEquals(tp.valueFromDouble(Double::toString, "a.b.c"), "3.0");
		assertEquals(tp.valueFromLong(Long::toHexString, "a.l"), "fedcba987654321");
	}

	@Test
	public void shouldReadAndConvertListValues() {
		TypedProperties tp = TypedProperties.from(properties);
		assertIterable(tp.valuesFromBoolean(Boolean::toString, "z.b"), "true", "false", "true");
		assertIterable(tp.valuesFromInt(Integer::toHexString, "z.i"), "12345678", "ffffffff", "ff");
		assertIterable(tp.valuesFromLong(Long::toHexString, "z.l"), "123456789abcdef0",
			"ffffffffffffffff", "ff");
		assertIterable(tp.valuesFromDouble(Double::toString, "z.d"), "123.4", "-0.1", "1000.0");
	}

	@Test
	public void shouldHaveStringRepresentationOfProperties() {
		Properties properties = new Properties();
		properties.put("a", "A");
		properties.put("b", "B");
		TypedProperties tp = TypedProperties.from(properties);
		assertMatch(tp.toString(), ".*a=A.*");
		assertMatch(tp.toString(), ".*b=B.*");
	}

	@Test
	public void shouldExtendPrefixWhenCreatingFromBaseProperties() {
		TypedProperties tp0 = TypedProperties.from(properties);
		TypedProperties tp1 = TypedProperties.from(tp0);
		assertEquals(tp1.value("a"), "A");
		TypedProperties tp2 = TypedProperties.from(tp1, "a");
		assertEquals(tp2.value("b"), "AB");
		TypedProperties tp3 = TypedProperties.from(tp2, "b", "c");
		assertEquals(tp3.value("d"), "4");
	}

	@Test
	public void shouldReturnStringValuesFromACommaSeparatedList() {
		TypedProperties tp = TypedProperties.from(properties);
		assertEquals(tp.stringValues("x"), Arrays.asList("X"));
		assertEquals(tp.stringValues("y"), Arrays.asList("YyY", "yy", "y"));
		assertEquals(tp.stringValues("z"), Arrays.asList());
		assertNull(tp.stringValues("Z"));
		List<String> def = Arrays.asList("d,ef");
		assertEquals(tp.stringValues(def, "x"), Arrays.asList("X"));
		assertEquals(tp.stringValues(def, "y"), Arrays.asList("YyY", "yy", "y"));
		assertEquals(tp.stringValues(def, "z"), Arrays.asList());
		assertEquals(tp.stringValues(def, "Z"), def);
	}

	@Test
	public void shouldAllowNullPrefix() {
		TypedProperties tp = TypedProperties.from(properties, new String[] { null });
		assertEquals(tp.value("a"), "A");
		tp = TypedProperties.from(properties, (String[]) null);
		assertEquals(tp.value("a"), "A");
		tp = TypedProperties.from(properties, null, null);
		assertEquals(tp.value("a"), "A");
		tp = TypedProperties.from(properties, "a", null);
		assertEquals(tp.value("b"), "AB");
		tp = TypedProperties.from(properties, null, "a");
		assertEquals(tp.value("b"), "AB");
	}

	@Test
	public void shouldOnlyReadPrefixedProperties() {
		TypedProperties tp = TypedProperties.from(properties, "a");
		assertEquals(tp.key("b.c"), "a.b.c");
		assertCollection(tp.keys(), "a.b.c.d", "a.b.c", "a.b", "a.abc", "a", "a.y", "a.n", "a.l");
		tp = TypedProperties.from(properties);
		assertEquals(tp.key("a.b"), "a.b");
		assertCollection(tp.keys(), properties.keySet());
	}

	@Test
	public void shouldAccessValuesWithKeySuffixes() {
		TypedProperties tp = TypedProperties.from(properties, "a");
		assertEquals(tp.value("b"), "AB");
		assertEquals(tp.intValue("b.c"), 3);
	}

	@Test
	public void shouldReturnDefaultValuesForMissingProperties() {
		TypedProperties tp = TypedProperties.from(properties);
		assertTrue(tp.booleanValue(true, "xx"));
		assertEquals(tp.charValue('x', "xx"), 'x');
		assertEquals(tp.stringValue("x", "xx"), "x");
		assertEquals(tp.byteValue(Byte.MIN_VALUE, "xx"), Byte.MIN_VALUE);
		assertEquals(tp.shortValue(Short.MIN_VALUE, "xx"), Short.MIN_VALUE);
		assertEquals(tp.intValue(Integer.MIN_VALUE, "xx"), Integer.MIN_VALUE);
		assertEquals(tp.longValue(Long.MIN_VALUE, "xx"), Long.MIN_VALUE);
		assertEquals(tp.floatValue(Float.MIN_VALUE, "xx"), Float.MIN_VALUE);
		assertEquals(tp.doubleValue(Double.MIN_VALUE, "xx"), Double.MIN_VALUE);
		assertPath(tp.pathValue(java.nio.file.Path.of("a"), "xx"), "a");
	}

	@Test
	public void shouldReadBooleanValuesAsFalseForUnparseableStrings() {
		TypedProperties tp = TypedProperties.from(properties);
		assertFalse(tp.booleanValue("a.b"));
		assertFalse(tp.booleanValue("a.b.c"));
	}

	@Test
	public void shouldReadCharValuesAsFirstCharInString() {
		TypedProperties tp = TypedProperties.from(properties);
		assertEquals(tp.charValue("a.b"), 'A');
		assertEquals(tp.charValue("a.b.c"), '3');
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableByte() {
		TypedProperties tp = TypedProperties.from(properties);
		tp.byteValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableShort() {
		TypedProperties tp = TypedProperties.from(properties);
		tp.shortValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableInt() {
		TypedProperties tp = TypedProperties.from(properties);
		tp.intValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableLong() {
		TypedProperties tp = TypedProperties.from(properties);
		tp.longValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableFloat() {
		TypedProperties tp = TypedProperties.from(properties);
		tp.floatValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableDouble() {
		TypedProperties tp = TypedProperties.from(properties);
		tp.doubleValue("x");
	}

}
