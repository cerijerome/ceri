package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeThat;
import java.util.Locale;
import org.junit.Test;

public class LocaleUtilTest {

	@Test
	public void testFromString() {
		Locale locale = new Locale("en", "US", "Test");
		assumeThat("en_US_Test", is(locale.toString()));
		Locale localeFromString = LocaleUtil.fromString("en_US_Test");
		assertThat(locale, is(localeFromString));
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
