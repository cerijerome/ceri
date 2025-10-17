package ceri.x10.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import ceri.common.collect.Immutable;
import ceri.common.collect.Sets;
import ceri.common.function.Functions;
import ceri.common.math.Maths;
import ceri.common.property.Parser;
import ceri.common.stream.Streams;
import ceri.common.text.Joiner;
import ceri.common.text.Regex;
import ceri.common.util.Basics;
import ceri.common.util.Validate;
import ceri.x10.util.X10Util;

public abstract class Command {
	private static final Pattern COMMAND_REGEX = Pattern.compile("([A-Pa-p])" + // house
		"(?:\\[([\\d\\s,]*)\\]|(\\d\\d?)|)" + // [units] or unit
		":(\\w+)" + // type
		"(?::(\\d+)%|:(\\w+)|)" + // percent or data
		"(?::(\\w+)|)"); // command
	private final House house;
	private final Set<Unit> units;
	private final FunctionType type;
	private final int data;
	private final int command;

	public interface Listener {
		default void allUnitsOff(@SuppressWarnings("unused") Command command) {}

		default void allLightsOff(@SuppressWarnings("unused") Command command) {}

		default void allLightsOn(@SuppressWarnings("unused") Command command) {}

		default void off(@SuppressWarnings("unused") Command command) {}

		default void on(@SuppressWarnings("unused") Command command) {}

		default void dim(@SuppressWarnings("unused") Command.Dim command) {}

		default void bright(@SuppressWarnings("unused") Command.Dim command) {}

		default void ext(@SuppressWarnings("unused") Command.Ext command) {}

		default Functions.Consumer<Command> asConsumer() {
			return command -> dispatcher(command).accept(this);
		}

		/**
		 * Returns a dispatch consumer that calls the matching CommandListener method for a command.
		 */
		static Functions.Consumer<Listener> dispatcher(Command command) {
			return switch (command.type()) {
				case allUnitsOff -> listener -> listener.allUnitsOff(command);
				case allLightsOff -> listener -> listener.allLightsOff(command);
				case allLightsOn -> listener -> listener.allLightsOn(command);
				case off -> listener -> listener.off(command);
				case on -> listener -> listener.on(command);
				case dim -> listener -> listener.dim((Command.Dim) command);
				case bright -> listener -> listener.bright((Command.Dim) command);
				case ext -> listener -> listener.ext((Command.Ext) command);
				default -> throw new UnsupportedOperationException(
					"Function type not supported: " + command);
			};
		}
	}

	public static Command from(String s) {
		var m = Regex.validMatch(COMMAND_REGEX, s, "command");
		int i = 1;
		var house = House.from(m.group(i++).charAt(0));
		var units = units(Basics.def(m.group(i++), m.group(i++)));
		var type = FunctionType.from(m.group(i++));
		var pc = Parser.string(Regex.group(m, i++)).toInt();
		var data = Parser.string(Regex.group(m, i++)).toInt(0);
		int command = Parser.string(Regex.group(m, i++)).toInt(0);
		return of(house, units, type, Basics.def(pc, data), command);
	}

	public static General allUnitsOff(House house) {
		Validate.nonNull(house);
		return new General(house, Set.of(), FunctionType.allUnitsOff);
	}

	public static General allLightsOn(House house) {
		Validate.nonNull(house);
		return new General(house, Set.of(), FunctionType.allLightsOn);
	}

	public static General allLightsOff(House house) {
		Validate.nonNull(house);
		return new General(house, Set.of(), FunctionType.allLightsOff);
	}

	public static General on(Address address) {
		return on(address.house, address.unit);
	}

	public static General on(House house, Unit... units) {
		return on(house, Arrays.asList(units));
	}

	public static General on(House house, Collection<Unit> units) {
		Validate.nonNull(house);
		return new General(house, normalize(units), FunctionType.on);
	}

	public static General off(Address address) {
		return off(address.house, address.unit);
	}

	public static General off(House house, Unit... units) {
		return off(house, Arrays.asList(units));
	}

	public static General off(House house, Collection<Unit> units) {
		Validate.nonNull(house);
		return new General(house, normalize(units), FunctionType.off);
	}

	public static Dim dim(Address address, int percent) {
		return dim(address.house, percent, address.unit);
	}

	public static Dim dim(House house, int percent, Unit... units) {
		return dim(house, percent, Arrays.asList(units));
	}

