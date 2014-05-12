package ceri.ci.audio;

import static ceri.common.test.TestUtil.assertException;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
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
	private static final File testFile = IoUtil.getResourceFile(AudioBehavior.class, "test.wav");

	@Test
	public void shouldClipToANewAudioObject() throws IOException {
		Audio audio = Audio.create(testFile);
		Audio audio2 = audio.clip(0, 16);
		assertThat(audio, not(audio2));
	}

	@Test
	public void shouldFailIfClipLimitsAreNotWithinDataRange() throws IOException {
		Audio audio = Audio.create(testFile);
		assertException(() -> audio.clip(11000, 11000));
		Audio audio2 = audio.clip(0, 16);
		assertThat(audio, not(audio2));
	}

	@Test
	public void shouldOnlyChangePitchIfNotNormalPitch() throws IOException {
		Audio audio = Audio.create(testFile);
		Audio audio2 = audio.changePitch(1.0f);
		Audio audio3 = audio.changePitch(2.0f);
		assertThat(audio, is(audio2));
		assertThat(audio, not(audio3));
	}

	@Test
	public void shouldNotThrowConstructorExceptionsForValidData() throws IOException {
		byte[] data = IoUtil.getContent(testFile);
		Audio audio = Audio.create(testFile);
		assertNotNull(audio);
		audio = Audio.create(data);
		assertNotNull(audio);
	}

	@Test(expected = IOException.class)
	public void shouldFailForInvalidData() throws IOException {
		try (FileTestHelper helper =
			FileTestHelper.builder().file("bad.wav", "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx").build()) {
			Audio.create(helper.file("bad.wav"));
		}
	}

	@Test(expected = IOException.class)
	public void shouldThrowIOExceptionWhenLineIsUnavailable() throws IOException {
		Audio audio = Audio.create(testFile);
		audio = new Audio(null, new byte[0]) {
			@Override
			SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
				throw new LineUnavailableException();
			}
		};
		audio.play();
	}

	@Test
	public void shouldPlayAudioData() throws IOException {
		final SourceDataLine line = Mockito.mock(SourceDataLine.class);
		Audio audio = Audio.create(testFile);
		audio = new Audio(null, new byte[0]) {
			@Override
			SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
				return line;
			}
		};
		audio.play();
	}

}
