package ceri.ci.alert;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
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
	
	public void alert(Builds builds) {
		Builds summarizedBuilds = BuildUtil.summarize(builds);
		Collection<String> breakNames = BuildUtil.summarizedBreakNames(summarizedBuilds);
		if (x10 != null) x10.alert(breakNames);
		if (zwave != null) zwave.alert(breakNames);
		if (web != null) web.update(summarizedBuilds);
		if (audio != null) audio.alert(summarizedBuilds);
	}
	
	public void clear() {
		if (x10 != null) x10.clear();
		if (zwave != null) zwave.clear();
		if (web != null) web.clear();
		if (audio != null) audio.clear();
	}
	
	@Override
	public void close() throws IOException {
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
		try {
			Properties properties = PropertyUtil.load(AudioAlerter.class, "audio.properties");
			return AudioAlerter.create(properties, null);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private WebAlerter createWeb() {
		File rootDir = IoUtil.getPackageDir(WebAlerter.class);
		return new WebAlerter(rootDir);
	}

}
