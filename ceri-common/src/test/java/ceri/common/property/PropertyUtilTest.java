package ceri.common.property;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.util.Properties;
import org.junit.Test;
import ceri.common.test.FileTestHelper;

public class PropertyUtilTest {

	@Test
	public void testProperty() {
		Properties properties = new Properties();
		properties.setProperty("a.b.c", "abc");
		String value = PropertyUtil.property(properties, Key.create("a", "b", "c"));
		assertThat(value, is("abc"));
	}

	@Test
	public void testLoadResource() throws IOException {
		Properties properties = PropertyUtil.load(getClass());
		assertThat(properties.getProperty("a.b.c"), is("abc"));
		assertThat(properties.getProperty("d.e.f"), is("def"));
	}

	@Test
	public void testLoadFile() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().file("a", "b=c").build()) {
			Properties properties = PropertyUtil.load(helper.file("a"));
			assertThat(properties.getProperty("b"), is("c"));
		}
	}

}
