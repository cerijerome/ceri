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
		Locale locale = new Locale("en", "US", "Test");
		assumeThat("en_US_Test", is(locale.toString()));
		Locale localeFromString = LocaleUtil.fromString("en_US_Test");
		assertEquals(locale, localeFromString);
		assertEquals(LocaleUtil.fromString(""), new Locale(""));
		assertEquals(LocaleUtil.fromString("a"), new Locale("a"));
		assertEquals(LocaleUtil.fromString("a_"), new Locale("a"));
		assertEquals(LocaleUtil.fromString("a_b"), new Locale("a", "b"));
		assertEquals(LocaleUtil.fromString("a_b_"), new Locale("a", "b"));
		assertEquals(LocaleUtil.fromString("a_b_c"), new Locale("a", "b", "c"));
		assertEquals(LocaleUtil.fromString("a_b_c_"), new Locale("a", "b", "c_"));
	}

	@Test
	public void testParentOf() {
		Locale locale = new Locale("en", "US", "Test");
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, new Locale("en", "US"));
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, new Locale("en"));
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, new Locale(""));
		locale = LocaleUtil.parentOf(locale);
		assertEquals(locale, new Locale(""));
	}

}
