package ceri.x10.cm11a.protocol;

import static ceri.x10.command.FunctionType.allLightsOff;
import static ceri.x10.command.FunctionType.allLightsOn;
import static ceri.x10.command.FunctionType.allUnitsOff;
import static ceri.x10.command.FunctionType.bright;
import static ceri.x10.command.FunctionType.dim;
import static ceri.x10.command.FunctionType.ext;
import static ceri.x10.command.FunctionType.off;
import static ceri.x10.command.FunctionType.on;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import ceri.common.text.ToStringHelper;
import ceri.common.util.HashCoder;
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
	private static final Map<FunctionType, BiFunction<Collection<Unit>, Entry, Command>> commands =
		commandMap();
	public final House house; // always present
	public final Unit unit; // for address
	public final FunctionType type; // null for address
	public final int data; // used for ext data and dim %
	public final int command; // only used for ext

	public static List<Entry> allFrom(Command command) {
		if (command.isNoOp()) return List.of();
		Entry function = functionFrom(command);
		List<Entry> entries = new ArrayList<>();
		command.units().forEach(unit -> entries.add(address(command.house(), unit)));
		entries.add(function);
		return entries;
	}

	private static Entry functionFrom(Command command) {
		switch (command.group()) {
		case house:
		case unit:
			return function(command.house(), command.type());
		case dim:
			Command.Dim dim = (Command.Dim) command;
			return dim(command.house(), command.type(), dim.percent());
		case ext:
			Command.Ext ext = (Command.Ext) command;
			return ext(command.house(), ext.data(), ext.command());
		default:
			throw new IllegalArgumentException("Unsupported command: " + command);
		}
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
		return HashCoder.hash(house, unit, type, data, command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Entry)) return false;
		Entry other = (Entry) obj;
		if (house != other.house) return false;
		if (unit != other.unit) return false;
		if (type != other.type) return false;
		if (data != other.data) return false;
		if (command != other.command) return false;
		return true;
	}

	@Override
	public String toString() {
		return ToStringHelper.createByClass(this, house, unit, type, data, command).toString();
	}

	private boolean isValidCommand(House house, Collection<Unit> units) {
		if (isAddress()) return false; // function is required
		if (type.group == FunctionGroup.house) return true; // house command ok
		if (house == null || units.isEmpty()) return false; // units required
		return house == this.house; // must be same house
	}

	private static Map<FunctionType, BiFunction<Collection<Unit>, Entry, Command>> commandMap() {
		return Map.of( //
			allUnitsOff, (units, input) -> Command.allUnitsOff(input.house), //
			allLightsOn, (units, input) -> Command.allLightsOn(input.house), //
			on, (units, input) -> Command.on(input.house, units), //
			off, (units, input) -> Command.off(input.house, units), //
			dim, (units, input) -> Command.dim(input.house, input.data, units), //
			bright, (units, input) -> Command.bright(input.house, input.data, units), //
			allLightsOff, (units, input) -> Command.allLightsOff(input.house), //
			ext, (units, input) -> Command.ext(input.house, input.data, input.command, units));
	}

}
