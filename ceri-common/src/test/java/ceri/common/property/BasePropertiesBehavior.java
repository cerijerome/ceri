package ceri.common.property;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.collection.CollectionUtil;

public class BasePropertiesBehavior {
	private static Properties properties = new Properties();

	@BeforeClass
	public static void createProperties() {
		properties.put("x", "X");
		properties.put("a", "A");
		properties.put("a.b", "AB");
		properties.put("a.b.c", "3");
	}

	@Test
	public void shouldOnlyReadPrefixedProperties() {
		BaseProperties bp = new BaseProperties(properties, "a") {};
		assertThat(bp.key("b.c"), is("a.b.c"));
		Collection<String> keys = new LinkedHashSet<>();
		assertThat(bp.keys(), is(CollectionUtil.addAll(keys, "a.b.c", "a.b", "a")));
	}

	@Test
	public void shouldAccessValuesWithKeySuffixes() {
		BaseProperties bp = new BaseProperties(properties, "a") {};
		assertThat(bp.value("b"), is("AB"));
		assertThat(bp.intValue("b.c"), is(3));
	}

	@Test
	public void shouldReturnDefaultValuesForMissingProperties() {
		BaseProperties bp = new BaseProperties(properties, null) {};
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

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableByte() {
		BaseProperties bp = new BaseProperties(properties, null) {};
		bp.byteValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableShort() {
		BaseProperties bp = new BaseProperties(properties, null) {};
		bp.shortValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableInt() {
		BaseProperties bp = new BaseProperties(properties, null) {};
		bp.intValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableLong() {
		BaseProperties bp = new BaseProperties(properties, null) {};
		bp.longValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableFloat() {
		BaseProperties bp = new BaseProperties(properties, null) {};
		bp.floatValue("x");
	}

	@Test(expected = NumberFormatException.class)
	public void shouldThrowExceptionForUnparseableDouble() {
		BaseProperties bp = new BaseProperties(properties, null) {};
		bp.doubleValue("x");
	}

}
