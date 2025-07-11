package ceri.common.property;

import static ceri.common.stream.StreamUtil.toList;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.ErrorGen.IOX;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Properties;
import org.junit.Test;
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
		assertEquals(properties.getProperty("a"), "a");
		assertEquals(properties.getProperty("a.b"), "AB");
		assertEquals(properties.getProperty("a.b.c"), "abc");
		assertEquals(properties.getProperty("a.b.c.d"), "ABCD");
	}

	@Test
	public void testStoreWithFailingIO() throws IOException {
		TestProperties properties = TestProperties.of();
		properties.store.error.setFrom(IOX);
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
			java.nio.file.Path file = helper.path("test.properties");
			assertThrown(() -> PropertyUtil.store(properties, file));
			PropertyUtil.store(new Properties(), file);
		}
	}

	@Test
	public void testLoadWithFailingIO() throws IOException {
		TestProperties properties = TestProperties.of();
		properties.load.error.setFrom(IOX);
		try (FileTestHelper helper = FileTestHelper.builder().build()) {
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
			try (var stream = Files.lines(file)) {
				List<String> lines = toList(stream.filter(line -> !line.startsWith("#")));
				assertCollection(lines, "a.b.c=abc", "a.b=ab");
			}
		}
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
}
