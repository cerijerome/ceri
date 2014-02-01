package ceri.ci.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import ceri.common.io.IoUtil;

public class Audio {
	public static final float NORMAL_PITCH = 1.0f;
	private final AudioFormat format;
	private final byte[] data;

	Audio(AudioFormat format, byte[] data) {
		this.format = format;
		this.data = data;
	}

	public static Audio create(byte[] data) throws IOException {
		try (InputStream in = new ByteArrayInputStream(data)) {
			return create(in);
		}
	}

	public static Audio create(File file) throws IOException {
		return create(IoUtil.getContent(file));
	}

	private static Audio create(InputStream is) throws IOException {
		try (AudioInputStream in = AudioSystem.getAudioInputStream(is)) {
			AudioFormat format = in.getFormat();
			byte[] data = IoUtil.getContent(in, 0);
			return new Audio(format, data);
		} catch (UnsupportedAudioFileException e) {
			throw new IOException(e);
		}
	}

	public Audio changePitch(float pitch) {
		if (pitch == NORMAL_PITCH) return this;
		AudioFormat newFormat =
			new AudioFormat(format.getEncoding(), format.getSampleRate() * pitch, format
				.getSampleSizeInBits(), format.getChannels(), format.getFrameSize(), format
				.getFrameRate(), format.isBigEndian());
		return new Audio(newFormat, data);
	}

	public Audio clip(int startOffset, int endOffset) {
		int len = data.length - startOffset - endOffset;
		if (len < 0) throw new IllegalArgumentException("Offsets cannot be larger than data size " +
			data.length);
		byte[] data = new byte[len];
		System.arraycopy(this.data, startOffset, data, 0, data.length);
		return new Audio(format, data);
	}

	public void play() throws IOException {
		try (SourceDataLine out = getSourceDataLine(format)) {
			out.open();
			out.start();
			out.write(data, 0, data.length);
			out.drain();
			out.flush();
		} catch (LineUnavailableException e) {
			throw new IOException(e);
		}
	}

	SourceDataLine getSourceDataLine(AudioFormat format) throws LineUnavailableException {
		return AudioSystem.getSourceDataLine(format);
	}

}
