package ceri.x10.cm17a;

import static ceri.common.function.FunctionUtil.execSilently;
import java.io.Closeable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.io.DeviceMode;
import ceri.common.text.ToStringHelper;
import ceri.log.util.LogUtil;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.serial.javax.util.SelfHealingSerialConnector;
import ceri.x10.cm17a.device.Cm17aConnector;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;

/**
 * Container for a cm17a controller.
 */
public class Cm17aContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	public final int id;
	public final DeviceMode mode;
	private final SerialConnector connector;
	private final Cm17aDevice cm17a;

	public static Cm17aContainer of(Cm17aConfig config) {
		return new Cm17aContainer(config);
	}

	private Cm17aContainer(Cm17aConfig config) {
		try {
			id = config.id;
			mode = config.mode;
			logger.info("Started({})", id);
			connector = createConnector(mode, config.deviceSerial);
			cm17a = createDevice(mode, connector, config.device);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	public Cm17aDevice cm17a() {
		return cm17a;
	}

	@Override
	public void close() {
		LogUtil.close(logger, cm17a, connector);
		logger.info("Stopped({})", id);
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, id, connector).toString();
	}

	private SerialConnector createConnector(DeviceMode mode, SelfHealingSerialConfig config) {
		if (mode != DeviceMode.enabled) return null;
		SelfHealingSerialConnector connector = SelfHealingSerialConnector.of(config);
		execSilently(connector::connect);
		return connector;
	}

	private Cm17aDevice createDevice(DeviceMode mode, SerialConnector connector,
		Cm17aDeviceConfig config) {
		if (mode != DeviceMode.enabled) return Cm17aDevice.of(config, Cm17aConnector.NULL);
		return Cm17aDevice.of(config, Cm17aConnector.serial(connector));
	}

}
