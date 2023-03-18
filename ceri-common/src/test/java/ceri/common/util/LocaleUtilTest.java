package ceri.common.util;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import java.util.Locale;
import org.junit.Test;

public class LocaleUtilTest {

	@Test
	public void testConstructorIsPrivate() {
		assertPrivateConstructor(LocaleUtil.class);
	}

	@Test
	public void testFromString() {
		Locale locale = Locale.of("en", "US", "Test");
		assumeThat("en_US_Test", is(locale.toString()));
		Locale localeFromString = LocaleUtil.fromString("en_US_Test");
		assertEquals(locale, localeFromString);
		assertEquals(LocaleUtil.fromString(""), Locale.ROOT);
		assertEquals(LocaleUtil.fromString("a"), Locale.of("a"));
		assertEquals(LocaleUtil.fromString("a_"), Locale.of("a"));
		assertEquals(LocaleUtil.fromString("a_b"), Locale.of("a", "b"));
		assertEquals(LocaleUtil.fromString("a_b_"), Locale.of("a", "b"));
		assertEquals(LocaleUtil.fromString("a_b_c"), Locale.of("a", "b", "c"));
		assertEquals(LocaleUtil.fromString("a_b_c_"), Locale.of("a", "b", "c_"));
	}

	@Test
	public void testParentOf() {
		Locale locale = Locale.of("en", "US", "Test");
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, Locale.of("en", "US"));
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, Locale.of("en"));
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, Locale.ROOT);
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, Locale.ROOT);
	}

}
