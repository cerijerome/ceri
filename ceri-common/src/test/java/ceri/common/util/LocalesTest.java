package ceri.common.util;

import java.util.Locale;
import org.junit.Test;
import ceri.common.test.Assert;

public class LocalesTest {

	@Test
	public void testConstructorIsPrivate() {
		Assert.privateConstructor(Locales.class);
	}

	@Test
	public void testFromString() {
		var locale = Locale.of("en", "US", "Test");
		Assert.string(locale, "en_US_Test");
		Assert.equal(Locales.from("en_US_Test"), locale);
		Assert.equal(Locales.from(""), Locale.ROOT);
		Assert.equal(Locales.from("a"), Locale.of("a"));
		Assert.equal(Locales.from("a_"), Locale.of("a"));
		Assert.equal(Locales.from("a_b"), Locale.of("a", "b"));
		Assert.equal(Locales.from("a_b_"), Locale.of("a", "b"));
		Assert.equal(Locales.from("a_b_c"), Locale.of("a", "b", "c"));
		Assert.equal(Locales.from("a_b_c_"), Locale.of("a", "b", "c_"));
	}

	@Test
	public void testParentOf() {
		var locale = Locale.of("en", "US", "Test");
		locale = Locales.parentOf(locale);
		Assert.equal(locale, Locale.of("en", "US"));
		locale = Locales.parentOf(locale);
		Assert.equal(locale, Locale.of("en"));
		locale = Locales.parentOf(locale);
		Assert.equal(locale, Locale.ROOT);
		locale = Locales.parentOf(locale);
		Assert.equal(locale, Locale.ROOT);
	}
}