	public static Dim dim(House house, int percent, Collection<Unit> units) {
		Validate.nonNull(house);
		percent = Maths.limit(percent, 0, X10Util.DIM_MAX_PERCENT);
		return new Dim(house, normalize(units), FunctionType.dim, percent);
	}

	public static Dim bright(Address address, int percent) {
		return bright(address.house, percent, address.unit);
	}

	public static Dim bright(House house, int percent, Unit... units) {
		return bright(house, percent, Arrays.asList(units));
	}

	public static Dim bright(House house, int percent, Collection<Unit> units) {
		Validate.nonNull(house);
		percent = Maths.limit(percent, 0, X10Util.DIM_MAX_PERCENT);
		return new Dim(house, normalize(units), FunctionType.bright, percent);
	}

	public static Ext ext(Address address, int data, int command) {
		return ext(address.house, data, command, address.unit);
	}

	public static Ext ext(House house, int data, int command, Unit... units) {
		return ext(house, data, command, Arrays.asList(units));
	}

	public static Ext ext(House house, int data, int command, Collection<Unit> units) {
		Validate.nonNull(house);
		Validate.ubyte(data);
		Validate.ubyte(command);
		return new Ext(house, normalize(units), data, command);
	}

	public static class General extends Command {
		private General(House house, Set<Unit> units, FunctionType type) {
			super(house, units, type, 0, 0);
		}
	}

	public static class Dim extends Command {
		private Dim(House house, Set<Unit> units, FunctionType type, int percent) {
			super(house, units, type, percent, 0);
		}

		public int percent() {
			return super.data;
		}
	}

	public static class Ext extends Command {
		private Ext(House house, Set<Unit> units, int data, int command) {
			super(house, units, FunctionType.ext, data, command);
		}

		public int data() {
			return super.data;
		}

		public int command() {
			return super.command;
		}
	}

	Command(House house, Set<Unit> units, FunctionType type, int data, int command) {
		this.house = house;
		this.units = units;
		this.type = type;
		this.data = data;
		this.command = command;
	}

	public House house() {
		return house;
	}

	public Set<Unit> units() {
		return units;
	}

	public Set<Address> addresses() {
		return Streams.from(units).map(unit -> Address.of(house, unit)).toSet();
	}

	public FunctionType type() {
		return type;
	}

	public FunctionGroup group() {
		return type.group;
	}

	/**
	 * Determines if the command has any action.
	 */
	public boolean isNoOp() {
		if (group() != FunctionGroup.house && units.isEmpty()) return true;
		if (group() == FunctionGroup.dim && data == 0) return true;
		return false;
	}

	public boolean isGroup(FunctionGroup group) {
		return group() == group;
	}

	@Override
	public int hashCode() {
		return Objects.hash(house, units, type, data, command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Command other)) return false;
		if (house != other.house) return false;
		if (!Objects.equals(units, other.units)) return false;
		if (type != other.type) return false;
		if (data != other.data) return false;
		if (command != other.command) return false;
		return true;
	}

	@Override
	public String toString() {
		return switch (group()) {
			case house -> String.format("%s:%s", house, type);
			case dim -> String.format("%s%s:%s:%d%%", house, unitStr(), type, data);
			case ext -> String.format("%s%s:%s:0x%02x:0x%02x", house, unitStr(), type, data,
				command);
			default -> String.format("%s%s:%s", house, unitStr(), type);
		};
	}

	private String unitStr() {
		return Joiner.ARRAY_COMPACT.join(u -> u.value, units);
	}

	private static Set<Unit> normalize(Collection<Unit> units) {
		if (units == null || units.isEmpty()) return Immutable.set();
		return Immutable.set(Sets::tree, units);
	}

	private static Set<Unit> units(String unitStr) {
		if (unitStr == null) return Set.of();
		return Parser.string(unitStr).split().filter(s -> s.length() > 0).asInts()
			.asEach(Unit::from).toSet();
	}

	private static Command of(House house, Set<Unit> units, FunctionType type, int data,
		int command) {
		return switch (type) {
			case allUnitsOff -> allUnitsOff(house);
			case allLightsOn -> allLightsOn(house);
			case allLightsOff -> allLightsOff(house);
			case on -> on(house, units);
			case off -> off(house, units);
			case dim -> dim(house, data, units);
			case bright -> bright(house, data, units);
			case ext -> ext(house, data, command, units);
			default -> throw new UnsupportedOperationException("Function not supported: " + type);
		};
	}
}
