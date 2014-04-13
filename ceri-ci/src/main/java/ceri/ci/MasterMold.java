package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.AlertServiceProperties;
import ceri.ci.alert.Alerters;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.build.BuildEvent;
import ceri.ci.build.Event;
import ceri.ci.proxy.MultiProxy;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.ZWaveAlerter;
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
	private final Web web;
	private final Alerters alerters;
	private final AlertService alertService;
	private final Email email;
	private final Proxy proxy;

	public static void main(String[] args) throws IOException {
		try (MasterMold masterMold = new MasterMold()) {
			@SuppressWarnings("resource")
			AlertService service = masterMold.alertService();
			BuildEvent event0 = new BuildEvent("bolt", "smoke", Event.broken("cdehaudt"));
			service.process(event0);
			BasicUtil.delay(10000);
			BuildEvent event1 = new BuildEvent("bolt", "smoke", Event.fixed("cdehaudt"));
			BuildEvent event2 = new BuildEvent("bolt", "regression", Event.broken("machung"));
			service.process(event1, event2);
			BasicUtil.delay(10000);
			BuildEvent event3 = new BuildEvent("bolt", "smoke", Event.fixed("dxie"));
			service.process(event3);
			BasicUtil.delay(10000);
			//			service.broken("bolt", "smoke", Arrays.asList("fuzhong", "cjerome"));
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
		AlertServiceProperties alertProperties =
			new AlertServiceProperties(properties, prefix, "alert");
		alerters = createAlerters(x10.alerter, zwave.alerter, audio.alerter, web.alerter);
		alertService = createAlertService(alerters, alertProperties);
		email = new Email(alertService, properties, prefix);
		proxy = new Proxy(properties, prefix);
	}

	@Override
	public void close() throws IOException {
		logger.info("Closing");
		if (email != null) IoUtil.close(email);
		if (alerters != null) IoUtil.close(alerters);
		if (alertService != null) IoUtil.close(alertService);
		x10.close();
	}

	public MultiProxy proxy() {
		return proxy.multi;
	}

	public AlertService alertService() {
		return alertService;
	}

	public WebAlerter webService() {
		return web.alerter;
	}

	private AlertService createAlertService(Alerters alerters, AlertServiceProperties properties) {
		return new AlertService(alerters, properties.reminderMs());
	}

	private Alerters createAlerters(X10Alerter x10, ZWaveAlerter zwave, AudioAlerter audio,
		WebAlerter web) {
		return Alerters.builder().x10(x10).zwave(zwave).web(web).audio(audio).build();
	}

}
