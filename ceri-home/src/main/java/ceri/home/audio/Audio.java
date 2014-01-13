package ceri.home.audio;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import ceri.common.io.IoUtil;

public class Audio implements AudioRef {
	private static final int BUFFER_SIZE = 32 * 1024;
	private final AudioFormat format;
	private final byte[] data;

	public Audio(AudioFormat format, byte[] data) {
		this.format = format;
		this.data = data;
	}

	private static abstract class BaseRef implements AudioRef {
		private final ClipInfo clipInfo;

		protected BaseRef(ClipInfo clipInfo) {
			this.clipInfo = clipInfo;
		}

		@Override
		public final Audio getAudio() throws IOException {
			try (AudioInputStream in = getInputStream()) {
				Audio audio = Audio.createFromStream(in);
				if (clipInfo != null) audio = audio.clip(clipInfo);
				return audio;
			}
		}

		protected abstract AudioInputStream getInputStream() throws IOException;
	}

	public static AudioRef createRefFromFile(final File file, final ClipInfo clipInfo) {
		return new BaseRef(clipInfo) {
			@Override
			protected AudioInputStream getInputStream() throws IOException {
				try {
					return AudioSystem.getAudioInputStream(file);
				} catch (UnsupportedAudioFileException e) {
					throw new IOException(e);
				}
			}
		};
	}

	public static Audio createFromStream(AudioInputStream in) throws IOException {
		try {
			AudioFormat format = in.getFormat();
			byte[] data = IoUtil.getContent(in, BUFFER_SIZE);
			return new Audio(format, data);
		} finally {
			in.close();
		}
	}

	@Override
	public Audio getAudio() {
		return this;
	}

	public AudioFormat getFormat() {
		return format;
	}

	public byte[] getData() {
		return data;
	}

	public Audio clip(ClipInfo clipInfo) {
		return new Audio(format, clipInfo.getClippedData(format.getFrameSize(), data));
	}

}
