package ceri.ci.audio;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import ceri.common.date.TimeUnit;
import ceri.common.io.IoUtil;
import ceri.common.util.BasicUtil;

public class AudioAlerter implements Closeable {
	private static final String AUDIO_FILE_SUFFIX = ".wav";
	private static final long TEN_MINS_MS = TimeUnit.minute.ms * 10;
	private static final int SHUTDOWN_POLL_MS = 200;
	private final File soundDir;
	private final long delayMs;
	private final int shutdownPollMs;
	private final Object sync = new Object();
	private Thread thread;

	public static void main(String[] args) throws Exception {
		try (AudioAlerter alerter = new AudioAlerter(IoUtil.getPackageDir(AudioAlerter.class))) {
			alerter.alert("shuochen", "fuzhong", "dxie");
			BasicUtil.delay(20000);
		}
	}

	public AudioAlerter(File soundDir) {
		this(soundDir, TEN_MINS_MS, SHUTDOWN_POLL_MS);
	}

	AudioAlerter(File soundDir, long delayMs, int shutdownPollMs) {
		this.soundDir = soundDir;
		this.delayMs = delayMs;
		this.shutdownPollMs = shutdownPollMs;
	}

	public void alert(String... keys) {
		start(createThread(keys));
	}

	public void clear(String... keys) throws IOException {
		stop();
		Clip.build_ok.play();
		Clip.thank_you.play();
		for (String key : keys)
			play(key);
	}

	@Override
	public void close() throws IOException {
		stop();
	}

	private void play(String key) throws IOException {
		Audio.create(new File(soundDir, key + AUDIO_FILE_SUFFIX)).play();
	}

	void doAlert(String... keys) throws IOException, InterruptedException {
		Clip.alarm.play();
		Clip.build_broken.play();
		for (String key : keys) play(key);
		Clip.you_have_30min.play();
		// Wait
		if (delayMs > 0) Thread.sleep(delayMs);
		for (String key : keys) play(key);
		Clip.you_have_20min.play();
		// Wait
		if (delayMs > 0) Thread.sleep(delayMs);
		for (String key : keys) play(key);
		Clip.you_have_10min.play();
		// Wait
		if (delayMs > 0) Thread.sleep(delayMs);
		for (String key : keys) play(key);
		Clip.out_of_time.play();
	}

	private Thread createThread(final String... keys) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					doAlert(keys);
				} catch (IOException e) {
				} catch (InterruptedException e) {
					// thread stopped
				}
			}
		});
		return thread;
	}

	private void start(Thread thread) {
		synchronized (sync) {
			stop();
			this.thread = thread;
			thread.start();
		}
	}

	private void stop() {
		synchronized (sync) {
			if (thread == null) return;
			while (thread.isAlive()) {
				thread.interrupt();
				BasicUtil.delay(shutdownPollMs);
			}
			thread = null;
		}
	}
}
