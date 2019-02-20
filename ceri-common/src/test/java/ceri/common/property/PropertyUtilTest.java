package ceri.common.property;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.test.FileTestHelper;

public class PropertyUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(PropertyUtil.class);
	}

	@Test
	public void testMerge() {
		Properties prop1 = new Properties();
		prop1.put("a", "a");
		prop1.put("a.b", "ab");
		prop1.put("a.b.c", "abc");
		Properties prop2 = new Properties();
		prop2.put("a.b", "AB");
		prop2.put("a.b.c.d", "ABCD");
		Properties properties = PropertyUtil.merge(prop1, prop2);
		assertThat(properties.getProperty("a"), is("a"));
		assertThat(properties.getProperty("a.b"), is("AB"));
		assertThat(properties.getProperty("a.b.c"), is("abc"));
		assertThat(properties.getProperty("a.b.c.d"), is("ABCD"));
	}

	@Test
	public void testStoreWithFailingIO() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			Properties properties = Mockito.mock(Properties.class);
			doThrow(new IOException()).when(properties).store((OutputStream) any(), anyString());
			File file = helper.file("test.properties");
			assertException(() -> PropertyUtil.store(properties, file));
			PropertyUtil.store(new Properties(), file);
		}
	}

	@Test
	public void testLoadWithFailingIO() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			Properties properties = Mockito.mock(Properties.class);
			doThrow(new IOException()).when(properties).load((InputStream) any());
			File file = helper.file("test.properties");
			assertException(() -> PropertyUtil.load(file));
			assertException(() -> PropertyUtil.load(getClass(), "test.properties"));
		}
	}

	@Test
	public void testStore() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			Properties properties = new Properties();
			properties.put("a.b", "ab");
			properties.put("a.b.c", "abc");
			File file = helper.file("test.properties");
			PropertyUtil.store(properties, file);
			List<String> lines =
				toList(Files.lines(Paths.get(file.toURI())).filter(line -> !line.startsWith("#")));
			assertCollection(lines, "a.b.c=abc", "a.b=ab");
		}
	}

	@Test
	public void testProperty() {
		Properties properties = new Properties();
		properties.setProperty("a.b.c", "abc");
		String value = PropertyUtil.property(properties, PathFactory.dot.path("a", "b", "c"));
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

	@Test
	public void testLoadFileFromName() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().file("a", "b=c").build()) {
			Properties properties = PropertyUtil.load(helper.file("a").getPath());
			assertThat(properties.getProperty("b"), is("c"));
		}
	}

	@Test
	public void testLoadLocators() throws IOException {
		Locator abc = Locator.of(getClass(), "property-test-a-b-c");
		Locator a = Locator.of(getClass(), "property-test-a");
		Locator def = Locator.of(getClass(), "property-test-d-e-f");
		Properties properties = PropertyUtil.load(abc, def, a);
		assertThat(properties.getProperty("a"), is("true"));
		assertThat(properties.getProperty("a.b.c"), is("true"));
		assertThat(properties.getProperty("d.e.f"), is("true"));
		assertThat(properties.getProperty("name"), is("property-test-a"));
	}

	@Test
	public void testLoadLocatorPaths() throws IOException {
		Locator abc = Locator.of(getClass(), "property-test-a-b-c");
		Locator def = Locator.of(getClass(), "property-test-d-e-f");
		Properties properties = PropertyUtil.loadPaths(abc, def);
		assertThat(properties.getProperty("a"), is("true"));
		assertThat(properties.getProperty("a.b.c"), is("true"));
		assertThat(properties.getProperty("d.e.f"), is("true"));
		assertThat(properties.getProperty("name"), is("property-test-d-e-f"));
	}

}
