package ceri.common.property;

import static ceri.common.property.PropertyUtil.load;
import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFalse;
import static ceri.common.test.AssertUtil.assertIterable;
import static ceri.common.test.AssertUtil.assertString;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.BeforeClass;
import org.junit.Test;
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
		assertEquals(ref.parse("3.1").toInt(), 31);
	}

	@Test
	public void shouldMerge() throws IOException {
		var tp = TypedProperties.merge(
			TypedProperties.from(load(getClass(), "property-test-a-b-c.properties")),
			TypedProperties.from(load(getClass(), "property-test-d-e-f.properties")));
		assertEquals(tp.get("name"), "property-test-d-e-f");
		assertEquals(tp.get("a.b.c"), "true");
		assertEquals(tp.get("d.e.f"), "true");
	}

	@Test
	public void shouldCreateFromResourceBundle() {
		var r = ResourceBundle.getBundle(PropertySource.class.getName(), Locale.ENGLISH);
		TypedProperties tp = TypedProperties.from(r);
		assertEquals(tp.get("name"), "PropertySource");
	}

	@Test
	public void shouldCreateFromFile() throws IOException {
		try (var files = FileTestHelper.builder().file("a/b/c", "ABC").build()) {
			TypedProperties tp = TypedProperties.from(files.root, "a");
			assertEquals(tp.get("b/c"), "ABC");
		}
	}

	@Test
	public void shouldNotReturnBlankValues() {
		var p = TypedProperties.from(properties);
		assertEquals(p.children().contains("n"), true);
		assertEquals(p.get("n"), null);
		assertEquals(p.get("n.n"), null);
	}

	@Test
	public void shouldSetValues() {
		var p = new Properties();
		p.setProperty("a.b.c", "123");
		var tp = TypedProperties.from(p, "a");
		tp.set(456, "b", "c");
		assertEquals(p.getProperty("a.b.c"), "456");
	}

	@Test
	public void shouldRemoveValues() {
		var p = new Properties();
		p.setProperty("a.b.c", "123");
		var tp = TypedProperties.from(p, "a");
		tp.set(null, "b", "c");
		assertFalse(p.containsKey("a.b.d"));
	}

	@Test
	public void shouldReturnDescendants() {
		var tp = TypedProperties.from(properties, "m.n.0");
		assertCollection(tp.descendants(), "a", "b", "b.c", "b.c.d", "b.d");
		assertCollection(tp.descendants("b"), "c", "c.d", "d");
	}

	@Test
	public void shouldReturnChildren() {
		var tp = TypedProperties.from(properties);
		assertCollection(tp.children(), "xyz", "x", "y", "z", "a", "m", "n", "3", "7");
		assertCollection(tp.children("m.n.0"), "a", "b");
		tp = TypedProperties.from(properties, "m.n.0");
		assertCollection(tp.children(), "a", "b");
		assertCollection(tp.children("b"), "c", "d");
	}

	@Test
	public void shouldReturnChildIds() {
		var tp = TypedProperties.from(properties);
		assertIterable(tp.childIds("m.n"), 0, 1, 2);
		assertIterable(tp.childIds("m"));
		assertIterable(tp.childIds(""), 3, 7);
		assertIterable(tp.childIds(), 3, 7);
		tp = TypedProperties.from(properties, "m");
		assertIterable(tp.childIds("n"), 0, 1, 2);
		tp = TypedProperties.from(properties, "m.n");
		assertIterable(tp.childIds(""), 0, 1, 2);
	}

	@Test
	public void shouldDetermineIfKeyExists() {
		var tp = TypedProperties.from(properties, "a");
		assertEquals(tp.hasKey(), true);
		assertEquals(tp.hasKey(""), true);
		assertEquals(tp.hasKey("b"), true);
		assertEquals(tp.hasKey("b.c"), true);
		assertEquals(tp.hasKey("c"), false);
		assertEquals(tp.hasKey("b.d"), false);
	}

	@Test
	public void shouldExtendPrefixWhenCreatingSubs() {
		var tp0 = TypedProperties.from(properties);
		var tp1 = tp0.sub();
		assertEquals(tp1.get("a"), "A");
		TypedProperties tp2 = tp1.sub("a");
		assertEquals(tp2.get("b"), "AB");
		TypedProperties tp3 = tp2.sub("b", "c");
		assertEquals(tp3.get("d"), "4");
	}

	@Test
	public void shouldAllowNullPrefix() {
		var tp = TypedProperties.from(properties, new String[] { null });
		assertEquals(tp.get("a"), "A");
		tp = TypedProperties.from(properties, null, null);
		assertEquals(tp.get("a"), "A");
		tp = TypedProperties.from(properties, "a", null);
		assertEquals(tp.get("b"), "AB");
		tp = TypedProperties.from(properties, null, "a");
		assertEquals(tp.get("b"), "AB");
		tp = TypedProperties.from(properties, (String[]) null);
		assertEquals(tp.get("a"), "A");
	}

	@Test
	public void shouldProvideStringRepresentation() {
		var tp = TypedProperties.from(properties, "a", "b", "c");
		assertString(tp, "Properties[a.b.c]");
	}

}
