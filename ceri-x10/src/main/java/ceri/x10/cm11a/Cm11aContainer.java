package ceri.x10.cm11a;

import static ceri.common.util.BasicUtil.defaultValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.function.RuntimeCloseable;
import ceri.common.io.Connector;
import ceri.common.io.Fixable;
import ceri.common.text.ToString;
import ceri.log.util.LogUtil;
import ceri.serial.comm.util.SelfHealingSerial;
import ceri.serial.comm.util.SelfHealingSerialConfig;
import ceri.x10.cm11a.Cm11aConfig.Type;
import ceri.x10.cm11a.device.Cm11a;
import ceri.x10.cm11a.device.Cm11aDevice;
import ceri.x10.cm11a.device.Cm11aDeviceConfig;
import ceri.x10.cm11a.device.Cm11aEmulator;

/**
 * Container for a cm11a controller.
 */
public class Cm11aContainer implements RuntimeCloseable {
	private static final Logger logger = LogManager.getFormatterLogger();
	public final int id;
	public final Type type;
	private final Connector.Fixable createdConnector;
	private final Connector.Fixable connector;
	private final Cm11a createdCm11a;
	public final Cm11a cm11a;

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
	 * Constructs a controller for the given connector.
	 */
	public static Cm11aContainer of(int id, Connector.Fixable connector) {
		return new Cm11aContainer(Cm11aConfig.builder().id(id).build(), null, connector);
	}

	/**
	 * Constructs a controller for the given connector.
	 */
	public static Cm11aContainer of(int id, Connector.Fixable connector, Cm11aDeviceConfig device) {
		return new Cm11aContainer(Cm11aConfig.builder().id(id).device(device).build(), null,
			connector);
	}

	private Cm11aContainer(Cm11aConfig config, Cm11a cm11a, Connector.Fixable connector) {
		try {
			id = config.id;
			type = config.type(cm11a, connector);
			createdConnector = createConnector(config.serial);
			this.connector = defaultValue(createdConnector, connector);
			createdCm11a = createCm11a(this.connector, config.device);
			this.cm11a = defaultValue(createdCm11a, cm11a);
			logger.info("[%d:%s] started", id, type);
		} catch (RuntimeException e) {
			close();
			throw e;
		}
	}

	@Override
	public void close() {
		LogUtil.close(createdCm11a, createdConnector);
		logger.info("[%d:%s] stopped", id, type);
	}

	@Override
	public String toString() {
		return ToString.forClass(this, id, type, connector);
	}

	@SuppressWarnings("resource")
	private Connector.Fixable createConnector(SelfHealingSerialConfig config) {
		return type == Type.connector ?
			Fixable.openSilently(SelfHealingSerial.of(config.replace(Cm11a.SERIAL))) : null;
	}

	@SuppressWarnings("resource")
	private Cm11a createCm11a(Connector.Fixable connector, Cm11aDeviceConfig config) {
		return switch (type) {
			case cm11aRef -> null;
			case connectorRef, connector -> Cm11aDevice.of(config, connector);
			case test -> Cm11aEmulator.of(config.queuePollTimeoutMs);
			default -> Cm11a.NULL;
		};
	}
}
