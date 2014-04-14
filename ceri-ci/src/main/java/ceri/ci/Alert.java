package ceri.ci;

import java.io.Closeable;
import java.io.IOException;
import java.util.Properties;
import ceri.ci.alert.AlertService;
import ceri.ci.alert.AlertServiceProperties;
import ceri.ci.alert.Alerters;
import ceri.ci.audio.AudioAlerter;
import ceri.ci.web.WebAlerter;
import ceri.ci.x10.X10Alerter;
import ceri.ci.zwave.ZWaveAlerter;
import ceri.common.io.IoUtil;

public class Alert implements Closeable {
	private final Alerters alerters;
	public final AlertService service;

	public Alert(X10Alerter x10, ZWaveAlerter zwave, AudioAlerter audio,
		WebAlerter web, Properties properties, String prefix) {
		AlertServiceProperties alertProperties =
			new AlertServiceProperties(properties, prefix, "alert");
		alerters = createAlerters(x10, zwave, audio, web, alertProperties);
		service = createAlertService(alerters, alertProperties);
	}

	@Override
	public void close() throws IOException {
		IoUtil.close(alerters);
		IoUtil.close(service);
	}
	
	private AlertService createAlertService(Alerters alerters, AlertServiceProperties properties) {
		return new AlertService(alerters, properties.reminderMs(), properties.shutdownTimeoutMs());
	}

	private Alerters createAlerters(X10Alerter x10, ZWaveAlerter zwave, AudioAlerter audio,
		WebAlerter web, AlertServiceProperties properties) {
		return Alerters.builder().x10(x10).zwave(zwave).web(web).audio(audio).shutdownTimeoutMs(
			properties.shutdownTimeoutMs()).build();
	}

}
