package ceri.x10.cm11a.protocol;

import java.util.Collection;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ceri.common.collection.Sets;
import ceri.common.function.Functions;
import ceri.x10.command.Command;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

/**
 * Collects input buffer entries, and dispatches commands built from the entries. Addresses are
 * captured until the house code changes, or a function is received.
 */
public class EntryCollector {
	private static final Logger logger = LogManager.getFormatterLogger();
	private final Functions.Consumer<Command> dispatcher;
	private final Set<Unit> lastUnits = Sets.tree();
	private House lastHouse = null;
	private boolean lastInputIsFunction = false;

	/**
	 * Constructs the dispatcher with a listener to receive command events.
	 */
	public EntryCollector(Functions.Consumer<Command> dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * ∆ Processes entries and dispatches commands. State is kept between invocations.
	 */
	public void collectAll(Collection<Entry> entries) {
		entries.forEach(this::collect);
	}

	/**
	 * Processes an entry. If the entry is an address, it is collected. If the entry is a supported
	 * function, a command is built from previous addresses, then dispatched.
	 */
	public void collect(Entry entry) {
		if (entry.isAddress()) {
			addToLast(entry.house, entry.unit);
		} else {
			Command command = entry.command(lastHouse, lastUnits);
			if (command != null) dispatcher.accept(command);
			else logger.warn("Failed to process entry: %s", entry);
			lastInputIsFunction = true;
		}
	}

	private void addToLast(House house, Unit unit) {
		if (lastHouse != house || lastInputIsFunction) clearLast();
		lastHouse = house;
		lastUnits.add(unit);
		lastInputIsFunction = false;
	}

	private void clearLast() {
		lastHouse = null;
		lastUnits.clear();
		lastInputIsFunction = false;
	}
}
