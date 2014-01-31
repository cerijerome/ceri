package ceri.ci.alert;

import java.io.Closeable;
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

/**
 * Container for alerter components. Web => heroes/villains grouped by job ZWave
 * => aggregate of broken jobs X10 => aggregate of broken jobs Audio => spoken
 * warnings when build fails or is fixed
 */
public class Alerters implements Closeable {
	public final X10Alerter x10;
	public final ZWaveAlerter zwave;
	public final AudioAlerter audio;
	public final WebAlerter web;

	public Alerters(Properties properties, String prefix) throws IOException {
		AlertersProperties props = new AlertersProperties(properties, prefix);
		x10 = props.x10Enabled() ? createX10(properties) : null;
		zwave = props.zwaveEnabled() ? createZWave(properties) : null;
		web = props.webEnabled() ? createWeb(properties) : null;
		audio = props.audioEnabled() ? createAudio(properties) : null;
	}

	public void alert(Builds builds) {
		System.out.println("alert");
		Builds summarizedBuilds = BuildUtil.summarize(builds);
		Collection<String> breakNames = BuildUtil.summarizedBreakNames(summarizedBuilds);
		if (x10 != null) x10.alert(breakNames);
		if (zwave != null) zwave.alert(breakNames);
		if (web != null) web.update(summarizedBuilds);
		if (audio != null) audio.alert(summarizedBuilds);
	}

	public void clear() {
		System.out.println("clear");
		if (x10 != null) x10.clear();
		if (zwave != null) zwave.clear();
		if (web != null) web.clear();
		if (audio != null) audio.clear();
	}

	public void remind() {
		System.out.println("remind");
		if (audio != null) audio.remind();
	}

	@Override
	public void close() throws IOException {
		IoUtil.close(x10);
	}

	ZWaveAlerter createZWave(Properties properties) {
		return ZWaveAlerter.create(properties, "zwave");
	}

	X10Alerter createX10(Properties properties) throws IOException {
		return X10Alerter.create(properties, "x10");
	}

	AudioAlerter createAudio(Properties properties) {
		return AudioAlerter.create(properties, "audio");
	}

	WebAlerter createWeb(Properties properties) {
		return WebAlerter.create(properties, "web");
	}

}
