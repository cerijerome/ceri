package ceri.ci.audio;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import java.util.Properties;
import org.junit.BeforeClass;
import org.junit.Test;

public class AudioAlerterPropertiesBehavior {
	private static Properties props = new Properties();
	
	@BeforeClass
	public static void init() {
		props.put("x.enabled", "true");
		props.put("x.pitch", "1.2");
	}
	
	@Test
	public void shouldReadValuesWithPrefix() {
		AudioAlerterProperties audio = new AudioAlerterProperties(props, "x");
		assertThat(audio.enabled(), is(true));
		assertThat(audio.pitch(), is(1.2f));
	}

}
