package ceri.common.property;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertFind;
import static ceri.common.test.AssertUtil.assertIllegalArg;
import static ceri.common.test.AssertUtil.assertThrown;
import static ceri.common.test.AssertUtil.assertUnordered;
import static ceri.common.test.AssertUtil.assertUnsupported;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.io.IoExceptions;
import ceri.common.test.FileTestHelper;

public class PropertySourceBehavior {
	private final ResourceBundle r =
		ResourceBundle.getBundle(PropertySource.class.getName(), Locale.ENGLISH);
	private Properties p = null;
	private FileTestHelper files = null;
	private PropertySource source = null;

	@After
	public void after() {
		Closeables.close(files);
		files = null;
	}

	@Test
	public void shouldRemoveProperties() {
		initProperties(Map.of("a", "A", "aaa", "AAA", "a.b", "AB", "a..b", "A.B", "a.c", "AC"));
		source.property("a.b.c", null); // no change
		assertEquals(source.modified(), false);
		source.property("a.c", null);
		assertEquals(source.modified(), true);
		assertUnordered(source.children(""), "a", "aaa");
		assertUnordered(source.children("a"), "b");
		assertUnordered(source.descendants(""), "a", "aaa", "a.b", "a..b");
		assertUnordered(source.descendants("a"), "b", ".b");
		source.property("a", null);
		assertEquals(source.modified(), true);
		assertUnordered(source.descendants(""), "aaa", "a.b", "a..b");
	}

	@Test
	public void shouldSetProperties() {
		initProperties(Map.of("a", "A", "a.b", "AB", "a.c", "AC"));
		source.property("a.c", "AC"); // no change
		assertEquals(source.modified(), false);
		source.property("a.b.c", "ABC");
		assertEquals(source.modified(), true);
		assertUnordered(source.descendants(""), "a", "a.b", "a.c", "a.b.c");
	}

	@Test
	public void shouldDetermineIfKeyExists() {
		initProperties(Map.of("a.b.c", "A-B-C", "a.cd", "A-CD"));
		assertEquals(source.hasKey("a.b.c"), true);
		assertEquals(source.hasKey("a.b"), true);
		assertEquals(source.hasKey("a"), true);
		assertEquals(source.hasKey("a.c"), false);
	}

	@Test
	public void shouldAccessResourceBundleSubKeys() {
		var source = PropertySource.Resource.of(r);
		assertUnordered(source.children(""), "a", "aaa", "name", "locale");
		assertUnordered(source.children("a.b"), "c", "d");
		assertUnordered(source.descendants(""), "a", "aaa", "a.b", "a.b.c", "a.b.d", "name",
			"locale");
		assertUnordered(source.descendants("a"), "b", "b.c", "b.d");
	}

	@Test
	public void shouldAccessResourceBundleValues() {
		var source = PropertySource.Resource.of(r);
		assertEquals(source.property("name"), "PropertySource");
		assertEquals(source.property("locale"), "en");
		assertEquals(source.property("a.b"), "ABX");
		assertEquals(source.hasKey("a.b.c"), true);
		assertEquals(source.hasKey("a.b.d"), true);
		assertEquals(source.hasKey("a.b"), true);
		assertEquals(source.hasKey("a"), true);
		assertEquals(source.hasKey("aa"), false);
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
		assertUnordered(source.children(""), "a", "aaa");
		assertUnordered(source.children("a"), "b", "c");
		assertUnordered(source.descendants(""), "a/b/c", "aaa", "a/b/d", "a/c");
		assertUnordered(source.descendants("a"), "b/c", "b/d", "c");
		assertEquals(source.hasKey("a/b/c"), true);
		assertEquals(source.hasKey("a/b"), true);
		assertEquals(source.hasKey("a"), true);
		assertEquals(source.hasKey("aa"), false);
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
		assertUnordered(PropertySource.NULL.children("a.b.c"));
		assertUnordered(PropertySource.NULL.descendants("a.b.c"));
		PropertySource.NULL.property("a.b.c", "ABC");
		assertEquals(PropertySource.NULL.property("a.b.c"), null);
		assertEquals(PropertySource.NULL.hasKey("a.b.c"), false);
		assertEquals(PropertySource.NULL.modified(), false);
	}

	@Test
	public void shouldProvideContextForFileReadFailure() throws IOException {
		initFiles(Map.of("a/b/c", "ABC"));
		assertEquals(PropertySource.fileRead(files.path("a/b/c")), "ABC");
		assertThrown(IoExceptions.Runtime.class, () -> PropertySource.fileRead(files.path("a/b")));
	}

	@Test
	public void shouldProvideContextForFileFailure() throws IOException {
		initFiles(Map.of("a/b/c", "ABC"));
		PropertySource.fileWrite(files.path("a/b/c"), "AAA");
		assertEquals(PropertySource.fileRead(files.path("a/b/c")), "AAA");
		assertThrown(IoExceptions.Runtime.class,
			() -> PropertySource.fileWrite(files.path("a/b"), "AA"));
		assertThrown(IoExceptions.Runtime.class, () -> PropertySource.fileRead(files.path("a/b")));
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
