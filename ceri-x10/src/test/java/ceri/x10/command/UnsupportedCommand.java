package ceri.x10.command;

import static ceri.common.validation.ValidationUtil.validateNotNull;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import ceri.common.collection.Immutable;

public class UnsupportedCommand extends Command {

	public static UnsupportedCommand hailReq(House house, Unit... units) {
		return hailReq(house, Arrays.asList(units));
	}

	public static UnsupportedCommand hailReq(House house, Collection<Unit> units) {
		validateNotNull(house);
		return new UnsupportedCommand(house, normalize(units), FunctionType.hailReq);
	}

	public static UnsupportedCommand hailAck(House house, Unit... units) {
		return hailAck(house, Arrays.asList(units));
	}

	public static UnsupportedCommand hailAck(House house, Collection<Unit> units) {
		validateNotNull(house);
		return new UnsupportedCommand(house, normalize(units), FunctionType.hailAck);
	}

	private UnsupportedCommand(House house, Set<Unit> units, FunctionType type) {
		super(house, units, type, 0, 0);
	}

	private static Set<Unit> normalize(Collection<Unit> units) {
		if (units == null || units.isEmpty()) return Set.of();
		return Immutable.wrap(new TreeSet<>(units));
	}
}
