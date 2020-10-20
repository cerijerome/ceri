package ceri.common.text;

import static ceri.common.test.AssertUtil.assertEquals;
import static ceri.common.test.AssertUtil.assertTrue;
import java.util.regex.Matcher;
import org.junit.Test;

public class RegexSequencerBehavior {

	@Test
	public void shouldStartNewMatchFromEndOfLastMatch() {
		RegexSequencer seq = RegexSequencer.of("abc123de45f6");
		Matcher m = seq.matcher("[a-z]+");
		assertTrue(m.find());
		assertEquals(m.group(), "abc");
		assertTrue(m.find());
		assertEquals(m.group(), "de");
		m = seq.matcher("[0-9]+");
		assertTrue(m.find());
		assertEquals(m.group(), "45");
	}

	@Test
	public void shouldIgnorePreviousUnmatchedMatchers() {
		RegexSequencer seq = RegexSequencer.of("abc123de45f6");
		Matcher m = seq.matcher("[a-z]+");
		m = seq.matcher("[a-z]+");
		assertTrue(m.find());
		assertEquals(m.group(), "abc");
	}

}
