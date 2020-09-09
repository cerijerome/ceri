package ceri.x10.cm11a.device;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.x10.command.BaseCommand;
import ceri.x10.command.CommandFactory;
import ceri.x10.command.DimCommand;
import ceri.x10.command.ExtCommand;
import ceri.x10.command.HouseCommand;
import ceri.x10.command.UnitCommand;
import ceri.x10.type.Address;
import ceri.x10.type.BaseFunction;
import ceri.x10.type.DimFunction;
import ceri.x10.type.ExtFunction;
import ceri.x10.type.FunctionType;

/**
 * Processes input buffer entries into commands and adds them to a given collection. Addresses in
 * sequence are captured, and used for the next function entries if they match house codes. Once a
 * new address is found after a function, the old address list is cleared. Not thread-safe.
 */
public class EntryDispatcher {
	private static final Logger logger = LogManager.getLogger();
	private final List<Address> lastAddresses = new ArrayList<>();
	private final Collection<? super BaseCommand<?>> commands;
	private boolean lastTypeFunction = false;

	/**
	 * Constructs the dispatcher with a listener to receive command events.
	 */
	public EntryDispatcher(Collection<? super BaseCommand<?>> commands) {
		this.commands = commands;
	}

	public static List<Entry> toEntries(BaseCommand<?> command) {
		List<Entry> entries = new ArrayList<>();
		switch (command.type.group) {
		case house:
			HouseCommand houseCommand = (HouseCommand) command;
			entries.add(Entry.of(houseCommand.function()));
			break;
		case unit:
			UnitCommand unitCommand = (UnitCommand) command;
			entries.add(Entry.of(unitCommand.address()));
			entries.add(Entry.of(unitCommand.function()));
			break;
		case dim:
			DimCommand dimCommand = (DimCommand) command;
			entries.add(Entry.of(dimCommand.address()));
			entries.add(Entry.of(dimCommand.function()));
			break;
		case extended:
			ExtCommand extCommand = (ExtCommand) command;
			entries.add(Entry.of(extCommand.address()));
			entries.add(Entry.of(extCommand.function()));
			break;
		default:
			logger.warn("Unsupported command: " + command);
		}
		return entries;
	}

	/**
	 * Processes a collection of entries and dispatches commands. State is kept between invocations.
	 */
	public void dispatch(Iterable<? extends Entry> entries) {
		for (Entry entry : entries) {
			if (entry.type != Entry.Type.address) {
				dispatchFunction(lastAddresses, entry);
				lastTypeFunction = true;
			} else {
				if (lastTypeFunction) lastAddresses.clear();
				lastAddresses.add(entry.asAddress());
				lastTypeFunction = false;
			}
		}
	}

	private void dispatchFunction(Iterable<? extends Address> addresses, Entry entry) {
		BaseFunction function = entry.asBaseFunction();
		if (dispatchHouseFunction(function)) return;
		for (Address address : addresses) {
			if (address.house != function.house) {
				logger.warn("Ignoring address as function house code does not match {}: {}",
					address.house, entry.asBaseFunction().house);
				continue;
			}
			if (dispatchUnitFunction(address, function)) continue;
			if (dispatchDimFunction(address, entry)) continue;
			if (dispatchExtFunction(address, entry)) continue;
			logger.warn("Unprocessed function: {}", function);
		}
	}

	private boolean dispatchHouseFunction(BaseFunction function) {
		if (function.type == FunctionType.allUnitsOff) commands.add(CommandFactory
			.allUnitsOff(function.house));
		else if (function.type == FunctionType.allLightsOff) commands.add(CommandFactory
			.allLightsOff(function.house));
		else if (function.type == FunctionType.allLightsOn) commands.add(CommandFactory
			.allLightsOn(function.house));
		else return false;
		return true;
	}

	private boolean dispatchUnitFunction(Address address, BaseFunction function) {
		if (function.type == FunctionType.off) commands.add(CommandFactory.off(address.house,
			address.unit));
		else if (function.type == FunctionType.on) commands.add(CommandFactory.on(address.house,
			address.unit));
		else return false;
		return true;
	}

	private boolean dispatchDimFunction(Address address, Entry entry) {
		DimFunction function = entry.asDimFunction();
		if (function == null) return false;
		if (function.type == FunctionType.dim) commands.add(CommandFactory.dim(address.house,
			address.unit, function.percent));
		else if (function.type == FunctionType.bright) commands.add(CommandFactory.bright(
			address.house, address.unit, function.percent));
		else return false;
		return true;
	}

	private boolean dispatchExtFunction(Address address, Entry entry) {
		ExtFunction function = entry.asExtFunction();
		if (function == null) return false;
		commands.add(ExtCommand.of(address.house, address.unit, function.data, function.command));
		return true;
	}

}
