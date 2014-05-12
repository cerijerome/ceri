package ceri.ci.alert;

import java.io.Closeable;
import java.util.Arrays;
import java.util.Collection;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.ci.common.Alerter;
import ceri.common.io.IoUtil;
import ceri.common.property.BaseProperties;

/**
 * Creates the alert service.
 */
public class AlertServiceContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	private static final String GROUP = "alert";
	private final AlerterGroup alerters;
	private final AlertServiceImpl service;

	public AlertServiceContainer(BaseProperties properties, Alerter... alerters) {
		this(properties, Arrays.asList(alerters));
	}

	public AlertService service() {
		return service;
	}

	public AlertServiceContainer(BaseProperties properties, Collection<Alerter> alerters) {
		AlertProperties alertProperties = new AlertProperties(properties, GROUP);
		logger.info("Creating alerter group");
		this.alerters = createAlerterGroup(alertProperties, alerters);
		logger.info("Creating alert service");
		service = createAlertService(this.alerters, alertProperties);
	}

	@Override
	public void close() {
		IoUtil.close(alerters);
		IoUtil.close(service);
	}

	private AlertServiceImpl createAlertService(AlerterGroup alerters, AlertProperties properties) {
		return new AlertServiceImpl(alerters, properties.reminderMs(), properties
			.shutdownTimeoutMs());
	}

	private AlerterGroup
		createAlerterGroup(AlertProperties properties, Collection<Alerter> alerters) {
		AlerterGroup.Builder builder =
			AlerterGroup.builder().shutdownTimeoutMs(properties.shutdownTimeoutMs());
		for (Alerter alerter : alerters)
			if (alerter != null) builder.alerters(alerter);
		return builder.build();
	}

}
