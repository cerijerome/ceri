package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.AlertServiceProperties;
import ceri.ci.alert.Alerters;
import ceri.ci.audio.AudioAlerter;
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
	private final X10 x10;
	private final ZWave zwave;
	private final Audio audio;
	private final Web web;
	private final Alerters alerters;
	private final AlertService alertService;

	public static void main(String[] args) throws IOException {
		try (MasterMold masterMold = new MasterMold()) {
			@SuppressWarnings("resource")
			AlertService service = masterMold.alertService();
			service.broken("bolt", "smoke", Arrays.asList("cdehaudt"));
			BasicUtil.delay(10000);
			service.fixed("bolt", "smoke", Arrays.asList("cdehaudt"));
			service.broken("bolt", "regression", Arrays.asList("machung"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("dxie"));
			BasicUtil.delay(10000);
			service.broken("bolt", "smoke", Arrays.asList("fuzhong", "cjerome"));
			BasicUtil.delay(1000);
		}
	}

	public MasterMold() throws IOException {
		this(PropertyUtil.load(AlertService.class, "alert.properties"));
	}

	public MasterMold(Properties properties) throws IOException {
		logger.debug(properties);
		x10 = new X10(properties);
		zwave = new ZWave(properties);
		audio = new Audio(properties);
		web = new Web(properties);
		AlertServiceProperties alertProperties = new AlertServiceProperties(properties, "alert");
		alerters =
			createAlerters(x10.alerter, zwave.alerter, audio.alerter, web.alerter, alertProperties);
		alertService = createAlertService(alerters, alertProperties);
	}

	@Override
	public void close() throws IOException {
		logger.info("Closing alerters");
		if (alerters != null) IoUtil.close(alerters);
		logger.info("Closing alertService");
		if (alertService != null) IoUtil.close(alertService);
		x10.close();
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
		WebAlerter web, AlertServiceProperties properties) {
		return Alerters.builder().x10(x10).zwave(zwave).web(web).audio(audio).timeoutMs(
			properties.timeoutMs()).build();
	}

}
