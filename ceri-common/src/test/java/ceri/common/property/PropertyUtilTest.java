package ceri.common.property;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.test.TestUtil.assertCollection;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static ceri.common.test.TestUtil.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class PropertyUtilTest {
	private static Properties properties;

	@BeforeClass
	public static void beforeClass() {
		properties = Mockito.mock(Properties.class);
	}

	@Before
	public void before() {
		Mockito.clearInvocations(properties); // reduce test times
	}

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
			doThrow(new IOException()).when(properties).store((Writer) any(), anyString());
			java.nio.file.Path file = helper.path("test.properties");
			TestUtil.assertThrown(() -> PropertyUtil.store(properties, file));
			PropertyUtil.store(new Properties(), file);
		}
	}

	@Test
	public void testLoadWithFailingIO() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			doThrow(new IOException()).when(properties).load((InputStream) any());
			java.nio.file.Path file = helper.path("test.properties");
			TestUtil.assertThrown(() -> PropertyUtil.load(file));
			TestUtil.assertThrown(() -> PropertyUtil.load(getClass(), "test.properties"));
		}
	}

	@Test
	public void testStore() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			Properties properties = new Properties();
			properties.put("a.b", "ab");
			properties.put("a.b.c", "abc");
			java.nio.file.Path file = helper.path("test.properties");
			PropertyUtil.store(properties, file);
			List<String> lines = toList(Files.lines(file).filter(line -> !line.startsWith("#")));
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
			Properties properties = PropertyUtil.load(helper.path("a"));
			assertThat(properties.getProperty("b"), is("c"));
		}
	}

	@Test
	public void testLoadFileFromName() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().file("a", "b=c").build()) {
			Properties properties = PropertyUtil.load(helper.path("a").toString());
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
