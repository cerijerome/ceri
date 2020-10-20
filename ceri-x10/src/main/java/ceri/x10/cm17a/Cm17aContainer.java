package ceri.x10.cm17a;

import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.util.BasicUtil.defaultValue;
import java.io.Closeable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.serial.javax.util.SelfHealingSerialConnector;
import ceri.x10.cm17a.device.Cm17a;
import ceri.x10.cm17a.device.Cm17aConnector;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;
import ceri.x10.cm17a.device.Cm17aEmulator;

/**
 * Container for a cm17a controller.
 */
public class Cm17aContainer implements Closeable {
	private static final Logger logger = LogManager.getLogger();
	public final int id;
	public final Cm17aType type;
	private final SerialConnector createdConnector;
	private final SerialConnector connector;
	private final Cm17a createdCm17a;
	private final Cm17a cm17a;

	public static enum Cm17aType {
		device,
		cm17aRef,
		deviceSerialRef,
		test,
		noOp;
	}

	/**
	 * Constructs the controller and connector.
	 */
	public static Cm17aContainer of(Cm17aConfig config) {
		return new Cm17aContainer(config, null, null);
	}

	/**
	 * Container with id for the given controller.
	 */
	public static Cm17aContainer of(int id, Cm17a cm17a) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).build(), cm17a, null);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm17aContainer of(int id, SerialConnector connector) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).build(), null, connector);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm17aContainer of(int id, SerialConnector connector, Cm17aDeviceConfig device) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).device(device).build(), null,
			connector);
	}

	private Cm17aContainer(Cm17aConfig config, Cm17a cm17a, SerialConnector connector) {
		try {
			id = config.id;
			type = cm17aType(config, cm17a, connector);
			logger.info("Started({}): {}", id, type);
			createdConnector = createConnector(type, config.deviceSerial);
			this.connector = defaultValue(createdConnector, connector);
			createdCm17a = createCm17a(type, connector, config.device);
			this.cm17a = defaultValue(createdCm17a, cm17a);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	public Cm17a cm17a() {
		return cm17a;
	}

	@Override
	public void close() {
		LogUtil.close(logger, createdCm17a, createdConnector);
		logger.info("Stopped({}): {}", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, connector);
	}

	private Cm17aType cm17aType(Cm17aConfig config, Cm17a cm17a, SerialConnector connector) {
		if (cm17a != null) return Cm17aType.cm17aRef;
		if (connector != null) return Cm17aType.deviceSerialRef;
		if (config.isDevice()) return Cm17aType.device;
		if (config.isTest()) return Cm17aType.test;
		return Cm17aType.noOp;
	}

	private SerialConnector createConnector(Cm17aType type, SelfHealingSerialConfig config) {
		if (type != Cm17aType.device) return null;
		SelfHealingSerialConnector connector = SelfHealingSerialConnector.of(config);
		execSilently(connector::connect);
		return connector;
	}

	private Cm17a createCm17a(Cm17aType type, SerialConnector connector, Cm17aDeviceConfig config) {
		if (type == Cm17aType.cm17aRef) return null;
		if (type == Cm17aType.deviceSerialRef || type == Cm17aType.device)
			return createDevice(connector, config);
		if (type == Cm17aType.test) return Cm17aEmulator.of(config.queuePollTimeoutMs);
		return Cm17a.NULL;
	}

	private Cm17aDevice createDevice(SerialConnector connector, Cm17aDeviceConfig config) {
		return Cm17aDevice.of(config, Cm17aConnector.serial(connector));
	}

}
