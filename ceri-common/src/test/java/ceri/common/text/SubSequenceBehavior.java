package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertString;
import static ceri.common.test.AssertUtil.assertTrue;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Test;
import ceri.common.util.CloseableUtil;

public class SubSequenceBehavior {
	private StringBuilder b = null;
	private PrintStream p = null;

	@After
	public void after() {
		CloseableUtil.close(p);
		p = null;
		b = null;
	}

	@Test
	public void shouldDetermineIfEmpty() {
		assertEquals(new SubSequence(null, 0, 0).isEmpty(), true);
		assertEquals(new SubSequence("test", 3, 3).isEmpty(), true);
		assertEquals(new SubSequence("test", 3, 2).isEmpty(), true);
		assertEquals(new SubSequence("test", -1, 1).isEmpty(), true);
		assertEquals(new SubSequence("test", 2, 5).isEmpty(), true);
	}

	@Test
	public void shouldAppend() throws IOException {
		init();
		assertTrue(new SubSequence("test", 0, 2).appendTo(p));
		assertTrue(new SubSequence("test", 2, 3).append(b));
		assertTrue(new SubSequence(new StringBuilder("test"), 3, 4).append(b));
		assertString(b, "test");
	}

	@Test
	public void shouldIgnoreEmptyString() throws IOException {
		init();
		assertEquals(new SubSequence(null, 0, 0).append(b), false);
		assertEquals(new SubSequence(null, 0, 0).appendTo(p), false);
		assertEquals(new SubSequence("", 0, 0).append(b), false);
		assertEquals(new SubSequence("", 0, 0).appendTo(p), false);
	}

	@Test
	public void shouldGetSubSequence() {
		assertEquals(new SubSequence(null, 0, 0).get(), "");
		assertEquals(new SubSequence("abc", -1, 0).get(), "");
		assertEquals(new SubSequence("abc", 1, 4).get(), "");
		assertEquals(new SubSequence("abc", 3, 2).get(), "");
		assertEquals(new SubSequence("abc", 1, 3).get(), "bc");
	}

	@Test
	public void shouldGetSubstring() {
		assertEquals(new SubSequence(null, 0, 0).string(), "");
		assertEquals(new SubSequence("abc", -1, 0).string(), "");
		assertEquals(new SubSequence("abc", 1, 4).string(), "");
		assertEquals(new SubSequence("abc", 3, 2).string(), "");
		assertEquals(new SubSequence("abc", 1, 3).string(), "bc");
	}

	private void init() {
		b = new StringBuilder();
		p = StringUtil.asPrintStream(b);
	}
}
