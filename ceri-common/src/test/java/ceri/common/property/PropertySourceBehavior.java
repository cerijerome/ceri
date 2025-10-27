package ceri.common.property;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import org.junit.After;
import org.junit.Test;
import ceri.common.function.Closeables;
import ceri.common.io.IoExceptions;
import ceri.common.test.Assert;
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
		Assert.equal(source.modified(), false);
		source.property("a.c", null);
		Assert.equal(source.modified(), true);
		Assert.unordered(source.children(""), "a", "aaa");
		Assert.unordered(source.children("a"), "b");
		Assert.unordered(source.descendants(""), "a", "aaa", "a.b", "a..b");
		Assert.unordered(source.descendants("a"), "b", ".b");
		source.property("a", null);
		Assert.equal(source.modified(), true);
		Assert.unordered(source.descendants(""), "aaa", "a.b", "a..b");
	}

	@Test
	public void shouldSetProperties() {
		initProperties(Map.of("a", "A", "a.b", "AB", "a.c", "AC"));
		source.property("a.c", "AC"); // no change
		Assert.equal(source.modified(), false);
		source.property("a.b.c", "ABC");
		Assert.equal(source.modified(), true);
		Assert.unordered(source.descendants(""), "a", "a.b", "a.c", "a.b.c");
	}

	@Test
	public void shouldDetermineIfKeyExists() {
		initProperties(Map.of("a.b.c", "A-B-C", "a.cd", "A-CD"));
		Assert.equal(source.hasKey("a.b.c"), true);
		Assert.equal(source.hasKey("a.b"), true);
		Assert.equal(source.hasKey("a"), true);
		Assert.equal(source.hasKey("a.c"), false);
	}

	@Test
	public void shouldAccessResourceBundleSubKeys() {
		var source = PropertySource.Resource.of(r);
		Assert.unordered(source.children(""), "a", "aaa", "name", "locale");
		Assert.unordered(source.children("a.b"), "c", "d");
		Assert.unordered(source.descendants(""), "a", "aaa", "a.b", "a.b.c", "a.b.d", "name",
			"locale");
		Assert.unordered(source.descendants("a"), "b", "b.c", "b.d");
	}

	@Test
	public void shouldAccessResourceBundleValues() {
		var source = PropertySource.Resource.of(r);
		Assert.equal(source.property("name"), "PropertySource");
		Assert.equal(source.property("locale"), "en");
		Assert.equal(source.property("a.b"), "ABX");
		Assert.equal(source.hasKey("a.b.c"), true);
		Assert.equal(source.hasKey("a.b.d"), true);
		Assert.equal(source.hasKey("a.b"), true);
		Assert.equal(source.hasKey("a"), true);
		Assert.equal(source.hasKey("aa"), false);
	}

	@Test
	public void shouldFailToWriteToResourceBundles() {
		var source = PropertySource.Resource.of(r);
		source.property("bbb", null); // no change
		source.property("aaa", "AAA"); // no change
		Assert.equal(source.modified(), false);
		Assert.unsupportedOp(() -> source.property("aaa", "AAAA"));
	}

	@Test
	public void shouldAccessFileSubKeys() throws IOException {
		initFiles(Map.of("a/b/c", "ABC", "aaa", "AAA", "a/b/d", "ABD", "a/c", "AC"));
		Assert.unordered(source.children(""), "a", "aaa");
		Assert.unordered(source.children("a"), "b", "c");
		Assert.unordered(source.descendants(""), "a/b/c", "aaa", "a/b/d", "a/c");
		Assert.unordered(source.descendants("a"), "b/c", "b/d", "c");
		Assert.equal(source.hasKey("a/b/c"), true);
		Assert.equal(source.hasKey("a/b"), true);
		Assert.equal(source.hasKey("a"), true);
		Assert.equal(source.hasKey("aa"), false);
	}

	@Test
	public void shouldGetFilesAsProperties() throws IOException {
		initFiles(Map.of("a/b/c", "ABC", "aaa", "AAA"));
		Assert.equal(source.property("a/b/c"), "ABC");
		Assert.equal(source.property("aaa"), "AAA");
		Assert.equal(source.property("a/b"), null);
		Assert.equal(source.property("a/b/c/d"), null);
	}

	@Test
	public void shouldSetFilesAsProperties() throws IOException {
		initFiles(Map.of("a/b/c", "ABC"));
		source.property("a/b/c/d", null);
		Assert.equal(source.property("a/b/c/d"), null);
		Assert.unsupportedOp(() -> source.property("a/b/c", null)); // unable to delete
		Assert.equal(source.modified(), false);
		source.property("a/b/c", "abc");
		Assert.equal(source.property("a/b/c"), "abc");
		Assert.equal(source.modified(), true);
	}

	@Test
	public void shouldAllowRelativePaths() throws IOException {
		initFiles(Map.of("a/b/c/d", "ABC", "a/c/d", "ACD", "aaa", "AAA"));
		Assert.equal(source.property("a/b/c/../../c/d"), "ACD");
		Assert.equal(source.property("a/../aaa"), "AAA");
		Assert.illegalArg(() -> source.property("a/../../aaa")); // cannot go beyond root
	}

	@Test
	public void shouldProvideStringRepresentation() throws IOException {
		Assert.find(PropertySource.Properties.of(new Properties()), "Properties");
		Assert.find(PropertySource.Resource.of(r), "Resource\\(.*PropertySource\\)");
		initFiles(Map.of());
		Assert.find(source, "File\\(.+\\)");
	}

	@Test
	public void shouldProvideNullImplementation() {
		Assert.unordered(PropertySource.NULL.children("a.b.c"));
		Assert.unordered(PropertySource.NULL.descendants("a.b.c"));
		PropertySource.NULL.property("a.b.c", "ABC");
		Assert.equal(PropertySource.NULL.property("a.b.c"), null);
		Assert.equal(PropertySource.NULL.hasKey("a.b.c"), false);
		Assert.equal(PropertySource.NULL.modified(), false);
	}

	@Test
	public void shouldProvideContextForFileReadFailure() throws IOException {
		initFiles(Map.of("a/b/c", "ABC"));
		Assert.equal(PropertySource.fileRead(files.path("a/b/c")), "ABC");
		Assert.thrown(IoExceptions.Runtime.class, () -> PropertySource.fileRead(files.path("a/b")));
	}

	@Test
	public void shouldProvideContextForFileFailure() throws IOException {
		initFiles(Map.of("a/b/c", "ABC"));
		PropertySource.fileWrite(files.path("a/b/c"), "AAA");
		Assert.equal(PropertySource.fileRead(files.path("a/b/c")), "AAA");
		Assert.thrown(IoExceptions.Runtime.class,
			() -> PropertySource.fileWrite(files.path("a/b"), "AA"));
		Assert.thrown(IoExceptions.Runtime.class, () -> PropertySource.fileRead(files.path("a/b")));
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
