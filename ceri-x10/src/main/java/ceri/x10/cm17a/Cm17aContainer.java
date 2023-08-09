package ceri.x10.cm17a;

import static ceri.common.util.BasicUtil.defaultValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.comm.Serial;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.device.Cm17a;
import ceri.x10.cm17a.device.Cm17aConnector;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;
import ceri.x10.cm17a.device.Cm17aEmulator;

/**
 * Container for a cm17a controller.
 */
public class Cm17aContainer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getLogger();
	public final int id;
	public final Cm17aType type;
	private final Serial.Fixable createdSerial;
	private final Serial.Fixable serial;
	private final Cm17a createdCm17a;
	private final Cm17a cm17a;

	public static enum Cm17aType {
		cm17aRef,
		serialRef,
		serial,
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
	public static Cm17aContainer of(int id, Serial.Fixable connector) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).build(), null, connector);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm17aContainer of(int id, Serial.Fixable connector, Cm17aDeviceConfig device) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).device(device).build(), null,
			connector);
	}

	private Cm17aContainer(Cm17aConfig config, Cm17a cm17a, Serial.Fixable connector) {
		try {
			id = config.id;
			type = cm17aType(config, cm17a, connector);
			logger.info("Started({}): {}", id, type);
			createdSerial = createSerial(type, config.serial);
			this.serial = defaultValue(createdSerial, connector);
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
		LogUtil.close(createdCm17a, createdSerial);
		logger.info("Stopped({}): {}", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, serial);
	}

	private Cm17aType cm17aType(Cm17aConfig config, Cm17a cm17a, Serial.Fixable serial) {
		if (cm17a != null) return Cm17aType.cm17aRef;
		if (serial != null) return Cm17aType.serialRef;
		if (config.isDevice()) return Cm17aType.serial;
		if (config.isTest()) return Cm17aType.test;
		return Cm17aType.noOp;
	}

	private Serial.Fixable createSerial(Cm17aType type, SelfHealingSerialConfig config) {
		if (type != Cm17aType.serial) return null;
		SelfHealingSerial serial = SelfHealingSerial.of(config);
		serial.openSilently();
		return serial;
	}

	@SuppressWarnings("resource")
	private Cm17a createCm17a(Cm17aType type, Serial.Fixable serial, Cm17aDeviceConfig config) {
		if (type == Cm17aType.cm17aRef) return null;
		if (type == Cm17aType.serialRef || type == Cm17aType.serial)
			return Cm17aDevice.of(config, Cm17aConnector.of(serial));
		if (type == Cm17aType.test) return Cm17aEmulator.of(config.queuePollTimeoutMs);
		return Cm17a.NULL;
	}
}
