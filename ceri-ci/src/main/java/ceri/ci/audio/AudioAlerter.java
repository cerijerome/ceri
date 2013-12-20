package ceri.ci.audio;

import java.io.File;
import java.io.IOException;
import ceri.ci.common.Alerter;
import ceri.common.date.TimeUnit;
import ceri.common.util.BasicUtil;

public class AudioAlerter implements Alerter {
	private static final long TEN_MINS_MS = TimeUnit.minute.ms * 10;
	private static final int SHUTDOWN_POLL_MS = 200;
	private final long delayMs;
	private final int shutdownPollMs;
	private final AudioPlayer player;
	private final Object sync = new Object();
	private Thread thread;

	public static void main(String[] args) throws Exception {
		try (AudioAlerter alerter =
			new AudioAlerter(new AudioPlayer(new File("./data/people"), new File("./data/clips")),
				2000, 200)) {
			alerter.alert("shuochen", "fuzhong", "dxie");
			BasicUtil.delay(20000);
		}
	}

	public AudioAlerter(File keyDir, File clipDir) {
		this(new AudioPlayer(keyDir, clipDir), TEN_MINS_MS, SHUTDOWN_POLL_MS);
	}

	AudioAlerter(AudioPlayer player, long delayMs, int shutdownPollMs) {
		this.player = player;
		this.delayMs = delayMs;
		this.shutdownPollMs = shutdownPollMs;
	}

	@Override
	public void alert(String... keys) {
		start(createThread(keys));
	}

	@Override
	public void clear(String... keys) {
		stop();
		player.play(AudioPlayer.Clip.build_ok);
		player.play(AudioPlayer.Clip.thank_you);
		for (String key : keys)
			player.play(key);
	}

	@Override
	public void close() throws IOException {
		stop();
	}

	void doAlert(String... keys) throws InterruptedException {
		player.play(AudioPlayer.Clip.alarm);
		player.play(AudioPlayer.Clip.build_broken);
		for (String key : keys)
			player.play(key);
		player.play(AudioPlayer.Clip.you_have_30min);
		// Wait
		if (delayMs > 0) Thread.sleep(delayMs);
		for (String key : keys)
			player.play(key);
		player.play(AudioPlayer.Clip.you_have_20min);
		// Wait
		if (delayMs > 0) Thread.sleep(delayMs);
		for (String key : keys)
			player.play(key);
		player.play(AudioPlayer.Clip.you_have_10min);
		// Wait
		if (delayMs > 0) Thread.sleep(delayMs);
		for (String key : keys)
			player.play(key);
		player.play(AudioPlayer.Clip.out_of_time);
	}

	private Thread createThread(final String... keys) {
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					doAlert(keys);
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
