package ceri.common.util;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.io.PrintStream;
import java.util.Collections;
import org.junit.Test;
import ceri.common.util.StringUtil.Align;

public class StringUtilTest {

	@Test
	public void testToString() {
		String toString = StringUtil.toString("{", "}", "|", "Test1", "Test2", "Test3");
		assertThat(toString, is("{Test1|Test2|Test3}"));
		toString = StringUtil.toString("{", "}", "|", "Test");
		assertThat(toString, is("{Test}"));
		toString = StringUtil.toString("{", "}", "|", Collections.singleton("Test"));
		assertThat(toString, is("{Test}"));
	}

	@Test
	public void testPaddingNumbers() {
		String padded = StringUtil.pad(Long.MAX_VALUE, 0);
		assertThat(padded, is("9223372036854775807"));
		padded = StringUtil.pad(Long.MAX_VALUE, 20);
		assertThat(padded, is("09223372036854775807"));
		padded = StringUtil.pad(Long.MIN_VALUE, 20);
		assertThat(padded, is("-9223372036854775808"));
		padded = StringUtil.pad(Long.MIN_VALUE, 25);
		assertThat(padded, is("-000009223372036854775808"));
	}

	@Test
	public void testPaddingString() {
		String padded = StringUtil.pad("", 0);
		assertThat(padded, is(""));
		padded = StringUtil.pad("\uffff", 2);
		assertThat(padded, is(" \uffff"));
		padded = StringUtil.pad("\u1fffhello\u2fff", 10, "\u3fff", Align.LEFT);
		assertThat(padded, is("\u1fffhello\u2fff\u3fff\u3fff\u3fff"));
	}

	@Test
	public void testSafeSubstring() {
		String sub = StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", 0);
		assertThat(sub, is("\u3fff\u3ffe\u3ffd"));
		sub = StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", 2);
		assertThat(sub, is("\u3ffd"));
		sub = StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", 4);
		assertThat(sub, is(""));
		sub = StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", -3, -1);
		assertThat(sub, is("\u3fff\u3ffe\u3ffd"));
		sub = StringUtil.safeSubstring("\u3fff\u3ffe\u3ffd", -4, 5);
		assertThat(sub, is("\u3fff\u3ffe\u3ffd"));
	}

	@Test
	public void testAsPrintStream() {
		StringBuilder b = new StringBuilder();
		try (PrintStream p = StringUtil.asPrintStream(b)) {
			p.println("Testing1");
			p.println("\0\b\t\f\'\"");
			p.close();
			assertThat(b.toString(), is("Testing1\n\0\b\t\f\'\"\n"));
		}
	}
	
}
