package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.alert.AlertService;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.common.io.IoUtil;
import ceri.common.property.PropertyUtil;
import ceri.common.util.BasicUtil;

/**
 * Creates everything, 'nuff said.
 */
public class MasterMold implements Closeable {
	private final Logger logger = LogManager.getLogger();
	private final MasterSlave masterSlave;
	private final X10 x10;
	private final ZWave zwave;
	private final Audio audio;
	private final Email email;
	public final Web web;
	public final Alert alert;
	public final Proxy proxy;

	public static void main(String[] args) throws IOException {
		try (MasterMold masterMold = new MasterMold()) {
//			AlertService service = masterMold.alert.service;
//			BuildEvent ev0 = new BuildEvent("bolt", "smoke", Event.failure("cdehaudt", "cjerome"));
//			BuildEvent ev1 = new BuildEvent("bolt", "metrics", Event.failure("punpal"));
//			BuildEvent ev2 = new BuildEvent("mweb", "integration", Event.failure("adicohen"));
//			service.process(ev0, ev1, ev2);
//			BasicUtil.delay(60000);
//			BuildEvent ev3 = new BuildEvent("bolt", "smoke", Event.success());
//			BuildEvent ev4 = new BuildEvent("bolt", "commit", Event.failure("uroblesmellin"));
//			BuildEvent ev5 = new BuildEvent("mweb", "integration", Event.failure("adicohen"));
//			service.process(ev3, ev4, ev5);
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
		x10 = new X10(properties, prefix);
		zwave = new ZWave(properties, prefix);
		audio = new Audio(properties, prefix);
		web = new Web(properties, prefix);
		alert =
			new Alert(x10.alerter, zwave.alerter, audio.alerter, web.alerter, properties, prefix);
		email = new Email(alert.service, properties, prefix);
		proxy = new Proxy(properties, prefix);
	}

	@Override
	public void close() {
		logger.info("Closing");
		IoUtil.close(email);
		IoUtil.close(x10);
		IoUtil.close(alert);
	}

}
