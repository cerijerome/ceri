package ceri.ci.audio;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import org.junit.Test;

public class ClipBehavior {

	@Test
	public void shouldLoadDataForEachClip() throws IOException {
		for (Clip clip : Clip.values()) {
			byte[] data = clip.load();
			assertTrue(data.length > 0);
		}
	}

}
