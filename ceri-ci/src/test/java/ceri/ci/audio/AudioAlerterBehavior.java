package ceri.ci.audio;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ceri.ci.build.Builds;
import ceri.ci.build.Event;

public class AudioAlerterBehavior {
	@Mock
	private AudioMessages message;
	private AudioAlerter audio;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);
		audio = new AudioAlerter(message);
	}

	@After
	public void end() {
		audio.close();
	}

	@Test
	public void shouldPlayReminderAudioForAnyBrokenJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").events(Event.failure());
		builds.build("b1").job("j0").events();
		audio.update(builds);
		reset(message);
		audio.remind();
		verify(message).playStillBroken("b0", "j0", names("n0", "n1"));
		verify(message).playStillBroken("b0", "j1", names());
	}

	@Test
	public void shouldPlayAudioForJustBrokenJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").events(Event.failure());
		builds.build("b1").job("j0").events();
		audio.update(builds);
		verify(message).playJustBroken("b0", "j0", names("n0", "n1"));
		verify(message).playJustBroken("b0", "j1", names());
	}

	@Test
	public void shouldPlayAudioForStillBrokenJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").events(Event.failure());
		audio.update(builds);
		reset(message);
		builds = new Builds();
		builds.build("b0").job("j0").events(Event.failure());
		builds.build("b0").job("j1").events(Event.failure("n2"));
		audio.update(builds);
		verify(message).playStillBroken("b0", "j0", names());
		verify(message).playStillBroken("b0", "j1", names("n2"));
	}

	@Test
	public void shouldPlayAudioForJustFixedJobs() throws IOException {
		Builds builds = new Builds();
		builds.build("b0").job("j0").events(Event.failure("n0", "n1"));
		builds.build("b0").job("j1").events(Event.failure());
		audio.update(builds);
		reset(message);
		builds = new Builds();
		builds.build("b0").job("j0").events(Event.success());
		builds.build("b0").job("j1").events(Event.success("n2"));
		audio.update(builds);
		verify(message).playJustFixed("b0", "j0", names());
		verify(message).playJustFixed("b0", "j1", names("n2"));
	}

	private Collection<String> names(String... names) {
		return new HashSet<>(Arrays.asList(names));
	}
}
