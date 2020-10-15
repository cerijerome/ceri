package ceri.common.util;

import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static ceri.common.test.TestUtil.assertThat;
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
		assertThat(locale, is(localeFromString));
		assertThat(LocaleUtil.fromString(""), is(new Locale("")));
		assertThat(LocaleUtil.fromString("a"), is(new Locale("a")));
		assertThat(LocaleUtil.fromString("a_"), is(new Locale("a")));
		assertThat(LocaleUtil.fromString("a_b"), is(new Locale("a", "b")));
		assertThat(LocaleUtil.fromString("a_b_"), is(new Locale("a", "b")));
		assertThat(LocaleUtil.fromString("a_b_c"), is(new Locale("a", "b", "c")));
		assertThat(LocaleUtil.fromString("a_b_c_"), is(new Locale("a", "b", "c_")));
	}

	@Test
	public void testParentOf() {
		Locale locale = new Locale("en", "US", "Test");
		locale = LocaleUtil.parentOf(locale);
		assertThat(locale, is(new Locale("en", "US")));
		locale = LocaleUtil.parentOf(locale);
		assertThat(locale, is(new Locale("en")));
		locale = LocaleUtil.parentOf(locale);
		assertThat(locale, is(new Locale("")));
		locale = LocaleUtil.parentOf(locale);
		assertThat(locale, is(new Locale("")));
	}

}
