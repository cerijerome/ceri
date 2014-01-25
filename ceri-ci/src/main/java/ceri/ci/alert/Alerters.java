package ceri.ci.alert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Properties;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.common.io.IoUtil;
import ceri.common.property.PropertyUtil;

/**
 * Container for alerter components.
 * Web => heroes/villains grouped by job
 * ZWave => aggregate of broken jobs
 * X10 => aggregate of broken jobs
 * Audio => spoken warnings when build fails or is fixed
 */
public class Alerters implements Closeable {
	public final X10Alerter x10;
	public final ZWaveAlerter zwave;
	public final AudioAlerter audio;
	public final WebAlerter web;

	public Alerters() {
		x10 = createX10();
		zwave = createZWave();
		audio = createAudio();
		web = createWeb();
	}
	
	@Override
	public void close() throws IOException {
		IoUtil.close(audio);
		IoUtil.close(x10);
	}

	private ZWaveAlerter createZWave() {
		try {
			Properties properties = PropertyUtil.load(ZWaveAlerter.class, "zwave.properties");
			return ZWaveAlerter.create(properties, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private X10Alerter createX10() {
		try {
			Properties properties = PropertyUtil.load(X10Alerter.class, "x10.properties");
			return X10Alerter.create(properties, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private AudioAlerter createAudio() {
		File rootDir = IoUtil.getPackageDir(AudioAlerter.class);
		return new AudioAlerter(rootDir);
	}

	private WebAlerter createWeb() {
		File rootDir = IoUtil.getPackageDir(WebAlerter.class);
		return new WebAlerter(rootDir);
	}

}
