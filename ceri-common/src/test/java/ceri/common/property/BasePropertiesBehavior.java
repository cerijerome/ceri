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
		properties.put("x",  "X");
		properties.put("a",  "A");
		properties.put("a.b",  "AB");
		properties.put("a.b.c",  "3");
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


}
