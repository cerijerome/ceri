package ceri.x10.command;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import ceri.common.collect.Immutable;
import ceri.common.collect.Sets;

public class UnsupportedCommand extends Command {

	public static UnsupportedCommand hailReq(House house, Unit... units) {
		return hailReq(house, Arrays.asList(units));
	}

	public static UnsupportedCommand hailReq(House house, Collection<Unit> units) {
		Objects.requireNonNull(house);
		return new UnsupportedCommand(house, normalize(units), FunctionType.hailReq);
	}

	public static UnsupportedCommand hailAck(House house, Unit... units) {
		return hailAck(house, Arrays.asList(units));
	}

	public static UnsupportedCommand hailAck(House house, Collection<Unit> units) {
		Objects.requireNonNull(house);
		return new UnsupportedCommand(house, normalize(units), FunctionType.hailAck);
	}

	private UnsupportedCommand(House house, Set<Unit> units, FunctionType type) {
		super(house, units, type, 0, 0);
	}

	private static Set<Unit> normalize(Collection<Unit> units) {
		return Immutable.set(Sets::tree, units);
	}
}
