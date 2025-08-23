package ceri.x10.cm11a.protocol;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import ceri.common.collection.Lists;
import ceri.common.function.Functions;
import ceri.common.text.ToString;
import ceri.x10.command.Command;
import ceri.x10.command.FunctionGroup;
import ceri.x10.command.FunctionType;
import ceri.x10.command.House;
import ceri.x10.command.Unit;

/**
 * Union of values representing a step in transmitting or receiving a command. The step is encoded
 * differently for transmitting vs receiving.
 */
public class Entry {
	private static final Map<FunctionType, //
		Functions.BiFunction<Collection<Unit>, Entry, Command>> commands = commandMap();
	public final House house; // always present
	public final Unit unit; // for address
	public final FunctionType type; // null for address
	public final int data; // used for ext data and dim %
	public final int command; // only used for ext

	public static List<Entry> allFrom(Command command) {
		if (command.isNoOp()) return List.of();
		Entry function = functionFrom(command);
		List<Entry> entries = Lists.of();
		command.units().forEach(unit -> entries.add(address(command.house(), unit)));
		entries.add(function);
		return entries;
	}

	private static Entry functionFrom(Command command) {
		return switch (command.group()) {
			case house, unit -> function(command.house(), command.type());
			case dim -> {
				Command.Dim dim = (Command.Dim) command;
				yield dim(command.house(), command.type(), dim.percent());
			}
			case ext -> {
				Command.Ext ext = (Command.Ext) command;
				yield ext(command.house(), ext.data(), ext.command());
			}
			default -> throw new IllegalArgumentException("Unsupported command: " + command);
		};
	}

	public static Entry address(House house, Unit unit) {
		return new Entry(house, unit, null, 0, 0);
	}

	public static Entry function(House house, FunctionType type) {
		return new Entry(house, null, type, 0, 0);
	}

	public static Entry dim(House house, FunctionType type, int percent) {
		return new Entry(house, null, type, percent, 0);
	}

	public static Entry ext(House house, int data, int command) {
		return new Entry(house, null, FunctionType.ext, data, command);
	}

	private Entry(House house, Unit unit, FunctionType type, int data, int command) {
		this.house = house;
		this.unit = unit;
		this.type = type;
		this.data = data;
		this.command = command;
	}

	public boolean isAddress() {
		return unit != null;
	}

	public boolean isGroup(FunctionGroup group) {
		return type != null && type.group == group;
	}

	public Command command(House house, Collection<Unit> units) {
		if (!isValidCommand(house, units)) return null;
		var commandFn = commands.get(type);
		if (commandFn == null) return null;
		return commandFn.apply(units, this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(house, unit, type, data, command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Entry other)) return false;
		if (house != other.house) return false;
		if (unit != other.unit) return false;
		if (type != other.type) return false;
		if (data != other.data) return false;
		if (command != other.command) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToString.forClass(this, house, unit, type, data, command);
	}

	private boolean isValidCommand(House house, Collection<Unit> units) {
		if (isAddress()) return false; // function is required
		if (type.group == FunctionGroup.house) return true; // house command ok
		if (house == null || units.isEmpty()) return false; // units required
		return house == this.house; // must be same house
	}

	private static Map<FunctionType, Functions.BiFunction<Collection<Unit>, Entry, Command>>
		commandMap() {
		return Map.of(FunctionType.allUnitsOff, (_, input) -> Command.allUnitsOff(input.house),
			FunctionType.allLightsOn, (_, input) -> Command.allLightsOn(input.house),
			FunctionType.on, (units, input) -> Command.on(input.house, units), //
			FunctionType.off, (units, input) -> Command.off(input.house, units), //
			FunctionType.dim, (units, input) -> Command.dim(input.house, input.data, units), //
			FunctionType.bright, (units, input) -> Command.bright(input.house, input.data, units),
			FunctionType.allLightsOff, (_, input) -> Command.allLightsOff(input.house),
			FunctionType.ext,
			(units, input) -> Command.ext(input.house, input.data, input.command, units));
	}
}
