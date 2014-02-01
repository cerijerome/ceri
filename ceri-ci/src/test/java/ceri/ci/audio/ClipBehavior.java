package ceri.ci.audio;

import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import org.junit.Test;

public class ClipBehavior {

	@Test
	public void shouldLoadDataForEachClip() throws IOException {
		for (Clip clip : Clip.values()) {
			Audio audio = clip.audio();
			assertNotNull(audio);
		}
	}

}
