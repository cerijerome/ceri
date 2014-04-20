package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.AlertServiceContainer;
import ceri.ci.audio.AudioContainer;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.ci.common.Alerter;
import ceri.ci.email.EmailContainer;
import ceri.ci.proxy.ProxyContainer;
import ceri.ci.web.WebContainer;
import ceri.ci.x10.X10Container;
import ceri.ci.zwave.ZWaveContainer;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;

/**
 * Creates everything, 'nuff said.
 */
public class MasterMold implements Closeable {
	private final Logger logger = LogManager.getLogger();
	private final MasterSlave masterSlave;
	private final X10Container x10;
	private final ZWaveContainer zwave;
	private final AudioContainer audio;
	private final EmailContainer email;
	public final WebContainer web;
	public final AlertServiceContainer alert;
	public final ProxyContainer proxy;

	public static void main(String[] args) throws IOException {
		try (MasterMold masterMold = new MasterMold()) {
						AlertService service = masterMold.alert.service;
						BuildEvent ev0 = new BuildEvent("bolt", "smoke", Event.failure("cdehaudt", "cjerome"));
						BuildEvent ev1 = new BuildEvent("bolt", "metrics", Event.failure("punpal"));
						BuildEvent ev2 = new BuildEvent("mweb", "integration", Event.failure("adicohen"));
						service.process(ev0, ev1, ev2);
						BasicUtil.delay(60000);
						BuildEvent ev3 = new BuildEvent("bolt", "smoke", Event.success());
						BuildEvent ev4 = new BuildEvent("bolt", "commit", Event.failure("uroblesmellin"));
						BuildEvent ev5 = new BuildEvent("mweb", "integration", Event.failure("adicohen"));
						service.process(ev3, ev4, ev5);
			while (true)
				BasicUtil.delay(10000);
		}
	}

	public MasterMold() throws IOException {
		this(PropertyUtil.load(AlertService.class, "alert.properties"));
	}

	public MasterMold(Properties properties) throws IOException {
		logger.debug(properties);
		masterSlave = MasterSlave.createFromEnv();
		String prefix = masterSlave.name;
		BaseProperties baseProperties = new BaseProperties(properties, prefix) {};
		x10 = new X10Container(baseProperties);
		zwave = new ZWaveContainer(baseProperties);
		audio = new AudioContainer(baseProperties);
		web = new WebContainer(baseProperties);
		Collection<Alerter> alerters =
			Arrays.asList(x10.alerter, zwave.alerter, audio.alerter, web.alerter);
		alert = new AlertServiceContainer(baseProperties, alerters);
		email = new EmailContainer(alert.service, baseProperties);
		proxy = new ProxyContainer(properties, prefix);
	}

	@Override
	public void close() {
		logger.info("Closing");
		email.close();
		audio.close();
		IoUtil.close(x10);
		IoUtil.close(alert);
	}

}
