package ceri.parser.token;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;

public class TokenUtilTest {

	@Test
	public void testMatcher() {
		Pattern pattern = Pattern.compile("a([bcd]+)e");
		Index i = new Index();
		i.set(4);
		Matcher m = TokenUtil.matcher(pattern, "123adbef", i);
		assertNull(m);
		assertThat(i.value(), is(4));
		i.set(3);
		m = TokenUtil.matcher(pattern, "123adbef", i);
		assertThat(m.group(1), is("db"));
		assertThat(i.value(), is(7));
	}

	@Test
	public void testMatches() {
		Pattern pattern = Pattern.compile("a([bcd]+)e");
		Index i = new Index();
		i.set(4);
		assertFalse(TokenUtil.matches(pattern, "123adbef", i));
		assertThat(i.value(), is(4));
		i.set(3);
		assertTrue(TokenUtil.matches(pattern, "123adbef", i));
		assertThat(i.value(), is(7));
	}

}
