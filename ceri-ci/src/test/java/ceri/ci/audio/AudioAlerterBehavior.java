package ceri.ci.audio;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class AudioAlerterBehavior {
	private AudioMessage message;
	private AudioAlerter audio;

	@Before
	public void init() {
		message = Mockito.mock(AudioMessage.class);
		audio = new AudioAlerter(message);
	}

	@Test
	public void shouldPlayReminderAudioForAnyBrokenJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").event(Event.failure());
		builds.build("b1").job("j0").event();
		audio.update(builds);
		reset(message);
		audio.remind();
		verify(message).playStillBroken("b0", "j0", names("n0", "n1"));
		verify(message).playStillBroken("b0", "j1", names());
		verifyNoMoreInteractions(message);
	}

	@Test
	public void shouldPlayAudioForJustBrokenJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").event(Event.failure());
		builds.build("b1").job("j0").event();
		audio.update(builds);
		verify(message).playRandomAlarm();
		verify(message).playJustBroken("b0", "j0", names("n0", "n1"));
		verify(message).playJustBroken("b0", "j1", names());
		verifyNoMoreInteractions(message);
	}

	@Test
	public void shouldPlayAudioForStillBrokenJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").event(Event.failure());
		audio.update(builds);
		reset(message);
		builds = new Builds();
		builds.build("b0").job("j0").event(Event.failure());
		builds.build("b0").job("j1").event(Event.failure("n2"));
		audio.update(builds);
		verify(message).playStillBroken("b0", "j0", names());
		verify(message).playStillBroken("b0", "j1", names("n2"));
		verifyNoMoreInteractions(message);
	}

	@Test
	public void shouldPlayAudioForJustFixedJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").event(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").event(Event.failure());
		audio.update(builds);
		reset(message);
		builds = new Builds();
		builds.build("b0").job("j0").event(Event.success());
		builds.build("b0").job("j1").event(Event.success("n2"));
		audio.update(builds);
		verify(message).playJustFixed("b0", "j0", names());
		verify(message).playJustFixed("b0", "j1", names("n2"));
		verifyNoMoreInteractions(message);
	}

	private Collection<String> names(String... names) {
		return new HashSet<>(Arrays.asList(names));
	}
}
