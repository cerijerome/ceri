package ceri.x10.cm11a;

import static ceri.common.function.FunctionUtil.execSilently;
import java.io.Closeable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.text.ToStringHelper;
import ceri.log.util.LogUtil;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.serial.javax.util.SelfHealingSerialConnector;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;
import ceri.x10.cm11a.device.Cm11aSerialAdapter;

/**
 * Container for a cm11a controller.
 */
public class Cm11aContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	public final int id;
	private final SerialConnector connector;
	public final Cm11aDevice cm11a;

	public static Cm11aContainer of(Cm11aConfig config) {
		return new Cm11aContainer(config);
	}

	private Cm11aContainer(Cm11aConfig config) {
		try {
			id = config.id;
			logger.info("Started({}): {}", id);
			connector = createConnector(config.deviceSerial);
			cm11a = createDevice(connector, config.device);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public void close() {
		LogUtil.close(logger, cm11a, connector);
		logger.info("Stopped({}): {}", id);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, id, connector).toString();
	}

	private SerialConnector createConnector(SelfHealingSerialConfig config) {
		config = SelfHealingSerialConfig.replace(config, Cm11aSerialAdapter.SERIAL_PARAMS);
		SelfHealingSerialConnector connector = SelfHealingSerialConnector.of(config);
		execSilently(connector::connect);
		return connector;
	}

	private Cm11aDevice createDevice(SerialConnector connector, Cm11aDeviceConfig config) {
		Cm11aSerialAdapter serial = Cm11aSerialAdapter.of(connector);
		return Cm11aDevice.of(config, serial);
	}

}
