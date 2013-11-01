package ceri.ci.audio;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import java.io.IOException;
import org.junit.Test;
import org.mockito.InOrder;
import ceri.ci.audio.AudioPlayer.Clip;

public class AudioAlerterBehavior {

	@Test
	public void shouldAlertMultipleTimes() throws IOException {
		AudioPlayer player = mock(AudioPlayer.class);
		try (AudioAlerter alerter = new AudioAlerter(player, 0, 1)) {
			alerter.alert("ceri");
		}
		InOrder inOrder = inOrder(player);
		inOrder.verify(player).play(Clip.alarm);
		inOrder.verify(player).play(Clip.build_broken);
		inOrder.verify(player).play("ceri");
		inOrder.verify(player).play(Clip.you_have_30min);
		inOrder.verify(player).play("ceri");
		inOrder.verify(player).play(Clip.you_have_20min);
		inOrder.verify(player).play("ceri");
		inOrder.verify(player).play(Clip.you_have_10min);
		inOrder.verify(player).play("ceri");
		inOrder.verify(player).play(Clip.out_of_time);
		verifyNoMoreInteractions(player);
	}
	
	@Test
	public void shouldClearRunningAlert() throws IOException {
		AudioPlayer player = mock(AudioPlayer.class);
		try (AudioAlerter alerter = new AudioAlerter(player, 1, 1)) {
			alerter.alert("ceri");
			alerter.clear();
		}
		InOrder inOrder = inOrder(player);
		inOrder.verify(player).play(Clip.alarm);
		inOrder.verify(player).play(Clip.build_broken);
		inOrder.verify(player).play("ceri");
		inOrder.verify(player).play(Clip.you_have_30min);
		inOrder.verify(player).play(Clip.build_ok);
		inOrder.verify(player).play(Clip.thank_you);
		verifyNoMoreInteractions(player);
	}
	
	@Test
	public void shouldRestartAlert() throws IOException {
		AudioPlayer player = mock(AudioPlayer.class);
		try (AudioAlerter alerter = new AudioAlerter(player, 1, 1)) {
			alerter.alert("ceri1");
			alerter.alert("ceri2");
		}
		InOrder inOrder = inOrder(player);
		inOrder.verify(player).play(Clip.alarm);
		inOrder.verify(player).play(Clip.build_broken);
		inOrder.verify(player).play("ceri1");
		inOrder.verify(player).play(Clip.you_have_30min);
		inOrder.verify(player).play(Clip.alarm);
		inOrder.verify(player).play(Clip.build_broken);
		inOrder.verify(player).play("ceri2");
		inOrder.verify(player).play(Clip.you_have_30min);
		verifyNoMoreInteractions(player);
	}
	
}
