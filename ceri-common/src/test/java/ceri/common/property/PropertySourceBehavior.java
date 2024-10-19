package ceri.common.property;

import static ceri.common.test.AssertUtil.assertCollection;
import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.Test;
import ceri.common.test.FileTestHelper;
import ceri.common.util.CloseableUtil;

public class PropertySourceBehavior {
	private final ResourceBundle r =
		ResourceBundle.getBundle(PropertySource.class.getName(), Locale.ENGLISH);
	private Properties p = null;
	private FileTestHelper files = null;
	private PropertySource source = null;

	@After
	public void after() {
		CloseableUtil.close(files);
		files = null;
	}

	@Test
	public void shouldRemoveProperties() {
		initProperties(Map.of("a", "A", "aaa", "AAA", "a.b", "AB", "a..b", "A.B", "a.c", "AC"));
		source.property("a.b.c", null); // no change
		assertEquals(source.modified(), false);
		source.property("a.c", null);
		assertEquals(source.modified(), true);
		assertCollection(source.children(""), "a", "aaa");
		assertCollection(source.children("a"), "b");
		assertCollection(source.descendants(""), "a", "aaa", "a.b", "a..b");
		assertCollection(source.descendants("a"), "b", ".b");
		source.property("a", null);
		assertEquals(source.modified(), true);
		assertCollection(source.descendants(""), "aaa", "a.b", "a..b");
	}

	@Test
	public void shouldSetProperties() {
		initProperties(Map.of("a", "A", "a.b", "AB", "a.c", "AC"));
		source.property("a.c", "AC"); // no change
		assertEquals(source.modified(), false);
		source.property("a.b.c", "ABC");
		assertEquals(source.modified(), true);
		assertCollection(source.descendants(""), "a", "a.b", "a.c", "a.b.c");
	}

	@Test
	public void shouldAccessResourceBundleSubKeys() {
		var source = PropertySource.Resource.of(r);
		assertCollection(source.children(""), "a", "aaa", "name", "locale");
		assertCollection(source.children("a.b"), "c", "d");
		assertCollection(source.descendants(""), "a", "aaa", "a.b", "a.b.c", "a.b.d", "name",
			"locale");
		assertCollection(source.descendants("a"), "b", "b.c", "b.d");
	}

	@Test
	public void shouldAccessResourceBundleValues() {
		var source = PropertySource.Resource.of(r);
		assertEquals(source.property("name"), "PropertySource");
		assertEquals(source.property("locale"), "en");
		assertEquals(source.property("a.b"), "ABX");
	}

	@Test
	public void shouldFailToWriteToResourceBundles() {
		var source = PropertySource.Resource.of(r);
		source.property("bbb", null); // no change
		source.property("aaa", "AAA"); // no change
		assertEquals(source.modified(), false);
		assertUnsupported(() -> source.property("aaa", "AAAA"));
	}

	@Test
	public void shouldAccessFileSubKeys() throws IOException {
		initFiles(Map.of("a/b/c", "ABC", "aaa", "AAA", "a/b/d", "ABD", "a/c", "AC"));
		assertCollection(source.children(""), "a", "aaa");
		assertCollection(source.children("a"), "b", "c");
		assertCollection(source.descendants(""), "a/b/c", "aaa", "a/b/d", "a/c");
		assertCollection(source.descendants("a"), "b/c", "b/d", "c");
	}

	@Test
	public void shouldGetFilesAsProperties() throws IOException {
		initFiles(Map.of("a/b/c", "ABC", "aaa", "AAA"));
		assertEquals(source.property("a/b/c"), "ABC");
		assertEquals(source.property("aaa"), "AAA");
		assertEquals(source.property("a/b"), null);
		assertEquals(source.property("a/b/c/d"), null);
	}

	@Test
	public void shouldSetFilesAsProperties() throws IOException {
		initFiles(Map.of("a/b/c", "ABC"));
		source.property("a/b/c/d", null);
		assertEquals(source.property("a/b/c/d"), null);
		assertUnsupported(() -> source.property("a/b/c", null)); // unable to delete
		assertEquals(source.modified(), false);
		source.property("a/b/c", "abc");
		assertEquals(source.property("a/b/c"), "abc");
		assertEquals(source.modified(), true);
	}

	@Test
	public void shouldAllowRelativePaths() throws IOException {
		initFiles(Map.of("a/b/c/d", "ABC", "a/c/d", "ACD", "aaa", "AAA"));
		assertEquals(source.property("a/b/c/../../c/d"), "ACD");
		assertEquals(source.property("a/../aaa"), "AAA");
		assertIllegalArg(() -> source.property("a/../../aaa")); // cannot go beyond root
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		assertFind(PropertySource.Properties.of(new Properties()), "Properties");
		assertFind(PropertySource.Resource.of(r), "Resource\\(.*PropertySource\\)");
		initFiles(Map.of());
		assertFind(source, "File\\(.+\\)");
	}

	@Test
	public void shouldProvideNullImplementation() {
		assertCollection(PropertySource.NULL.children("a.b.c"));
		assertCollection(PropertySource.NULL.descendants("a.b.c"));
		PropertySource.NULL.property("a.b.c", "ABC");
		assertEquals(PropertySource.NULL.property("a.b.c"), null);
		assertEquals(PropertySource.NULL.modified(), false);
	}

	private void initProperties(Map<String, String> properties) {
		p = new Properties();
		p.putAll(properties);
		source = PropertySource.Properties.of(p);
	}

	private void initFiles(Map<String, String> files) throws IOException {
		var b = FileTestHelper.builder();
		files.forEach(b::file);
		this.files = b.build();
		source = PropertySource.File.of(this.files.root.toString());
	}
}
