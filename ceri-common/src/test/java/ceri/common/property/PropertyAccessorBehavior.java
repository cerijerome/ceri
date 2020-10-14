package ceri.common.property;

import static ceri.common.test.TestUtil.assertCollection;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static ceri.common.test.TestUtil.assertThat;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.Test;

public class PropertyAccessorBehavior {

	@Test
	public void shouldAccessResourceBundles() {
		ResourceBundle r = ResourceBundle.getBundle( //
			"ceri.common.property.PropertyAccessor", Locale.ENGLISH);
		PropertyAccessor accessor = PropertyAccessor.from(r);
		assertThat(accessor.property("name"), is("PropertyAccessor"));
		assertThat(accessor.property("locale"), is("en"));
		assertNull(accessor.property("test"));
		assertThat(accessor.toString(), is(r.toString()));
		assertCollection(accessor.keys(), "name", "locale");
	}

	@Test
	public void shouldAccessResourceBundleAsAMap() {
		ResourceBundle r = ResourceBundle.getBundle( //
			"ceri.common.property.PropertyAccessor", Locale.ENGLISH);
		PropertyAccessor accessor = PropertyAccessor.from(r);
		Map<? super String, ? super String> map = accessor.properties();
		assertThat(map.get("name"), is("PropertyAccessor"));
		assertThat(map.get("locale"), is("en"));
		assertNull(map.get("test"));
	}

	@Test
	public void shouldAccessPropertyFiles() throws IOException {
		Properties p = PropertyUtil.load(getClass(), "PropertyAccessor_en.properties");
		PropertyAccessor accessor = PropertyAccessor.from(p);
		assertNull(accessor.property("name"));
		assertThat(accessor.property("locale"), is("en"));
		assertNull(accessor.property("test"));
		assertThat(accessor.toString(), is(p.toString()));
		assertCollection(accessor.keys(), "locale");
	}

	@Test
	public void shouldAccessPropertyFileAsAMap() throws IOException {
		Properties p = PropertyUtil.load(getClass(), "PropertyAccessor.properties");
		PropertyAccessor accessor = PropertyAccessor.from(p);
		Map<? super String, ? super String> map = accessor.properties();
		assertThat(map.get("name"), is("PropertyAccessor"));
		assertThat(map.get("locale"), is("none"));
		assertNull(map.get("test"));
	}

}
