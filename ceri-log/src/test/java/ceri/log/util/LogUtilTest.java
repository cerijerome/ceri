package ceri.log.util;

import static ceri.common.test.TestUtil.assertException;
import static ceri.common.test.TestUtil.assertPrivateConstructor;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import ceri.common.text.ToStringHelper;

public class LogUtilTest {

	static class TestClass {
		@Override
		public String toString() {
			return ToStringHelper.createByClass(this).children("A", "B", "C").toString();
		}
	}

	@Test
	public void testPrivateConstructor() {
		assertPrivateConstructor(LogUtil.class);
	}

	@Test
	public void testToHex() {
		assertThat(LogUtil.toHex(127).toString(), is("7f"));
		assertThat(LogUtil.toHex(-1).toString(), is("ffffffff"));
	}

	@Test
	public void testToString() {
		assertThat(LogUtil.toString(() -> "aaa").toString(), is("aaa"));
		Object obj = LogUtil.toString(() -> {
			throw new RuntimeException();
		});
		assertException(() -> obj.toString());
		Object obj2 = LogUtil.toString(() -> {
			throw new Exception();
		});
		assertException(() -> obj2.toString());
	}

	@Test
	public void testCompact() {
		TestClass test = new TestClass();
		assertTrue("Test class broken", test.toString().matches("(?s).*[\\r\\n\\t].*"));
		assertTrue("Test class broken", test.toString().matches("(?s).*\\s{2,}.*"));
		Object obj = LogUtil.compact(test);
		assertFalse("Contains non-space whitespace", obj.toString().matches("(?s).*[\\r\\n\\t].*"));
		assertFalse("Contains a whitespace sequence", obj.toString().matches("(?s).*\\s{2,}.*"));
	}

	@Test
	public void testCompactForNull() {
		Object obj = LogUtil.compact(null);
		assertThat(obj.toString(), is("null"));
	}

}
