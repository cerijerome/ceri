package ceri.x10.cm17a;

import static ceri.common.util.BasicUtil.defaultValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.Fixable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.comm.Serial;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm17a.Cm17aConfig.Type;
import ceri.x10.cm17a.device.Cm17a;
import ceri.x10.cm17a.device.Cm17aConnector;
import ceri.x10.cm17a.device.Cm17aDevice;
import ceri.x10.cm17a.device.Cm17aDeviceConfig;
import ceri.x10.cm17a.device.Cm17aEmulator;

/**
 * Container for a cm17a controller.
 */
public class Cm17aContainer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getFormatterLogger();
	public final int id;
	public final Type type;
	private final Serial.Fixable createdSerial;
	private final Serial.Fixable serial;
	private final Cm17a createdCm17a;
	public final Cm17a cm17a;

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
	public static Cm17aContainer of(int id, Serial.Fixable serial) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).build(), null, serial);
	}

	/**
	 * Constructs a controller for the given serial connector.
	 */
	public static Cm17aContainer of(int id, Serial.Fixable serial, Cm17aDeviceConfig device) {
		return new Cm17aContainer(Cm17aConfig.builder().id(id).device(device).build(), null,
			serial);
	}

	private Cm17aContainer(Cm17aConfig config, Cm17a cm17a, Serial.Fixable serial) {
		try {
			id = config.id;
			type = config.type(cm17a, serial);
			createdSerial = createSerial(config.serial);
			this.serial = defaultValue(createdSerial, serial);
			createdCm17a = createCm17a(this.serial, config.device);
			this.cm17a = defaultValue(createdCm17a, cm17a);
			logger.info("[%d:%s] started", id, type);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public void close() {
		LogUtil.close(createdCm17a, createdSerial);
		logger.info("[%d:%s] stopped", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, serial);
	}

	@SuppressWarnings("resource")
	private Serial.Fixable createSerial(SelfHealingSerialConfig config) {
		return type == Type.serial ? Fixable.openSilently(SelfHealingSerial.of(config)) : null;
	}

	@SuppressWarnings("resource")
	private Cm17a createCm17a(Serial.Fixable serial, Cm17aDeviceConfig config) {
		return switch (type) {
			case cm17aRef -> null;
			case serialRef, serial -> Cm17aDevice.of(config, Cm17aConnector.of(serial));
			case test -> Cm17aEmulator.of(config.queuePollTimeoutMs);
			default -> Cm17a.NULL;
		};
	}
}
