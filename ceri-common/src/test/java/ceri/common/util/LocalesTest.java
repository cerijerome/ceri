package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static ceri.common.test.AssertUtil.assertString;
import java.util.Locale;
import org.junit.Test;

public class LocalesTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(Locales.class);
	}

	@Test
	public void testFromString() {
		var locale = Locale.of("en", "US", "Test");
		assertString(locale, "en_US_Test");
		assertEquals(Locales.from("en_US_Test"), locale);
		assertEquals(Locales.from(""), Locale.ROOT);
		assertEquals(Locales.from("a"), Locale.of("a"));
		assertEquals(Locales.from("a_"), Locale.of("a"));
		assertEquals(Locales.from("a_b"), Locale.of("a", "b"));
		assertEquals(Locales.from("a_b_"), Locale.of("a", "b"));
		assertEquals(Locales.from("a_b_c"), Locale.of("a", "b", "c"));
		assertEquals(Locales.from("a_b_c_"), Locale.of("a", "b", "c_"));
	}

	@Test
	public void testParentOf() {
		var locale = Locale.of("en", "US", "Test");
		locale = Locales.parentOf(locale);
		assertEquals(locale, Locale.of("en", "US"));
		locale = Locales.parentOf(locale);
		assertEquals(locale, Locale.of("en"));
		locale = Locales.parentOf(locale);
		assertEquals(locale, Locale.ROOT);
		locale = Locales.parentOf(locale);
		assertEquals(locale, Locale.ROOT);
	}
}
