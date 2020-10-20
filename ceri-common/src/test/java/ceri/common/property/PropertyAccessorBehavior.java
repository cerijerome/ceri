package ceri.common.property;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertNull;
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
		assertEquals(accessor.property("name"), "PropertyAccessor");
		assertEquals(accessor.property("locale"), "en");
		assertNull(accessor.property("test"));
		assertEquals(accessor.toString(), r.toString());
		assertCollection(accessor.keys(), "name", "locale");
	}

	@Test
	public void shouldAccessResourceBundleAsAMap() {
		ResourceBundle r = ResourceBundle.getBundle( //
			"ceri.common.property.PropertyAccessor", Locale.ENGLISH);
		PropertyAccessor accessor = PropertyAccessor.from(r);
		Map<? super String, ? super String> map = accessor.properties();
		assertEquals(map.get("name"), "PropertyAccessor");
		assertEquals(map.get("locale"), "en");
		assertNull(map.get("test"));
	}

	@Test
	public void shouldAccessPropertyFiles() throws IOException {
		Properties p = PropertyUtil.load(getClass(), "PropertyAccessor_en.properties");
		PropertyAccessor accessor = PropertyAccessor.from(p);
		assertNull(accessor.property("name"));
		assertEquals(accessor.property("locale"), "en");
		assertNull(accessor.property("test"));
		assertEquals(accessor.toString(), p.toString());
		assertCollection(accessor.keys(), "locale");
	}

	@Test
	public void shouldAccessPropertyFileAsAMap() throws IOException {
		Properties p = PropertyUtil.load(getClass(), "PropertyAccessor.properties");
		PropertyAccessor accessor = PropertyAccessor.from(p);
		Map<? super String, ? super String> map = accessor.properties();
		assertEquals(map.get("name"), "PropertyAccessor");
		assertEquals(map.get("locale"), "none");
		assertNull(map.get("test"));
	}

}
