package ceri.common.property;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnordered;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import ceri.common.stream.Streams;
import ceri.common.test.ErrorGen;
import ceri.common.test.FileTestHelper;
import ceri.common.test.TestUtil;

public class PropertyUtilTest {
	private FileTestHelper helper;

	@After
	public void after() {
		helper = TestUtil.close(helper);
	}

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(PropertyUtil.class);
	}

	@Test
	public void testMerge() {
		var prop1 = new Properties();
		prop1.put("a", "a");
		prop1.put("a.b", "ab");
		prop1.put("a.b.c", "abc");
		var prop2 = new Properties();
		prop2.put("a.b", "AB");
		prop2.put("a.b.c.d", "ABCD");
		var properties = PropertyUtil.merge(prop1, prop2);
		assertEquals(properties.getProperty("a"), "a");
		assertEquals(properties.getProperty("a.b"), "AB");
		assertEquals(properties.getProperty("a.b.c"), "abc");
		assertEquals(properties.getProperty("a.b.c.d"), "ABCD");
	}

	@Test
	public void testStoreWithFailingIO() throws IOException {
		var properties = TestProperties.of();
		properties.store.error.setFrom(ErrorGen.IOX);
		helper = FileTestHelper.builder().build();
		var file = helper.path("test.properties");
		assertThrown(() -> PropertyUtil.store(properties, file));
		PropertyUtil.store(new Properties(), file);
	}

	@Test
	public void testLoadWithFailingIO() throws IOException {
		var properties = TestProperties.of();
		properties.load.error.setFrom(ErrorGen.IOX);
		helper = FileTestHelper.builder().build();
		var file = helper.path("test.properties");
		assertThrown(() -> PropertyUtil.load(file));
		assertThrown(() -> PropertyUtil.load(getClass(), "test.properties"));
	}

	@Test
	public void testStore() throws IOException {
		helper = FileTestHelper.builder().build();
		var properties = new Properties();
		properties.put("a.b", "ab");
		properties.put("a.b.c", "abc");
		var file = helper.path("test.properties");
		PropertyUtil.store(properties, file);
		try (var stream = Files.lines(file)) {
			var lines = Streams.from(stream).filter(line -> !line.startsWith("#")).toList();
			assertUnordered(lines, "a.b.c=abc", "a.b=ab");
		}
	}

	@Test
	public void testLoadResource() throws IOException {
		var properties = PropertyUtil.load(getClass(), getClass().getSimpleName() + ".properties");
		assertEquals(properties.getProperty("a.b.c"), "abc");
		assertEquals(properties.getProperty("d.e.f"), "def");
	}

	@Test
	public void testLoadFile() throws IOException {
		helper = FileTestHelper.builder().file("a", "b=c").build();
		Properties properties = PropertyUtil.load(helper.path("a"));
		assertEquals(properties.getProperty("b"), "c");
	}

	@Test
	public void testLoadFileFromName() throws IOException {
		helper = FileTestHelper.builder().file("a", "b=c").build();
		Properties properties = PropertyUtil.load(helper.path("a").toString());
		assertEquals(properties.getProperty("b"), "c");
	}
}
