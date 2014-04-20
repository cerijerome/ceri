package ceri.ci.alert;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.common.Alerter;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;

public class AlertServiceContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "alert";
	private final AlerterGroup alerters;
	public final AlertService service;

	public AlertServiceContainer(BaseProperties properties, Collection<Alerter> alerters) {
		AlertServiceProperties alertProperties = new AlertServiceProperties(properties, GROUP);
		logger.info("Creating alerter group");
		this.alerters = createAlerterGroup(alertProperties, alerters);
		logger.info("Creating alert service");
		service = createAlertService(this.alerters, alertProperties);
	}

	@Override
	public void close() throws IOException {
		IoUtil.close(alerters);
		IoUtil.close(service);
	}

	private AlertService
		createAlertService(AlerterGroup alerters, AlertServiceProperties properties) {
		return new AlertService(alerters, properties.reminderMs(), properties.shutdownTimeoutMs());
	}

	private AlerterGroup createAlerterGroup(AlertServiceProperties properties,
		Collection<Alerter> alerters) {
		AlerterGroup.Builder builder =
			AlerterGroup.builder().shutdownTimeoutMs(properties.shutdownTimeoutMs());
		for (Alerter alerter : alerters) if (alerter != null) builder.alerters(alerter);
		return builder.build();
	}

}
