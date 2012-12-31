package ceri.common.property;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;

public class PropertyUtilTest {

	@Test
	public void testProperty() {
		Properties properties = new Properties();
		properties.setProperty("a.b.c", "abc");
		String value = PropertyUtil.property(properties, Key.create("a", "b", "c"));
		assertThat(value, is("abc"));
	}

	@Test
	public void testLoad() throws IOException {
		Properties properties = PropertyUtil.load(getClass());
		assertThat(properties.getProperty("a.b.c"), is("abc"));
		assertThat(properties.getProperty("d.e.f"), is("def"));
	}

}
