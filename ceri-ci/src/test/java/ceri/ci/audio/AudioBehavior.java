package ceri.ci.audio;

import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import org.junit.Test;
import org.mockito.Mockito;
import ceri.common.io.IoUtil;
import ceri.common.test.FileTestHelper;

public class AudioBehavior {
	private static final File testFile = new File(IoUtil.getPackageDir(AudioBehavior.class),
		"test.wav");

	@Test
	public void shouldNotThrowConstructorExceptionsForValidData() throws IOException {
		byte[] data = IoUtil.getContent(testFile);
		Audio audio = new Audio.Builder(testFile).pitch(2.0f).build();
		audio = new Audio.Builder(data).pitch(1.0f).build();
		audio = Audio.create(data);
		audio = Audio.create(testFile);
		assertNotNull(audio);
	}

	@Test(expected = IOException.class)
	public void shouldFailForInvalidData() throws IOException {
		try (FileTestHelper helper =
			FileTestHelper.builder().file("bad.wav", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").build()) {
			Audio.create(helper.file("bad.wav"));
		}
	}

	@Test
	public void shouldPlayAudioData() throws IOException {
		final SourceDataLine line = Mockito.mock(SourceDataLine.class);
		Audio.Builder builder = new Audio.Builder(testFile);
		Audio audio = new Audio(builder) {
			@Override
			SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
				return line;
			}
		};
		audio.play();
	}

}
