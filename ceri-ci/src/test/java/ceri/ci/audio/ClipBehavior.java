package ceri.ci.audio;

import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ClipBehavior {

	@Test
	public void shouldLoadDataForEachClip() {
		for (Clip clip : Clip.values()) {
			assertNotNull(clip.filename);
		}
	}

}
