package ceri.ci.ecg.sjc;

import java.io.Closeable;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.MasterContainer;
import ceri.ci.audio.AudioContainer;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.ci.web.WebAlerter;
import ceri.ci.web.WebContainer;
import ceri.ci.x10.X10Container;
import ceri.ci.zwave.ZWaveContainer;
import ceri.common.util.BasicUtil;

/**
 * Creates everything, 'nuff said.
 */
public class MainContainer implements Closeable {
	private final Logger logger = LogManager.getLogger();
	private final X10Container x10;
	private final ZWaveContainer zwave;
	private final AudioContainer audio;
	private final WebContainer web;
	public final MasterContainer master;

	public static void main(String[] args) throws IOException {
		try (MainContainer container = new MainContainer()) {
			AlertService alert = container.master.alert();
			BuildEvent ev0 = new BuildEvent("bolt", "smoke", Event.failure("cdehaudt", "cjerome"));
			BuildEvent ev1 = new BuildEvent("bolt", "metrics", Event.failure("punpal"));
			BuildEvent ev2 = new BuildEvent("mweb", "integration", Event.failure("adicohen"));
			alert.process(ev0, ev1, ev2);
			BasicUtil.delay(10000);
			BuildEvent ev3 = new BuildEvent("bolt", "smoke", Event.success());
			BuildEvent ev4 = new BuildEvent("bolt", "commit", Event.failure("uroblesmellin"));
			BuildEvent ev5 = new BuildEvent("mweb", "integration", Event.failure("adicohen"));
			alert.process(ev3, ev4, ev5);
			while (true)
				BasicUtil.delay(10000);
		}
	}

	public MainContainer() throws IOException {
		MasterContainer.Builder builder = MasterContainer.builder();
		logger.debug(builder.properties);
		x10 = new X10Container(builder.properties);
		zwave = new ZWaveContainer(builder.properties);
		audio = new AudioContainer(builder.properties);
		web = new WebContainer(builder.properties);
		builder.alerters(x10.alerter, zwave.alerter, audio.alerter, web.alerter);
		builder.parsers(new BoltMWebEmailParser());
		master = builder.build();
	}

	@Override
	public void close() {
		logger.info("Closing");
		audio.close();
		x10.close();
		master.close();
	}

	public WebAlerter web() {
		return web.alerter;
	}
	
}
