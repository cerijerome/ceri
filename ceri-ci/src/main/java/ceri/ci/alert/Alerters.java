package ceri.ci.alert;

import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.build.BuildUtil;
import ceri.ci.build.Builds;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.ZWaveAlerter;

/**
 * Container for alerter components.
 */
public class Alerters {
	private static final Logger logger = LogManager.getLogger();
	public final X10Alerter x10;
	public final ZWaveAlerter zwave;
	public final AudioAlerter audio;
	public final WebAlerter web;

	public Alerters(X10Alerter x10, ZWaveAlerter zwave, AudioAlerter audio, WebAlerter web) {
		this.x10 = x10;
		this.zwave = zwave;
		this.audio = audio;
		this.web = web;
	}

	/**
	 * Notify alerters that builds have changed.
	 */
	public void alert(Builds builds) {
		logger.info("alert");
		Builds summarizedBuilds = BuildUtil.summarize(builds);
		Collection<String> breakNames = BuildUtil.summarizedBreakNames(summarizedBuilds);
		if (x10 != null) x10.alert(breakNames);
		if (zwave != null) zwave.alert(breakNames);
		if (web != null) web.update(summarizedBuilds);
		if (audio != null) audio.alert(summarizedBuilds);
	}

	/**
	 * Clear alerters' states.
	 */
	public void clear() {
		logger.info("clear");
		if (x10 != null) x10.clear();
		if (zwave != null) zwave.clear();
		if (web != null) web.clear();
		if (audio != null) audio.clear();
	}

	/**
	 * Remind alerters of current state.
	 */
	public void remind() {
		logger.info("remind");
		if (audio != null) audio.remind();
	}

}
