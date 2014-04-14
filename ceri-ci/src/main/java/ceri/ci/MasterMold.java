package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.alert.AlertService;
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
			//			@SuppressWarnings("resource")
			//			AlertService service = masterMold.alertService();
			//			BuildEvent event0 = new BuildEvent("bolt", "smoke", Event.broken("cdehaudt"));
			//			service.process(event0);
			//			BasicUtil.delay(10000);
			//			BuildEvent event1 = new BuildEvent("bolt", "smoke", Event.fixed("cdehaudt"));
			//			BuildEvent event2 = new BuildEvent("bolt", "regression", Event.broken("machung"));
			//			service.process(event1, event2);
			//			BasicUtil.delay(10000);
			//			BuildEvent event3 = new BuildEvent("bolt", "smoke", Event.fixed("dxie"));
			//			service.process(event3);
			BasicUtil.delay(10000);
			//			service.broken("bolt", "smoke", Arrays.asList("fuzhong", "cjerome"));
			BasicUtil.delay(100000);
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
	public void close() throws IOException {
		logger.info("Closing");
		IoUtil.close(email);
		IoUtil.close(x10);
		IoUtil.close(alert);
	}

}
