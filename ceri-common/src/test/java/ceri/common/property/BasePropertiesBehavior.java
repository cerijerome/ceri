package ceri.common.property;

import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.matchesRegex;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class BasePropertiesBehavior {
	private static Properties properties = new Properties();

	@BeforeClass
	public static void createProperties() {
		properties.put("x", "X");
		properties.put("y", "YyY,yy , y   ,");
		properties.put("z", ",");
		properties.put("a", "A");
		properties.put("a.b", "AB");
		properties.put("a.b.c", "3");
		properties.put("a.b.c.d", "4");
		properties.put("m.n.0.a", "mn0a");
		properties.put("m.n.0.b", "mn0b");
		properties.put("m.n.0.b.c", "mn0bc");
		properties.put("m.n.0.b.c.d", "mn0bcd");
		properties.put("m.n.0.b.d", "mn0bd");
		properties.put("m.n.1", "mn1");
		properties.put("m.n.2.a", "mn2a");
		properties.put("3.1", "31");
		properties.put("7.2", "72");
	}

	@Test
	public void shouldReturnDescendants() {
		BaseProperties bp = new BaseProperties(properties, "m.n.0") {};
		assertCollection(bp.descendants(), "a", "b", "b.c", "b.c.d", "b.d");
		assertCollection(bp.descendants("b"), "c", "c.d", "d");
	}
	
	@Test
	public void shouldReturnChildren() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertCollection(bp.children(), "x", "y", "z", "a", "m", "3", "7");
		assertCollection(bp.children("m.n.0"), "a", "b");
		bp = new BaseProperties(properties, "m.n.0") {};
		assertCollection(bp.children(), "a", "b");
		assertCollection(bp.children("b"), "c", "d");
	}
	
	@Test
	public void shouldReturnChildIds() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertCollection(bp.childIds("m.n"), 0, 1, 2);
		assertCollection(bp.childIds("m"));
		assertCollection(bp.childIds(""), 3, 7);
		assertCollection(bp.childIds(), 3, 7);
		bp = new BaseProperties(properties, "m") {};
		assertCollection(bp.childIds("n"), 0, 1, 2);
		bp = new BaseProperties(properties, "m.n") {};
		assertCollection(bp.childIds(""), 0, 1, 2);
	}
	
	@Test
	public void shouldReadValues() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertThat(bp.stringValue("", "a"), is("A"));
		assertThat(bp.booleanValue("a"), is(false));
		assertThat(bp.booleanValue(true, "a"), is(false));
		assertThat(bp.charValue("a"), is('A'));
		assertThat(bp.charValue('B', "a"), is('A'));
		assertThat(bp.shortValue("a.b.c"), is((short) 3));
		assertThat(bp.shortValue((short) 1, "a.b.c"), is((short) 3));
		assertThat(bp.intValue("a.b.c"), is(3));
		assertThat(bp.intValue(1, "a.b.c"), is(3));
		assertThat(bp.longValue("a.b.c"), is(3L));
		assertThat(bp.longValue(1L, "a.b.c"), is(3L));
		assertThat(bp.floatValue("a.b.c"), is(3.0f));
		assertThat(bp.floatValue(1.0f, "a.b.c"), is(3.0f));
		assertThat(bp.doubleValue("a.b.c"), is(3.0));
		assertThat(bp.doubleValue(1.0, "a.b.c"), is(3.0));
	}

	@Test
	public void shouldHaveStringRepresentationOfProperties() {
		Properties properties = new Properties();
		properties.put("a", "A");
		properties.put("b", "B");
		BaseProperties bp = new BaseProperties(properties) {};
		assertThat(bp.toString(), matchesRegex(".*a=A.*"));
		assertThat(bp.toString(), matchesRegex(".*b=B.*"));
	}

	@Test
	public void shouldExtendPrefixWhenCreatingFromBaseProperties() {
		BaseProperties bp0 = new BaseProperties(properties) {};
		BaseProperties bp1 = new BaseProperties(bp0) {};
		assertThat(bp1.value("a"), is("A"));
		BaseProperties bp2 = new BaseProperties(bp1, "a") {};
		assertThat(bp2.value("b"), is("AB"));
		BaseProperties bp3 = new BaseProperties(bp2, "b", "c") {};
		assertThat(bp3.value("d"), is("4"));
	}

	@Test
	public void shouldReturnStringValuesFromACommaSeparatedList() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertThat(bp.stringValues("x"), is(Arrays.asList("X")));
		assertThat(bp.stringValues("y"), is(Arrays.asList("YyY", "yy", "y")));
		assertThat(bp.stringValues("z"), is(Arrays.asList()));
		assertNull(bp.stringValues("Z"));
		Collection<String> def = Arrays.asList("d,ef");
		assertThat(bp.stringValues(def, "x"), is(Arrays.asList("X")));
		assertThat(bp.stringValues(def, "y"), is(Arrays.asList("YyY", "yy", "y")));
		assertThat(bp.stringValues(def, "z"), is(def));
		assertThat(bp.stringValues(def, "Z"), is(def));
	}

	@Test
	public void shouldAllowNullPrefix() {
		BaseProperties bp = new BaseProperties(properties, new String[] { null }) {};
		assertThat(bp.value("a"), is("A"));
		bp = new BaseProperties(properties, (String[]) null) {};
		assertThat(bp.value("a"), is("A"));
		bp = new BaseProperties(properties, null, null) {};
		assertThat(bp.value("a"), is("A"));
		bp = new BaseProperties(properties, "a", null) {};
		assertThat(bp.value("b"), is("AB"));
		bp = new BaseProperties(properties, null, "a") {};
		assertThat(bp.value("b"), is("AB"));
	}

	@Test
	public void shouldOnlyReadPrefixedProperties() {
		BaseProperties bp = new BaseProperties(properties, "a") {};
		assertThat(bp.key("b.c"), is("a.b.c"));
		assertCollection(bp.keys(), "a.b.c.d", "a.b.c", "a.b", "a");
	}

	@Test
	public void shouldAccessValuesWithKeySuffixes() {
		BaseProperties bp = new BaseProperties(properties, "a") {};
		assertThat(bp.value("b"), is("AB"));
		assertThat(bp.intValue("b.c"), is(3));
	}

	@Test
	public void shouldReturnDefaultValuesForMissingProperties() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertThat(bp.booleanValue(true, "xx"), is(true));
		assertThat(bp.charValue('x', "xx"), is('x'));
		assertThat(bp.stringValue("x", "xx"), is("x"));
		assertThat(bp.byteValue(Byte.MIN_VALUE, "xx"), is(Byte.MIN_VALUE));
		assertThat(bp.shortValue(Short.MIN_VALUE, "xx"), is(Short.MIN_VALUE));
		assertThat(bp.intValue(Integer.MIN_VALUE, "xx"), is(Integer.MIN_VALUE));
		assertThat(bp.longValue(Long.MIN_VALUE, "xx"), is(Long.MIN_VALUE));
		assertThat(bp.floatValue(Float.MIN_VALUE, "xx"), is(Float.MIN_VALUE));
		assertThat(bp.doubleValue(Double.MIN_VALUE, "xx"), is(Double.MIN_VALUE));
	}

	@Test
	public void shouldReadBooleanValuesAsFalseForUnparseableStrings() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertFalse(bp.booleanValue("a.b"));
		assertFalse(bp.booleanValue("a.b.c"));
	}

	@Test
	public void shouldReadCharValuesAsFirstCharInString() {
		BaseProperties bp = new BaseProperties(properties) {};
		assertThat(bp.charValue("a.b"), is('A'));
		assertThat(bp.charValue("a.b.c"), is('3'));
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableByte() {
		BaseProperties bp = new BaseProperties(properties) {};
		bp.byteValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableShort() {
		BaseProperties bp = new BaseProperties(properties) {};
		bp.shortValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableInt() {
		BaseProperties bp = new BaseProperties(properties) {};
		bp.intValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableLong() {
		BaseProperties bp = new BaseProperties(properties) {};
		bp.longValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableFloat() {
		BaseProperties bp = new BaseProperties(properties) {};
		bp.floatValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableDouble() {
		BaseProperties bp = new BaseProperties(properties) {};
		bp.doubleValue("x");
	}

}
