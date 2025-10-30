package ceri.common.property;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import org.junit.After;
import org.junit.Test;
import ceri.common.stream.Streams;
import ceri.common.test.Assert;
import ceri.common.test.ErrorGen;
import ceri.common.test.FileTestHelper;
import ceri.common.test.Testing;

public class PropertyTest {
	private FileTestHelper helper;

	@After
	public void after() {
		helper = Testing.close(helper);
	}

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Property.class);
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
		var properties = Property.merge(prop1, prop2);
		Assert.equal(properties.getProperty("a"), "a");
		Assert.equal(properties.getProperty("a.b"), "AB");
		Assert.equal(properties.getProperty("a.b.c"), "abc");
		Assert.equal(properties.getProperty("a.b.c.d"), "ABCD");
	}

	@Test
	public void testStoreWithFailingIO() throws IOException {
		var properties = TestProperties.of();
		properties.store.error.setFrom(ErrorGen.IOX);
		helper = FileTestHelper.builder().build();
		var file = helper.path("test.properties");
		Assert.thrown(() -> Property.store(properties, file));
		Property.store(new Properties(), file);
	}

	@Test
	public void testLoadClass() throws IOException {
		var properties = Property.load(TypedProperties.class);
		Assert.string(properties.get("abc"), "123");
	}

	@Test
	public void testLoadWithFailingIO() throws IOException {
		var properties = TestProperties.of();
		properties.load.error.setFrom(ErrorGen.IOX);
		helper = FileTestHelper.builder().build();
		var file = helper.path("test.properties");
		Assert.thrown(() -> Property.load(file));
		Assert.thrown(() -> Property.load(getClass(), "test.properties"));
	}

	@Test
	public void testStore() throws IOException {
		helper = FileTestHelper.builder().build();
		var properties = new Properties();
		properties.put("a.b", "ab");
		properties.put("a.b.c", "abc");
		var file = helper.path("test.properties");
		Property.store(properties, file);
		try (var stream = Files.lines(file)) {
			var lines = Streams.from(stream).filter(line -> !line.startsWith("#")).toList();
			Assert.unordered(lines, "a.b.c=abc", "a.b=ab");
		}
	}

	@Test
	public void testLoadResource() throws IOException {
		var properties = Property.load(getClass(), getClass().getSimpleName() + ".properties");
		Assert.equal(properties.getProperty("a.b.c"), "abc");
		Assert.equal(properties.getProperty("d.e.f"), "def");
	}

	@Test
	public void testLoadFile() throws IOException {
		helper = FileTestHelper.builder().file("a", "b=c").build();
		Properties properties = Property.load(helper.path("a"));
		Assert.equal(properties.getProperty("b"), "c");
	}

	@Test
	public void testLoadFileFromName() throws IOException {
		helper = FileTestHelper.builder().file("a", "b=c").build();
		Properties properties = Property.load(helper.path("a").toString());
		Assert.equal(properties.getProperty("b"), "c");
	}
}
