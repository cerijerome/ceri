package ceri.common.property;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;
import org.junit.Test;
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;

public class PropertyUtilTest {
	
	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(PropertyUtil.class);
	}

	@Test
	public void testStore() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			Properties properties = new Properties();
			properties.put("a.b", "ab");
			properties.put("a.b.c", "abc");
			File file = helper.file("test.properties");
			PropertyUtil.store(properties, file);
			String s = IoUtil.getContentString(file);
			BufferedReader r = new BufferedReader(new StringReader(s));
			String line;
			while ((line = r.readLine()).startsWith("#")) {}
			// Order doesn't matter
			assertThat(line, is("a.b.c=abc"));
			assertThat(r.readLine(), is("a.b=ab"));
		}
	}

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
