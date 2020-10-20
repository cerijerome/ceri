package ceri.common.property;

import static ceri.common.collection.StreamUtil.toList;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
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
		assertEquals(properties.getProperty("a"), "a");
		assertEquals(properties.getProperty("a.b"), "AB");
		assertEquals(properties.getProperty("a.b.c"), "abc");
		assertEquals(properties.getProperty("a.b.c.d"), "ABCD");
	}

	@Test
	public void testStoreWithFailingIO() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			doThrow(new IOException()).when(properties).store((Writer) any(), anyString());
			java.nio.file.Path file = helper.path("test.properties");
			assertThrown(() -> PropertyUtil.store(properties, file));
			PropertyUtil.store(new Properties(), file);
		}
	}

	@Test
	public void testLoadWithFailingIO() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			doThrow(new IOException()).when(properties).load((InputStream) any());
			java.nio.file.Path file = helper.path("test.properties");
			assertThrown(() -> PropertyUtil.load(file));
			assertThrown(() -> PropertyUtil.load(getClass(), "test.properties"));
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
		assertEquals(value, "abc");
	}

	@Test
	public void testLoadResource() throws IOException {
		Properties properties = PropertyUtil.load(getClass());
		assertEquals(properties.getProperty("a.b.c"), "abc");
		assertEquals(properties.getProperty("d.e.f"), "def");
	}

	@Test
	public void testLoadFile() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().file("a", "b=c").build()) {
			Properties properties = PropertyUtil.load(helper.path("a"));
			assertEquals(properties.getProperty("b"), "c");
		}
	}

	@Test
	public void testLoadFileFromName() throws IOException {
		try (FileTestHelper helper = FileTestHelper.builder().file("a", "b=c").build()) {
			Properties properties = PropertyUtil.load(helper.path("a").toString());
			assertEquals(properties.getProperty("b"), "c");
		}
	}

	@Test
	public void testLoadLocators() throws IOException {
		Locator abc = Locator.of(getClass(), "property-test-a-b-c");
		Locator a = Locator.of(getClass(), "property-test-a");
		Locator def = Locator.of(getClass(), "property-test-d-e-f");
		Properties properties = PropertyUtil.load(abc, def, a);
		assertEquals(properties.getProperty("a"), "true");
		assertEquals(properties.getProperty("a.b.c"), "true");
		assertEquals(properties.getProperty("d.e.f"), "true");
		assertEquals(properties.getProperty("name"), "property-test-a");
	}

	@Test
	public void testLoadLocatorPaths() throws IOException {
		Locator abc = Locator.of(getClass(), "property-test-a-b-c");
		Locator def = Locator.of(getClass(), "property-test-d-e-f");
		Properties properties = PropertyUtil.loadPaths(abc, def);
		assertEquals(properties.getProperty("a"), "true");
		assertEquals(properties.getProperty("a.b.c"), "true");
		assertEquals(properties.getProperty("d.e.f"), "true");
		assertEquals(properties.getProperty("name"), "property-test-d-e-f");
	}

}
