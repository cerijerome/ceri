package ceri.x10.cm11a;

import static ceri.common.function.FunctionUtil.execSilently;
import static ceri.common.util.BasicUtil.defaultValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.javax.SerialConnector;
import ceri.serial.javax.util.SelfHealingSerialConfig;
import ceri.serial.javax.util.SelfHealingSerialConnector;
import ceri.x10.cm11a.device.Cm11a;
import ceri.x10.cm11a.device.Cm11aConnector;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;
import ceri.x10.cm11a.device.Cm11aEmulator;

/**
 * Container for a cm11a controller.
 */
public class Cm11aContainer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	public final int id;
	public final Cm11aType type;
	private final SerialConnector createdConnector;
	private final SerialConnector connector;
	private final Cm11a createdCm11a;
	private final Cm11a cm11a;

	public static enum Cm11aType {
		device,
		cm11aRef,
		deviceSerialRef,
		test,
		noOp;
	}

	/**
	 * Constructs the controller and connector.
	 */
	public static Cm11aContainer of(Cm11aConfig config) {
		return new Cm11aContainer(config, null, null);
	}

	/**
	 * Container with id for the given controller.
	 */
	public static Cm11aContainer of(int id, Cm11a cm11a) {
		return new Cm11aContainer(Cm11aConfig.builder().id(id).build(), cm11a, null);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm11aContainer of(int id, SerialConnector connector) {
		return new Cm11aContainer(Cm11aConfig.builder().id(id).build(), null, connector);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm11aContainer of(int id, SerialConnector connector, Cm11aDeviceConfig device) {
		return new Cm11aContainer(Cm11aConfig.builder().id(id).device(device).build(), null,
			connector);
	}

	private Cm11aContainer(Cm11aConfig config, Cm11a cm11a, SerialConnector connector) {
		try {
			id = config.id;
			type = cm11aType(config, cm11a, connector);
			logger.info("Started({}): {}", id, type);
			createdConnector = createConnector(type, config.deviceSerial);
			this.connector = defaultValue(createdConnector, connector);
			createdCm11a = createCm11a(type, connector, config.device);
			this.cm11a = defaultValue(createdCm11a, cm11a);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	public Cm11a cm11a() {
		return cm11a;
	}

	@Override
	public void close() {
		LogUtil.close(logger, createdCm11a, createdConnector);
		logger.info("Stopped({}): {}", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, connector);
	}

	private Cm11aType cm11aType(Cm11aConfig config, Cm11a cm11a, SerialConnector connector) {
		if (cm11a != null) return Cm11aType.cm11aRef;
		if (connector != null) return Cm11aType.deviceSerialRef;
		if (config.isDevice()) return Cm11aType.device;
		if (config.isTest()) return Cm11aType.test;
		return Cm11aType.noOp;
	}

	private SerialConnector createConnector(Cm11aType type, SelfHealingSerialConfig config) {
		if (type != Cm11aType.device) return null;
		config = SelfHealingSerialConfig.replace(config, Cm11aConnector.Serial.PARAMS);
		SelfHealingSerialConnector connector = SelfHealingSerialConnector.of(config);
		execSilently(connector::connect);
		return connector;
	}

	private Cm11a createCm11a(Cm11aType type, SerialConnector connector, Cm11aDeviceConfig config) {
		if (type == Cm11aType.cm11aRef) return null;
		if (type == Cm11aType.deviceSerialRef || type == Cm11aType.device)
			return createDevice(connector, config);
		if (type == Cm11aType.test) return Cm11aEmulator.of(config.queuePollTimeoutMs);
		return Cm11a.NULL;
	}

	private Cm11aDevice createDevice(SerialConnector connector, Cm11aDeviceConfig config) {
		return Cm11aDevice.of(config, Cm11aConnector.serial(connector));
	}
}
