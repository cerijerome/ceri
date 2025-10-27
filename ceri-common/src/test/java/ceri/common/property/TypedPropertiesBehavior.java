package ceri.common.property;

import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.BeforeClass;
import org.junit.Test;
import ceri.common.test.Assert;
import ceri.common.test.FileTestHelper;

public class TypedPropertiesBehavior {
	private static Properties properties = new Properties();

	@BeforeClass
	public static void createProperties() {
		properties.put("xyz", "");
		properties.put("x", "X");
		properties.put("y", "YyY,yy , y   ,");
		properties.put("z", ",");
		properties.put("z.b", "true, false, true");
		properties.put("z.i", "0x12345678, -1, 255");
		properties.put("z.l", "0x123456789abcdef0, -1, 255");
		properties.put("z.d", "123.4, -0.1, 1e3");
		properties.put("a", "A");
		properties.put("a.b", "AB");
		properties.put("a.abc", "A,ABC");
		properties.put("a.b.c", "3");
		properties.put("a.b.c.d", "4");
		properties.put("a.y", "true");
		properties.put("a.n", "false");
		properties.put("a.l", "0xfedcba987654321");
		properties.put("m.n.0.a", "mn0a");
		properties.put("m.n.0.b", "mn0b");
		properties.put("m.n.0.b.c", "mn0bc");
		properties.put("m.n.0.b.c.d", "mn0bcd");
		properties.put("m.n.0.b.d", "mn0bd");
		properties.put("m.n.1", "mn1");
		properties.put("m.n.2.a", "mn2a");
		properties.put("n", "");
		properties.put("n.n", " ");
		properties.put("3.1", "31");
		properties.put("7.2", "72");
		properties.put("7.2.b", "true,false");
		properties.put("7.2.i", "7,2");
		properties.put("7.2.f", "7.2, 0.1");
	}

	@Test
	public void shouldProvideRef() {
		var ref = new TypedProperties.Ref(TypedProperties.from(properties)) {};
		Assert.equal(ref.parse("3.1").toInt(), 31);
	}

	@Test
	public void shouldLoadFromClass() throws IOException {
		// loads typed-properties.properties
		Assert.equal(TypedProperties.load(TypedProperties.class).parse("abc").toInt(), 123);
	}

	@Test
	public void shouldMerge() throws IOException {
		var tp = TypedProperties.merge(
			TypedProperties.load(getClass(), "property-test-a-b-c.properties"),
			TypedProperties.load(getClass(), "property-test-d-e-f.properties"));
		Assert.equal(tp.get("name"), "property-test-d-e-f");
		Assert.equal(tp.get("a.b.c"), "true");
		Assert.equal(tp.get("d.e.f"), "true");
	}

	@Test
	public void shouldCreateFromResourceBundle() {
		var r = ResourceBundle.getBundle(PropertySource.class.getName(), Locale.ENGLISH);
		TypedProperties tp = TypedProperties.from(r);
		Assert.equal(tp.get("name"), "PropertySource");
	}

	@Test
	public void shouldCreateFromFile() throws IOException {
		try (var files = FileTestHelper.builder().file("a/b/c", "ABC").build()) {
			TypedProperties tp = TypedProperties.from(files.root, "a");
			Assert.equal(tp.get("b/c"), "ABC");
		}
	}

	@Test
	public void shouldNotReturnBlankValues() {
		var p = TypedProperties.from(properties);
		Assert.equal(p.children().contains("n"), true);
		Assert.equal(p.get("n"), null);
		Assert.equal(p.get("n.n"), null);
	}

	@Test
	public void shouldSetValues() {
		var p = new Properties();
		p.setProperty("a.b.c", "123");
		var tp = TypedProperties.from(p, "a");
		tp.set(456, "b", "c");
		Assert.equal(p.getProperty("a.b.c"), "456");
	}

	@Test
	public void shouldRemoveValues() {
		var p = new Properties();
		p.setProperty("a.b.c", "123");
		var tp = TypedProperties.from(p, "a");
		tp.set(null, "b", "c");
		Assert.no(p.containsKey("a.b.d"));
	}

	@Test
	public void shouldReturnDescendants() {
		var tp = TypedProperties.from(properties, "m.n.0");
		Assert.unordered(tp.descendants(), "a", "b", "b.c", "b.c.d", "b.d");
		Assert.unordered(tp.descendants("b"), "c", "c.d", "d");
	}

	@Test
	public void shouldReturnChildren() {
		var tp = TypedProperties.from(properties);
		Assert.unordered(tp.children(), "xyz", "x", "y", "z", "a", "m", "n", "3", "7");
		Assert.unordered(tp.children("m.n.0"), "a", "b");
		tp = TypedProperties.from(properties, "m.n.0");
		Assert.unordered(tp.children(), "a", "b");
		Assert.unordered(tp.children("b"), "c", "d");
	}

	@Test
	public void shouldReturnChildIds() {
		var tp = TypedProperties.from(properties);
		Assert.ordered(tp.childIds("m.n"), 0, 1, 2);
		Assert.ordered(tp.childIds("m"));
		Assert.ordered(tp.childIds(""), 3, 7);
		Assert.ordered(tp.childIds(), 3, 7);
		tp = TypedProperties.from(properties, "m");
		Assert.ordered(tp.childIds("n"), 0, 1, 2);
		tp = TypedProperties.from(properties, "m.n");
		Assert.ordered(tp.childIds(""), 0, 1, 2);
	}

	@Test
	public void shouldDetermineIfKeyExists() {
		var tp = TypedProperties.from(properties, "a");
		Assert.equal(tp.hasKey(), true);
		Assert.equal(tp.hasKey(""), true);
		Assert.equal(tp.hasKey("b"), true);
		Assert.equal(tp.hasKey("b.c"), true);
		Assert.equal(tp.hasKey("c"), false);
		Assert.equal(tp.hasKey("b.d"), false);
	}

	@Test
	public void shouldExtendPrefixWhenCreatingSubs() {
		var tp0 = TypedProperties.from(properties);
		var tp1 = tp0.sub();
		Assert.equal(tp1.get("a"), "A");
		TypedProperties tp2 = tp1.sub("a");
		Assert.equal(tp2.get("b"), "AB");
		TypedProperties tp3 = tp2.sub("b", "c");
		Assert.equal(tp3.get("d"), "4");
	}

	@Test
	public void shouldAllowNullPrefix() {
		var tp = TypedProperties.from(properties, new String[] { null });
		Assert.equal(tp.get("a"), "A");
		tp = TypedProperties.from(properties, null, null);
		Assert.equal(tp.get("a"), "A");
		tp = TypedProperties.from(properties, "a", null);
		Assert.equal(tp.get("b"), "AB");
		tp = TypedProperties.from(properties, null, "a");
		Assert.equal(tp.get("b"), "AB");
		tp = TypedProperties.from(properties, (String[]) null);
		Assert.equal(tp.get("a"), "A");
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var tp = TypedProperties.from(properties, "a", "b", "c");
		Assert.string(tp, "Properties[a.b.c]");
	}
}
