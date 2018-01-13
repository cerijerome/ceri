package ceri.common.text;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import java.util.regex.Matcher;
import org.junit.Test;

public class RegexSequencerBehavior {

	@Test
	public void shouldStartNewMatchFromEndOfLastMatch() {
		RegexSequencer seq = RegexSequencer.of("abc123de45f6");
		Matcher m = seq.matcher("[a-z]+");
		assertTrue(m.find());
		assertThat(m.group(), is("abc"));
		assertTrue(m.find());
		assertThat(m.group(), is("de"));
		m = seq.matcher("[0-9]+");
		assertTrue(m.find());
		assertThat(m.group(), is("45"));
	}

	@Test
	public void shouldIgnorePreviousUnmatchedMatchers() {
		RegexSequencer seq = RegexSequencer.of("abc123de45f6");
		Matcher m = seq.matcher("[a-z]+");
		m = seq.matcher("[a-z]+");
		assertTrue(m.find());
		assertThat(m.group(), is("abc"));
	}

}
