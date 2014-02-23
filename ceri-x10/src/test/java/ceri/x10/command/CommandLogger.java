package ceri.x10.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An implementation of command listener that logs all activity.
 * Useful for debugging.
 */
public class CommandLogger implements CommandListener {
	private static final Logger logger = LogManager.getLogger();

	@Override
	public void allUnitsOff(HouseCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void allLightsOff(HouseCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void allLightsOn(HouseCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void off(UnitCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void on(UnitCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void dim(DimCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void bright(DimCommand command) {
		logger.info("Command received: {}", command);
	}

	@Override
	public void extended(ExtCommand command) {
		logger.info("Command received: {}", command);
	}

}
