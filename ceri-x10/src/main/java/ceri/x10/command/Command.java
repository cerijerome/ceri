package ceri.x10.command;

import static ceri.common.collection.StreamUtil.toSet;
import static ceri.common.math.MathUtil.limit;
import static ceri.common.text.RegexUtil.intGroup;
import static ceri.common.util.BasicUtil.defaultValue;
import static ceri.common.validation.ValidationUtil.validateNotNull;
import static ceri.common.validation.ValidationUtil.validateUbyte;
import static ceri.x10.util.X10Util.DIM_MAX_PERCENT;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ceri.common.collection.StreamUtil;
import ceri.common.text.RegexUtil;
import ceri.common.text.StringUtil;
import ceri.common.util.EqualsUtil;
import ceri.common.util.HashCoder;

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

	public static Command from(String s) {
		Matcher m = RegexUtil.matched(COMMAND_REGEX, s);
		if (m != null) return from(m);
		throw new IllegalArgumentException("Invalid command: " + s);
	}

	public static General allUnitsOff(House house) {
		validateNotNull(house);
		return new General(house, Set.of(), FunctionType.allUnitsOff);
	}

	public static General allLightsOn(House house) {
		validateNotNull(house);
		return new General(house, Set.of(), FunctionType.allLightsOn);
	}

	public static General allLightsOff(House house) {
		validateNotNull(house);
		return new General(house, Set.of(), FunctionType.allLightsOff);
	}

	public static General on(Address address) {
		return on(address.house, address.unit);
	}

	public static General on(House house, Unit... units) {
		return on(house, Arrays.asList(units));
	}

	public static General on(House house, Collection<Unit> units) {
		validateNotNull(house);
		return new General(house, normalize(units), FunctionType.on);
	}

	public static General off(Address address) {
		return off(address.house, address.unit);
	}

	public static General off(House house, Unit... units) {
		return off(house, Arrays.asList(units));
	}

	public static General off(House house, Collection<Unit> units) {
		validateNotNull(house);
		return new General(house, normalize(units), FunctionType.off);
	}

	public static Dim dim(Address address, int percent) {
		return dim(address.house, percent, address.unit);
	}

	public static Dim dim(House house, int percent, Unit... units) {
		return dim(house, percent, Arrays.asList(units));
	}

	public static Dim dim(House house, int percent, Collection<Unit> units) {
		validateNotNull(house);
		percent = limit(percent, 0, DIM_MAX_PERCENT);
		return new Dim(house, normalize(units), FunctionType.dim, percent);
	}

	public static Dim bright(Address address, int percent) {
		return bright(address.house, percent, address.unit);
	}

	public static Dim bright(House house, int percent, Unit... units) {
		return bright(house, percent, Arrays.asList(units));
	}

	public static Dim bright(House house, int percent, Collection<Unit> units) {
		validateNotNull(house);
		percent = limit(percent, 0, DIM_MAX_PERCENT);
		return new Dim(house, normalize(units), FunctionType.bright, percent);
	}

	public static Ext ext(Address address, int data, int command) {
		return ext(address.house, data, command, address.unit);
	}

	public static Ext ext(House house, int data, int command, Unit... units) {
		return ext(house, data, command, Arrays.asList(units));
	}

	public static Ext ext(House house, int data, int command, Collection<Unit> units) {
		validateNotNull(house);
		validateUbyte(data);
		validateUbyte(command);
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

	private Command(House house, Set<Unit> units, FunctionType type, int data, int command) {
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
		return toSet(units.stream().map(unit -> Address.of(house, unit)));
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
		return HashCoder.hash(house, units, type, data, command);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Command)) return false;
		Command other = (Command) obj;
		if (house != other.house) return false;
		if (!EqualsUtil.equals(units, other.units)) return false;
		if (type != other.type) return false;
		if (data != other.data) return false;
		if (command != other.command) return false;
		return true;
	}

	@Override
	public String toString() {
		switch (group()) {
		case house:
			return String.format("%s:%s", house, type);
		case dim:
			return String.format("%s[%s]:%s:%d%%", house, unitStr(), type, data);
		case ext:
			return String.format("%s[%s]:%s:0x%02x:0x%02x", house, unitStr(), type, data, command);
		default:
			return String.format("%s[%s]:%s", house, unitStr(), type);
		}
	}

	private String unitStr() {
		return StringUtil.join(",", unit -> String.valueOf(unit.value), units);
	}

	private static Set<Unit> normalize(Collection<Unit> units) {
		if (units == null || units.isEmpty()) return Set.of();
		return Collections.unmodifiableSet(new TreeSet<>(units));
	}

	private static Command from(Matcher m) {
		int i = 1;
		House house = House.from(m.group(i++).charAt(0));
		Set<Unit> units = units(defaultValue(m.group(i++), m.group(i++)));
		FunctionType type = FunctionType.from(m.group(i++));
		int data = defaultValue(defaultValue(intGroup(m, i++), intGroup(m, i++)), 0);
		int command = defaultValue(intGroup(m, i++), 0);
		return of(house, units, type, data, command);
	}

	private static Set<Unit> units(String unitStr) {
		if (unitStr == null) return Set.of();
		return StreamUtil.toSet(StringUtil.commaSplit(unitStr).stream().filter(s -> s.length() > 0)
			.mapToInt(Integer::parseInt).mapToObj(Unit::from));
	}

	private static Command of(House house, Set<Unit> units, FunctionType type, int data,
		int command) {
		switch (type) {
		case allUnitsOff:
			return allUnitsOff(house);
		case allLightsOn:
			return allLightsOn(house);
		case allLightsOff:
			return allLightsOff(house);
		case on:
			return on(house, units);
		case off:
			return off(house, units);
		case dim:
			return dim(house, data, units);
		case bright:
			return bright(house, data, units);
		case ext:
			return ext(house, data, command, units);
		default:
			throw new UnsupportedOperationException("Function not supported: " + type);
		}
	}

}
